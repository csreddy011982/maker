package com.dvtech.maker.service;

import com.dvtech.maker.test.ReportGenerator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import com.dvtech.maker.model.Transaction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.*;
import java.text.DecimalFormat;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Component
public class MockSplReportFeedGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(MockSplReportFeedGeneratorService.class);
    @Value("${bls-file-feed.spl-report.template-file-name}")
    private String templateFileName;
    @Value("${bls-file-feed.spl-report.account-file-path}")
    private String accountFilePath;
    @Value("${bls-file-feed.spl-report.companyName-file-path}")
    private  String companyNameFilePath;
    @Value("${bls-file-feed.spl-report.feed-names}")
    private List<String> splReportFeedNames;
    @Value("${bls-file-feed.spl-report.baiCode-file-path}")
    private String baiCodeFilePath;

    @Value("${bls-file-feed.spl-report.out-file-path}")
    private String outputDirPath;
   // private final Configuration freemarkerConfig;
    private static final AtomicInteger recordSequence = new AtomicInteger(0);
    private static final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);
    private static final Random RANDOM = new Random();
    private static final AtomicInteger sequenceCounter = new AtomicInteger(1);
    private static Configuration freemarkerConfig;
    // Stores company codes and names from file
    private Map<String, String> companyMap = new HashMap<>();
    @PostConstruct
    public void init() {
        // This method will be called automatically after the bean is initialized
        loadCompanyData();
        generateSplReportFeed();
    }
    static {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassLoaderForTemplateLoading(ReportGenerator.class.getClassLoader(), "templates");
    }
    @Scheduled(cron = "$(bls-file-feed.spl-report.schedule}")
    public void generateSplReportFeed() {
        try{
            splReportFeedNames.forEach(this::generateFile);
        }
        catch (Exception e) {
            log.error("Error generating SPL Report Feed", e);
        }
    }

    public void generateFile(String splReportFeedName) {
        try {
            if(splReportFeedName != null) {

                List<Long> accountNumbers = readFileLines(Paths.get(new DefaultResourceLoader().getResource(accountFilePath).getFile().getAbsolutePath()).toString());
                List<Map<String, Object>> reports = generateReports(accountNumbers);

                // Write to file with file header
                writeToFile(splReportFeedName, reports);
            }
            else {
                log.error("Special Report Feed NAME Null");
            }
        } catch (Exception e) {
            log.error("Error Occurred during ACON file generation ", e);
        }
    }

    private List<Long> readFileLines(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())  // Avoid empty lines
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }

    private  List<Map<String, Object>> generateReports(List<Long> accountNumbers) {

        List<Map<String, Object>> reports = new ArrayList<>();
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)).toUpperCase();

        for (Long accountNumber : accountNumbers) {
            for (Map.Entry<String, String> companyEntry : companyMap.entrySet()) {
                String formattedAccount = String.format("%014d", accountNumber);
                String accountNumberStr = String.format("%08d", accountNumber);
                // Fetch company details dynamically
                String companyCode = companyEntry.getKey();
                String companyName = companyEntry.getValue();
                int sequenceCounter = 1;

                // Account Header
                String header = String.format("H%s       %07d                                                      ", accountNumberStr, sequenceCounter++);
                // Generate Details
                List<String> details = generateDetails(accountNumber, sequenceCounter, companyCode, companyName);
                sequenceCounter += details.size();

                // Footer Section (Including last sequence number without "D")

                String lastSequenceNumber = String.format("%07d", sequenceCounter - 2);

                String footer = String.format("T%s       %s", accountNumberStr, lastSequenceNumber);
                // Create Report Structure
                reports.add(createReport(header, details, footer));
            }
        }
        return reports;
    }

    private static List<Transaction> generateTransactions(String accountNumber) {
        Transaction transaction = new Transaction();
        Random random = new Random();
        List<Transaction> transactions = new ArrayList<>();
        int transactionCount = 3 + random.nextInt(3); // 3 to 5 transactions

        for (int i = 0; i < transactionCount; i++) {
            String frAba = transaction.setFrAba(String.format("%06d", random.nextInt(999999)));
            String unitBankDDA = transaction.setUnitBankDDA(String.format("%013d", random.nextInt(999999999)));
            String unit = transaction.setUnit(String.format("%05d", random.nextInt(99999)));
            String unitName = transaction.setUnitName("GRAINGER");
            double amount = transaction.setAmount(100 + random.nextDouble() * 400); // Amount between 100 - 500
            transactions.add(new Transaction(frAba, unitBankDDA, unit, unitName, amount));
        }

        return transactions;
    }
    /**
     * Generates the File Header (Separate from Account Headers)
     */
    private static String generateFileHeader(String fileName) {
        String generatedNumber = generateRandom13DigitNumber();
        return String.format("L"+fileName+"%sTNT                  550", generatedNumber);
    }
    /**
     * Generates the File Header (Separate from Account Headers)
     */
    private static String generateFileFooter(String fileName) {
        int detailSequence = 1;
        String generatedNumber = generateRandom13DigitNumber();
        return String.format("T"+fileName+"%06d  ", detailSequence);
    }
    /**
     * Generates a 13-digit random number
     */
    private static String generateRandom13DigitNumber() {
        Random random = new Random();
        long number = 1_000_000_000_000L + random.nextLong();
        return String.valueOf(number);
    }
    private static Map<String, Object> createReport(String header, List<String> details, String footer) {
        Map<String, Object> report = new HashMap<>();
        report.put("header", header);
        report.put("details", details);
        report.put("footer", footer);
        return report;
    }
    private  void writeToFile(String fileName, List<Map<String, Object>> reports) {
        // Generate timestamp in the format "MMM_dd_yyyy_HHmmss"
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).toUpperCase();
        // Append timestamp to the provided filename
        String sanitizedOutputDir = outputDirPath.replace("\"", "").trim();
        log.info("Writing report to directory: {}", sanitizedOutputDir);

        String finalFilePath = Paths.get(sanitizedOutputDir, String.format("%s%s.DAT", fileName, timestamp)).toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(finalFilePath))) {
            // Write File Header first
            writer.write(generateFileHeader(fileName));
            writer.newLine();
            Template template = freemarkerConfig.getTemplate("reportTemplate.ftl");
            for (Map<String, Object> report : reports) {
                StringWriter stringWriter = new StringWriter();
                template.process(report, stringWriter);
                writer.write(stringWriter.toString());
                writer.newLine();
             }
            writer.write(generateFileFooter(fileName));
            System.out.println("Report written to " + finalFilePath);
        } catch (IOException | TemplateException e) {
            log.error("Error writing report file: {}", finalFilePath, e);
        }
    }
    private static List<String> generateDetails(Long accountNumber, int sequenceCounter, String companyCode, String companyName) {
        String formattedAccount = String.format("%014d", accountNumber);
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        //String companyCode = "0901";
        List<String> details = new ArrayList<>();
        int pageCounter = 1;
        int detailSequence = 1;  // Reset to 1 for every account
        details.add(String.format("D%06d1                     %s %s   PAGE %d", detailSequence++,companyName, companyCode,pageCounter++));
        details.add(String.format("D%06d                      ACH CASH CONCENTRATION", detailSequence++));
        details.add(String.format("D%06d                      REPORTED AS OF %s", detailSequence++, currentDate));
        details.add(String.format("D%06d                        PRINTED ON %s", detailSequence++, currentDate));
        details.add(String.format("D%06d0         ACCOUNT %s %s %s", detailSequence++, formattedAccount,companyName, companyCode));
        details.add(String.format("D%06d          REDEPOSIT  / REJECT REPAIR ACTIVITY ", detailSequence++));
        details.add(String.format("D%06d0                AUTOMATED CLEARING HOUSE DEPOSIT VERIFICATION FOR: %s", detailSequence++, companyCode));
        details.add(String.format("D%06d0          FR/ABA      UNIT BANK DDA      UNIT      UNIT NAME    AMOUNT ", detailSequence++));
        details.add(String.format("D%06d           ----------------------------------------------------------- ", detailSequence++));
        // Transactions
        List<Transaction> transactions = generateTransactions(formattedAccount);
        double totalDeposits = 0.0;
        for (Transaction transaction : transactions) {
            String transactionDetail = String.format("D%06d            %-10s %-15s %-10s %-10s %10.2f",
                    detailSequence++, transaction.getFrAba(), transaction.getUnitBankDDA(), transaction.getUnit(), transaction.getUnitName(), transaction.getAmount());
            details.add(transactionDetail);
            totalDeposits += transaction.getAmount();
        }

        // Calculate Totals
        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
        String formattedTotalDeposits = decimalFormat.format(totalDeposits);
        String formattedTotalCredits = decimalFormat.format(totalDeposits);
        String formattedTotalDebits = "0.00"; // Modify logic if needed

        details.add(String.format("D%06d0          DEPOSIT ACCOUNT NUMBER: %s  DEPOSIT TOTAL: %s", detailSequence++, formattedAccount, formattedTotalDeposits));
        details.add(String.format("D%06d0          TOTAL CREDITS: %s  TOTAL DEBITS: %s", detailSequence++, formattedTotalCredits, formattedTotalDebits));

        return details;
    }

    private static String formatDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return localDate.getMonth().toString().substring(0, 3) + " " + localDate.getDayOfMonth() + ", " + localDate.getYear();
    }
    private void loadCompanyData() {
        if (companyNameFilePath == null || companyNameFilePath.trim().isEmpty()) {
            log.error("Company name file path is null or empty! Check application.properties.");
            return;
        }

        try (InputStream inputStream = new DefaultResourceLoader().getResource(companyNameFilePath).getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    companyMap.put(parts[0].trim(), parts[1].trim());
                }
            }
            log.info("Loaded company data: {}", companyMap);
        } catch (IOException e) {
            log.error("Error loading company data from file: {}", companyNameFilePath, e);
        }
    }




}


