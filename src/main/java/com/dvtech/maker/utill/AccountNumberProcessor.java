package com.dvtech.maker.utill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountNumberProcessor {

    // Method to format a single account number to a 17-digit string with leading zeros
    public static String formatAccountNumber(Long accountNumber) {
        return String.format("%017d", accountNumber);
    }

    // Method to format a list of account numbers
    public static List<String> formatAccountNumbers(List<Long> accountNumbers) {
        List<String> formattedNumbers = new ArrayList<>();
        for (Long number : accountNumbers) {
            formattedNumbers.add(formatAccountNumber(number));
        }
        return formattedNumbers;
    }

    // Method to generate records map based on formatted account numbers
    public static List<Map<String, Object>> generateRecordsMap(List<Long> accountNumbers) {
        List<String> formattedAccountNumbers = formatAccountNumbers(accountNumbers);
        List<Map<String, Object>> recordsMap = new ArrayList<>();

        for (String formattedNumber : formattedAccountNumbers) {
            Map<String, Object> record = new HashMap<>();
            record.put("recordType", "ACCOUNT " + formattedNumber + " WW GRAINGER 0901");
            recordsMap.add(record);
        }

        return recordsMap;
    }

    public static void main(String[] args) {
        // Example usage
        List<Long> accountNumbers = new ArrayList<>();
        accountNumbers.add(54070L);
        accountNumbers.add(2731649L);
        accountNumbers.add(62111L);
        accountNumbers.add(4922L);
        accountNumbers.add(49820L);
        accountNumbers.add(31296733L);

        List<Map<String, Object>> recordsMap = generateRecordsMap(accountNumbers);

        // Display the records
        for (Map<String, Object> record : recordsMap) {
            System.out.println(record.get("recordType"));
        }
    }
}
