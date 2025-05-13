package com.example.be9ik_wallet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvBirthDate, tvUsername, tvUserId, tvReceptionCode, tvBalance;
    private TextView tvJoinedDate;
    private Chip chipVerified; // Utilisation directe de Chip au lieu de com.google.android.material.chip.Chip
    private Button btnAddFunds, btnTransfer, btnDeleteAccount;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            fetchUserData();
        } else {
            Toast.makeText(this, "Utilisateur non authentifié", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvBirthDate = findViewById(R.id.tv_birth_date);
        tvUsername = findViewById(R.id.tv_username);
        tvUserId = findViewById(R.id.tv_user_id);
        tvReceptionCode = findViewById(R.id.tv_reception_code);
        tvBalance = findViewById(R.id.tv_balance);
        tvJoinedDate = findViewById(R.id.tv_profile_joined);
        chipVerified = findViewById(R.id.chip_verified); // Initialisation correcte du Chip
        btnAddFunds = findViewById(R.id.btn_add_funds);
        btnTransfer = findViewById(R.id.btn_transfer);
        progressBar = findViewById(R.id.progress_bar);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        btnAddFunds.setOnClickListener(v -> {
            Toast.makeText(this, "Ajouter des fonds", Toast.LENGTH_SHORT).show();
        });

        btnTransfer.setOnClickListener(v -> {
            Toast.makeText(this, "Transférer de l'argent", Toast.LENGTH_SHORT).show();
        });

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> deleteUserAccount());
        }
    }

    private void fetchUserData() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                try {
                    if (snapshot.exists()) {
                        String id = snapshot.child("id_utilisateur").getValue(String.class);
                        String name = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                        String birthDate = snapshot.child("birthDate").getValue(String.class);
                        
                        // Gérer le code de transaction qui peut être Long ou String
                        String receptionCode = null;
                        Object codeTransaction = snapshot.child("codeTransaction").getValue();
                        if (codeTransaction != null) {
                            receptionCode = codeTransaction.toString();
                        }

                        // Gérer le solde qui peut être Long ou Double
                        float balance = 0f;
                        Object balanceObj = snapshot.child("balance").getValue();
                        if (balanceObj != null) {
                            if (balanceObj instanceof Long) {
                                balance = ((Long) balanceObj).floatValue();
                            } else if (balanceObj instanceof Double) {
                                balance = ((Double) balanceObj).floatValue();
                            }
                        }

                        String dateSignUp = snapshot.child("dateSignUp").getValue(String.class);
                        Boolean verified = snapshot.child("verified").getValue(Boolean.class);

                        displayUserData(
                                name != null && lastName != null ? name + " " + lastName : "N/A",
                                email != null ? email : "N/A",
                                phoneNumber != null ? phoneNumber : "N/A",
                                birthDate != null ? birthDate : "N/A",
                                id != null ? id : "N/A",
                                receptionCode != null ? receptionCode : "N/A",
                                balance,
                                username != null ? username : "N/A",
                                dateSignUp != null ? dateSignUp : "N/A",
                                verified != null && verified,
                                null
                        );
                    } else {
                        Toast.makeText(ProfileActivity.this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Erreur inattendue: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayUserData(String fullName, String email, String phone,
                                 String birthDate, String userId,
                                 String receptionCode, float balance, String username,
                                 String dateSignUp, boolean verified, String qrCode) {
        try {
            if (tvName != null) tvName.setText(fullName);
            if (tvEmail != null) tvEmail.setText(email);
            if (tvPhone != null) tvPhone.setText(phone);
            if (tvBirthDate != null) tvBirthDate.setText(birthDate);
            if (tvUserId != null) tvUserId.setText(userId);
            if (tvReceptionCode != null) tvReceptionCode.setText(receptionCode);
            if (tvBalance != null) tvBalance.setText(String.format("%.3f DT", balance));
            if (tvUsername != null) tvUsername.setText(username);
            if (tvJoinedDate != null) tvJoinedDate.setText("Membre depuis " + dateSignUp);

            if (chipVerified != null) {
                chipVerified.setText(verified ? "VERIFIED" : "NON VERIFIED");
                chipVerified.setChipBackgroundColorResource(verified ? R.color.verified_chip_background : R.color.unverified_chip_background);
                chipVerified.setOnClickListener(v -> {
                    if (verified) {
                        startActivity(new Intent(ProfileActivity.this, VerificationDone.class));
                    } else {
                        startActivity(new Intent(ProfileActivity.this, VerificationActivity.class));
                        finish();
                    }
                });
            }

            TextView tvProfileName = findViewById(R.id.tv_profile_name);
            if (tvProfileName != null) {
                tvProfileName.setText(username);
            }

            // Générer et afficher le QR code
            ImageView ivQrCode = findViewById(R.id.iv_qr_code);
            if (ivQrCode != null) {
                try {
                    // Utiliser l'ID utilisateur pour le QR code
                    String qrContent = userId;
                    Log.d("QRCode", "Génération du QR code avec l'ID: " + qrContent);
                    
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    BitMatrix bitMatrix = multiFormatWriter.encode(qrContent, BarcodeFormat.QR_CODE, 500, 500);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    ivQrCode.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                    Log.e("QRCode", "Erreur lors de la génération du QR code: " + e.getMessage());
                    Toast.makeText(this, "Erreur lors de la génération du QR code", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(ProfileActivity.this, "Erreur d'affichage: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Supprimer d'abord les données de la base de données
            databaseReference.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Ensuite supprimer le compte d'authentification
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this,
                                                "Compte supprimé avec succès",
                                                Toast.LENGTH_SHORT).show();
                                        // Rediriger vers l'écran de connexion
                                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ProfileActivity.this,
                                                "Erreur lors de la suppression du compte: " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this,
                                "Erreur lors de la suppression des données: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}