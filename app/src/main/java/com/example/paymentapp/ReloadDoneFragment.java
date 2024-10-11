package com.example.paymentapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class ReloadDoneFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reload_done, container, false);

        // Retrieve the data from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String amount = arguments.getString("amount", "0");
            int bankImageResId = arguments.getInt("bank_image_res", -1);
            String bankName = arguments.getString("bank_name", "Default Bank");
            String userId = arguments.getString("userId");
            String dateTime = getCurrentDateTime();

            // Set the amount to the TextViews
            TextView totalAmount = view.findViewById(R.id.total_amount);
            TextView totalAmount2 = view.findViewById(R.id.total_amount2);
            TextView dateTimeTextView = view.findViewById(R.id.date_time);
            ImageView bankImageView = view.findViewById(R.id.bank_image);
            TextView bankNameTextView = view.findViewById(R.id.bank_name);

            totalAmount.setText("RM " + amount);
            totalAmount2.setText("RM " + amount);
            dateTimeTextView.setText(dateTime);
            bankImageView.setImageResource(bankImageResId);
            bankNameTextView.setText(bankName);

            // Generate a random reference ID
            TextView referenceIdTextView = view.findViewById(R.id.reference_id);
            Random random = new Random();
            long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            String referenceId = String.valueOf(referenceNumber);
            referenceIdTextView.setText(referenceId);

            Button okButton = view.findViewById(R.id.ok_button);
            okButton.setOnClickListener(v -> {
                // Fetch current wallet amount from Firebase and update
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId);

                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Location", "1");

                        if (task.getResult().exists()) {
                            Log.d("Location", "2");

                            double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                            double reloadAmt = Double.parseDouble(amount);
                            double updatedWalletAmt = currentWalletAmt + reloadAmt;

                            // Update the wallet amount in Firebase
                            walletRef.child("walletAmt").setValue(updatedWalletAmt);

                            Log.d("WalletAmount", String.valueOf(updatedWalletAmt));
                            Log.d("Location", "3");


                            // Also update the transaction history for this reload
                            DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                            String transactionId = transactionHistoryRef.push().getKey();
                            Transaction transaction = new Transaction(transactionId, bankImageResId, dateTime, "Reload", referenceId, reloadAmt);
                            transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d("Transaction", "Transaction saved successfully");
                                    // Navigate to HomeFragment
                                    navigateToHomeFragment(userId);
                                } else {
                                    Log.e("Transaction", "Failed to save transaction", task1.getException());
                                }
                            });

                        }
                    }
                });
            });
        }
        return view;
    }

    private void navigateToHomeFragment(String userId) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        homeFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, homeFragment)
                .addToBackStack(null)
                .commit();
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("dd MMM yyyy, hh:mma", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.GONE);
        }

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.hide();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }

        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.show();
        }
    }
}
