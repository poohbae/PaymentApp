package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransferMoneyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer_money, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Retrieve the person name from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String userImageUrl = arguments.getString("userImageUrl");
            double walletAmt = arguments.getDouble("walletAmt");
            String personImageUrl = arguments.getString("personImageUrl");
            String personName = arguments.getString("personName");
            String personMobileNumber = arguments.getString("personMobileNumber");
            String personId = arguments.getString("personId");

            EditText inputAmountEditText = view.findViewById(R.id.input_amount);
            ImageView personImageView = view.findViewById(R.id.person_image);
            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView mobileNumberTextView = view.findViewById(R.id.mobile_number);
            TextView transferNoteTextView = view.findViewById(R.id.transfer_note);
            EditText transferPurposeEditText = view.findViewById(R.id.transfer_purpose);

            // Load the image using Glide from the URL
            Glide.with(getContext())
                    .load(personImageUrl)  // Load the image from Firebase Storage
                    .placeholder(R.drawable.person)  // Optional placeholder image
                    .into(personImageView);
            personNameTextView.setText(personName);
            mobileNumberTextView.setText(personMobileNumber);
            transferNoteTextView.setText(String.format("You can transfer up to RM %.2f", walletAmt));

            // Add TextWatcher to restrict input to two decimal places
            inputAmountEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No need to do anything before the text is changed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = s.toString();

                    // If the input contains a decimal point, check the number of decimal places
                    if (input.contains(".")) {
                        int decimalIndex = input.indexOf(".");

                        // If more than two decimal places are entered, truncate the input
                        if (input.length() - decimalIndex > 3) {
                            inputAmountEditText.setText(input.substring(0, decimalIndex + 3));
                            inputAmountEditText.setSelection(inputAmountEditText.getText().length());
                        }
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    // No need to do anything after the text is changed
                }
            });

            // Set up a click listener for proceeding to TransferDoneFragment
            Button transferButton = view.findViewById(R.id.transfer_button);
            transferButton.setOnClickListener(v -> {
                // Get the values entered by the user
                String amountStr = inputAmountEditText.getText().toString().trim();
                String transferPurpose = transferPurposeEditText.getText().toString().trim();

                // Validation: Check if the amount is empty
                if (amountStr.isEmpty()) {
                    inputAmountEditText.setError("Amount cannot be empty");
                    inputAmountEditText.requestFocus();
                    return;
                }

                // Convert amount to double for comparison
                double amount = Double.parseDouble(amountStr);

                // Validation: Check if the amount exceeds wallet balance
                if (amount > walletAmt) {
                    inputAmountEditText.setError("Amount exceeds wallet balance");
                    inputAmountEditText.requestFocus();
                    return;
                }

                // Validation: Check if transfer purpose is empty
                if (transferPurpose.isEmpty()) {
                    transferPurpose = "Fund Transfer"; // Set to default value if empty
                }

                // Create a new bundle to pass the data to TransferDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("amount", amountStr);
                bundle.putString("personImageUrl", personImageUrl);
                bundle.putString("personName", personName);
                bundle.putString("personMobileNumber", personMobileNumber);
                bundle.putString("personId", personId);
                bundle.putString("transferPurpose", transferPurpose);

                // Create TransferDoneFragment instance and pass the arguments
                TransferDoneFragment transferDoneFragment = new TransferDoneFragment();
                transferDoneFragment.setArguments(bundle);

                // Replace the current fragment with TransferDoneFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, transferDoneFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    // Hide Toolbar and BottomAppBar when this fragment is visible
    @Override
    public void onResume() {
        super.onResume();

        // Hide the Toolbar
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        // Hide the BottomAppBar
        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.GONE);
        }

        // Hide the FAB
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.hide();  // Hide FAB using the hide method
        }
    }

    // Show Toolbar and BottomAppBar when leaving this fragment
    @Override
    public void onPause() {
        super.onPause();

        // Show the Toolbar again when leaving this fragment
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }

        // Show the BottomAppBar again
        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }

        // Show the FAB again
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.show();  // Show FAB using the show method
        }
    }
}
