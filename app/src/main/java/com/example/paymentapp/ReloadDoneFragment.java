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

public class ReloadDoneFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reload_done, container, false);

        // Retrieve the data from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String amount = arguments.getString("amount", "0");
            int bankImageResId = arguments.getInt("bank_image_res", -1);
            String bankName = arguments.getString("bank_name", "Default Bank");

            // Set the amount to the TextViews
            TextView totalAmount = view.findViewById(R.id.total_amount);
            TextView totalAmount2 = view.findViewById(R.id.total_amount2);
            ImageView bankImageView = view.findViewById(R.id.bank_image);
            TextView bankNameTextView = view.findViewById(R.id.bank_name);

            totalAmount.setText("RM " + amount);
            totalAmount2.setText("RM " + amount);
            bankImageView.setImageResource(bankImageResId);
            bankNameTextView.setText(bankName);
        }

        // Set the current date and time
        TextView dateTimeTextView = view.findViewById(R.id.date_time);
        String dateTime = new SimpleDateFormat("dd MMM yyyy, hh:mma")
                .format(Calendar.getInstance().getTime())
                .replace("AM", "am").replace("PM", "pm");
        dateTimeTextView.setText(dateTime);

        // Generate and set a random reference ID
        TextView referenceId = view.findViewById(R.id.reference_id);
        Random random = new Random();
        long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
        referenceId.setText(String.valueOf(referenceNumber));

        Button okButton = view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
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
