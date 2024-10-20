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

public class RequestConfirmFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_confirm, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String userImageUrl = arguments.getString("userImageUrl");
            String amountStr = arguments.getString("amount");
            String personImageUrl = arguments.getString("personImageUrl");
            String personName = arguments.getString("personName");
            String personMobileNumber = arguments.getString("personMobileNumber");

            TextView amountTextView = view.findViewById(R.id.total_amount);
            ImageView personImageView = view.findViewById(R.id.person_image);
            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView mobileNumberTextView = view.findViewById(R.id.mobile_number);
            EditText noteEditText = view.findViewById(R.id.note);

            double amount = Double.parseDouble(amountStr);
            amountTextView.setText(String.format("RM %.2f", amount));

            Glide.with(getContext())
                    .load(personImageUrl)  // Load the image from Firebase Storage
                    .placeholder(R.drawable.person)  // Optional placeholder image
                    .into(personImageView);
            personNameTextView.setText(personName);
            mobileNumberTextView.setText(personMobileNumber);

            Button requestButton = view.findViewById(R.id.request_button);
            requestButton.setOnClickListener(v -> {
                String note = noteEditText.getText().toString().trim();

                // Validation: Check if note is empty
                if (note.trim().isEmpty()) {
                    note = "N/A";
                }

                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("amount", amountStr);
                bundle.putString("personImageUrl", personImageUrl);
                bundle.putString("personName", personName);
                bundle.putString("personMobileNumber", personMobileNumber);
                bundle.putString("note", note);

                RequestDoneFragment requestDoneFragment = new RequestDoneFragment();
                requestDoneFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, requestDoneFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
        return view;
    }

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
