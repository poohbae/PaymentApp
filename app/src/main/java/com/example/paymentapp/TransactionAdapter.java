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
    private List<Transaction> transactions;
    private Context context;
    private String userId;

    // Constructor to initialize transactions, context, and userId
    public TransactionAdapter(List<Transaction> transactions, Context context, String userId) {
        this.transactions = transactions;
        this.context = context;
        this.userId = userId;
    }

    // Inflates the layout for each transaction item and returns a ViewHolder
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    // Binds data to each ViewHolder, setting up details for each transaction
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set the transaction date, source, and note
        holder.transactionDate.setText(transaction.datetime);
        holder.transactionSource.setText(transaction.source);
        holder.transactionNote.setText((transaction.note == null || "N/A".equals(transaction.note))
                ? "Ref ID: " + transaction.refId
                : transaction.note);

        // If the transaction is for a bill, set the note to display the bill number
        if (transaction.source.equals("Split Bill") || transaction.source.equals("Select & Pay")){
            holder.transactionNote.setText("Bill No: #" + transaction.refId);
        }

        // Determine the amount color and icon based on transaction type
        if (transaction.source.equals("Reload") ||
                (transaction.source.equals("Transfer") && userId.equals(transaction.recipientId)) ||
                (transaction.source.equals("Request") && userId.equals(transaction.recipientId))) {

            // Show the amount as positive with green color
            holder.transactionAmount.setText(String.format("+ RM %.2f", transaction.amount));
            holder.transactionAmount.setTextColor(Color.parseColor("#388E3C"));

            // Set icon based on icon resource ID or load sender image URL if available
            if (transaction.iconResId != 0) {
                holder.icon.setImageResource(transaction.iconResId);
            } else if (transaction.senderImageUrl != null) {
                Glide.with(context).load(transaction.senderImageUrl).into(holder.icon);
            }
        } else {
            // Show the amount as negative with red color for outgoing transactions
            holder.transactionAmount.setText(String.format("- RM %.2f", transaction.amount));
            holder.transactionAmount.setTextColor(Color.parseColor("#D32F2F"));

            // Set icon based on icon resource ID or load recipient image URL if available
            if (transaction.iconResId != 0) {
                holder.icon.setImageResource(transaction.iconResId);
            } else if (transaction.senderImageUrl != null) {
                Glide.with(context).load(transaction.recipientImageUrl).into(holder.icon);
            }
        }
    }

    // Returns the total number of transactions to be displayed
    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // ViewHolder class for managing and recycling transaction item views
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