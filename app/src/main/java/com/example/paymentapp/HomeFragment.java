package com.example.paymentapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private String userId, userMobileNumber, userImageUrl;
    private Double walletAmt;
    private List<Transaction> transactions;
    private RecyclerView transactionRecyclerView;
    private TransactionAdapter transactionAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Retrieve userId from the arguments passed to this fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString("userId");
        }

        TextView balanceAmount = view.findViewById(R.id.balance_amount);

        // Fetch wallet balance and user information from Firebase
        fetchWalletAmount(balanceAmount);
        fetchMobileNumber();
        fetchImageUrl();

        // Set up "Reload" button and navigate to ReloadFragment when clicked
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

        // Set up "Request" button and navigate to RequestFragment when clicked
        CardView requestButton = view.findViewById(R.id.request_button);
        requestButton.setOnClickListener(v -> {
            Bundle bundle3 = new Bundle();
            bundle3.putString("userId", userId);
            bundle3.putString("userMobileNumber", userMobileNumber);
            bundle3.putString("userImageUrl", userImageUrl);
            RequestFragment requestFragment = new RequestFragment();
            requestFragment.setArguments(bundle3);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, requestFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set up "Transfer" button and navigate to TransferFragment when clicked
        CardView transferButton = view.findViewById(R.id.transfer_button);
        transferButton.setOnClickListener(v -> {
            Bundle bundle4 = new Bundle();
            bundle4.putString("userId", userId);
            bundle4.putString("userImageUrl", userImageUrl);
            bundle4.putDouble("walletAmt", walletAmt);
            TransferFragment transferFragment = new TransferFragment();
            transferFragment.setArguments(bundle4);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, transferFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set up "Pay" button and navigate to PayFragment when clicked
        CardView payButton = view.findViewById(R.id.pay_button);
        payButton.setOnClickListener(v -> {
            Bundle bundle5 = new Bundle();
            bundle5.putString("userId", userId);
            bundle5.putDouble("walletAmt", walletAmt);
            PayFragment payFragment = new PayFragment();
            payFragment.setArguments(bundle5);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, payFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set up RecyclerView to display transaction history
        transactionRecyclerView = view.findViewById(R.id.transaction_list);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize transaction list and adapter
        transactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactions, getContext(), userId);
        transactionRecyclerView.setAdapter(transactionAdapter);

        // Fetch and display transaction data
        fetchAndPopulateTransactions();

        // Set up "See All" button to display all transactions in a dialog
        TextView seeAllButton = view.findViewById(R.id.see_all_button);
        seeAllButton.setOnClickListener(v -> showBottomDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh wallet balance and transaction history when fragment is visible
        TextView balanceAmount = getView().findViewById(R.id.balance_amount);
        fetchWalletAmount(balanceAmount);

        fetchMobileNumber();
        fetchImageUrl();

        fetchAndPopulateTransactions();
    }

    // Fetches the user's wallet amount from Firebase and updates the balance text
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

    // Fetches the user's mobile number from Firebase
    private void fetchMobileNumber() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                userMobileNumber = task.getResult().child("mobileNumber").getValue(String.class);
            } else {
                Log.e("HomeFragment", "Failed to fetch mobile number for userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve mobile number.", Toast.LENGTH_SHORT).show();
        });
    }
    // Fetches the user's profile image URL from Firebase
    private void fetchImageUrl() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                userImageUrl = task.getResult().child("image").getValue(String.class);
            } else {
                Log.e("HomeFragment", "Failed to fetch image for userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve image.", Toast.LENGTH_SHORT).show();
        });
    }

    // Fetches transaction history from Firebase, sorts by date, and populates the RecyclerView
    private void fetchAndPopulateTransactions() {
        DatabaseReference transactionHistoryRef = FirebaseDatabase.getInstance()
                .getReference("Wallets").child("W" + userId).child("transactionHistory");

        transactionHistoryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                transactions.clear();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mma", Locale.getDefault());

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);

                    // Add transaction if the status is not 0
                    if (transaction != null && transaction.status != 0) {
                        try {
                            Date transactionDate = dateFormat.parse(transaction.datetime);
                            transaction.setParsedDate(transactionDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            continue;  // if parsing fails
                        }
                        transactions.add(transaction);
                    }
                }

                // Sort transactions in descending order by date and notify adapter
                transactions.sort((t1, t2) -> t2.getParsedDate().compareTo(t1.getParsedDate()));
                transactionAdapter.notifyDataSetChanged();
            }
        });
    }

    // Displays a bottom dialog showing all transaction history
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.transaction_see_all);

        ImageView closeButton = dialog.findViewById(R.id.close_button);
        RecyclerView transactionHistoryRecyclerView = dialog.findViewById(R.id.transaction_history_recycler_view);

        // Set up RecyclerView in dialog to display full transaction history
        transactionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        TransactionAdapter transactionDialogAdapter = new TransactionAdapter(transactions, getActivity(), userId);
        transactionHistoryRecyclerView.setAdapter(transactionDialogAdapter);

        // Close the dialog when the close button is clicked
        closeButton.setOnClickListener(view -> dialog.dismiss());

        // Show the dialog with custom animation and style
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
    }
}
