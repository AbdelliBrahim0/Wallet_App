package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName, tvBalanceAmount;
    private MaterialButton btnToggleBalance, btnProfile, btnReceiveMoney, btnSendMoney, btnTransactionHistory;
    private boolean isBalanceVisible = true;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        loadUserData();
        setupToggleBalance();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.user_name);
        tvBalanceAmount = findViewById(R.id.balance_amount);
        btnToggleBalance = findViewById(R.id.btn_toggle_balance);
        btnProfile = findViewById(R.id.btn_profile);
        btnReceiveMoney = findViewById(R.id.btn_receive_money);
        btnSendMoney = findViewById(R.id.btn_send_money);
        btnTransactionHistory = findViewById(R.id.btn_transaction_history);

        btnReceiveMoney.setOnClickListener(v -> showReceiveOptionsPopup());
        btnSendMoney.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EnvoyerArgentActivity.class);
            startActivity(intent);
        });
        btnTransactionHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Historique.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "HistoriqueActivity introuvable", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserClass user = snapshot.getValue(UserClass.class);
                    if (user != null) {
                        tvUserName.setText(user.getUsername());
                        tvBalanceAmount.setText(String.format("%.3f DT", user.getBalance()));
                        setupProfileButton(user.getUsername()); // Pass username to ProfileActivity
                    }
                } else {
                    tvUserName.setText("Utilisateur introuvable");
                    btnProfile.setOnClickListener(v ->
                            Toast.makeText(MainActivity.this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToggleBalance() {
        btnToggleBalance.setOnClickListener(v -> {
            if (isBalanceVisible) {
                tvBalanceAmount.setText("****");
                btnToggleBalance.setIconResource(R.drawable.ic_visibility_off);
            } else {
                loadUserData(); // Reload balance
                btnToggleBalance.setIconResource(R.drawable.ic_visibility);
            }
            isBalanceVisible = !isBalanceVisible;
        });
    }

    private void setupProfileButton(String username) {
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private void showReceiveOptionsPopup() {
        // Créez un Dialog
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.activity_popup_receive_options);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Configurez les boutons dans le popup
        MaterialButton btnCodeCadeau = dialog.findViewById(R.id.btn_code_cadeau);
        MaterialButton btnLbe9i = dialog.findViewById(R.id.btn_lbe9i);
        MaterialButton btnTransfere = dialog.findViewById(R.id.btn_transfere);
        MaterialButton btnFermer = dialog.findViewById(R.id.btn_fermer);

        btnCodeCadeau.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CodeCadeau.class);
            startActivity(intent);
            dialog.dismiss();
        });

        btnLbe9i.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Be9i.class);
            startActivity(intent);
            dialog.dismiss();
        });

        btnTransfere.setOnClickListener(v -> {
            Toast.makeText(this, "Transfert sélectionné", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnFermer.setOnClickListener(v -> dialog.dismiss());

        // Affichez le Dialog
        dialog.show();
    }
}