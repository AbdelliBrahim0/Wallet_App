package com.example.be9ik_wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserClass {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String birthDate;
    private String phoneNumber;
    private String password;
    private String gender;
    private boolean termsAccepted;
    private int codeTransaction;
    private List<String> voucherTab;
    private float balance;

    // Constructor vide
    public UserClass() {
        this.codeTransaction = generateTransactionCode();
        this.voucherTab = new ArrayList<>();
        this.balance = 0.0f;
    }

    // Constructor pour le formulaire
    public UserClass(String firstName, String lastName, String username, String email,
                String birthDate, String phoneNumber, String password, String gender,
                boolean termsAccepted) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.gender = gender;
        this.termsAccepted = termsAccepted;
        this.codeTransaction = generateTransactionCode();
        this.voucherTab = new ArrayList<>();
        this.balance = 0.0f;
    }

    // Méthode pour générer un code de transaction aléatoire à 4 chiffres
    private int generateTransactionCode() {
        Random random = new Random();
        return 1000 + random.nextInt(9000); // Génère un nombre entre 1000 et 9999
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public int getCodeTransaction() {
        return codeTransaction;
    }

    public void setCodeTransaction(int codeTransaction) {
        this.codeTransaction = codeTransaction;
    }

    public List<String> getVoucherTab() {
        return voucherTab;
    }

    public void setVoucherTab(List<String> voucherTab) {
        this.voucherTab = voucherTab != null ? voucherTab : new ArrayList<>();
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", termsAccepted=" + termsAccepted +
                ", codeTransaction=" + codeTransaction +
                ", voucherTab=" + voucherTab +
                ", balance=" + balance +
                '}';
    }
}

















