package com.example.paymentapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SplitBillFragment extends Fragment {

    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_split_bill, container, false);


        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            int quantity = arguments.getInt("quantity", 1); // Retrieve quantity, default to 1 if not found

            TextView splitIntoTextView = view.findViewById(R.id.split_into);
            String splitText = getString(R.string.split_into) + " 1/" + quantity;
            splitIntoTextView.setText(splitText);
        }

        TextView uncheckTextView = view.findViewById(R.id.uncheck);
        uncheckTextView.setOnClickListener(v -> uncheckAllCheckboxes(view));

        Button payButton = view.findViewById(R.id.pay_button);
        payButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            SplitBillDoneFragment splitBillDoneFragment = new SplitBillDoneFragment();
            splitBillDoneFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, splitBillDoneFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void uncheckAllCheckboxes(View view) {
        // Find the LinearLayout containing the CheckBoxes
        LinearLayout layout = view.findViewById(R.id.checkbox_layout);

        if (layout != null) {
            // Loop through all views within the layout
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);

                // Check if this view is a CheckBox and uncheck it if true
                if (child instanceof CheckBox) {
                    ((CheckBox) child).setChecked(false);
                }
                // If the CheckBox is nested within another layout, we need to search within that layout
                else if (child instanceof LinearLayout) {
                    LinearLayout innerLayout = (LinearLayout) child;
                    for (int j = 0; j < innerLayout.getChildCount(); j++) {
                        View innerChild = innerLayout.getChildAt(j);
                        if (innerChild instanceof CheckBox) {
                            ((CheckBox) innerChild).setChecked(false);
                        }
                    }
                }
            }
        }
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