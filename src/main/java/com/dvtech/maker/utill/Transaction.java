package com.dvtech.maker.utill;

public class Transaction {
    private int sequenceNumber;
    private String frAba;
    private String unitBankDda;

    public Transaction(int sequenceNumber, String frAba, String unitBankDda) {
        this.sequenceNumber = sequenceNumber;
        this.frAba = frAba;
        this.unitBankDda = unitBankDda;
    }

    // Getters and setters
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFrAba() {
        return frAba;
    }

    public void setFrAba(String frAba) {
        this.frAba = frAba;
    }

    public String getUnitBankDda() {
        return unitBankDda;
    }

    public void setUnitBankDda(String unitBankDda) {
        this.unitBankDda = unitBankDda;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "sequenceNumber=" + sequenceNumber +
                ", frAba='" + frAba + '\'' +
                ", unitBankDda='" + unitBankDda + '\'' +
                '}';
    }
}
