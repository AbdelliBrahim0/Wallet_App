package com.example.be9ik_wallet;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class AcheterVoucher extends AppCompatActivity {
    private static final String TAG = "AcheterVoucher";
    private RadioGroup typeGroup;
    private RadioGroup amountGroup;
    private TextView summaryType;
    private TextView summaryAmount;
    private TextView totalAmount;
    private MaterialButton confirmButton;
    private String selectedType = "";
    private double selectedAmount = 0.0;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_acheter_voucher);

            // Initialiser Firebase
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();

            // Initialiser les vues
            initializeViews();
            setupListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Erreur lors de l'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Configurer la toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Acheter un Voucher");
                }
            }

            // Initialiser les vues
            typeGroup = findViewById(R.id.type_group);
            amountGroup = findViewById(R.id.amount_group);
            summaryType = findViewById(R.id.summary_type);
            summaryAmount = findViewById(R.id.summary_amount);
            totalAmount = findViewById(R.id.summary_total);
            confirmButton = findViewById(R.id.confirm_purchase_button);

            // Vérifier que toutes les vues sont initialisées
            if (typeGroup == null || amountGroup == null || summaryType == null || 
                summaryAmount == null || totalAmount == null || confirmButton == null) {
                throw new IllegalStateException("Une ou plusieurs vues n'ont pas été trouvées");
            }

            // Initialiser les valeurs par défaut
            summaryType.setText("Non sélectionné");
            summaryAmount.setText("0.00 DT");
            totalAmount.setText("0.00 DT");
            confirmButton.setEnabled(false);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupListeners() {
        try {
            typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
                try {
                    RadioButton selectedButton = findViewById(checkedId);
                    if (selectedButton != null) {
                        selectedType = selectedButton.getText().toString();
                        updateSummary();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in type selection", e);
                    Toast.makeText(this, "Erreur lors de la sélection du type", Toast.LENGTH_SHORT).show();
                }
            });

            amountGroup.setOnCheckedChangeListener((group, checkedId) -> {
                try {
                    RadioButton selectedButton = findViewById(checkedId);
                    if (selectedButton != null) {
                        String amountText = selectedButton.getText().toString().replace(" DT", "");
                        selectedAmount = Double.parseDouble(amountText);
                        updateSummary();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in amount selection", e);
                    Toast.makeText(this, "Erreur lors de la sélection du montant", Toast.LENGTH_SHORT).show();
                }
            });

            confirmButton.setOnClickListener(v -> {
                try {
                    processPurchase();
                } catch (Exception e) {
                    Log.e(TAG, "Error in purchase process", e);
                    Toast.makeText(this, "Erreur lors du traitement de l'achat", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
            throw e;
        }
    }

    private void updateSummary() {
        try {
            if (!selectedType.isEmpty() && selectedAmount > 0) {
                summaryType.setText(selectedType);
                summaryAmount.setText(String.format("%.2f DT", selectedAmount));
                totalAmount.setText(String.format("%.2f DT", selectedAmount));
                confirmButton.setEnabled(true);
            } else {
                confirmButton.setEnabled(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating summary", e);
            Toast.makeText(this, "Erreur lors de la mise à jour du résumé", Toast.LENGTH_SHORT).show();
        }
    }

    private void processPurchase() {
        try {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vous devez être connecté pour effectuer un achat", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            // Vérifier le solde de l'utilisateur
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            Double currentBalance = dataSnapshot.child("balance").getValue(Double.class);
                            if (currentBalance == null) {
                                Toast.makeText(AcheterVoucher.this, "Erreur: Solde non disponible", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (currentBalance >= selectedAmount) {
                                // Générer un code unique à 4 chiffres
                                String voucherCode = generateVoucherCode();
                                // Créer le voucher
                                String voucherId = mDatabase.child("vouchers").push().getKey();
                                if (voucherId == null) {
                                    Toast.makeText(AcheterVoucher.this, "Erreur lors de la création du voucher", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // Lier le voucher à l'utilisateur
                                VoucherClass voucher = new VoucherClass(
                                    voucherId,
                                    selectedType,
                                    selectedAmount,
                                    voucherCode,
                                    System.currentTimeMillis(),
                                    false,
                                    userId // <-- userId bien renseigné ici
                                );
                                // Mettre à jour le solde et sauvegarder le voucher
                                mDatabase.child("users").child(userId).child("balance")
                                    .setValue(currentBalance - selectedAmount)
                                    .addOnSuccessListener(aVoid -> {
                                        mDatabase.child("vouchers").child(voucherId).setValue(voucher)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(AcheterVoucher.this, 
                                                    "Voucher acheté avec succès! Code: " + voucherCode, 
                                                    Toast.LENGTH_LONG).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error saving voucher", e);
                                                Toast.makeText(AcheterVoucher.this,
                                                    "Erreur lors de l'achat du voucher",
                                                    Toast.LENGTH_SHORT).show();
                                            });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating balance", e);
                                        Toast.makeText(AcheterVoucher.this,
                                            "Erreur lors de la mise à jour du solde",
                                            Toast.LENGTH_SHORT).show();
                                    });
                            } else {
                                Toast.makeText(AcheterVoucher.this,
                                    "Solde insuffisant",
                                    Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AcheterVoucher.this,
                                "Erreur: Profil utilisateur non trouvé",
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onDataChange", e);
                        Toast.makeText(AcheterVoucher.this,
                            "Erreur lors du traitement de l'achat",
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error checking balance", databaseError.toException());
                    Toast.makeText(AcheterVoucher.this,
                        "Erreur lors de la vérification du solde",
                        Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in processPurchase", e);
            Toast.makeText(this, "Erreur lors du traitement de l'achat", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateVoucherCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // Génère un nombre entre 1000 et 9999
        return String.format("%04d", code);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}