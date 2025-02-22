package com.dvtech.maker.model;

public class Transaction {
    private String frAba;
    private String unitBankDDA;
    private String unit;
    private String unitName;
    private double amount;

    public String getFrAba() {
        return frAba;
    }

    public String setFrAba(String frAba) {
       return  frAba;
    }

    public String getUnitBankDDA() {
        return unitBankDDA;
    }

    public String setUnitBankDDA(String unitBankDDA) {
        return  unitBankDDA;
    }

    public String getUnit() {
        return unit;
    }

    public String setUnit(String unit) {
        return unit;
    }

    public String getUnitName() {
        return unitName;
    }

    public String setUnitName(String unitName) {
        return unitName;
    }

    public double getAmount() {
        return amount;
    }

    public double setAmount(double amount) {
        return amount;
    }
    public Transaction()
    {

    }
    public Transaction(String frAba, String unitBankDDA, String unit, String unitName, double amount) {
        this.frAba = frAba;
        this.unitBankDDA = unitBankDDA;
        this.unit = unit;
        this.unitName = unitName;
        this.amount = amount;
    }
}
