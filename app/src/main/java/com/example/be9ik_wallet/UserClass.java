package com.example.be9ik_wallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class UserClass {
    private String id_utilisateur;
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
    private int betaCoin; // Nouveau champ
    private boolean verified; // Nouveau champ
    private String dateSignUp; // Nouveau champ
    private String role; // Nouveau champ

    // Constructor vide
    public UserClass() {
        this.codeTransaction = generateTransactionCode();
        this.voucherTab = new ArrayList<>();
        this.balance = 0.0f;
        this.betaCoin = 0; // Initialisé à 0
        this.verified = false; // Initialisé à false
        this.dateSignUp = getCurrentDate(); // Date actuelle
        this.role = "user"; // Initialisé à "user"
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
        this.betaCoin = 0; // Initialisé à 0
        this.verified = false; // Initialisé à false
        this.dateSignUp = getCurrentDate(); // Date actuelle
        this.role = "user"; // Initialisé à "user"
    }

    // Méthode pour générer un code de transaction aléatoire à 4 chiffres
    private int generateTransactionCode() {
        Random random = new Random();
        return 1000 + random.nextInt(9000); // Génère un nombre entre 1000 et 9999
    }

    // Méthode pour obtenir la date actuelle
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Getters et Setters pour les nouveaux champs
    public int getBetaCoin() {
        return betaCoin;
    }

    public void setBetaCoin(int betaCoin) {
        this.betaCoin = betaCoin;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getDateSignUp() {
        return dateSignUp;
    }

    public void setDateSignUp(String dateSignUp) {
        this.dateSignUp = dateSignUp;
    }

    // Getters et Setters pour le nouveau champ
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getters et Setters
    public String getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(String id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
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
                "id_utilisateur='" + id_utilisateur + '\'' +
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
                ", role='" + role + '\'' +
                '}';
    }
}

















