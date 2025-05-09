package com.example.be9ik_wallet;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CodeCadeau extends AppCompatActivity {
    private TextInputEditText editTextCodeCadeau;
    private MaterialButton buttonSubmitCodeCadeau, btnBack;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_code_cadeau);
        
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        editTextCodeCadeau = findViewById(R.id.editTextCodeCadeau);
        buttonSubmitCodeCadeau = findViewById(R.id.buttonSubmitCodeCadeau);
        btnBack = findViewById(R.id.btn_back);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set click listeners
        buttonSubmitCodeCadeau.setOnClickListener(v -> validateGiftCode());
        btnBack.setOnClickListener(v -> finish());
    }

    private void validateGiftCode() {
        String code = editTextCodeCadeau.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un code cadeau", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query to find the gift code
        Query query = mDatabase.child("gift_codes").orderByChild("code").equalTo(code);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the first matching gift code
                    DataSnapshot giftCodeSnapshot = dataSnapshot.getChildren().iterator().next();
                    String giftCodeId = giftCodeSnapshot.getKey();
                    boolean isUsed = giftCodeSnapshot.child("isUsed").getValue(Boolean.class);
                    
                    if (!isUsed) {
                        // Get the gift amount
                        double amount = giftCodeSnapshot.child("montant").getValue(Double.class);
                        
                        // Mark code as used and update user balance
                        mDatabase.child("gift_codes").child(giftCodeId).child("isUsed").setValue(true)
                            .addOnSuccessListener(aVoid -> updateUserBalance(amount))
                            .addOnFailureListener(e -> Toast.makeText(CodeCadeau.this, 
                                "Erreur lors de la mise à jour du code", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(CodeCadeau.this, "Ce code a déjà été utilisé", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CodeCadeau.this, "Code cadeau invalide", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CodeCadeau.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserBalance(double giftAmount) {
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double currentBalance = snapshot.child("balance").getValue(Double.class);
                    double newBalance = currentBalance + giftAmount;

                    // Update the balance
                    mDatabase.child("users").child(currentUserId).child("balance").setValue(newBalance)
                        .addOnSuccessListener(aVoid -> showSuccessPopup(giftAmount))
                        .addOnFailureListener(e -> Toast.makeText(CodeCadeau.this, 
                            "Erreur lors de la mise à jour du solde", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CodeCadeau.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessPopup(double amount) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_success);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set success message
        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        // Show success message
        Toast.makeText(this, "Félicitations! Vous avez reçu +" + amount + "€", Toast.LENGTH_LONG).show();
        dialog.show();
    }
}