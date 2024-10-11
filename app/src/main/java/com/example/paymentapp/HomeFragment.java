package com.example.paymentapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

class Transaction {
    public String transactionId;
    public int iconResId;
    public String datetime;
    public String source;
    public String note;
    public double amount;

    public Transaction(String transactionId, int iconResId, String datetime, String source, String note, double amount) {
        this.transactionId = transactionId;
        this.iconResId = iconResId;
        this.datetime = datetime;
        this.source = source;
        this.note = note;
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
        seeAllButton.setOnClickListener(v -> showBottomDialog());

        transactions = new ArrayList<>();
        LinearLayout transactionList = view.findViewById(R.id.transaction_list);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Fetch and display the wallet balance
        TextView balanceAmount = getView().findViewById(R.id.balance_amount);
        fetchWalletAmount(balanceAmount);

        // Fetch and populate transactions
        LinearLayout transactionList = getView().findViewById(R.id.transaction_list);
        fetchAndPopulateTransactions(transactionList);
    }

    private void fetchWalletAmount(TextView balanceAmount) {
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("Wallets");

        String walletId = "W" + userId;

        walletsRef.child(walletId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                walletAmt = task.getResult().child("walletAmt").getValue(Double.class);

                if (walletAmt != null) {
                    balanceAmount.setText(String.format("RM %.2f", walletAmt));
                } else {
                    balanceAmount.setText("-");
                    Log.e("HomeFragment", "walletAmt is null for walletId: " + walletId);
                }
            } else {
                Log.e("HomeFragment", "No wallet found for walletId: " + walletId);
                balanceAmount.setText("-");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve wallet amount.", Toast.LENGTH_SHORT).show();
            balanceAmount.setText("-");
        });
    }

    private void fetchAndPopulateTransactions(LinearLayout transactionList) {
        DatabaseReference transactionHistoryRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId).child("transactionHistory");

        transactionHistoryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                transactionList.removeAllViews();  // Clear old views before adding new ones
                transactions.clear();  // Clear the old transaction list

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String transactionId = snapshot.child("transactionId").getValue(String.class);
                    int iconResId = snapshot.child("iconResId").getValue(Integer.class);
                    String datetime = snapshot.child("datetime").getValue(String.class);
                    String source = snapshot.child("source").getValue(String.class);
                    String note = snapshot.child("note").getValue(String.class);
                    double amount = snapshot.child("amount").getValue(Double.class);

                    Transaction transaction = new Transaction(transactionId, iconResId, datetime, source, note, amount);
                    transactions.add(transaction);

                    addTransactionItem(transactionList, iconResId, datetime, source, note, String.format("RM %.2f", amount), getActivity());
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve transactions.", Toast.LENGTH_SHORT).show();
        });
    }

    private void addTransactionItem(LinearLayout parent, int iconResId, String date, String label, String source, String amount, Context context) {
        LinearLayout transactionItem = new LinearLayout(context);
        transactionItem.setOrientation(LinearLayout.HORIZONTAL);
        transactionItem.setPadding(8, 8, 8, dpToPx(context, 15));
        transactionItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
        transactionItem.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(context, 40), dpToPx(context, 40));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(iconResId);
        transactionItem.addView(icon);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textContainer.setLayoutParams(textContainerParams);
        textContainer.setPadding(dpToPx(context, 8), 0, 0, 0);

        TextView transactionDate = new TextView(context);
        transactionDate.setText(date);
        transactionDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textContainer.addView(transactionDate);

        TextView transactionLabel = new TextView(context);
        transactionLabel.setText(label);
        transactionLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        transactionLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(transactionLabel);

        TextView transactionSource = new TextView(context);
        transactionSource.setText(source);
        transactionSource.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textContainer.addView(transactionSource);

        transactionItem.addView(textContainer);

        TextView transactionAmount = new TextView(context);
        transactionAmount.setText(amount);
        transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        transactionAmount.setTextColor(Color.parseColor("#D32F2F"));
        transactionItem.addView(transactionAmount);

        parent.addView(transactionItem);
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.see_all);

        // Initialize dialog views
        LinearLayout transactionHistory = dialog.findViewById(R.id.transaction_history);
        ImageView closeButton = dialog.findViewById(R.id.closeButton);

        // Populate transactionHistory using the transactions list
        for (Transaction transaction : transactions) {
            addTransactionItem(transactionHistory, transaction.iconResId, transaction.datetime, transaction.source, transaction.note, String.format("RM %.2f", transaction.amount), getActivity());
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

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
