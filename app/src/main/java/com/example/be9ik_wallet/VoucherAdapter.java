package com.example.be9ik_wallet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private List<VoucherClass> vouchers = new ArrayList<>();
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onVoucherClick(VoucherClass voucher);
    }

    public VoucherAdapter(OnVoucherClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        VoucherClass voucher = vouchers.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    public void setVouchers(List<VoucherClass> vouchers) {
        this.vouchers = vouchers;
        notifyDataSetChanged();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private ImageView voucherIcon;
        private TextView voucherName;
        private TextView voucherDescription;
        private TextView voucherValidity;
        private TextView voucherPrice;

        VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            voucherIcon = itemView.findViewById(R.id.voucher_icon);
            voucherName = itemView.findViewById(R.id.voucher_name);
            voucherDescription = itemView.findViewById(R.id.voucher_description);
            voucherValidity = itemView.findViewById(R.id.voucher_validity);
            voucherPrice = itemView.findViewById(R.id.voucher_price);
        }

        void bind(VoucherClass voucher) {
            // Définir l'icône en fonction du type de voucher
            voucherIcon.setImageResource(voucher.getType().contains("Food") ? 
                R.drawable.ic_food : R.drawable.ic_coffee);

            voucherName.setText(voucher.getType());
            voucherDescription.setText("Code: " + voucher.getCode());
            
            // Formater la date d'achat
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String purchaseDate = sdf.format(new Date(voucher.getPurchaseDate()));
            voucherValidity.setText("Acheté le " + purchaseDate);
            
            voucherPrice.setText(String.format(Locale.getDefault(), "%.2f DT", voucher.getAmount()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherClick(voucher);
                }
            });
        }
    }
} 