package com.dvtech.maker.service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@Service
@Slf4j
@RequiredArgsConstructor
public class MockAdviceDataFeedService {
    @Value("${bls-file-feed.advice.template-file-name}")
    private String templateFileName;

     @Value("${bls-file-feed.advice.feed-names}")
     private List<String> adviceFeedName;

    @Value("${bls-file-feed.advice.out-file-name}")
    private String outFileName;

    @Value("${bls-file-feed.input-data.account-file-path}")
    private String accountFilePath;


    @Value("${bls-file-feed.input-data.baiCode-file-path}")
    private String baiCodeFilePath;

//    @Value("${bls-file-feed.advice.remote-file-location}")
//    private String remoteFileLocation;
//
//    @Value("${bls-file-feed.system-environment.host}")
//    private String sftpServerName;
//
//    @Value("${bls-file-feed.system-environment.user}")
//    private String sftpUserName;
//
//    @Value("${bls-file-feed.system-environment.key}")
//    private String sftpKey;

    private final Configuration freemarkerConfig;
    private static final AtomicInteger recordSequence = new AtomicInteger(0);
    private static final AtomicInteger transactionSequence = new AtomicInteger(0);
    private static final AtomicInteger totalLines = new AtomicInteger(0);
    private static final String DATA_FILE_EXTENSION = ".dat";
    private static final String MARKER_FILE_EXTENSION = ".mrk";

    @Scheduled(cron = "${bls-file-feed.advice.schedule}")
    public void generateAdviceFeed() {
        adviceFeedName.forEach(this::generateFile);
    }

    private void generateFile(String adviceFeedName) {
        try {
            List<Long> accountNumbers = readFileLines(Paths.get(new DefaultResourceLoader()
                    .getResource(accountFilePath).getFile().getAbsolutePath()).toString());

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("header", createHeaderRecordMap());
            List<Map<String, Object>> transactions = accountNumbers.stream()
                    .map(this::createTransactionRecord)
                    .collect(Collectors.toList());

            dataModel.put("transactions", transactions);
            double totalAmount = transactions.stream()
                    .mapToDouble(txn -> Double.parseDouble(txn.get("amount").toString()))
                    .sum();

            dataModel.put("trailer", createTrailerRecordMap());
            Template template = freemarkerConfig.getTemplate(templateFileName);
            try (Writer writer = new FileWriter(outFileName)) {
                template.process(dataModel, writer);
            }

            log.info("File generated successfully: {}", outFileName);
            recordSequence.set(0);
            transactionSequence.set(0);
            totalLines.set(0);

        } catch (Exception e) {
            log.error("Error occurred", e);
        }
    }

    private List<Long> readFileLines(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines().map(Long::parseLong).collect(Collectors.toList());
        }
    }

    private Map<String, Object> createHeaderRecordMap() {
        Map<String, Object> record = new HashMap<>();
        record.put("adviceFeedName", "MAP");
        record.put("creationDate", getFileCreationDateTime());
        record.put("recordNumber", formatRecordNumber(recordSequence.incrementAndGet()));
        totalLines.incrementAndGet();
        return record;
    }

    private Map<String, Object> createTransactionRecord(long accountNumber) {
        String amount = formatAmount(Math.random() * 1000);
        Map<String, Object> transaction = new HashMap<>();
        int recordNumber = recordSequence.incrementAndGet();
        Map.Entry<String, String> randomEntry = baiCodeMap().entrySet()
                .toArray(new Map.Entry[0])[new Random().nextInt(baiCodeMap().size())];

        transaction.put("recordNumber", formatRecordNumber(recordNumber));
        transaction.put("accountNumber", String.format("%013d", accountNumber));
        transaction.put("tranCode", randomEntry.getKey());
        transaction.put("baiCode", randomEntry.getValue());
        transaction.put("sequenceNumber", transactionSequence.incrementAndGet());
        transaction.put("dateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")));
        transaction.put("amount", amount);
        transaction.put("details", Collections.singletonList(createDetailRecord(recordNumber)));
        transaction.put("floats", Collections.singletonList(createFloatRecord(recordNumber, amount)));
        transaction.put("filler", String.format("%-10s", ""));
        totalLines.incrementAndGet();
        return transaction;
    }

    private Map<String, Object> createDetailRecord(int recordNumber) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("recordNumber", formatRecordNumber(recordNumber));
        detail.put("description", String.format("%-255s", "ACH DESCRIPTION FOR THE " + recordNumber));
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
        return new DecimalFormat("000000000000.00").format(amount);
    }

    private Map<String, String> baiCodeMap() {
        try {
            return Files.lines(Paths.get(new DefaultResourceLoader().getResource(baiCodeFilePath).getFile().getAbsolutePath()))
                    .map(line -> line.split("\\|", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public void upload(String feedName, String payload) throws SftpException {
//        Session session = null;
//        ChannelSftp channel = null;
//        String remoteFeedName = remoteFileLocation + feedName;
//        try {
//            JSch jsch = new JSch();
//            session = jsch.getSession(sftpUserName, sftpServerName, 22);
//            session.setPassword(sftpKey);
//            Properties config = new Properties();
//            config.put("StrictHostKeyChecking", "no");
//            config.put("PreferredAuthentications", "password");
//            session.setConfig(config);
//            session.connect();
//            channel = (ChannelSftp) session.openChannel("sftp");
//            channel.connect();
//            channel.put(new ByteArrayInputStream(new byte[0]), remoteFeedName + MARKER_FILE_EXTENSION);
//            channel.put(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)), remoteFeedName + DATA_FILE_EXTENSION);
//        } catch (JSchException e) {
//            log.error("SFTP error", e);
//        } finally {
//            try {
//                if (channel != null) channel.disconnect();
//            } catch (Exception ignored) {
//            }
//            try {
//                if (session != null) session.disconnect();
//            } catch (Exception ignored) {
//            }
//        }
//    }
}

