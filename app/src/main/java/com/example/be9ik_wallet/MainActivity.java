package com.example.be9ik_wallet;

import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tvUserName, tvBalanceAmount;
    private MaterialButton btnToggleBalance, btnProfile, btnReceiveMoney, btnSendMoney;
    private MaterialButton btnTransactionHistory, btnUseBalance;
    private TextView seeAllVouchers, seeAllTransactions;
    private RecyclerView recentTransactionsRecyclerView;
    private RecentTransactionsAdapter transactionsAdapter;

    private boolean isBalanceVisible = true;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Vérifier si l'utilisateur est connecté
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            // Si l'utilisateur n'est pas connecté, rediriger vers LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // L'utilisateur est connecté, continuer avec l'initialisation
        setContentView(R.layout.activity_main);
        
        currentUserId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        try {
            initViews();
            setupClickListeners();
            loadUserData();
            loadRecentTransactions();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initViews() {
        try {
            // User info
            tvUserName = findViewById(R.id.user_name);
            
            // Balance section
            tvBalanceAmount = findViewById(R.id.balance_amount);
            btnToggleBalance = findViewById(R.id.btn_toggle_balance);
            
            // Action buttons
            btnProfile = findViewById(R.id.btn_profile);
            btnReceiveMoney = findViewById(R.id.btn_receive_money);
            btnSendMoney = findViewById(R.id.btn_send_money);
            btnTransactionHistory = findViewById(R.id.btn_transaction_history);
            btnUseBalance = findViewById(R.id.btn_use_balance);
            
            // See all buttons
            seeAllVouchers = findViewById(R.id.see_all_vouchers);
            seeAllTransactions = findViewById(R.id.see_all_transactions);
            
            // Recent transactions
            recentTransactionsRecyclerView = findViewById(R.id.recent_transactions_recycler_view);
            recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            transactionsAdapter = new RecentTransactionsAdapter(this, currentUserId);
            recentTransactionsRecyclerView.setAdapter(transactionsAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'initialisation des vues: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        try {
            btnToggleBalance.setOnClickListener(v -> toggleBalanceVisibility());
            
            btnProfile.setOnClickListener(v -> showProfileMenu());

            btnReceiveMoney.setOnClickListener(v -> showReceiveOptionsPopup());
            
            btnSendMoney.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, envoyer_argent.class);
                startActivity(intent);
            });

            btnTransactionHistory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, Historique.class);
                startActivity(intent);
            });

            btnUseBalance.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfiterActivity.class);
                startActivity(intent);
            });

            seeAllVouchers.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, Voucher.class);
                startActivity(intent);
            });

            seeAllTransactions.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, Historique.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la configuration des boutons: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void toggleBalanceVisibility() {
        try {
            if (isBalanceVisible) {
                tvBalanceAmount.setText("****");
                btnToggleBalance.setIconResource(R.drawable.ic_visibility_off);
            } else {
                loadUserData();
                btnToggleBalance.setIconResource(R.drawable.ic_visibility);
            }
            isBalanceVisible = !isBalanceVisible;
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors du changement de visibilité: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        try {
            if (mDatabase == null || currentUserId == null) {
                Toast.makeText(this, "Erreur: Base de données ou ID utilisateur non initialisé", Toast.LENGTH_LONG).show();
                return;
            }

            // Afficher un indicateur de chargement
            if (tvBalanceAmount != null) {
                tvBalanceAmount.setText("Chargement...");
            }
            if (tvUserName != null) {
                tvUserName.setText("Chargement...");
            }

            // Log pour le débogage
            System.out.println("Début du chargement des données pour l'utilisateur: " + currentUserId);

            mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        System.out.println("Données reçues: " + snapshot.getValue());
                        
                        if (!snapshot.exists()) {
                            System.out.println("Erreur: Snapshot n'existe pas");
                            Toast.makeText(MainActivity.this, "Erreur: Profil utilisateur non trouvé", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Récupérer les données de l'utilisateur
                        UserClass user = snapshot.getValue(UserClass.class);
                        System.out.println("Utilisateur récupéré: " + (user != null ? user.toString() : "null"));

                        if (user != null) {
                            // Afficher le nom d'utilisateur
                            if (tvUserName != null) {
                                String displayName = user.getUsername();
                                System.out.println("Username à afficher: " + displayName);
                                if (displayName != null && !displayName.isEmpty()) {
                                    tvUserName.setText(displayName);
                                } else {
                                    tvUserName.setText("Utilisateur");
                                }
                            }

                            // Afficher le solde
                            if (tvBalanceAmount != null && isBalanceVisible) {
                                float balance = user.getBalance();
                                System.out.println("Solde à afficher: " + balance);
                                String balanceText = String.format(Locale.getDefault(), "%.2f DT", balance);
                                tvBalanceAmount.setText(balanceText);
                            }

                            // Charger les transactions récentes
                            loadRecentTransactions();
                            
                            // Charger les vouchers
                            loadUserVouchers();
                        } else {
                            System.out.println("Erreur: Objet utilisateur null");
                            Toast.makeText(MainActivity.this, "Erreur: Données utilisateur non valides", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur dans onDataChange: " + e.getMessage());
                        e.printStackTrace();
                        String errorMsg = "Erreur lors du chargement des données: " + e.getMessage();
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println("Erreur Firebase: " + error.getMessage());
                    String errorMsg = "Erreur de connexion à la base de données: " + error.getMessage();
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            System.out.println("Erreur critique dans loadUserData: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = "Erreur critique: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void loadRecentTransactions() {
        try {
            if (mDatabase == null || currentUserId == null || transactionsAdapter == null) {
                Toast.makeText(this, "Erreur: Impossible de charger les transactions - Initialisation incomplète", Toast.LENGTH_LONG).show();
                return;
            }

            // Afficher un état de chargement dans le RecyclerView
            transactionsAdapter.showLoadingState(true);

            mDatabase.child("users").child(currentUserId).child("transactions")
                    .orderByChild("timestamp")
                    .limitToLast(5)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<TransactionData> transactions = new ArrayList<>();
                                
                                if (!snapshot.exists() || !snapshot.hasChildren()) {
                                    transactionsAdapter.showLoadingState(false);
                                    transactionsAdapter.showEmptyState(true);
                                    return;
                                }

                                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                                    TransactionData transaction = transactionSnapshot.getValue(TransactionData.class);
                                    if (transaction != null) {
                                        transactions.add(0, transaction); // Ajouter au début pour avoir l'ordre chronologique inverse
                                    }
                                }

                                transactionsAdapter.showLoadingState(false);
                                if (transactions.isEmpty()) {
                                    transactionsAdapter.showEmptyState(true);
                                } else {
                                    transactionsAdapter.setTransactions(transactions);
                                }
                            } catch (Exception e) {
                                String errorMsg = "Erreur lors du traitement des transactions: " + e.getMessage();
                                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                transactionsAdapter.showError(true, "Erreur de chargement des transactions");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            String errorMsg = "Erreur de chargement des transactions: " + error.getMessage();
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            transactionsAdapter.showError(true, "Erreur de connexion");
                        }
                    });
        } catch (Exception e) {
            String errorMsg = "Erreur critique lors du chargement des transactions: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            if (transactionsAdapter != null) {
                transactionsAdapter.showError(true, "Erreur système");
            }
        }
    }

    private void loadUserVouchers() {
        mDatabase.child("vouchers")
                .orderByChild("userId")
                .equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            // Mettre à jour l'UI avec les vouchers
                            updateVouchersUI(snapshot);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating vouchers UI", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading vouchers", error.toException());
                    }
                });
    }

    private void updateVouchersUI(DataSnapshot snapshot) {
        // Mettre à jour les cartes de vouchers dans le HorizontalScrollView
        LinearLayout voucherContainer = findViewById(R.id.voucher_container);
        if (voucherContainer == null) return;

        voucherContainer.removeAllViews();

        if (!snapshot.exists() || !snapshot.hasChildren()) {
            // Afficher un message si aucun voucher
            TextView noVouchersText = new TextView(this);
            noVouchersText.setText("Aucun voucher disponible");
            noVouchersText.setTextColor(getResources().getColor(android.R.color.white));
            noVouchersText.setTextSize(16);
            noVouchersText.setPadding(32, 32, 32, 32);
            voucherContainer.addView(noVouchersText);
            return;
        }

        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
            VoucherClass voucher = voucherSnapshot.getValue(VoucherClass.class);
            if (voucher != null) {
                // Créer une carte pour chaque voucher
                MaterialCardView voucherCard = createVoucherCard(voucher);
                voucherContainer.addView(voucherCard);
            }
        }
    }

    private MaterialCardView createVoucherCard(VoucherClass voucher) {
        // Créer la carte
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.voucher_card_width),
                (int) getResources().getDimension(R.dimen.voucher_card_height)
        );
        cardParams.setMargins(20, 20, 20, 20);
        card.setLayoutParams(cardParams);
        card.setRadius(24);
        card.setCardElevation(16);
        card.setStrokeWidth(0);
        card.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Créer le layout principal
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundResource(R.drawable.voucher_card_background);
        layout.setPadding(32, 32, 32, 32);

        // Ajouter l'icône avec un fond circulaire orange
        ImageView icon = new ImageView(this);
        icon.setImageResource(voucher.getType().contains("Food") ? 
                R.drawable.ic_food : R.drawable.ic_coffee);
        icon.setBackgroundResource(R.drawable.icon_background_circle_orange);
        icon.setPadding(18, 18, 18, 18);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(72, 72);
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layout.addView(icon, iconParams);

        // Ajouter le nom du voucher avec style amélioré
        TextView nameText = new TextView(this);
        nameText.setText(voucher.getType());
        nameText.setTextColor(getResources().getColor(android.R.color.white));
        nameText.setTextSize(28);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setShadowLayer(8, 0, 2, Color.parseColor("#40000000"));
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.addRule(RelativeLayout.CENTER_VERTICAL);
        nameParams.addRule(RelativeLayout.END_OF, icon.getId());
        nameParams.setMarginStart(28);
        layout.addView(nameText, nameParams);

        // Ajouter le montant avec style amélioré
        TextView amountText = new TextView(this);
        amountText.setText(String.format("%.2f DT", voucher.getAmount()));
        amountText.setTextColor(Color.parseColor("#FF9800"));
        amountText.setTextSize(24);
        amountText.setTypeface(null, Typeface.BOLD);
        amountText.setShadowLayer(8, 0, 2, Color.parseColor("#40000000"));
        RelativeLayout.LayoutParams amountParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        amountParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        amountParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layout.addView(amountText, amountParams);

        // Ajouter une ligne décorative plus élégante
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#FF9800"));
        divider.setAlpha(0.12f);
        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                1
        );
        dividerParams.addRule(RelativeLayout.CENTER_VERTICAL);
        dividerParams.setMargins(0, 20, 0, 20);
        layout.addView(divider, dividerParams);

        // Ajouter le code avec style amélioré
        TextView codeText = new TextView(this);
        codeText.setText("**** " + voucher.getCode());
        codeText.setTextColor(Color.parseColor("#FF9800"));
        codeText.setTextSize(22);
        codeText.setTypeface(null, Typeface.BOLD);
        codeText.setShadowLayer(8, 0, 2, Color.parseColor("#40000000"));
        RelativeLayout.LayoutParams codeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        codeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layout.addView(codeText, codeParams);

        // Ajouter la date avec style amélioré
        TextView dateText = new TextView(this);
        dateText.setText("Acheté le " + new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new java.util.Date(voucher.getPurchaseDate())));
        dateText.setTextColor(getResources().getColor(android.R.color.white));
        dateText.setTextSize(14);
        dateText.setAlpha(0.5f);
        dateText.setShadowLayer(6, 0, 1, Color.parseColor("#40000000"));
        RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dateParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layout.addView(dateText, dateParams);

        card.addView(layout);
        return card;
    }

    private void showReceiveOptionsPopup() {
        try {
            final android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(R.layout.activity_popup_receive_options);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            MaterialButton btnCodeCadeau = dialog.findViewById(R.id.btn_code_cadeau);
            MaterialButton btnLbe9i = dialog.findViewById(R.id.btn_lbe9i);
            MaterialButton btnTransfere = dialog.findViewById(R.id.btn_transfere);
            MaterialButton btnFermer = dialog.findViewById(R.id.btn_fermer);

            btnCodeCadeau.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, CodeCadeau.class));
                dialog.dismiss();
            });

            btnLbe9i.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, Be9i.class));
                dialog.dismiss();
            });

            btnTransfere.setOnClickListener(v -> {
                Toast.makeText(this, "Transfert sélectionné", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnFermer.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'affichage du popup: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showProfileMenu() {
        // Créer et configurer le Dialog
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.popup_profile_menu);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Configurer les clics
        dialog.findViewById(R.id.btnViewProfile).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        dialog.findViewById(R.id.btnSettings).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Paramètres (à venir)", Toast.LENGTH_SHORT).show();
        });

        dialog.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            dialog.dismiss();
            showLogoutConfirmation();
        });

        dialog.show();
    }

    private void showLogoutConfirmation() {
        android.app.Dialog confirmDialog = new android.app.Dialog(this);
        confirmDialog.setContentView(R.layout.popup_confirmation);
        confirmDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = confirmDialog.findViewById(R.id.tvTitle);
        TextView tvMessage = confirmDialog.findViewById(R.id.tvMessage);
        MaterialButton btnConfirm = confirmDialog.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = confirmDialog.findViewById(R.id.btnCancel);

        tvTitle.setText("Déconnexion");
        tvMessage.setText("Êtes-vous sûr de vouloir vous déconnecter ?");
        btnConfirm.setText("Déconnexion");
        btnConfirm.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

        btnConfirm.setOnClickListener(v -> {
            confirmDialog.dismiss();
            performLogout();
        });

        btnCancel.setOnClickListener(v -> confirmDialog.dismiss());

        confirmDialog.show();
    }

    private void performLogout() {
        try {
            // Déconnexion de Firebase
            FirebaseAuth.getInstance().signOut();

            // Effacer les données locales si nécessaire
            // ...

            // Rediriger vers l'écran de connexion
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la déconnexion: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Vérifier si l'utilisateur est toujours connecté
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}