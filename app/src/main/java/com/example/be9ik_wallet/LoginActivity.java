package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialCheckBox cbRememberMe;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvCreateAccount;
    private View loadingOverlay;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.login_username); // Note: Changed to email login
        etPassword = findViewById(R.id.login_password);
        cbRememberMe = findViewById(R.id.remember_me);
        btnLogin = findViewById(R.id.login_button);
        tvForgotPassword = findViewById(R.id.forgot_password);
        tvCreateAccount = findViewById(R.id.create_account);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, UserTypeSelectionActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim().toLowerCase(); // Normalize email to lowercase
        String password = etPassword.getText().toString().trim();

        Log.d("LoginActivity", "Attempting login with email: " + email + " and password: " + password);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                loadingOverlay.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Log.d("LoginActivity", "Login successful for email: " + email);
                    String userId = mAuth.getCurrentUser().getUid();
                    Log.d("LoginActivity", "User ID: " + userId);

                    // First check if user exists in merchants
                    DatabaseReference merchantsRef = FirebaseDatabase.getInstance().getReference("merchants");
                    merchantsRef.orderByChild("ownerEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.d("LoginActivity", "Merchant found for email: " + email);
                                navigateToDashMarchand();
                            } else {
                                Log.d("LoginActivity", "No merchant found for email: " + email);

                                // Check if user exists in users
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                usersRef.child(userId).child("role").get().addOnCompleteListener(roleTask -> {
                                    if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                                        String role = roleTask.getResult().getValue(String.class);
                                        Log.d("LoginActivity", "Retrieved role: " + role);

                                        if ("user".equals(role)) {
                                            navigateToMainActivity();
                                        } else {
                                            Log.e("LoginActivity", "Unknown role: " + role);
                                            Toast.makeText(LoginActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e("LoginActivity", "Failed to retrieve role or user not found");
                                        Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e("LoginActivity", "Database error: " + error.getMessage());
                            Toast.makeText(LoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("LoginActivity", "Login failed: " + task.getException().getMessage());
                    Toast.makeText(LoginActivity.this,
                            "Login failed: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email to reset password");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashMarchand() {
        Intent intent = new Intent(LoginActivity.this, DashMarchand.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}