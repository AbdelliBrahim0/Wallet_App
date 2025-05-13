package com.example.be9ik_wallet;

public class VoucherClass {
    private String id;
    private String type;
    private double amount;
    private String code;
    private long purchaseDate;
    private boolean isUsed;
    private String userId;

    public VoucherClass() {
        // Constructeur vide requis pour Firebase
    }

    public VoucherClass(String id, String type, double amount, String code, long purchaseDate, boolean isUsed, String userId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.code = code;
        this.purchaseDate = purchaseDate;
        this.isUsed = isUsed;
        this.userId = userId;
    }

    public VoucherClass(String type, double amount, String code, String userId) {
        this.type = type;
        this.amount = amount;
        this.code = code;
        this.purchaseDate = System.currentTimeMillis();
        this.isUsed = false;
        this.userId = userId;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public long getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
} 