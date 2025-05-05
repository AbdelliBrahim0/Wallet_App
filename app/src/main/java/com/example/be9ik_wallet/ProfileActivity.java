package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvBirthDate, tvUsername, tvUserId, tvReceptionCode, tvBalance;
    private TextView tvJoinedDate;
    private Chip chipVerified; // Utilisation directe de Chip au lieu de com.google.android.material.chip.Chip
    private Button btnAddFunds, btnTransfer;
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

        btnAddFunds.setOnClickListener(v -> {
            Toast.makeText(this, "Ajouter des fonds", Toast.LENGTH_SHORT).show();
        });

        btnTransfer.setOnClickListener(v -> {
            Toast.makeText(this, "Transférer de l'argent", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchUserData() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phoneNumber = snapshot.hasChild("phoneNumber") ? String.valueOf(snapshot.child("phoneNumber").getValue()) : null;
                    String birthDate = snapshot.child("birthDate").getValue(String.class);
                    String receptionCode = snapshot.hasChild("codeTransaction") ? String.valueOf(snapshot.child("codeTransaction").getValue()) : null;
                    Double balance = snapshot.child("balance").getValue(Double.class);
                    String dateSignUp = snapshot.child("dateSignUp").getValue(String.class);
                    Boolean verified = snapshot.child("verified").getValue(Boolean.class);

                    displayUserData(
                            name != null && lastName != null ? name + " " + lastName : "N/A",
                            email != null ? email : "N/A",
                            phoneNumber != null ? phoneNumber : "N/A",
                            birthDate != null ? birthDate : "N/A",
                            id != null ? id : "N/A",
                            receptionCode != null ? receptionCode : "N/A",
                            balance != null ? balance.floatValue() : 0f,
                            username != null ? username : "N/A",
                            dateSignUp != null ? dateSignUp : "N/A",
                            verified != null && verified
                    );
                } else {
                    Toast.makeText(ProfileActivity.this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
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
                                 String dateSignUp, boolean verified) {
        tvName.setText(fullName);
        tvEmail.setText(email);
        tvPhone.setText(phone);
        tvBirthDate.setText(birthDate);
        tvUserId.setText(userId);
        tvReceptionCode.setText(receptionCode);
        tvBalance.setText(String.format("%.3f DT", balance));
        tvUsername.setText(username);
        tvJoinedDate.setText("Membre depuis " + dateSignUp);

        // Mise à jour du Chip
        chipVerified.setText(verified ? "VERIFIED" : "NON VERIFIED");
        chipVerified.setChipBackgroundColorResource(verified ? R.color.verified_chip_background : R.color.unverified_chip_background);

        // Correction du OnClickListener
        chipVerified.setOnClickListener(v -> {
            if (verified) {
                // Si vérifié, aller vers VerificationDone
                startActivity(new Intent(ProfileActivity.this, VerificationDone.class));
            } else {
                // Si non vérifié, aller vers VerificationActivity
                startActivity(new Intent(ProfileActivity.this, VerificationActivity.class));
                finish(); // Optionnel : ferme ProfileActivity si nécessaire
            }
        });

        // Mise à jour du nom d'utilisateur dans le header si nécessaire
        TextView tvProfileName = findViewById(R.id.tv_profile_name);
        if (tvProfileName != null) {
            tvProfileName.setText(username);
        }
    }
}