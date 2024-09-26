package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RequestConfirmFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_confirm, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Retrieve the person name from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String amount = arguments.getString("amount");
            int personImageResId = arguments.getInt("person_image_res", -1);
            String personName = arguments.getString("person_name");
            String phoneNumber = arguments.getString("phone_number");

            TextView amountTextView = view.findViewById(R.id.total_amount);
            ImageView personImageView = view.findViewById(R.id.person_image);
            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView phoneNumberTextView = view.findViewById(R.id.phone_number);
            EditText noteEditText = view.findViewById(R.id.note);

            amountTextView.setText("RM " + amount);
            personImageView.setImageResource(personImageResId);
            personNameTextView.setText(personName);
            phoneNumberTextView.setText(phoneNumber);

            // Set up a click listener for proceeding to RequestDoneFragment
            Button requestButton = view.findViewById(R.id.request_button);
            requestButton.setOnClickListener(v -> {
                String note = noteEditText.getText().toString().trim();

                // Validation: Check if note is empty
                if (note.isEmpty()) {
                    note = "-"; // Set to default value if empty
                }

                // Create a new bundle to pass the data to RequestDoneFragment
                Bundle bundle = new Bundle();
                bundle.putString("amount", amount);
                bundle.putString("person_name", personName);
                bundle.putString("phone_number", phoneNumber);
                bundle.putString("note", note);

                // Create RequestDoneFragment instance and pass the arguments
                RequestDoneFragment requestDoneFragment = new RequestDoneFragment();
                requestDoneFragment.setArguments(bundle);

                // Replace the current fragment with RequestDoneFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, requestDoneFragment)
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
