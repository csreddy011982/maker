package com.dvtech.maker.model;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransactionAd {

    private int sequenceNumber;
    private Long accountNumber;
    private String individualId;
    private String name;
    private String tc;
    private String creditAmount;
    private String debitAmount;
    private String itemCount;
    private String abaNumber;
    private String fileReferenceNumber;

    public TransactionAd(int sequenceNumber, Long accountNumber, String individualId, String name, String tc,
                         String creditAmount, String debitAmount, String itemCount, String abaNumber,
                         String fileReferenceNumber, String s) {
        this.sequenceNumber = sequenceNumber;
        this.accountNumber = accountNumber;
        this.individualId = individualId;
        this.name = name;
        this.tc = tc;
        this.creditAmount = creditAmount;
        this.debitAmount = debitAmount;
        this.itemCount = itemCount;
        this.abaNumber = abaNumber;
        this.fileReferenceNumber = fileReferenceNumber;
    }


    // Getters for FreeMarker template
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public String getIndividualId() {
        return individualId;
    }

    public String getName() {
        return name;
    }

    public String getTc() {
        return tc;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public String getDebitAmount() {
        return debitAmount;
    }

    public String getItemCount() {
        return itemCount;
    }

    public String getAbaNumber() {
        return abaNumber;
    }

    public String getFileReferenceNumber() {
        return fileReferenceNumber;
    }
}


