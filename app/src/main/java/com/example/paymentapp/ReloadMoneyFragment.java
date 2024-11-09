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

public class ReloadMoneyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reload_money, container, false);

        // Back button to navigate back to the previous fragment
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView totalAmountTextView = view.findViewById(R.id.total_amount);
        ImageView bankImageView = view.findViewById(R.id.bank_image);
        TextView bankNameTextView = view.findViewById(R.id.bank_name);

        // Retrieve the arguments passed to this fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String amount = arguments.getString("amount", "0");
            int bankImageResId = arguments.getInt("bankImageRes", -1);
            String bankName = arguments.getString("bankName");

            // Display the amount and bank details
            double amountValue = Double.parseDouble(amount); // Convert String to double
            totalAmountTextView.setText(String.format("RM %.2f", amountValue));
            bankImageView.setImageResource(bankImageResId);
            bankNameTextView.setText(bankName);

            // Set up 'Pay' button click listener
            Button payButton = view.findViewById(R.id.pay_button);
            payButton.setOnClickListener(v -> {
                // Pass data to ReloadDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("amount", amount);
                bundle.putInt("bankImageRes", bankImageResId);
                bundle.putString("bankName", bankName);

                ReloadDoneFragment reloadDoneFragment = new ReloadDoneFragment();
                reloadDoneFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, reloadDoneFragment)
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
