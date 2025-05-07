package com.example.be9ik_wallet;

import java.io.Serializable;
import java.util.Random;

public class MerchantClass implements Serializable {
    private int id_merchant; // 4-digit ID
    private String businessName;
    private String businessDomain;
    private String businessLocation;
    private String businessDescription;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerUsername;
    private String ownerEmail;
    private String ownerCIN;
    private String ownerPassword;
    private boolean termsAccepted;
    private double balance; // Initial balance
    private String role; // New attribute

    // Default constructor required for Firebase
    public MerchantClass() {
        this.role = "merchant"; // Initialize role to "merchant"
    }

    // Constructor
    public MerchantClass(String businessName, String businessDomain, String businessLocation,
                         String businessDescription, String ownerFirstName, String ownerLastName,
                         String ownerUsername, String ownerEmail, String ownerCIN,
                         String ownerPassword, boolean termsAccepted) {
        this.id_merchant = generateId(); // Generate a 4-digit ID
        this.businessName = businessName;
        this.businessDomain = businessDomain;
        this.businessLocation = businessLocation;
        this.businessDescription = businessDescription;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.ownerUsername = ownerUsername;
        this.ownerEmail = ownerEmail;
        this.ownerCIN = ownerCIN;
        this.ownerPassword = ownerPassword;
        this.termsAccepted = termsAccepted;
        this.balance = 0.000; // Initial balance
        this.role = "merchant"; // Initialize role to "merchant"
    }

    // Generate a 4-digit ID
    private int generateId() {
        Random random = new Random();
        return 1000 + random.nextInt(9000); // Generate a number between 1000 and 9999
    }

    // Getters and Setters
    public int getId_merchant() {
        return id_merchant;
    }

    public void setId_merchant(int id_merchant) {
        this.id_merchant = id_merchant;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessDomain() {
        return businessDomain;
    }

    public void setBusinessDomain(String businessDomain) {
        this.businessDomain = businessDomain;
    }

    public String getBusinessLocation() {
        return businessLocation;
    }

    public void setBusinessLocation(String businessLocation) {
        this.businessLocation = businessLocation;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerCIN() {
        return ownerCIN;
    }

    public void setOwnerCIN(String ownerCIN) {
        this.ownerCIN = ownerCIN;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    // Getter and Setter for role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "MerchantClass{" +
                "id_merchant=" + id_merchant +
                ", businessName='" + businessName + '\'' +
                ", businessDomain='" + businessDomain + '\'' +
                ", businessLocation='" + businessLocation + '\'' +
                ", businessDescription='" + businessDescription + '\'' +
                ", ownerFirstName='" + ownerFirstName + '\'' +
                ", ownerLastName='" + ownerLastName + '\'' +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", ownerCIN='" + ownerCIN + '\'' +
                ", termsAccepted=" + termsAccepted +
                ", balance=" + balance +
                ", role='" + role + '\'' +
                '}';
    }
}
