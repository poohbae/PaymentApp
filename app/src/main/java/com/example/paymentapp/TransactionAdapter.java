package com.example.paymentapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Register.Transaction> transactions;
    private Context context;
    private String userId;

    public TransactionAdapter(List<Register.Transaction> transactions, Context context, String userId) {
        this.transactions = transactions;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Register.Transaction transaction = transactions.get(position);

        // Set date, source, and note
        holder.transactionDate.setText(transaction.datetime);
        holder.transactionSource.setText(transaction.source);
        holder.transactionNote.setText((transaction.note == null || "N/A".equals(transaction.note))
                ? "Ref ID: " + transaction.refId
                : transaction.note);

        // Set amount and color based on source
        if ("Reload".equals(transaction.source) ||
                (transaction.source.equals("Transfer") && userId.equals(transaction.recipientId)) ||
                (transaction.source.equals("Request") && userId.equals(transaction.recipientId))) {
            holder.transactionAmount.setText(String.format("+ RM %.2f", transaction.amount));
            holder.transactionAmount.setTextColor(Color.parseColor("#388E3C"));
            if (transaction.iconResId != 0) {
                holder.icon.setImageResource(transaction.iconResId);
            } else if (transaction.senderImageUrl != null) {
                Glide.with(context).load(transaction.senderImageUrl).into(holder.icon);
            }
        } else {
            holder.transactionAmount.setText(String.format("- RM %.2f", transaction.amount));
            holder.transactionAmount.setTextColor(Color.parseColor("#D32F2F"));
            Glide.with(context).load(transaction.recipientImageUrl).into(holder.icon);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionDate, transactionSource, transactionNote, transactionAmount;
        ImageView icon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            transactionDate = itemView.findViewById(R.id.transaction_date);
            transactionSource = itemView.findViewById(R.id.transaction_source);
            transactionNote = itemView.findViewById(R.id.transaction_note);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);
        }
    }
}