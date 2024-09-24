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

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransferMoneyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer_money, container, false);

        // Find the back button
        ImageView backButton = view.findViewById(R.id.back_button);

        // Set a click listener on the back button to navigate back
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Find the views
        EditText inputAmountEditText = view.findViewById(R.id.input_amount);
        ImageView personImageView = view.findViewById(R.id.person_image);
        TextView personNameTextView = view.findViewById(R.id.person_name);
        EditText transferPurposeEditText = view.findViewById(R.id.transfer_purpose);

        // Retrieve the person name from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String personName = arguments.getString("person_name");
            personNameTextView.setText(personName);

            // Assuming person_image is a drawable resource, get its resource ID
            int personImageResId = R.drawable.poh_zi_jun;  // Replace with actual drawable

            // Set up a click listener for proceeding to TransferDoneFragment
            Button transferButton = view.findViewById(R.id.transfer_button);
            transferButton.setOnClickListener(v -> {
                // Get the values entered by the user
                String amount = inputAmountEditText.getText().toString().trim();
                String transferPurpose = transferPurposeEditText.getText().toString().trim();

                // Validation: Check if the amount is empty
                if (amount.isEmpty()) {
                    inputAmountEditText.setError("Amount cannot be empty");
                    inputAmountEditText.requestFocus();
                    return;
                }

                // Validation: Check if transfer purpose is empty
                if (transferPurpose.isEmpty()) {
                    transferPurpose = "Fund Transfer"; // Set to default value if empty
                }

                // Create a new bundle to pass the data to TransferDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("amount", amount);
                bundle.putInt("person_image", personImageResId);  // Pass the image resource ID
                bundle.putString("person_name", personName);
                bundle.putString("transfer_purpose", transferPurpose);

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
