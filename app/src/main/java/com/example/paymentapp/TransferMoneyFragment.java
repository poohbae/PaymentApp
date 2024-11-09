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

        // Back button to navigate back to the previous fragment
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Retrieve data from arguments
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

            // Load and display the person's image using Glide
            Glide.with(getContext())
                    .load(personImageUrl)  // URL of the person’s image
                    .placeholder(R.drawable.person)  // Placeholder image if loading fails
                    .into(personImageView);
            personNameTextView.setText(personName);
            mobileNumberTextView.setText(personMobileNumber);
            transferNoteTextView.setText(String.format("You can transfer up to RM %.2f", walletAmt));

            // Add TextWatcher to restrict input to two decimal places
            inputAmountEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No action needed before text change
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = s.toString();

                    // Limit to two decimal places
                    if (input.contains(".")) {
                        int decimalIndex = input.indexOf(".");
                        if (input.length() - decimalIndex > 3) {
                            inputAmountEditText.setText(input.substring(0, decimalIndex + 3));
                            inputAmountEditText.setSelection(inputAmountEditText.getText().length());
                        }
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    // No action needed after text change
                }
            });

            // Set up a click listener for proceeding to TransferDoneFragment
            Button transferButton = view.findViewById(R.id.transfer_button);
            transferButton.setOnClickListener(v -> {
                // Retrieve user-entered amount and purpose
                String amountStr = inputAmountEditText.getText().toString().trim();
                String transferPurpose = transferPurposeEditText.getText().toString().trim();

                // Validation: Check if the amount field is empty
                if (amountStr.isEmpty()) {
                    inputAmountEditText.setError("Amount cannot be empty");
                    inputAmountEditText.requestFocus();
                    return;
                }

                // Validation: Ensure the entered amount does not exceed wallet balance
                double amount = Double.parseDouble(amountStr);
                if (amount > walletAmt) {
                    inputAmountEditText.setError("Amount exceeds wallet balance");
                    inputAmountEditText.requestFocus();
                    return;
                }

                // Set default purpose if none is provided
                if (transferPurpose.isEmpty()) {
                    transferPurpose = "Fund Transfer";
                }

                // Pass data to TransferDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("amount", amountStr);
                bundle.putString("personImageUrl", personImageUrl);
                bundle.putString("personName", personName);
                bundle.putString("personMobileNumber", personMobileNumber);
                bundle.putString("personId", personId);
                bundle.putString("transferPurpose", transferPurpose);

                TransferDoneFragment transferDoneFragment = new TransferDoneFragment();
                transferDoneFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, transferDoneFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
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
