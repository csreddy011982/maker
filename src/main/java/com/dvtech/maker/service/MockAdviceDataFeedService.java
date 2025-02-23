package com.dvtech.maker.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Component
public class MockAdviceDataFeedService {
    @Value("${bls-file-feed.advice.template-file-name}")
    private String templateFileName;

    @Value("${bls-file-feed.advice.feed-names}")
    private List<String> adviceFeedNames;

    @Value("${bls-file-feed.input-data.account-file-path}")
    private String accountFilePath;

    @Value("${bls-file-feed.input-data.baiCode-file-path}")
    private String baiCodeFilePath;

    @Value("${bls-file-feed.advice.remote-file-location}")
    private String remoteFileLocation;

    // private final MockBLServiceUtil mockBLServiceUtil;
    private final Configuration freemarkerConfig;
    private final AtomicInteger recordSequence = new AtomicInteger(0);
    private final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);

   // @Scheduled(cron = "${bls-file-feed.advice.schedule}")
    public void generateAdviceFeed() {
        adviceFeedNames.forEach(this::generateFile);
    }
    private void generateFile(String adviceFeedName) {
        try {
            List<Long> accountNumbers = readFileLines(Paths.get(new DefaultResourceLoader().getResource(accountFilePath).getFile().getAbsolutePath()).toString());

            Map<String, Object> dataModel = new HashMap();
            dataModel.put("header", createHeaderRecordMap(adviceFeedName));
            dataModel.put("transactions", accountNumbers.stream()
                    .map(this::createTransactionRecord)
                    .collect(Collectors.toList()));
            dataModel.put("trailer", createTrailerRecordMap());

            // Call the extracted method to generate and write the file
            generateFileFromTemplate(adviceFeedName, dataModel);
            log.info("File generated successfully: {}", adviceFeedName);

            recordSequence.set(0);
            transactionSequence.set(0);
            totalLines.set(0);
        } catch (Exception e) {
            log.error("Error Occurred", e);
        }
    }
    /**
     * New method for generating a file from FreeMarker template.
     */
    private void generateFileFromTemplate(String outputFilePath, Map<String, Object> dataModel) {
        try (Writer writer = new FileWriter(outputFilePath)) {
            Template template = freemarkerConfig.getTemplate(templateFileName);
            template.process(dataModel, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Error processing FreeMarker template: " + outputFilePath, e);
        }
    }

    private List<Long> readFileLines(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines().map(Long::parseLong).collect(Collectors.toList());
        }
    }
        private Map<String, Object> createHeaderRecordMap(String adviceFeedName) {
            Map<String, Object> record = new HashMap<>();
            record.put("adviceFeedName", adviceFeedName);
            record.put("creationDate", getFileCreationDateTime());
            record.put("recordNumber", formatRecordNumber(recordSequence.incrementAndGet()));
            totalLines.incrementAndGet();
            return record;
        }

        private Map<String, Object> createTransactionRecord(long accountNumber) {
            String amount = formatAmount(Math.random() * 1000);
            Map<String, Object> transaction = new HashMap<>();
            int recordNumber = recordSequence.incrementAndGet();

            Map.Entry<String, String> randomEntry = (Map.Entry<String, String>) baiCodeMap().entrySet()
                    .toArray()[new Random().nextInt(baiCodeMap().size())];

            transaction.put("recordNumber", formatRecordNumber(recordNumber));
            transaction.put("accountNumber", String.format("%013d", accountNumber));
            transaction.put("tranCode", randomEntry.getKey());
            transaction.put("baiCode", randomEntry.getValue());
            transaction.put("sequenceNumber", transactionSequence.incrementAndGet());
            transaction.put("dateTime", LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")));
            transaction.put("amount", amount);
            transaction.put("details", Collections.singletonList(createDetailRecord(recordNumber)));
            transaction.put("floats", Collections.singletonList(createFloatRecord(recordNumber, amount)));
            transaction.put("filler", String.format("%19s", ""));
            totalLines.incrementAndGet();
            return transaction;
        }
        private Map<String, Object> createDetailRecord(int recordNumber) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("recordNumber", formatRecordNumber(recordNumber));
            detail.put("description", String.format("%-255s", "ACH DESCRIPTION FOR THE " + recordNumber));
            detail.put("filter", "~");
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
        private Map<String, Object> createTrailerRecordMap() {
            Map<String, Object> record = new HashMap<>();
            record.put("creationDate", getFileCreationDateTime());
            record.put("recordNumber", formatRecordNumber(recordSequence.incrementAndGet()));
            record.put("totalLines", formatRecordNumber(totalLines.incrementAndGet()));
            return record;
        }
        private String formatRecordNumber(int recordNumber) {
            return String.format("%09d", recordNumber);
        }
        private String getFileCreationDateTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        private String formatAmount(double amount) {
            return " " + new DecimalFormat("000000000000.00").format(amount);
        }
        private String formatSequenceNumber(int recordNumber) {
            return String.format("%011d", recordNumber);
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
