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

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private String userId;
    private Double walletAmt;
    private List<Register.Transaction> transactions;

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
            bundle2.putString("userId", userId);
            bundle2.putDouble("walletAmt", walletAmt);
            ReloadFragment reloadFragment = new ReloadFragment();
            reloadFragment.setArguments(bundle2);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, reloadFragment)
                    .addToBackStack(null)
                    .commit();
        });

        CardView requestButton = view.findViewById(R.id.request_button);
        requestButton.setOnClickListener(v -> {
            Bundle bundle3 = new Bundle();
            bundle3.putString("userId", userId);
            RequestFragment requestFragment = new RequestFragment();
            requestFragment.setArguments(bundle3);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, requestFragment)
                    .addToBackStack(null)
                    .commit();
        });

        CardView investButton = view.findViewById(R.id.invest_button);
        investButton.setOnClickListener(v -> {
            Bundle bundle4 = new Bundle();
            bundle4.putString("userId", userId);
            InvestFragment investFragment = new InvestFragment();
            investFragment.setArguments(bundle4);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, investFragment)
                    .addToBackStack(null)
                    .commit();
        });

        CardView transferButton = view.findViewById(R.id.transfer_button);
        transferButton.setOnClickListener(v -> {
            Bundle bundle5 = new Bundle();
            bundle5.putString("userId", userId);
            bundle5.putDouble("walletAmt", walletAmt);
            TransferFragment transferFragment = new TransferFragment();
            transferFragment.setArguments(bundle5);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, transferFragment)
                    .addToBackStack(null)
                    .commit();
        });

        TextView seeAllButton = view.findViewById(R.id.see_all_button);
        seeAllButton.setOnClickListener(v -> showBottomDialog());

        transactions = new ArrayList<>();
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
                    int status = snapshot.child("status").getValue(Integer.class);
                    int iconResId = snapshot.child("iconResId").getValue(Integer.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String datetime = snapshot.child("datetime").getValue(String.class);
                    String source = snapshot.child("source").getValue(String.class);
                    String note = snapshot.child("note").getValue(String.class);
                    String refId = snapshot.child("refId").getValue(String.class);
                    String mobileNumber = snapshot.child("mobileNumber").getValue(String.class);
                    double amount = snapshot.child("amount").getValue(Double.class);

                    // Skip this transaction if status == 0
                    if (status == 0) {
                        continue;
                    }

                    Register.Transaction transaction;
                    if (source.equals("Reload")) {
                        transaction = new Register.Transaction(transactionId, iconResId, datetime, source, refId, amount);
                    }
                    else if (source.equals("Request")) {
                        transaction = new Register.Transaction(transactionId, status, imageUrl, datetime, source, note, refId, mobileNumber, amount);
                    }
                    else {
                        transaction = new Register.Transaction(transactionId, imageUrl, datetime, source, note, refId, amount);
                    }
                    transactions.add(transaction);
                }
                // Sort transactions in descending order by datetime
                transactions.sort((t1, t2) -> t2.datetime.compareTo(t1.datetime));

                // Add transactions to the list
                for (Register.Transaction transaction : transactions) {
                    if (transaction.source.equals("Reload")) {
                        addTransactionItem(transactionList, transaction.iconResId, transaction.datetime, transaction.source, "Ref ID: " + transaction.refId, String.format("RM %.2f", transaction.amount), getActivity());
                    } else if (transaction.source.equals("Request")) {
                        if (transaction.note.equals("N/A")) {
                            addTransactionItem(transactionList, transaction.imageUrl, transaction.datetime, transaction.source, "Ref ID: " + transaction.refId, String.format("RM %.2f", transaction.amount), getActivity());
                        } else {
                            addTransactionItem(transactionList, transaction.imageUrl, transaction.datetime, transaction.source, transaction.note + " (Ref ID: " + transaction.refId + ")", String.format("RM %.2f", transaction.amount), getActivity());
                        }
                    } else {
                        addTransactionItem(transactionList, transaction.imageUrl, transaction.datetime, transaction.source, transaction.note + " (Ref ID: " + transaction.refId + ")", String.format("RM %.2f", transaction.amount), getActivity());
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve transactions.", Toast.LENGTH_SHORT).show();
        });
    }

    private void addTransactionItem(LinearLayout parent, Object imageSource, String date, String source, String note, String amount, Context context) {
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
        if (source.equals("Reload")) {
            icon.setImageResource((int) imageSource);
        } else {
            Glide.with(context).load((String) imageSource).into(icon);
        }
        transactionItem.addView(icon);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textContainer.setLayoutParams(textContainerParams);
        textContainer.setPadding(dpToPx(context, 15), 0, 0, 0);

        TextView transactionDateTime = new TextView(context);
        transactionDateTime.setText(date);
        transactionDateTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textContainer.addView(transactionDateTime);

        TextView transactionSource = new TextView(context);
        transactionSource.setText(source);
        transactionSource.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        transactionSource.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(transactionSource);

        TextView transactionNote = new TextView(context);
        transactionNote.setText(note);
        transactionNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textContainer.addView(transactionNote);

        transactionItem.addView(textContainer);

        TextView transactionAmount = new TextView(context);
        if (source.equals("Reload") || source.equals("Request")) {
            transactionAmount.setText(String.format("+ %s", amount));
            transactionAmount.setTextColor(Color.parseColor("#388E3C"));  // Green color for positive amount
        } else if (source.equals("Transfer")) {
            transactionAmount.setText(String.format("- %s", amount));
            transactionAmount.setTextColor(Color.parseColor("#D32F2F"));  // Red color for negative amount
        }
        transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        transactionItem.addView(transactionAmount);

        parent.addView(transactionItem);
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.transaction_see_all);

        LinearLayout transactionHistory = dialog.findViewById(R.id.transaction_history);
        ImageView closeButton = dialog.findViewById(R.id.close_button);

        for (Register.Transaction transaction : transactions) {
            if (transaction.source.equals("Reload")) {
                addTransactionItem(transactionHistory, transaction.iconResId, transaction.datetime, transaction.source, "Ref ID: " + transaction.refId, String.format("RM %.2f", transaction.amount), getActivity());
            } else if (transaction.source.equals("Request")) {
                if (transaction.note.equals("N/A")) {
                    addTransactionItem(transactionHistory, transaction.imageUrl, transaction.datetime, transaction.source, "Ref ID: " + transaction.refId, String.format("RM %.2f", transaction.amount), getActivity());
                } else {
                    addTransactionItem(transactionHistory, transaction.imageUrl, transaction.datetime, transaction.source, transaction.note + " (Ref ID: " + transaction.refId + ")", String.format("RM %.2f", transaction.amount), getActivity());
                }
            } else {
                addTransactionItem(transactionHistory, transaction.imageUrl, transaction.datetime, transaction.source, transaction.note + " (Ref ID: " + transaction.refId + ")", String.format("RM %.2f", transaction.amount), getActivity());
            }
        }

        closeButton.setOnClickListener(view -> dialog.dismiss());

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
