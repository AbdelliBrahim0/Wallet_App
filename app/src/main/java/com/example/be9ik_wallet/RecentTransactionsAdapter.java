package com.example.be9ik_wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentTransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_ERROR = 2;
    private static final int TYPE_EMPTY = 3;

    private List<TransactionData> transactions;
    private Context context;
    private String currentUserId;
    private boolean isLoading = false;
    private boolean isError = false;
    private boolean isEmpty = false;
    private String errorMessage = "";

    public RecentTransactionsAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.transactions = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case TYPE_ITEM:
                return new TransactionViewHolder(
                    inflater.inflate(R.layout.item_transaction, parent, false)
                );
            case TYPE_LOADING:
                return new LoadingViewHolder(
                    inflater.inflate(R.layout.item_loading, parent, false)
                );
            case TYPE_ERROR:
                return new ErrorViewHolder(
                    inflater.inflate(R.layout.item_error, parent, false)
                );
            case TYPE_EMPTY:
                return new EmptyViewHolder(
                    inflater.inflate(R.layout.item_empty, parent, false)
                );
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransactionViewHolder) {
            TransactionData transaction = transactions.get(position);
            TransactionViewHolder viewHolder = (TransactionViewHolder) holder;
            
            // Configuration de la transaction
            String amountText = String.format(Locale.getDefault(), "%.2f DT", transaction.getAmount());
            
            // Définir la couleur et le préfixe en fonction du type de transaction
            if ("SENT".equals(transaction.getType())) {
                viewHolder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red));
                amountText = "- " + amountText;
                viewHolder.transactionIcon.setImageResource(R.drawable.ic_send);
                viewHolder.transactionIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_light));
                viewHolder.transactionIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.red));
            } else {
                viewHolder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
                amountText = "+ " + amountText;
                viewHolder.transactionIcon.setImageResource(R.drawable.ic_receive);
                viewHolder.transactionIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green_light));
                viewHolder.transactionIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.green));
            }
            
            viewHolder.tvAmount.setText(amountText);
            viewHolder.tvDate.setText(transaction.getFormattedDate());
            viewHolder.tvDescription.setText(transaction.getDescription());
            
        } else if (holder instanceof ErrorViewHolder) {
            ErrorViewHolder errorHolder = (ErrorViewHolder) holder;
            errorHolder.tvError.setText(errorMessage);
        }
    }

    @Override
    public int getItemCount() {
        if (isLoading || isError || isEmpty) {
            return 1;
        }
        return transactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading) {
            return TYPE_LOADING;
        } else if (isError) {
            return TYPE_ERROR;
        } else if (isEmpty) {
            return TYPE_EMPTY;
        }
        return TYPE_ITEM;
    }

    public void setTransactions(List<TransactionData> transactions) {
        this.transactions = transactions;
        this.isLoading = false;
        this.isError = false;
        this.isEmpty = transactions.isEmpty();
        notifyDataSetChanged();
    }

    public void showLoadingState(boolean show) {
        this.isLoading = show;
        this.isError = false;
        this.isEmpty = false;
        if (show) {
            this.transactions.clear();
        }
        notifyDataSetChanged();
    }

    public void showError(boolean show, String message) {
        this.isError = show;
        this.isLoading = false;
        this.isEmpty = false;
        this.errorMessage = message;
        if (show) {
            this.transactions.clear();
        }
        notifyDataSetChanged();
    }

    public void showEmptyState(boolean show) {
        this.isEmpty = show;
        this.isLoading = false;
        this.isError = false;
        if (show) {
            this.transactions.clear();
        }
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView transactionIcon;
        TextView tvDescription;
        TextView tvDate;
        TextView tvAmount;

        TransactionViewHolder(View itemView) {
            super(itemView);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ErrorViewHolder extends RecyclerView.ViewHolder {
        TextView tvError;

        ErrorViewHolder(View itemView) {
            super(itemView);
            tvError = itemView.findViewById(R.id.tvError);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
} 