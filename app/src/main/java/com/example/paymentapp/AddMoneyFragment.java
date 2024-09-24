package com.example.paymentapp;

import android.os.Bundle;
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

public class AddMoneyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_money, container, false);

        // Retrieve the amount from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String amount = arguments.getString("amount", "0");

            // Display the amount in a TextView
            TextView totalAmount = view.findViewById(R.id.total_amount);
            totalAmount.setText(amount);

            // Retrieve bank details from the layout
            ImageView bankImage = view.findViewById(R.id.bank_image);
            TextView bankName = view.findViewById(R.id.bank_name);

            // Get the drawable resource ID for the bank image
            int bankImageResId = R.drawable.maybank;

            // Set up a click listener for proceeding to ReloadDoneFragment
            Button payButton = view.findViewById(R.id.pay_button);
            payButton.setOnClickListener(v -> {
                // Create a new bundle to pass the data to ReloadDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("amount", amount);
                bundle.putString("bank_name", bankName.getText().toString());
                bundle.putInt("bank_image", bankImageResId);

                // Create ReloadDoneFragment instance and pass the arguments
                ReloadDoneFragment reloadDoneFragment = new ReloadDoneFragment();
                reloadDoneFragment.setArguments(bundle);

                // Replace the current fragment with ReloadDoneFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, reloadDoneFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Find the back button
        ImageView backButton = view.findViewById(R.id.back_button);

        // Set a click listener on the back button to navigate back to ReloadFragment
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
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
