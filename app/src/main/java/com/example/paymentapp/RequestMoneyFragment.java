package com.example.paymentapp;

import android.os.Bundle;
import android.util.Log;
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

public class RequestMoneyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_money, container, false);

        // Set up back button to return to previous fragment
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String userImageUrl = arguments.getString("userImageUrl");
            String personImageUrl = arguments.getString("personImageUrl");
            String personName = arguments.getString("personName");
            String personMobileNumber = arguments.getString("personMobileNumber");

            EditText inputAmountEditText = view.findViewById(R.id.input_amount);
            ImageView personImageView = view.findViewById(R.id.person_image);
            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView mobileNumberTextView = view.findViewById(R.id.mobile_number);

            // Load and display the person's image using Glide
            Glide.with(getContext())
                    .load(personImageUrl)  // URL of the person’s image
                    .placeholder(R.drawable.person)  // Placeholder image if loading fails
                    .into(personImageView);
            personNameTextView.setText(personName);
            mobileNumberTextView.setText(personMobileNumber);

            // Add TextWatcher to restrict input
            inputAmountEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No action needed before text change
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = s.toString();

                    // Limit the maximum input value to 10,000
                    double inputValue = Double.parseDouble(input);
                    if (inputValue > 10000) {
                        inputAmountEditText.setText(input.substring(0, start));  // Truncate to last valid point
                        inputAmountEditText.setSelection(inputAmountEditText.getText().length());  // Move cursor to the end
                        return;
                    }

                    // Limit decimal places to two digits
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

            // Set up click listener to navigate to RequestConfirmFragment
            Button requestButton = view.findViewById(R.id.request_button);
            requestButton.setOnClickListener(v -> {

                // Validate if the amount is empty
                String amountStr = inputAmountEditText.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    inputAmountEditText.setError("Amount cannot be empty");
                    inputAmountEditText.requestFocus();
                    return;
                }

                // Pass data to RequestConfirmFragment
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("amount", amountStr);
                bundle.putString("personImageUrl", personImageUrl);
                bundle.putString("personName", personName);
                bundle.putString("personMobileNumber", personMobileNumber);

                RequestConfirmFragment requestConfirmFragment = new RequestConfirmFragment();
                requestConfirmFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, requestConfirmFragment)
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
