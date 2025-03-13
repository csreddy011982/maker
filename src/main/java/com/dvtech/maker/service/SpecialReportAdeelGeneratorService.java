package com.dvtech.maker.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service

@RequiredArgsConstructor
public class SpecialReportAdeelGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(SpecialReportAdeelGeneratorService.class);
    @Value("${bls-file-feed.adeel.template-file-name}")
    private String templateFileName;
    private final Configuration freemarkerConfig;
    private final MockBlsServiceUtil mockBlsServiceUtil;
    private static final AtomicInteger sequence = new AtomicInteger(10);
    private double total;
    private double totalItemCount;
    @Scheduled(cron = "${bls-file-feed.adeel.schedule}")
    public void generateFeed() throws TemplateException, IOException {
        total = 0;
        totalItemCount = 0;
        Map<String, Object> dataModel = new HashMap<>();
        Template template = freemarkerConfig.getTemplate(templateFileName);
        dataModel.put("header", createHeaderRecordMap());
        List<Map<String, String>> detailRecords = IntStream.range(0, 19)
                .mapToObj(i -> createDetailRecordMap())
                .collect(Collectors.toList());
        dataModel.put("detailRecords", detailRecords);
        dataModel.put("report", createReportMap());

        try (Writer writer = new FileWriter("ADELL.dat")) {
            template.process(dataModel, writer);
        }

    }

    private Map<String, String> createDetailRecordMap() {
        Map<String, String> detailRecordMap = new HashMap<>();
        detailRecordMap.put("sequence", String.format("%07d", sequence.getAndIncrement()));
        detailRecordMap.put("nextSequence", String.format("%07d", sequence.getAndIncrement()));
        detailRecordMap.put("individualId", String.format("%10s", mockBlsServiceUtil.getAba()));
        detailRecordMap.put("individualName", String.format("%20s", getRandomCompanyName()));
        detailRecordMap.put("tc", String.format("%20s",getRandomTransactionCode()));
        detailRecordMap.put("creditedAmount", String.format("%10s", getRandomAmount()));
        detailRecordMap.put("debitAmount", getRandomDebitAmount());
        double itemCount = Double.parseDouble(getRandomDigit());
        detailRecordMap.put("itemCount",String.format("%5s",getRandomDigit()));
        detailRecordMap.put("aba",String.format("%5s",mockBlsServiceUtil.getAba()));
        detailRecordMap.put("randomAccountNumber", String.format("%20s",getRandomNumber(10)));
        detailRecordMap.put("referenceNumber", getRandomNumber(10));
        detailRecordMap.put("unitName", "DEN COMP");
        detailRecordMap.put("amount", String.format("%28s", getRandomAmount()));
        totalItemCount += itemCount;
        return detailRecordMap;
    }

    private Map<String, String> createReportMap() {
        Map<String, String> report = new HashMap<>();

        report.put("accountNumberNoPadding", "12345678");
        report.put("accountNumber", "0012345678");
        report.put("reportedDate",getDate());
        report.put("companyCode","135847320");
        report.put("printDate",getDate());
        report.put("sequenceOne", String.format("%07d", sequence.getAndIncrement()));
        report.put("sequenceTwo", String.format("%07d", sequence.get()));
        report.put("total", new DecimalFormat("#,##0.00").format(total));
        report.put("totalItemCount", String.valueOf(totalItemCount));
        report.put("totalLines", String.format("%06d", sequence.get()));
        report.put("date",getFileCreationDate());
        return report;
    }


    private Map<String, String> createHeaderRecordMap() {
        Map<String, String> header = new HashMap<>();
        header.put("reportDate", getReportDate());
        header.put("creationDate", getFileCreationDateTime());
        return header;
    }

    private String getFileCreationDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    }

    private String getReportDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
    }
    private String getFileCreationDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yy"));
    }
    private String getRandomNumber(int length) {
        SecureRandom randomData = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(randomData.nextInt(10));
        }
        return sb.toString();
    }
    private String getRandomDigit() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(9) + 1); // Generates 1-9 and converts to String
    }



    private String getDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")).toUpperCase();
    }

    private String getRandomAmount() {
        SecureRandom randomData = new SecureRandom();
        double amount = 1000 + (999999 - 1000) * randomData.nextDouble();
        total = total + amount;
        return new DecimalFormat("#,###.00").format(amount);
    }
    private String getRandomDebitAmount() {
        double amount = 0.00;
        return new DecimalFormat("0.00").format(amount);
    }
    private String getRandomCompanyName() {
        String[] names = {"GINA J ERRIGO", "CHARLOTTE ANDERSON", "JENNIFER J FITZPATRICK", "ANGELO L DICKINSON"};
        return names[ThreadLocalRandom.current().nextInt(names.length)];
    }
    private String getRandomTransactionCode() {
        int[] codes = {22, 32, 52};
        return String.valueOf(codes[ThreadLocalRandom.current().nextInt(codes.length)]);
    }

}
