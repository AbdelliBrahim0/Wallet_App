package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProfiterActivity extends AppCompatActivity {

    private TextView balanceAmount;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private MaterialCardView cardPayerFactures;
    private MaterialCardView cardRechargeMobile;
    private MaterialCardView cardAcheterVoucher;
    private MaterialCardView cardEnchereBeta;
    private MaterialCardView cardSitesPartenaires;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profiter);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        balanceAmount = findViewById(R.id.balance_amount);
        cardPayerFactures = findViewById(R.id.card_payer_factures);
        cardRechargeMobile = findViewById(R.id.card_recharge_mobile);
        cardAcheterVoucher = findViewById(R.id.card_acheter_voucher);
        cardEnchereBeta = findViewById(R.id.card_enchere_beta);
        cardSitesPartenaires = findViewById(R.id.card_sites_partenaires);

        // Set click listener for factures card
        cardPayerFactures.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfiterActivity.this, facture_choix.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ProfiterActivity.this, 
                    "Erreur lors de l'ouverture de la page des factures: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for recharge mobile card
        cardRechargeMobile.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfiterActivity.this, rechargeTelephonique.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ProfiterActivity.this, 
                    "Erreur lors de l'ouverture de la page de recharge: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for acheter voucher card
        cardAcheterVoucher.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfiterActivity.this, Voucher.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ProfiterActivity.this,
                    "Erreur lors de l'ouverture de la page voucher: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for enchere beta card
        cardEnchereBeta.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfiterActivity.this, ComingSoon.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ProfiterActivity.this,
                    "Erreur lors de l'ouverture de la page Coming Soon: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for sites partenaires card
        cardSitesPartenaires.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfiterActivity.this, partenaires.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ProfiterActivity.this,
                    "Erreur lors de l'ouverture de la page partenaires: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Load user data
        loadUserData();

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        mDatabase.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        UserClass user = snapshot.getValue(UserClass.class);
                        if (user != null) {
                            // Format and display the balance
                            String balanceText = String.format(Locale.getDefault(), "%.2f DT", user.getBalance());
                            balanceAmount.setText(balanceText);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ProfiterActivity.this, 
                        "Erreur lors du chargement du solde: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfiterActivity.this,
                    "Erreur de connexion à la base de données: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}