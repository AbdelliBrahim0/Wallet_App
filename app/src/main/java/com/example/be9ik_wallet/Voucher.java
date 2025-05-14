package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Voucher extends AppCompatActivity {
    private static final String TAG = "Voucher";
    private TextView balanceText;
    private RecyclerView vouchersRecyclerView;
    private VoucherAdapter voucherAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voucher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mes Vouchers");

        balanceText = findViewById(R.id.balance_text);
        vouchersRecyclerView = findViewById(R.id.vouchers_recycler_view);
        MaterialButton buyVoucherButton = findViewById(R.id.buyVoucherButton);

        // Configure the RecyclerView
        vouchersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        voucherAdapter = new VoucherAdapter(voucher -> {
            Intent intent = new Intent(Voucher.this, PayerVoucher.class);
            intent.putExtra("voucherId", voucher.getId()); // Pass voucher ID if needed
            startActivity(intent);
        });
        vouchersRecyclerView.setAdapter(voucherAdapter);

        // Configure buy voucher button
        buyVoucherButton.setOnClickListener(v -> {
            startActivity(new Intent(Voucher.this, AcheterVoucher.class));
        });

        // Load user balance and vouchers
        loadUserData();
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        // Load balance
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double balance = dataSnapshot.child("balance").getValue(Double.class);
                    balanceText.setText(String.format("%.2f DT", balance));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading balance", databaseError.toException());
            }
        });

        // Load vouchers
        mDatabase.child("vouchers")
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<VoucherClass> vouchers = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            VoucherClass voucher = snapshot.getValue(VoucherClass.class);
                            if (voucher != null) {
                                vouchers.add(voucher);
                            }
                        }
                        voucherAdapter.setVouchers(vouchers);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error loading vouchers", databaseError.toException());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Reload data when activity resumes
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}