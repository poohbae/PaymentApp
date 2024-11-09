package com.example.paymentapp;

import java.util.Date;

public class Transaction {
    public String transactionId;
    public int iconResId; // reload and pay
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

    // Parsed date for internal processing, not stored in Firebase
    private Date parsedDate;

    // No-argument constructor required for Firebase
    public Transaction() {
    }

    // Constructor for "reload" and "pay" transactions
    public Transaction(String transactionId, int iconResId, String datetime, String source, String refId, double amount) {
        this.transactionId = transactionId;
        this.iconResId = iconResId;
        this.datetime = datetime;
        this.source = source;
        this.refId = refId;
        this.status = 1; // Default status for completed transactions
        this.amount = amount;
    }

    // Constructor for "transfer" transactions
    public Transaction(String transactionId, String recipientImageUrl, String senderImageUrl, String datetime, String source, String note, String refId, String mobileNumber, String recipientId, double amount) {
        this.transactionId = transactionId;
        this.iconResId = 0; // No icon for "transfer" transactions
        this.recipientImageUrl = recipientImageUrl;
        this.senderImageUrl = senderImageUrl;
        this.datetime = datetime;
        this.source = source;
        this.note = note;
        this.refId = refId;
        this.status = 1; // Default status for completed transactions
        this.mobileNumber = mobileNumber;
        this.recipientId = recipientId;
        this.amount = amount;
    }

    // Constructor for "request" transactions
    public Transaction(String transactionId, String recipientImageUrl, String senderImageUrl, String datetime, String source, String note, String refId, int status, String mobileNumber, String recipientId, double amount) {
        this.transactionId = transactionId;
        this.iconResId = 0; // No icon for "request" transactions
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

    // Getter and Setter for parsedDate, used for internal date processing
    public Date getParsedDate() {
        return parsedDate;
    }

    public void setParsedDate(Date parsedDate) {
        this.parsedDate = parsedDate;
    }
}
