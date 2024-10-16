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

public class RequestDoneFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_done, container, false);

        // Get the passed arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String dateTime = getCurrentDateTime();
            String personImageUrl = arguments.getString("personImageUrl");
            String personName = arguments.getString("personName");
            String mobileNumber = arguments.getString("mobileNumber");
            String amountStr = arguments.getString("amount");
            String note = arguments.getString("note");

            TextView dateTimeTextView = view.findViewById(R.id.date_time);
            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView mobileNumberTextView = view.findViewById(R.id.mobile_number);
            TextView amountTextView = view.findViewById(R.id.total_amount);
            TextView noteLabelTextView = view.findViewById(R.id.note_label);
            TextView noteTextView = view.findViewById(R.id.note);

            dateTimeTextView.setText(dateTime);
            personNameTextView.setText(personName);
            mobileNumberTextView.setText(mobileNumber);
            double amount = Double.parseDouble(amountStr);
            amountTextView.setText(String.format("RM %.2f", amount));

            if ("N/A".equals(note)) {
                noteLabelTextView.setVisibility(View.INVISIBLE);
                noteTextView.setVisibility(View.INVISIBLE);
            }
            else{
                noteTextView.setText(note);
            }

            TextView referenceIdTextView = view.findViewById(R.id.reference_id);
            Random random = new Random();
            long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            String referenceId = String.valueOf(referenceNumber);
            referenceIdTextView.setText(referenceId);

            Button doneButton = view.findViewById(R.id.done_button);
            doneButton.setOnClickListener(v -> {
                // Fetch current wallet amount from Firebase and update
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId); // Removed extra "W"

                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Fetch current wallet amount
                            double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                            double requestAmt = Double.parseDouble(amountStr);
                            double updatedWalletAmt = currentWalletAmt + requestAmt;

                            // Update the wallet amount in Firebase
                            walletRef.child("walletAmt").setValue(updatedWalletAmt).addOnCompleteListener(taskUpdate -> {
                                if (taskUpdate.isSuccessful()) {
                                    DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                                    String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                                    Register.Transaction transaction = new Register.Transaction(transactionId, 0, personImageUrl, dateTime, "Request", note, referenceId, requestAmt);
                                    transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("Transaction", "Transaction saved successfully");

                                            navigateToHomeFragment(userId);
                                        } else {
                                            Log.e("Transaction", "Failed to save transaction", task1.getException());
                                        }
                                    });
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

    // Hide Toolbar and BottomAppBar when this fragment is visible
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

    // Show Toolbar and BottomAppBar when leaving this fragment
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