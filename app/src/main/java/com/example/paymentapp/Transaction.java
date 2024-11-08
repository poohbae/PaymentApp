package com.example.paymentapp;

import java.util.Date;

public class Transaction {
    public String transactionId;
    public int iconResId; // reload
    public String recipientImageUrl; // request and transfer
    public String senderImageUrl;  // request and transfer
    public String datetime;
    public String source;
    public String note;  // request and transfer
    public String refId;
    public int status;  // request
    public String mobileNumber;
    public String recipientId; // request and transfer
    public double amount;

    private Date parsedDate;

    // No-argument constructor required for Firebase
    public Transaction() {
    }

    // For reload and pay
    public Transaction(String transactionId, int iconResId, String datetime, String source, String refId, double amount) {
        this.transactionId = transactionId;
        this.iconResId = iconResId;
        this.datetime = datetime;
        this.source = source;
        this.refId = refId;
        this.status = 1;
        this.amount = amount;
    }

    // For transfer
    public Transaction(String transactionId, String recipientImageUrl, String senderImageUrl, String datetime, String source, String note, String refId, String mobileNumber, String recipientId, double amount) {
        this.transactionId = transactionId;
        this.iconResId = 0;
        this.recipientImageUrl = recipientImageUrl;
        this.senderImageUrl = senderImageUrl;
        this.datetime = datetime;
        this.source = source;
        this.note = note;
        this.refId = refId;
        this.status = 1;
        this.mobileNumber = mobileNumber;
        this.recipientId = recipientId;
        this.amount = amount;
    }

    // For request
    public Transaction(String transactionId, String recipientImageUrl, String senderImageUrl, String datetime, String source, String note, String refId, int status, String mobileNumber, String recipientId, double amount) {
        this.transactionId = transactionId;
        this.iconResId = 0;
        this.recipientImageUrl = recipientImageUrl;
        this.senderImageUrl = senderImageUrl;
        this.datetime = datetime;
        this.source = source;
        this.note = note;
        this.refId = refId;
        this.status = status;
        this.mobileNumber = mobileNumber;
        this.recipientId = recipientId;
        this.amount = amount;
    }


    // Getter and Setter for parsedDate
    public Date getParsedDate() {
        return parsedDate;
    }

    public void setParsedDate(Date parsedDate) {
        this.parsedDate = parsedDate;
    }
}

