package com.example.be9ik_wallet;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Be9i extends AppCompatActivity {
    private MaterialButton btnBack, btnValidate, btnScanQrCode;
    private TextInputEditText editTextSellerId, editTextAmount;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQRScanner();
                } else {
                    Toast.makeText(this, "Permission de la caméra requise pour scanner le QR code", Toast.LENGTH_SHORT).show();
                }
            });

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
        btnScanQrCode = findViewById(R.id.buttonScanQrCode);
        editTextSellerId = findViewById(R.id.editTextSellerId);
        editTextAmount = findViewById(R.id.editTextAmount);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnValidate.setOnClickListener(v -> validatePayment());
        btnScanQrCode.setOnClickListener(v -> checkCameraPermissionAndScan());
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startQRScanner();
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scannez le QR code du marchand");
        integrator.setCameraId(0);  // Use back camera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true); // Lock orientation
        integrator.setCaptureActivity(QRScannerActivity.class); // Use custom capture activity
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedContent = result.getContents();
                Log.d("QRScan", "Contenu scanné brut: " + scannedContent);
                
                // Vérifier si le contenu scanné est un ID marchand valide
                if (scannedContent.matches("[a-zA-Z0-9_-]+")) {
                    Log.d("QRScan", "Format ID valide détecté");
                    verifyAndSetMerchantId(scannedContent);
                } else {
                    Log.e("QRScan", "Format ID invalide: " + scannedContent);
                    Toast.makeText(this, "QR code invalide", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void verifyAndSetMerchantId(String merchantId) {
        Log.d("QRScan", "Vérification de l'ID marchand: " + merchantId);
        
        // Rechercher le marchand par son ID dans la collection merchants
        mDatabase.child("merchants").orderByChild("id_merchant").equalTo(Integer.parseInt(merchantId))
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d("QRScan", "Réponse de la base de données - Existe: " + snapshot.exists());
                    if (snapshot.exists()) {
                        // Récupérer le premier (et unique) marchand trouvé
                        DataSnapshot merchantSnapshot = snapshot.getChildren().iterator().next();
                        MerchantClass merchant = merchantSnapshot.getValue(MerchantClass.class);
                        
                        if (merchant != null) {
                            Log.d("QRScan", "Marchand trouvé: " + merchant.getBusinessName());
                            editTextSellerId.setText(String.valueOf(merchant.getId_merchant()));
                            Toast.makeText(Be9i.this, 
                                "Marchand trouvé: " + merchant.getBusinessName(), 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("QRScan", "Erreur lors de la conversion des données du marchand");
                            Toast.makeText(Be9i.this, 
                                "Erreur lors de la lecture des données du marchand", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("QRScan", "Aucun marchand trouvé pour l'ID: " + merchantId);
                        Toast.makeText(Be9i.this, 
                            "Marchand non trouvé", 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("QRScan", "Erreur de base de données: " + error.getMessage());
                    Toast.makeText(Be9i.this, 
                        "Erreur: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
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
        // Rechercher le marchand par son ID dans la collection merchants
        mDatabase.child("merchants").orderByChild("id_merchant").equalTo(Integer.parseInt(merchantId))
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(Be9i.this, "Marchand non trouvé", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Récupérer le premier (et unique) marchand trouvé
                    DataSnapshot merchantSnapshot = snapshot.getChildren().iterator().next();
                    MerchantClass merchant = merchantSnapshot.getValue(MerchantClass.class);

                    if (merchant == null) {
                        Toast.makeText(Be9i.this, "Erreur lors de la lecture des données du marchand", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Vérifier le solde du marchand
                    if (merchant.getBalance() < amount) {
                        Toast.makeText(Be9i.this, "Le marchand n'a pas assez de solde", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Show CIN verification popup
                    showCINVerificationPopup(merchantId, amount, merchant.getOwnerCIN());
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