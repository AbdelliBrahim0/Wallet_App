package com.example.be9ik_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
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

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}