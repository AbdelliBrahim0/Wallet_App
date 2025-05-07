package com.example.be9ik_wallet;

import android.os.Bundle;
import android.view.View;
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

public class DashMarchand extends AppCompatActivity {

    private TextView balanceTextView;
    private TextView hiddenBalanceTextView;
    private boolean isBalanceVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_marchand);

        balanceTextView = findViewById(R.id.textSolde);
        hiddenBalanceTextView = findViewById(R.id.textSoldeHidden);
        MaterialButton toggleBalanceButton = findViewById(R.id.btn_toggle_balance);

        toggleBalanceButton.setOnClickListener(v -> toggleBalanceVisibility());

        // Fetch balance from Firebase
        fetchBalanceFromDatabase();

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

    private void fetchBalanceFromDatabase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("merchants");

        // Query to find the merchant with matching UID
        databaseReference.orderByChild("ownerEmail").equalTo(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot merchantSnapshot : snapshot.getChildren()) {
                                MerchantClass merchant = merchantSnapshot.getValue(MerchantClass.class);
                                if (merchant != null) {
                                    String balance = String.format("%.3f", merchant.getBalance()) + "DT";
                                    balanceTextView.setText(balance);
                                    hiddenBalanceTextView.setText("**** DT");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle database error
                    }
                });
    }
}