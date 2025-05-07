package com.example.be9ik_wallet;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class envoyer_argent extends AppCompatActivity {
    private TextInputEditText editTextIdRecepteur, editTextMontant;
    private MaterialButton btnPoursuivre, btnScannerQR;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_envoyer_argent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialisation des vues
        initViews();

        // Configuration des listeners
        setupListeners();
    }

    private void initViews() {
        editTextIdRecepteur = findViewById(R.id.editTextIdRecepteur);
        editTextMontant = findViewById(R.id.editTextMontant);
        btnPoursuivre = findViewById(R.id.btnPoursuivre);
        btnScannerQR = findViewById(R.id.btnScannerQR);
    }

    private void setupListeners() {
        btnPoursuivre.setOnClickListener(v -> validateAndTransfer());
    }

    private void validateAndTransfer() {
        String recepteurId = editTextIdRecepteur.getText().toString().trim();
        String montantStr = editTextMontant.getText().toString().trim();

        if (recepteurId.isEmpty() || montantStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        float montant;
        try {
            montant = Float.parseFloat(montantStr);
            if (montant <= 0) {
                Toast.makeText(this, "Le montant doit être supérieur à 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier le solde de l'émetteur et demander le code de transaction
        verifyBalanceAndShowCodeDialog(recepteurId, montant);
    }

    private void verifyBalanceAndShowCodeDialog(String recepteurId, float montant) {
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot senderSnapshot) {
                UserClass sender = senderSnapshot.getValue(UserClass.class);
                if (sender != null && sender.getBalance() >= montant) {
                    // Afficher le dialogue du code de transaction
                    showTransactionCodeDialog(sender, recepteurId, montant);
                } else {
                    Toast.makeText(envoyer_argent.this, "Solde insuffisant", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(envoyer_argent.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTransactionCodeDialog(UserClass sender, String recepteurId, float montant) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_code_transaction);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText editTextCode = dialog.findViewById(R.id.editTextCodeTransaction);
        MaterialButton btnConfirmer = dialog.findViewById(R.id.btnConfirmer);
        MaterialButton btnAnnuler = dialog.findViewById(R.id.btnAnnuler);

        // Afficher le code de transaction attendu (à supprimer en production)
        Log.d("TransactionCode", "Code attendu: " + sender.getCodeTransaction());

        btnConfirmer.setOnClickListener(v -> {
            String codeEntre = editTextCode.getText().toString().trim();
            if (codeEntre.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer le code de transaction", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int code = Integer.parseInt(codeEntre);
                int expectedCode = sender.getCodeTransaction();
                
                // Logs de débogage
                Log.d("TransactionCode", "Code entré: " + code);
                Log.d("TransactionCode", "Code attendu: " + expectedCode);
                
                if (code == expectedCode) {
                    dialog.dismiss();
                    // Vérifier si le récepteur existe
                    verifyReceiverAndTransfer(sender, recepteurId, montant);
                } else {
                    Toast.makeText(this, "Code de transaction incorrect (" + code + " != " + expectedCode + ")", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Code invalide", Toast.LENGTH_SHORT).show();
            }
        });

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void verifyReceiverAndTransfer(UserClass sender, String recepteurId, float montant) {
        mDatabase.child("users").child(recepteurId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot receiverSnapshot) {
                if (receiverSnapshot.exists()) {
                    UserClass receiver = receiverSnapshot.getValue(UserClass.class);
                    if (receiver != null) {
                        // Effectuer le transfert
                        performTransfer(sender, receiver, montant);
                    }
                } else {
                    Toast.makeText(envoyer_argent.this, "ID récepteur invalide", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(envoyer_argent.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performTransfer(UserClass sender, UserClass receiver, float montant) {
        // Mise à jour des soldes
        float newSenderBalance = sender.getBalance() - montant;
        float newReceiverBalance = receiver.getBalance() + montant;

        // Créer un ID unique pour la transaction
        String transactionId = mDatabase.child("transactions").push().getKey();

        // Créer l'objet TransactionData
        TransactionData transactionData = new TransactionData(
            transactionId,
            sender.getId_utilisateur(),
            receiver.getId_utilisateur(),
            sender.getUsername(),
            receiver.getUsername(),
            montant,
            "SENT"
        );

        // Créer une copie de la transaction pour le récepteur
        TransactionData receiverTransactionData = new TransactionData(
            transactionId,
            sender.getId_utilisateur(),
            receiver.getId_utilisateur(),
            sender.getUsername(),
            receiver.getUsername(),
            montant,
            "RECEIVED"
        );

        // Créer un Map pour toutes les mises à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("/users/" + currentUserId + "/balance", newSenderBalance);
        updates.put("/users/" + receiver.getId_utilisateur() + "/balance", newReceiverBalance);
        updates.put("/users/" + currentUserId + "/transactions/" + transactionId, transactionData);
        updates.put("/users/" + receiver.getId_utilisateur() + "/transactions/" + transactionId, receiverTransactionData);

        // Effectuer toutes les mises à jour atomiquement
        mDatabase.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                // Afficher le ticket de transfert sur le thread principal
                runOnUiThread(() -> {
                    showTransferTicket(sender, receiver, montant);
                    Toast.makeText(envoyer_argent.this, "Transfert réussi!", Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                runOnUiThread(() -> {
                    Toast.makeText(envoyer_argent.this, 
                        "Erreur lors du transfert: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            });
    }

    private void showTransferTicket(UserClass sender, UserClass receiver, float montant) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_transfer_ticket);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Initialiser les vues du ticket
        TextView tvDate = dialog.findViewById(R.id.tvDate);
        TextView tvMontant = dialog.findViewById(R.id.tvMontant);
        TextView tvEmetteur = dialog.findViewById(R.id.tvEmetteur);
        TextView tvRecepteur = dialog.findViewById(R.id.tvRecepteur);
        MaterialButton btnFermer = dialog.findViewById(R.id.btnFermer);

        // Formater la date
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        // Remplir les informations
        tvDate.setText(currentDate);
        tvMontant.setText(String.format(Locale.getDefault(), "%.2f €", montant));
        tvEmetteur.setText(sender.getUsername());
        tvRecepteur.setText(receiver.getUsername());

        btnFermer.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Fermer l'activité et retourner à l'écran principal
        });

        dialog.show();
    }
}