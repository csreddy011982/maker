package com.dvtech.maker.utill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class AccountTransactionMapper {
    // Method to read transactions from a data source and map them to account numbers
    public static Map<Long, List<Transaction>> mapTransactionsToAccounts(List<String> transactionData) {
        Map<Long, List<Transaction>> accountTransactionsMap = new HashMap<>();

        for (String data : transactionData) {
            // Assuming each data string is formatted as "accountNumber,sequenceNumber,frAba,unitBankDda"
            String[] parts = data.split(",");
            if (parts.length == 4) {
                Long accountNumber = Long.parseLong(parts[0]);
                int sequenceNumber = Integer.parseInt(parts[1]);
                String frAba = parts[2];
                String unitBankDda = parts[3];

                Transaction transaction = new Transaction(sequenceNumber, frAba, unitBankDda);

                // Add the transaction to the corresponding account's list
                accountTransactionsMap
                        .computeIfAbsent(accountNumber, k -> new ArrayList<>())
                        .add(transaction);
            }
        }

        return accountTransactionsMap;
    }

    public static void main(String[] args) {
        // Example transaction data
        List<String> transactionData = new ArrayList<>();
        transactionData.add("54070,1,123456789,987654321");
        transactionData.add("54070,2,123456780,987654320");
        transactionData.add("2731649,1,123456781,987654322");

        Map<Long, List<Transaction>> accountTransactionsMap = mapTransactionsToAccounts(transactionData);

        // Display the transactions for each account
        for (Map.Entry<Long, List<Transaction>> entry : accountTransactionsMap.entrySet()) {
            System.out.println("Account Number: " + entry.getKey());
            for (Transaction transaction : entry.getValue()) {
                System.out.println(transaction);
            }
        }
    }
}
