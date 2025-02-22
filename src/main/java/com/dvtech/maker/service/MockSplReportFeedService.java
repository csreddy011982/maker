package com.dvtech.maker.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MockSplReportFeedService {

    private static final Logger log = LoggerFactory.getLogger(MockSplReportFeedGeneratorService.class);
    @Value("${bls-file-feed.spl-report.template-file-name}")
    private String templateFileName;
    @Value("${bls-file-feed.spl-report.account-file-path}")
    private String accountFilePath;
    @Value("${bls-file-feed.spl-report.feed-names}")
    private List<String> splReportFeedNames;
    @Value("${bls-file-feed.spl-report.baiCode-file-path}")
    private String baiCodeFilePath;

    @Value("${bls-file-feed.spl-report.out-file-path}")
    private String outputDirPath;
    private final Configuration freemarkerConfig;
    private static final AtomicInteger recordSequence = new AtomicInteger(0);
    private static final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);
    private static final Random RANDOM = new Random();
    private static final AtomicInteger sequenceCounter = new AtomicInteger(1);

    public MockSplReportFeedService(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    @PostConstruct
    public void init() {
        // This method will be called automatically after the bean is initialized
        generateSplReportFeed();
    }

    @Scheduled(cron = "${bls-file-feed.spl-report.schedule}")
    public void generateSplReportFeed() {

        splReportFeedNames.forEach(this::generateFile);

    }

    public void generateFile(String splReportFeedName) {
        try {
            List<Long> accountNumbers = readFileLines(Paths.get(new DefaultResourceLoader().getResource(accountFilePath).getFile().getAbsolutePath()).toString());

            List<Map<String, Object>> recordsMap = Arrays.asList(
                    createRecord("recordType", "WW GRAINGER" + getCompanyCode()),
                    createRecord("recordType", "ACH CASH CONCENTRATION"),
                    createRecord("recordType", "REPORTED AS OF " + getCurrentDate()),
                    createRecord("recordType", "PRINTED ON " + getCurrentDate()),
                    createRecord("recordType", "ACCOUNT " + formatAccountNumbers(accountNumbers) + " WW GRAINGER 0901"),
                    createRecord("recordType", "FR/ABA    UNIT BANK DDA      UNIT      UNIT NAME    AMOUNT"),
                    createRecord("recordType", "-----------------------------------------------------------")
            );

            String extractedAccountNumber = extractAccountNumber(recordsMap);

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("first", createFirstRecordMap(splReportFeedName));
            dataModel.put("header", createHeaderRecordMap());
            dataModel.put("records", generateDynamicRecords(recordsMap, sequenceCounter));

            List<Map<String, Object>> transactions = accountNumbers.stream().map(this::createTransactionRecord).collect(Collectors.toList());
            double totalDeposit = transactions.stream().mapToDouble(txn -> (double) txn.get("unitAmount")).sum();
            dataModel.put("transactions", transactions);

            Map<String, Object> depositDetails = new HashMap<>();
            depositDetails.put("sequenceNumber", String.format("D%08d", sequenceCounter.getAndIncrement()));
            depositDetails.put("accountNumber", extractedAccountNumber);
            depositDetails.put("depositTotal", totalDeposit);
            dataModel.put("depositDetails", depositDetails);

            Template template = freemarkerConfig.getTemplate(templateFileName);
            Writer writer = new StringWriter();
            template.process(dataModel, writer);

            // Ensure directory exists
            Files.createDirectories(Paths.get(outputDirPath));
            String filePath = Paths.get(outputDirPath, splReportFeedName + ".txt").toString();

            // Write to file
            try (Writer fileWriter = Files.newBufferedWriter(Paths.get(filePath))) {
                fileWriter.write(writer.toString());
                log.info("File successfully written: {}", filePath);
            } catch (IOException e) {
                log.error("Error writing file: {}", filePath, e);
            }

            log.info("File generated successfully: {}", splReportFeedName);
            recordSequence.set(0);
            transactionSequence.set(0);
            totalLines.set(0);

        } catch (Exception e) {
            log.error("Error occurred while generating the SPL report feed for: {}", splReportFeedName, e);
        }
    }


    private static Map<String, Object> createTransaction(AtomicInteger sequenceCounter, String frAba, String unitBankDda, String unit, String unitName, double amount) {
        Map<String, Object> txn = new HashMap<>();
        txn.put("sequenceNumber", String.format("D%08d", sequenceCounter.getAndIncrement()));
        txn.put("frAba", frAba);
        txn.put("unitBankDda", unitBankDda);
        txn.put("unit", unit);
        txn.put("unitName", unitName);
        txn.put("amount", amount);
        return txn;
    }
    private static Map<String, Object> createRecord(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Map<String, Object> createFirstRecordMap(String splReportFeedName) {
        Map<String, Object> record = new HashMap<>();
        record.put("specialFileType", splReportFeedName);
        record.put("recordNumber", generateFirstLineNumber());
        record.put("companyCode", getCompanyCode());
        totalLines.incrementAndGet();
        return record;
    }
    private Map<String,Object> createCompanyRecordMap(){
        Map<String, Object> companyData = new HashMap<>();
        companyData.put("detailNumber",formatRecordNumber(recordSequence.incrementAndGet()));
        companyData.put("name", getCompanyName());
        companyData.put("achType", getAchType());
        return  companyData;
    }

    private static List<Map<String, Object>> generateDynamicRecords(List<Map<String, Object>> recordsList, AtomicInteger sequenceCounter) {
        List<Map<String, Object>> records = new ArrayList<>();

        for (Map<String, Object> record : recordsList) {
            Map<String, Object> newRecord = new HashMap<>(record);
            newRecord.put("sequenceNumber", String.format("D%08d", sequenceCounter.getAndIncrement()));
            records.add(newRecord);
        }
        return records;
    }
    private Map<String, String> generateDynamic(List<String> records) {
        Map<String, String> recordMap = new LinkedHashMap<>();
        int recordNumber = 1;

        for (String record : records) {
            String key = String.format("D%07d", recordNumber); // Generates D0000001, D0000002, ...
            recordMap.put(key, record);
            recordNumber++;
        }

        return recordMap;
    }

    private Map<String, Object> createHeaderRecordMap() {
        Map<String, Object> headerData = new HashMap<>();
        headerData.put("headerNumber", generateHNumber());
        headerData.put("recordNumber", formatRecordNumber(recordSequence.incrementAndGet()));
        headerData.put("pageNumber", getNextPageNumber() );
        headerData.put("reportDate", getCurrentDate());
        headerData.put("printDate", getCurrentDate());
        totalLines.incrementAndGet();
        return headerData;
    }
    private String generateHNumber(){
        return String.format("%08d",1000000 + RANDOM.nextInt(90000000));
    }

    private static String generateFirstLineNumber() {
        String datePart = new SimpleDateFormat("yyMMddHHmmss").format(new Date()); // Current timestamp
        String randomDigits = String.format("%06d", RANDOM.nextInt(99999)); // Random 6-digit number
        String suffix = "TNT"; // Fixed suffix

        return  datePart + randomDigits + suffix;
    }
    private int currentPage = 1;
    // Method to get the next page number
    public String getNextPageNumber() {
        return  "PAGE " +(currentPage++);
    }

    private String getCompanyCode(){
        return "550";
    }
    private String getCompanyName(){
        return "WW GRAINGER 0901";
    }
    private String getAchType(){
        return "ACH CASH CONCENTRATION";
    }

    private static String formatSequenceNumber(int recordNumber) {
        return String.format("%08d", recordNumber);
    }
    private String formatRecordNumber(int recordNumber) {
        return String.format("%06d", recordNumber);
    }
    // ✅ Generate a Random 5-Digit Unit Number
    private static String generateRandomUnitNumber() {
        Random rand = new Random();
        return String.format("%05d", rand.nextInt(90000) + 10000); // Generates a 5-digit number
    }

    private static String generateFrAba() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(90000) + 10000); // Generates a 6-digit number
    }

      private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
    }

    private List<Long> readFileLines(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines().map(Long::parseLong).collect(Collectors.toList());
        }
    }
    private static String extractAccountNumber(List<Map<String, Object>> recordsMap) {
        Pattern pattern = Pattern.compile("ACCOUNT(\\d+)"); // Regex to find account number
        for (Map<String, Object> record : recordsMap) {
            String recordType = (String) record.get("recordType");
            Matcher matcher = pattern.matcher(recordType);
            if (matcher.find()) {
                String accountNumber = matcher.group(1); // Extract and return the account number
                return accountNumber.length() > 8 ? accountNumber.substring(accountNumber.length() - 8) : accountNumber;
            }

        }
        throw new RuntimeException("Account number not found in recordsMap!");
    }
    // ✅ Generate a Random Account Number (Simulated)
    private static String generateAccountNumberUpdated() {
        Random rand = new Random();
        return String.format("%010d", rand.nextInt(1000000000)); // Generate 10-digit random account number
    }
    private Map<String, Object> createTransactionRecord(long accountNumber) {
        Double amount = formatAmount(Math.random() * 1000);
        Map<String, Object> txn = new HashMap<>();
        txn.put("sequenceNumber",String.format("D%08d", sequenceCounter.getAndIncrement()));
        txn.put("frAba", generateFrAba());
        txn.put("unitBankDda", String.format("%013d", accountNumber));
        txn.put("unit", generateRandomUnitNumber());
        txn.put("unitName", "GRAINGER");
        txn.put("unitAmount", amount);
        totalLines.incrementAndGet();
        return txn;
    }

    private double formatAmount(double amount) {
        //return " " + new DecimalFormat("0000000000000000.00").format(amount);
        //return new DecimalFormat("#.00").format(amount);
        return Double.parseDouble(new DecimalFormat("#.00").format(amount));

    }
    private static List<String> formatAccountNumbers(List<Long> accountNumbers) {
        return accountNumbers.stream()
                .map(num -> String.format("%017d", num))
                .collect(Collectors.toList());
    }

}
