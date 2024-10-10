package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransferFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView balanceAmountTextView = view.findViewById(R.id.balance_amount);
        if (getArguments() != null) {
            double walletAmt = getArguments().getDouble("walletAmt", 0.0);
            balanceAmountTextView.setText(String.format("RM %.2f", walletAmt));
        }

        // Find the CardView by ID
        CardView person = view.findViewById(R.id.person);

        // Find the TextView inside the CardView
        ImageView personImageView = view.findViewById(R.id.person_image);
        personImageView.setTag(R.drawable.poh_zi_jun);
        TextView personNameTextView = view.findViewById(R.id.person_name);
        TextView phoneNumberTextView = view.findViewById(R.id.phone_number);

        // Set click listener for the CardView
        person.setOnClickListener(v -> {
            int personImageResId = (int) personImageView.getTag();
            String personName = personNameTextView.getText().toString();
            String phoneNumber = phoneNumberTextView.getText().toString();

            // Create a new instance of TransferMoneyFragment
            TransferMoneyFragment transferMoneyFragment = new TransferMoneyFragment();

            // Create a bundle to pass the name
            Bundle bundle = new Bundle();
            bundle.putInt("person_image_res", personImageResId);
            bundle.putString("person_name", personName);
            bundle.putString("phone_number", phoneNumber);
            transferMoneyFragment.setArguments(bundle);

            // Navigate to TransferMoneyFragment with the bundle
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, transferMoneyFragment)
                    .addToBackStack(null)
                    .commit();
        });

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
            fab.hide();  // Hide FAB using the hide method
        }
    }

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
            fab.show();  // Show FAB using the show method
        }
    }
}
