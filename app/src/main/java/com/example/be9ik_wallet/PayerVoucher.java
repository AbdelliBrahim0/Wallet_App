package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.be9ik_wallet.CustomCaptureActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;

import android.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PayerVoucher extends AppCompatActivity {

    private String normalizedBusinessDomain;

    // Méthode pour générer un ticket de paiement détaillé
    private String generatePaymentTicket(String merchantName, String merchantLocation, String voucherType, Double amount) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());

        StringBuilder ticket = new StringBuilder();
        ticket.append("--- TICKET DE PAIEMENT ---\n\n")
              .append("Date et Heure: ").append(currentDateTime).append("\n")
              .append("Marchand: ").append(merchantName != null ? merchantName : "N/A").append("\n")
              .append("Adresse: ").append(merchantLocation != null ? merchantLocation : "N/A").append("\n")
              .append("Type de Voucher: ").append(voucherType).append("\n")
              .append("Montant Payé: ").append(String.format("%.2f €", amount)).append("\n\n")
              .append("Merci pour votre paiement!");

        return ticket.toString();
    }

    // Méthode pour afficher le ticket de paiement
    private void showPaymentTicket(String ticketDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation de Paiement")
               .setMessage(ticketDetails)
               .setPositiveButton("Fermer", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Méthode pour enregistrer la transaction dans l'historique
    private void saveTransactionToHistory(String merchantName, String merchantLocation, String voucherType, Double amount) {
        // Récupérer l'utilisateur actuel
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions");

        // Générer une clé unique pour la transaction
        String transactionId = transactionsRef.push().getKey();

        // Créer un objet transaction
        HashMap<String, Object> transactionData = new HashMap<>();
        transactionData.put("id", transactionId);
        transactionData.put("type", "SENT");
        transactionData.put("description", "Paiement par voucher chez " + merchantName);
        transactionData.put("amount", amount);
        transactionData.put("timestamp", System.currentTimeMillis());
        transactionData.put("merchantName", merchantName);
        transactionData.put("merchantLocation", merchantLocation);
        transactionData.put("voucherType", voucherType);

        // Enregistrer la transaction
        if (transactionId != null) {
            transactionsRef.child(transactionId).setValue(transactionData)
                .addOnSuccessListener(aVoid -> Log.d("PayerVoucher", "Transaction enregistrée avec succès"))
                .addOnFailureListener(e -> Log.e("PayerVoucher", "Erreur lors de l'enregistrement de la transaction", e));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payer_voucher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Récupérer l'ID du voucher depuis l'intent
        String voucherId = getIntent().getStringExtra("voucherId");

        if (voucherId != null) {
            fetchVoucherData(voucherId);
        } else {
            Log.e("PayerVoucher", "Aucun ID de voucher fourni.");
        }

        findViewById(R.id.btn_scan_qr).setOnClickListener(v -> {
            Intent intent = new Intent(PayerVoucher.this, CustomCaptureActivity.class);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 1);
        });

        findViewById(R.id.btn_pay).setOnClickListener(v -> {
            String enteredMerchantId = ((TextView) findViewById(R.id.et_merchant_id)).getText().toString();
            if (enteredMerchantId.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer l'ID du marchand.", Toast.LENGTH_SHORT).show();
                return;
            }

            String normalizedMerchantId = enteredMerchantId.trim();
            Log.d("PayerVoucher", "ID du marchand saisi (normalisé) : " + normalizedMerchantId);

            // Convert input to integer for matching
            try {
                int merchantIdInt = Integer.parseInt(normalizedMerchantId);

                // Log the full content of the 'merchants' node for debugging
                FirebaseDatabase.getInstance().getReference("merchants").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("PayerVoucher", "Contenu complet du nœud 'merchants' : " + dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PayerVoucher", "Erreur lors de la récupération du nœud 'merchants' : " + databaseError.getMessage());
                    }
                });

                // Existing query to find the merchant by id_merchant
                DatabaseReference merchantsRef = FirebaseDatabase.getInstance().getReference("merchants");
                merchantsRef.orderByChild("id_merchant").equalTo(merchantIdInt).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("PayerVoucher", "Requête Firebase exécutée pour id_merchant: " + merchantIdInt);
                        Log.d("PayerVoucher", "Nombre de marchands trouvés: " + dataSnapshot.getChildrenCount());

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Log.d("PayerVoucher", "Marchand récupéré: " + snapshot.getValue());
                        }

                        if (!dataSnapshot.exists()) {
                            Toast.makeText(PayerVoucher.this, "Marchand introuvable.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DataSnapshot merchantSnapshot = dataSnapshot.getChildren().iterator().next();
                        String businessDomain = merchantSnapshot.child("businessDomain").getValue(String.class);
                        Double merchantBalance = merchantSnapshot.child("balance").getValue(Double.class);

                        if (businessDomain == null || merchantBalance == null) {
                            Toast.makeText(PayerVoucher.this, "Données du marchand invalides.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DatabaseReference voucherRef = FirebaseDatabase.getInstance().getReference("vouchers").child(getIntent().getStringExtra("voucherId"));
                        voucherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot voucherSnapshot) {
                                if (!voucherSnapshot.exists()) {
                                    Toast.makeText(PayerVoucher.this, "Voucher introuvable.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String voucherType = voucherSnapshot.child("type").getValue(String.class);
                                Double voucherAmount = voucherSnapshot.child("amount").getValue(Double.class);

                                if (voucherType == null || voucherAmount == null) {
                                    Toast.makeText(PayerVoucher.this, "Données du voucher invalides.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Normalize voucher type and business domain
                                String normalizedVoucherType = voucherType.trim().toLowerCase();
                                String normalizedBusinessDomain = businessDomain.trim().toLowerCase();

                                // Specific matching rules for voucher types and merchant domains
                                boolean isValidVoucherForMerchant = 
                                    (normalizedVoucherType.equals("food") && normalizedBusinessDomain.equals("restaurant")) ||
                                    (normalizedVoucherType.equals("coffee") && normalizedBusinessDomain.equals("café")); 
                                if (isValidVoucherForMerchant) {
                                    merchantSnapshot.getRef().child("balance").setValue(merchantBalance + voucherAmount);
                                    voucherRef.removeValue();
                                    
                                    // Générer et afficher le ticket de paiement
                                    String merchantName = merchantSnapshot.child("businessName").getValue(String.class);
                                    String merchantLocation = merchantSnapshot.child("businessLocation").getValue(String.class);
                                    String ticketDetails = generatePaymentTicket(
                                        merchantName, 
                                        merchantLocation, 
                                        voucherType, 
                                        voucherAmount
                                    );
                                    showPaymentTicket(ticketDetails);
                                    
                                    // Enregistrer la transaction dans l'historique
                                    saveTransactionToHistory(
                                        merchantName, 
                                        merchantLocation, 
                                        voucherType, 
                                        voucherAmount
                                    );
                                    
                                    Toast.makeText(PayerVoucher.this, "Paiement effectué avec succès.", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(PayerVoucher.this, "Le type de voucher ne correspond pas au domaine du marchand.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(PayerVoucher.this, "Erreur lors de la récupération du voucher.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PayerVoucher", "Erreur Firebase: " + databaseError.getMessage());
                        Toast.makeText(PayerVoucher.this, "Erreur lors de la récupération des marchands.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Log.e("PayerVoucher", "ID du marchand invalide : " + normalizedMerchantId);
                Toast.makeText(PayerVoucher.this, "ID du marchand invalide.", Toast.LENGTH_SHORT).show();
            }
        }); // Fin de la méthode onClickListener
    }

    private void fetchVoucherData(String voucherId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("vouchers").child(voucherId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String voucherType = dataSnapshot.child("type").getValue(String.class);
                    String voucherCode = dataSnapshot.child("code").getValue(String.class);
                    Double voucherAmount = dataSnapshot.child("amount").getValue(Double.class);

                    // Mettre à jour l'interface utilisateur
                    TextView typeTextView = findViewById(R.id.voucher_type);
                    TextView codeTextView = findViewById(R.id.voucher_code);
                    TextView amountTextView = findViewById(R.id.voucher_amount);

                    typeTextView.setText("Type: " + voucherType);
                    codeTextView.setText("Code: " + voucherCode);
                    amountTextView.setText(String.format("Montant: %.2f DT", voucherAmount));
                } else {
                    Log.e("PayerVoucher", "Le voucher avec l'ID " + voucherId + " n'existe pas.");
                }
            } // Fin de la méthode onDataChange

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("PayerVoucher", "Erreur lors de la récupération des données: " + databaseError.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String scannedData = data.getStringExtra("SCAN_RESULT");
            TextView merchantIdField = findViewById(R.id.et_merchant_id);
            merchantIdField.setText(scannedData);
        }
    } // Fin de la méthode onActivityResult
} // Fin de la classe PayerVoucher