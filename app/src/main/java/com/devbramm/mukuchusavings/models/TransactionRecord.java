package com.devbramm.mukuchusavings.models;

public class TransactionRecord {
    private String userName;
    private String uid;
    private String transactid;
    private String transactionType;
    private String transactionDate;
    private String transactionTime;
    private Float transactionAmount;
    private String transactionDescription;

    public TransactionRecord() {
    }

    public TransactionRecord(String userName, String uid, String transactid, String transactionType, String transactionDate, String transactionTime, Float transactionAmount, String transactionDescription) {
        this.userName = userName;
        this.uid = uid;
        this.transactid = transactid;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
        this.transactionAmount = transactionAmount;
        this.transactionDescription = transactionDescription;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTransactid() {
        return transactid;
    }

    public void setTransactid(String transactid) {
        this.transactid = transactid;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Float getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Float transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }
}
