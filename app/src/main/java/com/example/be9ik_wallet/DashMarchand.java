package com.example.be9ik_wallet;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
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

public class DashMarchand extends AppCompatActivity {

    private TextView balanceTextView;
    private TextView hiddenBalanceTextView;
    private ImageView qrCodeImageView;
    private boolean isBalanceVisible = true;
    private String merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_marchand);

        balanceTextView = findViewById(R.id.textSolde);
        hiddenBalanceTextView = findViewById(R.id.textSoldeHidden);
        qrCodeImageView = findViewById(R.id.iv_qr_code);
        MaterialButton toggleBalanceButton = findViewById(R.id.btn_toggle_balance);

        toggleBalanceButton.setOnClickListener(v -> toggleBalanceVisibility());

        // Fetch merchant data from Firebase
        fetchMerchantData();

        // Make sure your root view in activity_dash_marchand.xml has android:id="@+id/main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void toggleBalanceVisibility() {
        if (isBalanceVisible) {
            balanceTextView.setVisibility(View.GONE);
            hiddenBalanceTextView.setVisibility(View.VISIBLE);
        } else {
            balanceTextView.setVisibility(View.VISIBLE);
            hiddenBalanceTextView.setVisibility(View.GONE);
        }
        isBalanceVisible = !isBalanceVisible;
    }

    private void fetchMerchantData() {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("merchants");

        // Query to find the merchant with matching email
        databaseReference.orderByChild("ownerEmail").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot merchantSnapshot : snapshot.getChildren()) {
                                MerchantClass merchant = merchantSnapshot.getValue(MerchantClass.class);
                                if (merchant != null) {
                                    merchantId = String.valueOf(merchant.getId_merchant());
                                    String balance = String.format("%.3f", merchant.getBalance()) + "DT";
                                    balanceTextView.setText(balance);
                                    hiddenBalanceTextView.setText("**** DT");
                                    
                                    // Update merchant name
                                    TextView merchantNameView = findViewById(R.id.merchant_name);
                                    if (merchantNameView != null) {
                                        merchantNameView.setText(merchant.getBusinessName());
                                    }

                                    // Generate and display QR code
                                    generateQRCode(merchantId);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("DashMarchand", "Database error: " + error.getMessage());
                    }
                });
    }

    private void generateQRCode(String merchantId) {
        try {
            Log.d("QRScan", "Génération du QR code pour l'ID marchand: " + merchantId);
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(merchantId, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCodeImageView.setImageBitmap(bitmap);
            Log.d("QRScan", "QR code généré avec succès");
        } catch (WriterException e) {
            Log.e("QRScan", "Erreur lors de la génération du QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}