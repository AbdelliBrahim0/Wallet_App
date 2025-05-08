package com.example.be9ik_wallet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Historique extends AppCompatActivity {
    private RecyclerView transactionsRecyclerView;
    private RecentTransactionsAdapter adapter;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private List<TransactionData> allTransactions;
    private String currentFilter = "all";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        // Initialisation Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        allTransactions = new ArrayList<>();

        initViews();
        setupListeners();
        loadTransactions();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentTransactionsAdapter(this, currentUserId);
        transactionsRecyclerView.setAdapter(adapter);

        searchEditText = findViewById(R.id.searchEditText);
        filterChipGroup = findViewById(R.id.filterChipGroup);
    }

    private void setupListeners() {
        // Écouteur pour la recherche
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Écouteur pour les filtres
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipSent) {
                currentFilter = "SENT";
            } else if (checkedId == R.id.chipReceived) {
                currentFilter = "RECEIVED";
            }
            filterTransactions();
        });
    }

    private void loadTransactions() {
        adapter.showLoadingState(true);

        mDatabase.child("users").child(currentUserId).child("transactions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {
                            allTransactions.clear();
                            
                            if (!snapshot.exists() || !snapshot.hasChildren()) {
                                adapter.showEmptyState(true);
                                return;
                            }

                            for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                                TransactionData transaction = transactionSnapshot.getValue(TransactionData.class);
                                if (transaction != null) {
                                    allTransactions.add(transaction);
                                }
                            }

                            filterTransactions();
                        } catch (Exception e) {
                            String errorMsg = "Erreur lors du chargement des transactions: " + e.getMessage();
                            Toast.makeText(Historique.this, errorMsg, Toast.LENGTH_LONG).show();
                            adapter.showError(true, "Erreur de chargement");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        String errorMsg = "Erreur de connexion: " + error.getMessage();
                        Toast.makeText(Historique.this, errorMsg, Toast.LENGTH_LONG).show();
                        adapter.showError(true, "Erreur de connexion");
                    }
                });
    }

    private void filterTransactions() {
        List<TransactionData> filteredList = new ArrayList<>();

        for (TransactionData transaction : allTransactions) {
            // Appliquer le filtre de type
            boolean matchesType = currentFilter.equals("all") || 
                                transaction.getType().equals(currentFilter);

            // Appliquer le filtre de recherche
            boolean matchesSearch = searchQuery.isEmpty() ||
                                  transaction.getDescription().toLowerCase().contains(searchQuery) ||
                                  String.format("%.2f", transaction.getAmount()).contains(searchQuery);

            if (matchesType && matchesSearch) {
                filteredList.add(transaction);
            }
        }

        adapter.showLoadingState(false);
        if (filteredList.isEmpty()) {
            adapter.showEmptyState(true);
        } else {
            adapter.setTransactions(filteredList);
        }
    }
}