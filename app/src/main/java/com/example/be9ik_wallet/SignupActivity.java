package com.example.be9ik_wallet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_GENDER = "gender";

    private TextInputEditText etFirstName, etLastName, etUsername, etEmail,
            etBirthDate, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgGender;
    private MaterialCheckBox cbTerms;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        initViews();

        findViewById(R.id.signup_button).setOnClickListener(v -> validateAndRegister());
        findViewById(R.id.text_login_link).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        etFirstName = findViewById(R.id.signup_name);
        etLastName = findViewById(R.id.signup_last_name);
        etUsername = findViewById(R.id.signup_username);
        etEmail = findViewById(R.id.signup_email);
        etBirthDate = findViewById(R.id.signup_birth_date);
        etPhone = findViewById(R.id.signup_phone_number);
        etPassword = findViewById(R.id.signup_password);
        rgGender = findViewById(R.id.signup_gender_group);
        cbTerms = findViewById(R.id.terms_checkbox);
    }

    private void validateAndRegister() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = getSelectedGender();
        boolean termsAccepted = cbTerms.isChecked();

        if (!validateInputs(firstName, lastName, username, email, birthDate, phone, password, gender, termsAccepted)) {
            return;
        }

        registerWithFirebase(email, password, firstName, lastName, username, birthDate, phone, gender);
    }

    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.signup_gender_male) {
            return "Male";
        } else if (selectedId == R.id.signup_gender_female) {
            return "Female";
        }
        return ""; // No selection
    }

    private boolean validateInputs(String firstName, String lastName, String username,
                                   String email, String birthDate, String phone,
                                   String password, String gender, boolean termsAccepted) {
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            return false;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            return false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return false;
        }

        if (birthDate.isEmpty()) {
            etBirthDate.setError("Birth date is required");
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner un seul genre (Homme ou Femme)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!termsAccepted) {
            Toast.makeText(this, "You must accept the terms", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerWithFirebase(String email, String password,
                                      String firstName, String lastName, String username,
                                      String birthDate, String phone, String gender) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), firstName, lastName, username,
                                    email, birthDate, phone, gender);
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(SignupActivity.this,
                                "Registration failed: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                        task.getException().printStackTrace(); // Log the error for debugging
                    }
                });
    }

    private void saveUserToDatabase(String userId, String firstName, String lastName,
                                    String username, String email, String birthDate,
                                    String phone, String gender) {
        UserClass user = new UserClass(firstName, lastName, username, email,
                birthDate, phone, "", gender, true);
        user.setId_utilisateur(userId);
        user.setRole("user"); // Définir le rôle par défaut à "user"

        mDatabase.child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this,
                            "Registration successful!",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this,
                            "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveGender(String gender) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_GENDER, gender);
        editor.apply();
    }

    private void navigateToNextStep() {
        // Navigate to the next step in the signup process
        Intent intent = new Intent(SignupActivity.this, MainActivity.class); // Replace with the actual activity if needed
        startActivity(intent);
        finish();
    }
}