package com.example.paymentapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_done, container, false);

        // Retrieve arguments passed from the previous fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String userImageUrl = arguments.getString("userImageUrl");
            String dateTime = getCurrentDateTime();
            String personImageUrl = arguments.getString("personImageUrl");
            String personName = arguments.getString("personName");
            String personMobileNumber = arguments.getString("personMobileNumber");
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
            mobileNumberTextView.setText(personMobileNumber);

            double amount = Double.parseDouble(amountStr);
            amountTextView.setText(String.format("RM %.2f", amount));

            // Display the note if available; hide the note label if it's "N/A"
            if ("N/A".equals(note)) {
                noteLabelTextView.setVisibility(View.INVISIBLE);
                noteTextView.setVisibility(View.INVISIBLE);
            }
            else{
                noteTextView.setText(note);
            }

            // Generate and display a random reference ID
            TextView referenceIdTextView = view.findViewById(R.id.reference_id);
            Random random = new Random();
            long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            String referenceId = String.valueOf(referenceNumber);
            referenceIdTextView.setText(referenceId);

            // Set up the "Done" button to finalize the request and save to database
            Button doneButton = view.findViewById(R.id.done_button);
            doneButton.setOnClickListener(v -> {
                // Reference to the user's wallet in Firebase
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId);

                // Fetch and update wallet data in Firebase
                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Retrieve the current wallet amount
                            double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);

                            // Update wallet amount and save transaction details
                            walletRef.child("walletAmt").setValue(currentWalletAmt).addOnCompleteListener(taskUpdate -> {
                                if (taskUpdate.isSuccessful()) {
                                    DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                                    String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                                    // Create a Transaction object to store in Firebase
                                    Transaction transaction = new Transaction(transactionId, userImageUrl, personImageUrl, dateTime, "Request", note, referenceId, 0, personMobileNumber, userId, amount);
                                    transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("Transaction", "Transaction saved successfully");
                                            navigateToHomeFragment(userId); // Navigate to HomeFragment upon success
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

    // Navigate back to the HomeFragment after transaction completion
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

    // Returns the current date and time formatted as "dd MMM yyyy, hh:mma"
    private String getCurrentDateTime() {
        return new SimpleDateFormat("dd MMM yyyy, hh:mma", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    // Hide the ActionBar, BottomAppBar, and FloatingActionButton in this fragment
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

    // Show the ActionBar, BottomAppBar, and FloatingActionButton when leaving this fragment
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