package com.dvtech.maker.service;

import com.dvtech.maker.model.TransactionAd;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockAdeelReport {
    private static final Logger log = LoggerFactory.getLogger(MockAdeelReport.class);

    @Value("${bls-file-feed.spl-report.account-file-path}")
    private String accountFilePath;
    @Value("${bls-file-feed.spl-report.baiCode-file-path}")
    private String baiCodeFilePath;

    @Value("${bls-file-feed.spl-report.feed-names}")
    private List<String> splReportFeedNames;

    @Value("${bls-file-feed.spl-report.out-file-path}")
    private String outputDirPath;

    @Autowired
    private Configuration freemarkerConfig;

    private static final AtomicInteger sequenceCounter = new AtomicInteger(0);
    private static final Random RANDOM = new Random();
    private final AtomicInteger recordSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);
    private static final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final DecimalFormat df = new DecimalFormat("000000");
    /**
     * Define a Map to associate each feed name with its respective template file.
     */
    private static final Map<String, String> TEMPLATE_MAPPING = new HashMap<>();

    static {
        TEMPLATE_MAPPING.put("ACON", "ACONTemplate.ftl");
        TEMPLATE_MAPPING.put("ADEEL", "ADEELTemplate.ftl");
        TEMPLATE_MAPPING.put("SPEL", "SPLTemplate.ftl");
    }

    @Scheduled(cron = "${bls-file-feed.advice.schedule}")
    public void generateAdviceFeed() {
        splReportFeedNames.forEach(this::generateFile);
    }

    private void generateFile(String adviceFeedName) {
        try {
            List<Long> accountNumbers = readFileLines(Paths.get(new DefaultResourceLoader().getResource(accountFilePath).getFile().getAbsolutePath()).toString());
            // Build the data model for the template
            Map<String, Object> dataModel = buildDataModel(adviceFeedName, accountNumbers);
                 // Retrieve the template file dynamically based on the feed name
            String templateFileName = TEMPLATE_MAPPING.getOrDefault(adviceFeedName, "defaultTemplate.ftl");
            generateFileFromTemplate(adviceFeedName, templateFileName, dataModel);
            recordSequence.set(0);
            transactionSequence.set(0);
            totalLines.set(0);
            log.info("File generated successfully: {}", adviceFeedName);
        } catch (Exception e) {
            log.error("Error Occurred", e);
        }
    }
    private Map<String, Object> buildDataModel(String adviceFeedName, List<Long> accountNumbers) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("firstLine", firstHeaderRecordMap(adviceFeedName));
        dataModel.put("transactions", accountNumbers.stream()
                .map(this::createTransactionRecord)
                .collect(Collectors.toList()));
        return dataModel;
    }
    private Map<String, Object> createTransactionRecord(long accountNumber) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yy", Locale.ENGLISH)).toUpperCase();
        String amount = formatAmount(Math.random() * 1000);

        Map.Entry<String, String> randomEntry = (Map.Entry<String, String>) baiCodeMap().entrySet()
                .toArray()[new Random().nextInt(baiCodeMap().size())];

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("header", Collections.singletonList(createHeaderRecordMap(accountNumber)));
        transaction.put("company",Collections.singletonList(createCompanyRecordMap(accountNumber)) );
        transaction.put("details",Collections.singletonList(createTransactionDetailRecord(accountNumber)) );
        transaction.put("detailsDeleted",Collections.singletonList(createDeletedDetailRecord(accountNumber)) );
        transaction.put("trailer",Collections.singletonList(createTrailerRecordMap(accountNumber)));
        return transaction;
    }
    private Map<String, Object> firstHeaderRecordMap(String adviceFeedName) {
        Map<String, Object> record = new HashMap<>();
        record.put("adviceFeedName", adviceFeedName);
        record.put("creationDate", getFileCreationDateTime());
        return record;
    }
    private void generateFileFromTemplate(String outputFilePath, String templateFileName, Map<String, Object> dataModel) {
        try (Writer writer = new FileWriter(outputFilePath)) {
            Template template = freemarkerConfig.getTemplate(templateFileName);
            template.process(dataModel, writer);

        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Error processing FreeMarker template: " + outputFilePath, e);
        }
    }

    private List<Long> readFileLines(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines().map(Long::parseLong).collect(Collectors.toList());
        }
    }


    private Map<String, Object> createHeaderRecordMap(long accountNumber) {
        Map<String, Object> record = new HashMap<>();
        record.put("accountNumber", String.format("%08d", accountNumber));
        record.put("recordSequenceNumber",  recordFormatNumber(recordSequence.incrementAndGet()));
        totalLines.incrementAndGet();
        return record;
    }

    private Map<String, Object> createTrailerRecordMap(long accountNumber) {
        Map<String, Object> record = new HashMap<>();
        record.put("accountNumber", String.format("%08d", accountNumber));
        record.put("lastDigit",  recordFormatNumber(transactionSequence.get()));
        return record;
    }

    private Map<String, Object> createCompanyRecordMap(long accountNumber) {
        Map<String, Object> record = new HashMap<>();
        int pageCounter = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yy");
        String formattedDate = LocalDate.now().format(formatter);
        record.put("transactionSequenceNumber", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("currentDate", formattedDate);
        record.put("pageNumber",  pageCounter++);
        record.put("lincolnLife", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("companyWithNumber", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("accountNumber",  String.format("%013d", accountNumber));
        record.put("add", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add1", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add2", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add3", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add4", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add5", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add6", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        record.put("add7", transationFormatRecordNumber(transactionSequence.incrementAndGet()));

        return record;
    }



    private Map<String, Object> createDeletedDetailRecord(Long accountNumber) {
        String individualID = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 9);
        String abaNumber = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 9);
        String fileReferenceNumber =  UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 9);
        int tranCode = ThreadLocalRandom.current().nextInt(10, 100);
        double randomAmount = ThreadLocalRandom.current().nextDouble(1000, 10000);
        String formattedAmount = String.format("%,.2f", randomAmount);
        Map<String, Object> detail = new HashMap<>();
        detail.put("recordNumber", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        detail.put("totalCreditAmount", individualID);
        detail.put("totalDebitAmount", individualID);
        detail.put("sequenceFileReferenceNumber",transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        detail.put("deletionTotalCredit", individualID);
        detail.put("deletionTotalDebit",fileReferenceNumber);
        detail.put("totalCount",7);
        totalLines.incrementAndGet();
        return detail;
    }
    private Map<String, Object> createTransactionDetailRecord(Long accountNumber) {
        String individualID = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 9);
        String abaNumber = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 9);
        String fileReferenceNumber =  UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 9);
        int tranCode = ThreadLocalRandom.current().nextInt(10, 100);
        double randomAmount = ThreadLocalRandom.current().nextDouble(1000, 10000);
        String formattedAmount = String.format("%,.2f", randomAmount);
        Map<String, Object> detail = new HashMap<>();
        detail.put("recordNumber", transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        detail.put("individualID", individualID);
        detail.put("individualName", "GINA J ERRIGO");
        detail.put("tranCode", tranCode);
        detail.put("creditAmount", formattedAmount);
        detail.put("debitAmount", formattedAmount);
        detail.put("itemCount","1");
        detail.put("abaNumber", abaNumber);
        detail.put("accountNumber", String.format("%013d", accountNumber));
        detail.put("sequenceFileReferenceNumber",transationFormatRecordNumber(transactionSequence.incrementAndGet()));
        detail.put("fileReferenceNumber",fileReferenceNumber);
        totalLines.incrementAndGet();
        return detail;
    }


    private Map<String, String> createFloatRecord(int recordNumber, String amount) {
        Map<String, String> floatRecord = new HashMap<>();
        floatRecord.put("recordNumber", formatRecordNumber(recordNumber));
        floatRecord.put("amount", amount);
        totalLines.incrementAndGet();
        return floatRecord;
    }


    private String formatRecordNumber(int recordNumber) {
        return String.format("%09d", recordNumber);
    }
    private static String transationFormatRecordNumber(int recordNumber) {
        return String.format("%06d", recordNumber);
    }
    private static String recordFormatNumber(int recordNumber) {
        return String.format("%06d", recordNumber);
    }

    private String getFileCreationDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String formatAmount(double amount) {
        return new DecimalFormat("000000000000.00").format(amount);
    }
    private Map<String, String> baiCodeMap() {
        String filePath;
        try {
            filePath = new DefaultResourceLoader().getResource(baiCodeFilePath).getFile().getAbsolutePath();
            return Files.lines(Paths.get(filePath))
                    .map(line -> line.split("\\|"))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
