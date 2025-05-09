package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class popup_receive_options extends AppCompatActivity {
    private MaterialButton btnCodeCadeau, btnLbe9i, btnTransfere, btnFermer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_receive_options);

        // Use the root MaterialCardView for window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        btnCodeCadeau = findViewById(R.id.btn_code_cadeau);
        btnLbe9i = findViewById(R.id.btn_lbe9i);
        btnTransfere = findViewById(R.id.btn_transfere);
        btnFermer = findViewById(R.id.btn_fermer);

        // Set click listeners
        btnCodeCadeau.setOnClickListener(v -> {
            Intent intent = new Intent(popup_receive_options.this, CodeCadeau.class);
            startActivity(intent);
            finish();
        });

        btnLbe9i.setOnClickListener(v -> {
            Intent intent = new Intent(popup_receive_options.this, Be9i.class);
            startActivity(intent);
            finish();
        });

        btnTransfere.setOnClickListener(v -> {
            Toast.makeText(this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
        });

        btnFermer.setOnClickListener(v -> finish());
    }
}