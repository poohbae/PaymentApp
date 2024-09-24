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

            // Now you can use the amount as needed, for example, display it in a TextView
            TextView totalAmount = view.findViewById(R.id.total_amount); // Ensure this TextView is in your layout
            totalAmount.setText(amount);

            // Set up a click listener for proceeding to ReloadDoneFragment
            Button payButton = view.findViewById(R.id.pay_button);  // Assuming there's a proceed button in the layout
            payButton.setOnClickListener(v -> {
                // Create a new bundle to pass the same amount to ReloadDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("amount", amount);

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
        backButton.setOnClickListener(v -> {
            // Go back to ReloadFragment using popBackStack, which preserves its state
            getParentFragmentManager().popBackStack();
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