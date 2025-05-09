package com.example.be9ik_wallet;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionData {
    private String transactionId;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String receiverName;
    private double amount;
    private String type;  // "SENT", "RECEIVED", "CREDIT", "DEBIT"
    private long timestamp;
    private String description;
    private String otherPartyId;

    // Required empty constructor for Firebase
    public TransactionData() {}

    // Constructor for new merchant payment system
    public TransactionData(String transactionId, String type, double amount, String otherPartyId, String description, long timestamp) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.otherPartyId = otherPartyId;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Constructor for existing send money system
    public TransactionData(String transactionId, String senderId, String receiverId, 
                         String senderName, String receiverName, float amount, String type) {
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.amount = amount;
        this.type = type;
        this.timestamp = new Date().getTime();
        
        // Set description based on type
        if ("SENT".equals(type)) {
            this.description = "Envoyé à " + receiverName;
            this.otherPartyId = receiverId;
        } else {
            this.description = "Reçu de " + senderName;
            this.otherPartyId = senderId;
        }
    }

    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOtherPartyId() {
        return otherPartyId;
    }

    public void setOtherPartyId(String otherPartyId) {
        this.otherPartyId = otherPartyId;
    }

    public String getDescription() {
        if (description != null) {
            return description;
        }
        // Fallback for old transactions
        return "SENT".equals(type) ? "Envoyé à " + receiverName : "Reçu de " + senderName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 