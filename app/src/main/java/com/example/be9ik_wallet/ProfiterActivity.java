package com.example.be9ik_wallet;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        // Load user data
        loadUserData();

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