package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ReloadFragment extends Fragment {

    private EditText inputAmount;
    private Button rm100Button, rm200Button, rm300Button, rm500Button, payNowButton;
    private String selectedAmount = "";
    private TextView topUpAmountTextView, totalAmountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reload, container, false);

        // Initialize views
        inputAmount = view.findViewById(R.id.input_amount);
        rm100Button = view.findViewById(R.id.rm100_button);
        rm200Button = view.findViewById(R.id.rm200_button);
        rm300Button = view.findViewById(R.id.rm300_button);
        rm500Button = view.findViewById(R.id.rm500_button);
        payNowButton = view.findViewById(R.id.pay_now_button);

        // Bind top_up_amount and total_amount TextViews
        topUpAmountTextView = view.findViewById(R.id.top_up_amount);  // Make sure the IDs are correct
        totalAmountTextView = view.findViewById(R.id.total_amount);

        // Set default values for top_up_amount and total_amount
        updateAmount("0");

        // Find the back button
        ImageView backButton = view.findViewById(R.id.back_button);

        // Set a click listener on the back button to navigate back to ReloadFragment
        backButton.setOnClickListener(v -> {
            // Go back to HomeFragment using popBackStack, which preserves its state
            getParentFragmentManager().popBackStack();
        });

        // Set button click listeners to update selectedAmount
        rm100Button.setOnClickListener(v -> {
            selectedAmount = "100";
            inputAmount.setText("100");  // Clear the EditText to prioritize button selection
            updateAmount("100");         // Update the displayed amounts
        });

        rm200Button.setOnClickListener(v -> {
            selectedAmount = "200";
            inputAmount.setText("200");
            updateAmount("200");
        });

        rm300Button.setOnClickListener(v -> {
            selectedAmount = "300";
            inputAmount.setText("300");
            updateAmount("300");
        });

        rm500Button.setOnClickListener(v -> {
            selectedAmount = "500";
            inputAmount.setText("500");
            updateAmount("500");
        });

        // Pay button click listener
        payNowButton.setOnClickListener(v -> {
            // Check if the EditText has a manually entered amount
            String manualAmount = inputAmount.getText().toString().trim();
            String amountToSend;

            // Prioritize the manually entered amount if it's not empty
            if (!manualAmount.isEmpty()) {
                amountToSend = manualAmount;
                updateAmount(manualAmount);  // Update the displayed amounts with manual input
            } else if (!selectedAmount.isEmpty()) {
                amountToSend = selectedAmount;
            } else {
                // If no amount is entered or selected, show an error message on the EditText
                inputAmount.setError("Please enter or select an amount");
                inputAmount.requestFocus();  // Focus the EditText to prompt user to correct it
                return;
            }

            // Pass the amount to AddMoneyFragment
            Bundle bundle = new Bundle();
            bundle.putString("amount", amountToSend);

            AddMoneyFragment addMoneyFragment = new AddMoneyFragment();
            addMoneyFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, addMoneyFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    // Helper method to update the top_up_amount and total_amount TextViews
    private void updateAmount(String amount) {
        // Set the same amount for both top_up_amount and total_amount
        topUpAmountTextView.setText("RM " + amount);
        totalAmountTextView.setText("RM " + amount);
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
