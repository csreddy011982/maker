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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class MockAconReport {
    private static final Logger log = LoggerFactory.getLogger(MockAconReport.class);

    @Value("${bls-file-feed.spl-acon.account-file-path}")
    private String accountFilePath;
    @Value("${bls-file-feed.spl-acon.feed-names}")
    private List<String> splReportFeedNames;
    @Value("${bls-file-feed.spl-acon.template-file-name}")
    private String templateFileName;
    @Value("${bls-file-feed.spl-acon.out-file-path}")
    private String outputDirPath;

//    @Value("${bls-file-feed.advice.schedule}")
//    private String cronSchedule;

    @Autowired
    private Configuration freemarkerConfig;

    private static final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final AtomicInteger recordSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);
    @Scheduled(cron = "${bls-file-feed.spl-acon.schedule}")
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

            String outputFileName = outputDirPath + "ACON" + getFileCreationDateTime() + "TNT.DAT";


            //generateFileFromTemplate(outputFileName, "defaultTemplate.ftl", dataModel);
            generateFileFromTemplate("ACON", dataModel);
            // log.info("Writing data to file: {}", outputFileName);
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



        double totalDebitAmount = transactions.stream()
                .map(t -> t.get("debitAmount"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .mapToDouble(Double::parseDouble)
                .sum();

        int totalRecords = transactionSequence.get();
        dataModel.put("transactions", transactions);
        dataModel.put("trailer", createTrailerRecordMap(accountNumber));

        //dataModel.put("trailer", createTrailer(totalRecords + 2));
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
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)).toUpperCase();
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
        accountTransaction.put("recordNumber8", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("creationDate", getFileCreationDate());
        accountTransaction.put("accountNumber", String.format("%013d", accountNumber));
        accountTransaction.put("companyCode", "0901");
        accountTransaction.put("currentDate",currentDate);
        List<Map<String, Object>> detailsList = new ArrayList<>();
        int numberOfTransactions = ThreadLocalRandom.current().nextInt(1, 4); // 1-3 transactions per account

        double totalCreditAmount = 0.0; // Variable to store the total sum of credited amounts
        double totalDebitAmount = 0.0;  // Variable to store the total sum of debited amounts

        for (int i = 0; i < numberOfTransactions; i++) {
            Random random = new Random();
            double creditAmountValue = Math.random() * 1000; // Generate a random credit amount
            double debitAmountValue = Math.random() * 500;  // Generate a random debit amount

            String creditAmount = formatAmount(creditAmountValue); // Format for display
            String debitAmount = formatAmount(debitAmountValue);
            Map<String, Object> detail = new HashMap<>();
            detail.put("recordNumber", formatRecordNumberWithD(transactionSequence.incrementAndGet()));

            detail.put("frAba", String.format("%06d", random.nextInt(999999)));
            detail.put("unitBankDDA", String.format("%013d", random.nextInt(999999999)));
            detail.put("unit", String.format("%05d", random.nextInt(99999)));
            detail.put("unitName","GRAINGER");
            detail.put("creditAmount", creditAmount);
            detail.put("debitAmount", debitAmount);

            // Add to total sums
            totalCreditAmount += creditAmountValue;
            totalDebitAmount += debitAmountValue;

            totalLines.incrementAndGet();
            detailsList.add(detail);
        }
        accountTransaction.put("deposit", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("total", formatRecordNumberWithD(transactionSequence.incrementAndGet()));
        accountTransaction.put("lastEndRecord", formatRecordNumber(transactionSequence.incrementAndGet()-1));
        accountTransaction.put("totalCreditAmount", formatAmount(totalCreditAmount));
        accountTransaction.put("totalDebitAmount", formatAmount(totalDebitAmount));


        //accountTransaction.put("totalCreditAmount", formatAmount(totalCreditAmount));
        // ✅ Ensure detailsList is added
        accountTransaction.put("detailsList", detailsList);

        // ✅ Add transaction object to the list
        transactionList.add(accountTransaction);
        recordSequence.set(0);
        transactionSequence.set(0);
        totalLines.set(0);
        // totalLines.set(0);
        return transactionList;
    }

    private int generateRandomNumberForTwoDigit() {
        return ThreadLocalRandom.current().nextInt(1, 11); // Upper bound is exclusive (11 means it goes up to 10)
    }


    private Map<String, Object> createTrailer(int recordNumber) {
        Map<String, Object> trailer = new HashMap<>();
        trailer.put("recordNumber", formatRecordNumberWithD(recordNumber));
        return trailer;
    }


    private void generateFileFromTemplate(String baseFileName, Map<String, Object> dataModel) {
        // Generate timestamp in "yyyyMMddHHmmss" format
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Append timestamp to the base file name
        String outputFilePath = String.format("%s_%s.DAT", baseFileName, timestamp);

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
    private String formatRecordNumber(int number) {
        return String.format("%06d", number);
    }
    private String formatRecordNumberWithD(int number) {
        return String.format("D%07d", number);
    }

    private String generateRandomID() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000000, 999999999));
    }

    private double generateRandomAmount() {
        // double amount = ThreadLocalRandom.current().nextDouble(1000, 10000);
        return ThreadLocalRandom.current().nextDouble(1000, 10000);
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
    private Map<String, Object> createTrailerRecordMap(long accountNumber) {
        Map<String, Object> record = new HashMap<>();
        record.put("accountNumber", String.format("%08d", accountNumber));
        record.put("lastDigit",  recordFormatNumber(transactionSequence.get()));
        return record;
    }
    private static String recordFormatNumber(int recordNumber) {
        return String.format("%06d", recordNumber);
    }
}
