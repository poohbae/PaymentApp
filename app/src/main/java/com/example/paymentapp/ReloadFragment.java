package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
        });

        rm200Button.setOnClickListener(v -> {
            selectedAmount = "200";
            inputAmount.setText("200");  // Clear the EditText to prioritize button selection
        });

        rm300Button.setOnClickListener(v -> {
            selectedAmount = "300";
            inputAmount.setText("300");  // Clear the EditText to prioritize button selection
        });

        rm500Button.setOnClickListener(v -> {
            selectedAmount = "500";
            inputAmount.setText("500");  // Clear the EditText to prioritize button selection
        });

        // Pay button click listener
        payNowButton.setOnClickListener(v -> {
            // Check if the EditText has a manually entered amount
            String manualAmount = inputAmount.getText().toString().trim();
            String amountToSend;

            // Prioritize the manually entered amount if it's not empty
            if (!manualAmount.isEmpty()) {
                amountToSend = manualAmount;
            } else if (!selectedAmount.isEmpty()) {
                amountToSend = selectedAmount;
            } else {
                // If no amount is entered or selected, show a message and stop the action
                Toast.makeText(getActivity(), "Please enter or select an amount", Toast.LENGTH_SHORT).show();
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
