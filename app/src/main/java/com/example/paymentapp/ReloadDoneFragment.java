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

        // Find the close button (replacing back button with close button)
        ImageView closeButton = view.findViewById(R.id.close_button);

        // Set a click listener on the close button to navigate to HomeFragment
        closeButton.setOnClickListener(v -> {
            // Navigate to HomeFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Retrieve the amount from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String amount = arguments.getString("amount", "0");

            // Set amount to the first TextView (total_amount)
            TextView totalAmount = view.findViewById(R.id.total_amount); // Ensure this TextView is in your layout
            totalAmount.setText("RM " + amount);

            // Set amount to the second TextView (total_amount2)
            TextView totalAmount2 = view.findViewById(R.id.total_amount2); // Ensure this TextView is also in your layout
            totalAmount2.setText("RM " + amount);
        }

        TextView dateTime = view.findViewById(R.id.date_time);

        // Get current date and time in the desired format directly
        String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mma")
                .format(Calendar.getInstance().getTime())
                .replace("AM", "am").replace("PM", "pm");

        // Set the formatted date and time to the TextView
        dateTime.setText(formattedDate);

        TextView referenceId = view.findViewById(R.id.reference_id);

        // Generate a random 10-digit number
        Random random = new Random();
        long referenceNumber = 1000000000L + (long)(random.nextDouble() * 9000000000L);

        // Set the generated reference ID to the TextView
        referenceId.setText(String.valueOf(referenceNumber));

        // Find the close button (replacing back button with close button)
        Button okButton = view.findViewById(R.id.ok_button);

        // Set a click listener on the close button to navigate to HomeFragment
        okButton.setOnClickListener(v -> {
            // Navigate to HomeFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new HomeFragment())
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
