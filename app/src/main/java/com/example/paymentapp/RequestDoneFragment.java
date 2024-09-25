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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class RequestDoneFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_done, container, false);

        ImageView closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        TextView dateTimeTextView = view.findViewById(R.id.date_time);
        String dateTime = new SimpleDateFormat("dd MMM yyyy, hh:mma")
                .format(Calendar.getInstance().getTime())
                .replace("AM", "am").replace("PM", "pm");
        dateTimeTextView.setText(dateTime);

        // Get the passed arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String personName = arguments.getString("person_name");
            String phoneNumber = arguments.getString("phone_number");
            String amount = arguments.getString("amount");
            String note = arguments.getString("note");

            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView phoneNumberTextView = view.findViewById(R.id.phone_number);
            TextView amountTextView = view.findViewById(R.id.total_amount);
            TextView noteTextView = view.findViewById(R.id.note);

            personNameTextView.setText(personName);
            phoneNumberTextView.setText(phoneNumber);
            amountTextView.setText("RM " + amount);
            noteTextView.setText(note);
        }

        Button doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, new HomeFragment())
                .addToBackStack(null)
                .commit());

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