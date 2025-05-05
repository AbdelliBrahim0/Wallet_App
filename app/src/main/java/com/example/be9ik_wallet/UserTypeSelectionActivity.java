package com.example.be9ik_wallet;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class UserTypeSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardUser, cardMerchant;
    private RadioButton radioUser, radioMerchant;
    private MaterialButton btnContinue;
    private LinearProgressIndicator progressIndicator;
    private View particlesView;

    private boolean isUserSelected = false;
    private boolean isMerchantSelected = false;
    private ParticlesDrawable particlesDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        // Initialize views
        cardUser = findViewById(R.id.card_user);
        cardMerchant = findViewById(R.id.card_merchant);
        radioUser = findViewById(R.id.radio_user);
        radioMerchant = findViewById(R.id.radio_merchant);
        btnContinue = findViewById(R.id.btn_continue);
        progressIndicator = findViewById(R.id.progress_indicator);
        particlesView = findViewById(R.id.particles_view);

        // Setup particles animation
        setupParticlesAnimation();

        // Setup card selection
        setupCardSelection();

        // Setup continue button
        setupContinueButton();

        // Animate entry
        animateEntry();
    }

    private void setupParticlesAnimation() {
        particlesDrawable = new ParticlesDrawable();
        particlesDrawable.setSpeed(1.5f);
        particlesDrawable.setParticleColor(ContextCompat.getColor(this, R.color.particle_color));
        particlesView.setBackground(particlesDrawable);
        particlesDrawable.start();
    }

    private void setupCardSelection() {
        cardUser.setOnClickListener(v -> {
            selectUserCard();
        });

        cardMerchant.setOnClickListener(v -> {
            selectMerchantCard();
        });

        radioUser.setOnClickListener(v -> {
            selectUserCard();
        });

        radioMerchant.setOnClickListener(v -> {
            selectMerchantCard();
        });
    }

    private void selectUserCard() {
        isUserSelected = true;
        isMerchantSelected = false;

        cardUser.setStrokeColor(ContextCompat.getColor(this, R.color.oranger));
        cardUser.setStrokeWidth(4);
        cardMerchant.setStrokeColor(ContextCompat.getColor(this, R.color.dark_gray));
        cardMerchant.setStrokeWidth(1);

        radioUser.setChecked(true);
        radioMerchant.setChecked(false);

        animateCardSelection(cardUser, true);
        animateCardSelection(cardMerchant, false);

        btnContinue.setEnabled(true);
    }

    private void selectMerchantCard() {
        isUserSelected = false;
        isMerchantSelected = true;

        cardUser.setStrokeColor(ContextCompat.getColor(this, R.color.dark_gray));
        cardUser.setStrokeWidth(1);
        cardMerchant.setStrokeColor(ContextCompat.getColor(this, R.color.lavender));
        cardMerchant.setStrokeWidth(4);

        radioUser.setChecked(false);
        radioMerchant.setChecked(true);

        animateCardSelection(cardUser, false);
        animateCardSelection(cardMerchant, true);

        btnContinue.setEnabled(true);
    }

    private void animateCardSelection(MaterialCardView card, boolean isSelected) {
        float scaleStart = isSelected ? 0.95f : 1.0f;
        float scaleEnd = isSelected ? 1.05f : 0.95f;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", scaleStart, scaleEnd);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", scaleStart, scaleEnd);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator());
        animatorSet.start();
    }

    private void setupContinueButton() {
        btnContinue.setEnabled(false);

        btnContinue.setOnClickListener(v -> {
            if (isUserSelected || isMerchantSelected) {
                // Show loading state
                btnContinue.setEnabled(false);
                btnContinue.setText("Chargement...");

                // Simulate loading
                btnContinue.postDelayed(() -> {
                    // Navigate to next screen based on selection
                    Intent intent;
                    if (isUserSelected) {
                        intent = new Intent(UserTypeSelectionActivity.this, SignupActivity.class);
                        intent.putExtra("USER_TYPE", "USER");
                    } else {
                        intent = new Intent(UserTypeSelectionActivity.this, MerchantRegistrationActivity.class);
                        intent.putExtra("USER_TYPE", "MERCHANT");
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }, 800);
            } else {
                Toast.makeText(this, "Veuillez sélectionner un type de compte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animateEntry() {
        // Animate header
        View headerSection = findViewById(R.id.header_section);
        headerSection.setTranslationY(-100f);
        headerSection.setAlpha(0f);

        ObjectAnimator headerTranslation = ObjectAnimator.ofFloat(headerSection, "translationY", -100f, 0f);
        ObjectAnimator headerAlpha = ObjectAnimator.ofFloat(headerSection, "alpha", 0f, 1f);

        // Animate cards
        cardUser.setScaleX(0.8f);
        cardUser.setScaleY(0.8f);
        cardUser.setAlpha(0f);

        cardMerchant.setScaleX(0.8f);
        cardMerchant.setScaleY(0.8f);
        cardMerchant.setAlpha(0f);

        ObjectAnimator userScaleX = ObjectAnimator.ofFloat(cardUser, "scaleX", 0.8f, 1f);
        ObjectAnimator userScaleY = ObjectAnimator.ofFloat(cardUser, "scaleY", 0.8f, 1f);
        ObjectAnimator userAlpha = ObjectAnimator.ofFloat(cardUser, "alpha", 0f, 1f);

        ObjectAnimator merchantScaleX = ObjectAnimator.ofFloat(cardMerchant, "scaleX", 0.8f, 1f);
        ObjectAnimator merchantScaleY = ObjectAnimator.ofFloat(cardMerchant, "scaleY", 0.8f, 1f);
        ObjectAnimator merchantAlpha = ObjectAnimator.ofFloat(cardMerchant, "alpha", 0f, 1f);

        // Animate button
        btnContinue.setTranslationY(100f);
        btnContinue.setAlpha(0f);

        ObjectAnimator buttonTranslation = ObjectAnimator.ofFloat(btnContinue, "translationY", 100f, 0f);
        ObjectAnimator buttonAlpha = ObjectAnimator.ofFloat(btnContinue, "alpha", 0f, 1f);

        // Create animation set
        AnimatorSet headerAnimSet = new AnimatorSet();
        headerAnimSet.playTogether(headerTranslation, headerAlpha);
        headerAnimSet.setDuration(600);
        headerAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet userAnimSet = new AnimatorSet();
        userAnimSet.playTogether(userScaleX, userScaleY, userAlpha);
        userAnimSet.setDuration(500);
        userAnimSet.setStartDelay(300);
        userAnimSet.setInterpolator(new OvershootInterpolator());

        AnimatorSet merchantAnimSet = new AnimatorSet();
        merchantAnimSet.playTogether(merchantScaleX, merchantScaleY, merchantAlpha);
        merchantAnimSet.setDuration(500);
        merchantAnimSet.setStartDelay(400);
        merchantAnimSet.setInterpolator(new OvershootInterpolator());

        AnimatorSet buttonAnimSet = new AnimatorSet();
        buttonAnimSet.playTogether(buttonTranslation, buttonAlpha);
        buttonAnimSet.setDuration(500);
        buttonAnimSet.setStartDelay(600);
        buttonAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Start animations
        headerAnimSet.start();
        userAnimSet.start();
        merchantAnimSet.start();
        buttonAnimSet.start();

        // Animate progress indicator
        progressIndicator.setProgress(0);
        progressIndicator.postDelayed(() -> {
            ObjectAnimator progressAnim = ObjectAnimator.ofInt(progressIndicator, "progress", 0, 33);
            progressAnim.setDuration(1000);
            progressAnim.start();
        }, 800);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (particlesDrawable != null) {
            particlesDrawable.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (particlesDrawable != null) {
            particlesDrawable.stop();
        }
    }
}