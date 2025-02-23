package com.dvtech.maker.service;

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
    @Value("${bls-file-feed.spl-report.feed-names}")
    private List<String> splReportFeedNames;
    @Value("${bls-file-feed.spl-report.template-file-name}")
    private String templateFileName;
    @Value("${bls-file-feed.spl-report.out-file-path}")
    private String outputDirPath;

    @Value("${bls-file-feed.advice.schedule}")
    private String cronSchedule;

    @Autowired
    private Configuration freemarkerConfig;

    private static final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final AtomicInteger recordSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);
    @Scheduled(cron = "${bls-file-feed.advice.schedule}")
    public void generateAdviceFeed() {
        splReportFeedNames.forEach(this::generateFile);
    }

    public void generateFile(String adviceFeedName) {
        log.info("Scheduled Job Started for ACH Deletion Report");

        try {
            File accountFile = new DefaultResourceLoader().getResource(accountFilePath).getFile();
            List<Long> accountNumbers = readFileLines(accountFile.getAbsolutePath());

            if (accountNumbers.isEmpty()) {
                log.warn("No accounts found in account.dat. Skipping file generation.");
                return;
            }

            Map<String, Object> dataModel = buildDataModel(adviceFeedName, accountNumbers);
            log.info("Data Model Created: {}", dataModel);

            if (dataModel.isEmpty()) {
                log.warn("Data model is empty, skipping file writing.");
                return;
            }

            String outputFileName = outputDirPath + "LADEL" + getFileCreationDateTime() + "TNT.DAT";
            log.info("Writing data to file: {}", outputFileName);

            //generateFileFromTemplate(outputFileName, "defaultTemplate.ftl", dataModel);
            generateFileFromTemplate("ADELL", dataModel);

        } catch (Exception e) {
            log.error("Error Occurred while generating file", e);
        }
    }


    private Map<String, Object> buildDataModel(String adviceFeedName,List<Long> accountNumbers) {
        Map<String, Object> dataModel = new HashMap<>();

        long accountNumber = accountNumbers.isEmpty() ? 0 : accountNumbers.get(0);
        dataModel.put("firstLine", firstHeaderRecordMap(adviceFeedName));
       // dataModel.put("header", createHeader(accountNumber, recordSequence.incrementAndGet()));

        List<Map<String, Object>> transactions = accountNumbers.stream()
                .map(this::createTransactionRecords)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            log.warn("No transactions were created, skipping file generation.");
            return Collections.emptyMap();
        }

        double totalCreditAmount = transactions.stream()
                .map(t -> t.get("creditAmount"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .mapToDouble(Double::parseDouble)
                .sum();

        double totalDebitAmount = transactions.stream()
                .map(t -> t.get("debitAmount"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .mapToDouble(Double::parseDouble)
                .sum();

        int totalRecords = transactionSequence.get();
        dataModel.put("transactions", transactions);
        dataModel.put("totalCreditAmount", formatAmount(totalCreditAmount));
        dataModel.put("totalDebitAmount", formatAmount(totalDebitAmount));
        dataModel.put("totalRecordNumber", (totalRecords ));
        dataModel.put("trailer", createTrailer(totalRecords + 2));
        recordSequence.set(0);
        transactionSequence.set(0);
        totalLines.set(0);
        return dataModel;
    }



    private Map<String, Object> createHeader(long accountNumber, int recordSequence) {
        Map<String, Object> header = new HashMap<>();
        // ✅ Store dynamic values in header
        header.put("recordNumber", String.format("%06d", recordSequence));
         // Ensures 13-digit formatting
        header.put("accountNumber", String.format("%08d", accountNumber));
        header.put("creationDate", getFileCreationDate());
        totalLines.incrementAndGet();
        return header;
    }


    private List<Map<String, Object>> createTransactionRecords(Long accountNumber) {
        List<Map<String, Object>> transactionList = new ArrayList<>();
        Map<String, Object> accountTransaction = new HashMap<>();

        // ✅ Add recordNumber at the transaction level
        accountTransaction.put("account", String.format("%08d", accountNumber));
        accountTransaction.put("sequence", String.format("%06d", recordSequence.incrementAndGet()));
        accountTransaction.put("recordNumber", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber1", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber2", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber3", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber4", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber5", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber6", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("recordNumber7", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("creationDate", getFileCreationDate());
        accountTransaction.put("accountNumber", String.format("%013d", accountNumber));

        List<Map<String, Object>> detailsList = new ArrayList<>();
        int numberOfTransactions = ThreadLocalRandom.current().nextInt(1, 4); // 1-3 transactions per account

        for (int i = 0; i < numberOfTransactions; i++) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("recordNumber", formatRecordNumberWithD(transactionSequence.incrementAndGet()));

            detail.put("individualID", generateRandomID());
            detail.put("individualName", getRandomName());
            detail.put("tranCode", getRandomTransactionCode());
            detail.put("creditAmount", generateRandomAmount());
            detail.put("debitAmount", formatAmount(0.00));
            detail.put("abaNumber", generateRandomABANumber());
            detail.put("accountNumber", String.format("%013d", accountNumber));
            detail.put("fileReferenceNumber", generateRandomFileReference());
            detail.put("recordNumber1", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
            totalLines.incrementAndGet();
            detailsList.add(detail);
        }

        // ✅ Ensure detailsList is added
        accountTransaction.put("detailsList", detailsList);

        // ✅ Add transaction object to the list
        transactionList.add(accountTransaction);
        recordSequence.set(0);
        transactionSequence.set(0);
       // totalLines.set(0);
        return transactionList;
    }



    private Map<String, Object> createTrailer(int recordNumber) {
        Map<String, Object> trailer = new HashMap<>();
        trailer.put("recordNumber", formatRecordNumberWithD(recordNumber));
        return trailer;
    }
    private void generateFileFromTemplate(String outputFilePath, Map<String, Object> dataModel) {
        try (Writer writer = new FileWriter(outputFilePath)) {
            Template template = freemarkerConfig.getTemplate(templateFileName);
            template.process(dataModel, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Error processing FreeMarker template: " + outputFilePath, e);
        }
    }

    private List<Long> readFileLines(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines()
                    .filter(line -> line.matches("\\d+"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }

    private String formatRecordNumberWithD(int number) {
        return String.format("D%06d", number);
    }

    private String generateRandomID() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000000, 999999999));
    }

    private String generateRandomAmount() {
        double amount = ThreadLocalRandom.current().nextDouble(1000, 10000);
        return DECIMAL_FORMAT.format(amount);
    }

    private String generateRandomABANumber() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000000, 999999999));
    }

    private String generateRandomFileReference() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(221730000, 221739999));
    }

    private int getRandomTransactionCode() {
        int[] codes = {22, 32, 52};
        return codes[ThreadLocalRandom.current().nextInt(codes.length)];
    }

    private String getRandomName() {
        String[] names = {"GINA J ERRIGO", "CHARLOTTE ANDERSON", "JENNIFER J FITZPATRICK", "ANGELO L DICKINSON"};
        return names[ThreadLocalRandom.current().nextInt(names.length)];
    }

    private String formatAmount(double amount) {
        return String.format("%10.2f", amount);
    }

    private String getFileCreationDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yy"));
    }

    private String getFileCreationDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    private Map<String, Object> firstHeaderRecordMap(String adviceFeedName) {
        Map<String, Object> record = new HashMap<>();
        record.put("adviceFeedName", adviceFeedName);
        record.put("creationDate", getFileCreationDateTime());
        return record;
    }
}
