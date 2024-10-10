package com.example.paymentapp;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

class Transaction {
    public int iconResId;
    public String date;
    public String label;
    public String source;
    public String amount;

    // Constructor
    public Transaction(int iconResId, String date, String label, String source, String amount) {
        this.iconResId = iconResId;
        this.date = date;
        this.label = label;
        this.source = source;
        this.amount = amount;
    }
}

public class HomeFragment extends Fragment {
    private String userId;
    private Double walletAmt;
    private List<Transaction> transactions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString("userId");
        }

        TextView balanceAmount = view.findViewById(R.id.balance_amount);
        fetchWalletAmount(balanceAmount);

        CardView reloadButton = view.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(v -> {
            Bundle bundle2 = new Bundle();
            bundle2.putDouble("walletAmt", walletAmt); // Pass the walletAmt
            ReloadFragment reloadFragment = new ReloadFragment();
            reloadFragment.setArguments(bundle2);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, reloadFragment)
                    .addToBackStack(null)
                    .commit();
        });

        CardView requestButton = view.findViewById(R.id.request_button);
        requestButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new RequestFragment())
                    .addToBackStack(null) // Add the transaction to the back stack
                    .commit();
        });

        CardView investButton = view.findViewById(R.id.invest_button);
        investButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new InvestFragment())
                    .addToBackStack(null)
                    .commit();
        });

        CardView transferButton = view.findViewById(R.id.transfer_button);
        transferButton.setOnClickListener(v -> {
            Bundle bundle3 = new Bundle();
            bundle3.putDouble("walletAmt", walletAmt);
            TransferFragment transferFragment = new TransferFragment();
            transferFragment.setArguments(bundle3);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, transferFragment)
                    .addToBackStack(null)
                    .commit();
        });

        TextView seeAllButton = view.findViewById(R.id.see_all_button);
        seeAllButton.setOnClickListener(v -> {
            showBottomDialog();
            Toast.makeText(getActivity(), "See All Clicked", Toast.LENGTH_SHORT).show();
        });

        // Initialize the transactions list
        transactions = new ArrayList<>();
        // transactions.add(new Transaction(R.drawable.add, "1 Aug 2024", "Netflix", "Auto Transfer", "- RM11.00"));

        // Find the parent LinearLayout where transaction items will be added
        LinearLayout transactionList = view.findViewById(R.id.transaction_list);

        // Populate transactionList using the transactions list
        for (Transaction transaction : transactions) {
            addTransactionItem(transactionList, transaction.iconResId, transaction.date, transaction.label, transaction.source, transaction.amount, getActivity());
        }

        return view;
    }

    private void fetchWalletAmount(TextView balanceAmount) {
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("Wallets");

        String walletId = "W" + userId;

        // Query to find the wallet by walletId
        walletsRef.child(walletId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Retrieve walletAmt directly since we are using walletId
                walletAmt = task.getResult().child("walletAmt").getValue(Double.class);

                // Set wallet amount to the TextView
                if (walletAmt != null) {
                    balanceAmount.setText(String.format("RM %.2f", walletAmt));
                } else {
                    balanceAmount.setText("-"); // Handle null case
                    Log.e("HomeFragment", "walletAmt is null for walletId: " + walletId);
                }
            } else {
                // Handle the case where no wallet is found
                Log.e("HomeFragment", "No wallet found for walletId: " + walletId);
                balanceAmount.setText("-"); // Default value or error handling
            }
        }).addOnFailureListener(e -> {
            // Handle any errors while retrieving data
            Toast.makeText(getActivity(), "Failed to retrieve wallet amount.", Toast.LENGTH_SHORT).show();
            balanceAmount.setText("-"); // Default value on failure
        });
    }

    // Method to show a bottom dialog
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.see_all);

        // Initialize dialog views
        LinearLayout transactionHistory = dialog.findViewById(R.id.transaction_history);
        ImageView closeButton = dialog.findViewById(R.id.closeButton);

        // Populate transactionHistory using the transactions list
        for (Transaction transaction : transactions) {
            addTransactionItem(transactionHistory, transaction.iconResId, transaction.date, transaction.label, transaction.source, transaction.amount, getActivity());
        }

        // Set up click listener for close button
        closeButton.setOnClickListener(view -> dialog.dismiss());

        // Show the dialog and set its properties
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
    }

    // Helper method to add transaction items dynamically
    private void addTransactionItem(LinearLayout parent, int iconResId, String date, String label, String source, String amount, Context context) {
        // Same method as before to add transaction items to the parent layout
        LinearLayout transactionItem = new LinearLayout(context);
        transactionItem.setOrientation(LinearLayout.HORIZONTAL);
        transactionItem.setPadding(8, 8, 8, dpToPx(context, 15)); // 8dp padding and 15dp bottom padding
        transactionItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
        transactionItem.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create the ImageView for the transaction icon
        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(context, 40), dpToPx(context, 40)); // Consistent 40dp width and height
        icon.setLayoutParams(iconParams);
        icon.setImageResource(iconResId); // Use the passed iconResId
        transactionItem.addView(icon);

        // Create the LinearLayout for the text content (date, label, source)
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1); // Weight of 1
        textContainer.setLayoutParams(textContainerParams);
        textContainer.setPadding(dpToPx(context, 8), 0, 0, 0); // 8dp paddingStart

        // Create a TextView for the transaction date
        TextView transactionDate = new TextView(context);
        transactionDate.setText(date);
        transactionDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 16sp text size
        textContainer.addView(transactionDate);

        // Create a TextView for the transaction label
        TextView transactionLabel = new TextView(context);
        transactionLabel.setText(label);
        transactionLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 16sp text size
        transactionLabel.setTypeface(null, android.graphics.Typeface.BOLD); // Bold
        textContainer.addView(transactionLabel);

        // Create a TextView for the transaction source
        TextView transactionSource = new TextView(context);
        transactionSource.setText(source);
        transactionSource.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); // 14sp text size
        textContainer.addView(transactionSource);

        // Add the text container to the transaction item
        transactionItem.addView(textContainer);

        // Create a TextView for the transaction amount
        TextView transactionAmount = new TextView(context);
        transactionAmount.setText(amount);
        transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 16sp text size
        transactionAmount.setTextColor(Color.parseColor("#D32F2F")); // Red color for the amount
        transactionItem.addView(transactionAmount);

        // Add the entire transaction item to the parent layout
        parent.addView(transactionItem);
    }

    // Helper method to convert dp to pixels
    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

