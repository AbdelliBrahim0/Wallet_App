package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MerchantRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "MerchantRegistration";
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_merchant_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("merchants");
        Log.d(TAG, "Firebase Database reference initialized: " + databaseReference.toString());

        // Populate the business domain dropdown
        AutoCompleteTextView businessDomainDropdown = findViewById(R.id.business_domain);
        String[] businessDomains = {"Restaurant", "Café", "Magasin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, businessDomains);
        businessDomainDropdown.setAdapter(adapter);

        MaterialButton registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            // Collect inputs
            String businessName = ((TextInputEditText) findViewById(R.id.business_name)).getText().toString();
            String businessDomain = ((AutoCompleteTextView) findViewById(R.id.business_domain)).getText().toString();
            String businessLocation = ((TextInputEditText) findViewById(R.id.business_location)).getText().toString();
            String businessDescription = ((TextInputEditText) findViewById(R.id.business_description)).getText().toString();
            String ownerFirstName = ((TextInputEditText) findViewById(R.id.owner_first_name)).getText().toString();
            String ownerLastName = ((TextInputEditText) findViewById(R.id.owner_last_name)).getText().toString();
            String ownerUsername = ((TextInputEditText) findViewById(R.id.owner_username)).getText().toString();
            String ownerEmail = ((TextInputEditText) findViewById(R.id.owner_email)).getText().toString();
            String ownerCIN = ((TextInputEditText) findViewById(R.id.owner_cin)).getText().toString();
            String ownerPassword = ((TextInputEditText) findViewById(R.id.owner_password)).getText().toString();
            boolean termsAccepted = ((MaterialCheckBox) findViewById(R.id.terms_checkbox)).isChecked();

            if (!termsAccepted) {
                Toast.makeText(this, "Vous devez accepter les conditions générales.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate required fields
            if (businessName.isEmpty() || businessDomain.isEmpty() || ownerFirstName.isEmpty() || ownerEmail.isEmpty() || ownerPassword.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create Merchant object
            MerchantClass merchant = new MerchantClass(
                    businessName, businessDomain, businessLocation, businessDescription,
                    ownerFirstName, ownerLastName, ownerUsername, ownerEmail, ownerCIN,
                    ownerPassword, termsAccepted
            );

            Log.d(TAG, "Merchant object created: " + merchant.toString());

            // Save to database
            saveMerchantToDatabase(merchant);
        });
    }

    private void saveMerchantToDatabase(MerchantClass merchant) {
        String merchantId = databaseReference.push().getKey(); // Generate unique ID
        if (merchantId != null) {
            Log.d(TAG, "Generated merchant ID: " + merchantId);
            databaseReference.child(merchantId).setValue(merchant)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Merchant saved successfully.");
                    Toast.makeText(this, "Inscription réussie!", Toast.LENGTH_SHORT).show();

                    // Redirect to UserTypeSelectionActivity
                    Intent intent = new Intent(this, UserTypeSelectionActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'inscription: " + e.getMessage());
                    Toast.makeText(this, "Erreur lors de l'inscription: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Log.e(TAG, "Erreur lors de la génération de l'ID marchand.");
            Toast.makeText(this, "Erreur lors de la génération de l'ID marchand.", Toast.LENGTH_SHORT).show();
        }
    }
}