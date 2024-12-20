package com.example.paymentapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SelectPaymentMethodFragment extends Fragment {

    String userId;
    Double walletAmt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_payment_method, container, false);

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
        }

        fetchWalletAmount(); // Fetch the wallet amount from Firebase

        // Set up the back button to navigate to HomeFragment
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, homeFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set up Split Bill button with click listener to show bottom dialog
        CardView splitBillButton = view.findViewById(R.id.split_bill);
        splitBillButton.setOnClickListener(v -> showBottomDialog());

        // Set up Select and Pay button with click listener to navigate to SelectPayFragment
        CardView selectAndPayButton = view.findViewById(R.id.select_pay);
        selectAndPayButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            bundle.putDouble("walletAmt", walletAmt);

            SelectPayFragment selectPayFragment = new SelectPayFragment();
            selectPayFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, selectPayFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    // Fetch wallet amount from Firebase for the specified user
    private void fetchWalletAmount() {
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("Wallets");

        String walletId = "W" + userId;

        walletsRef.child(walletId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                walletAmt = task.getResult().child("walletAmt").getValue(Double.class);
            } else {
                Log.e("SelectPaymentMethodFragment", "No wallet found for walletId: " + walletId);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve wallet amount.", Toast.LENGTH_SHORT).show();
        });
    }

    // Show bottom dialog for setting the number of people to split the bill with
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.split_bill_dialog);

        Button minusButton = dialog.findViewById(R.id.minus_button);
        Button plusButton = dialog.findViewById(R.id.plus_button);
        Button confirmButton = dialog.findViewById(R.id.confirm_button);
        EditText quantityEditText = dialog.findViewById(R.id.quantity);

        // Set default value to avoid empty EditText issues
        quantityEditText.setText("2");

        // Handle the minus button click to decrease the quantity
        minusButton.setOnClickListener(v -> {
            String quantityText = quantityEditText.getText().toString();
            int quantity = quantityText.isEmpty() ? 2 : Integer.parseInt(quantityText);
            if (quantity > 2) {
                quantity--;
                quantityEditText.setText(String.valueOf(quantity));
            }
        });

        // Add TextWatcher to ensure quantity EditText does not become empty
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed here
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Ensure the EditText does not become empty
                if (s.toString().isEmpty()) {
                    quantityEditText.setText("2"); // Set a default value if empty
                }
            }
        });

        // Handle the plus button click to increase the quantity
        plusButton.setOnClickListener(v -> {
            String quantityText = quantityEditText.getText().toString();
            int quantity = quantityText.isEmpty() ? 2 : Integer.parseInt(quantityText);
            if (quantity < 20) {
                quantity++;
                quantityEditText.setText(String.valueOf(quantity));
            }
        });

        // Confirm button to save the selected quantity and navigate to SplitBillFragment
        confirmButton.setOnClickListener(v -> {
            String quantityText = quantityEditText.getText().toString();
            int quantity = quantityText.isEmpty() ? 2 : Integer.parseInt(quantityText);

            dialog.dismiss();

            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            bundle.putDouble("walletAmt", walletAmt);
            bundle.putInt("quantity", quantity);

            SplitBillFragment splitBillFragment = new SplitBillFragment();
            splitBillFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, splitBillFragment)
                    .addToBackStack(null)
                    .commit();
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
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