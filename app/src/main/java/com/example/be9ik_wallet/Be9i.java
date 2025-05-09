package com.example.be9ik_wallet;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Be9i extends AppCompatActivity {
    private MaterialButton btnBack, btnValidate;
    private TextInputEditText editTextSellerId, editTextAmount;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_be9i);
        
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        btnValidate = findViewById(R.id.buttonValidate);
        editTextSellerId = findViewById(R.id.editTextSellerId);
        editTextAmount = findViewById(R.id.editTextAmount);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnValidate.setOnClickListener(v -> validatePayment());
    }

    private void validatePayment() {
        String merchantId = editTextSellerId.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();

        if (merchantId.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify merchant exists and has sufficient balance
        verifyMerchant(merchantId, amount);
    }

    private void verifyMerchant(String merchantId, double amount) {
        mDatabase.child("merchants").child(merchantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(Be9i.this, "Marchand non trouvé", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Récupérer le CIN du marchand
                String ownerCIN = snapshot.child("ownerCIN").getValue(String.class);
                if (ownerCIN == null || ownerCIN.isEmpty()) {
                    Toast.makeText(Be9i.this, "Erreur: CIN du marchand non défini", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vérifier le solde du marchand
                Double merchantBalance = snapshot.child("balance").getValue(Double.class);
                if (merchantBalance == null || merchantBalance < amount) {
                    Toast.makeText(Be9i.this, "Le marchand n'a pas assez de solde", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show CIN verification popup
                showCINVerificationPopup(merchantId, amount, ownerCIN);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Be9i.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCINVerificationPopup(String merchantId, double amount, String correctCIN) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_popup_code_vendeur);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText editTextTransactionCode = dialog.findViewById(R.id.editTextTransactionCode);
        MaterialButton btnConfirm = dialog.findViewById(R.id.buttonConfirm);
        MaterialButton btnCancel = dialog.findViewById(R.id.buttonCancel);

        btnConfirm.setOnClickListener(v -> {
            String enteredCIN = editTextTransactionCode.getText().toString().trim();
            if (enteredCIN.equals(correctCIN)) {
                processTransaction(merchantId, amount);
                dialog.dismiss();
            } else {
                Toast.makeText(Be9i.this, "Code CIN incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void processTransaction(String merchantId, double amount) {
        String transactionId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();

        // Update merchant balance in merchants collection
        updates.put("/merchants/" + merchantId + "/balance", ServerValue.increment(-amount));

        // Update current user balance in users collection
        updates.put("/users/" + currentUserId + "/balance", ServerValue.increment(amount));

        // Create transaction record for merchant
        TransactionData merchantTransaction = new TransactionData(
            transactionId,
            "DEBIT",
            amount,
            currentUserId,
            "Paiement à " + currentUserId,
            timestamp
        );
        updates.put("/merchants/" + merchantId + "/transactions/" + transactionId, merchantTransaction);

        // Create transaction record for current user
        TransactionData userTransaction = new TransactionData(
            transactionId,
            "CREDIT",
            amount,
            merchantId,
            "Reçu du marchand " + merchantId,
            timestamp
        );
        updates.put("/users/" + currentUserId + "/transactions/" + transactionId, userTransaction);

        mDatabase.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(Be9i.this, "Transaction réussie", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(Be9i.this, "Erreur lors de la transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}