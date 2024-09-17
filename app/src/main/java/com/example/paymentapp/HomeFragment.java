package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the "See All" button in the inflated layout
        TextView seeAllButton = view.findViewById(R.id.see_all_button);

        // Set a click listener on the "See All" button
        seeAllButton.setOnClickListener(v -> {
            // Handle the click event here
            Toast.makeText(getActivity(), "See All Clicked", Toast.LENGTH_SHORT).show();
        });

        // Find the parent LinearLayout where transaction items will be added
        LinearLayout transactionList = view.findViewById(R.id.transaction_list);

        // Create five transaction items dynamically
        addTransactionItem(transactionList, "1 Aug 2024", "Netflix", "Auto Transfer", "- RM11.00", getActivity());
        addTransactionItem(transactionList, "2 Aug 2024", "Spotify", "Auto Transfer", "- RM15.00", getActivity());
        addTransactionItem(transactionList, "3 Aug 2024", "Apple Music", "Auto Transfer", "- RM20.00", getActivity());
        addTransactionItem(transactionList, "4 Aug 2024", "HBO Max", "Auto Transfer", "- RM25.00", getActivity());
        addTransactionItem(transactionList, "5 Aug 2024", "Disney+", "Auto Transfer", "- RM30.00", getActivity());

        return view;
    }

    // Helper method to add transaction items dynamically
    private void addTransactionItem(LinearLayout parent, String date, String label, String source, String amount, Context context) {
        // Create the main container for the transaction item
        LinearLayout transactionItem = new LinearLayout(context);
        transactionItem.setOrientation(LinearLayout.HORIZONTAL);
        transactionItem.setPadding(8, 8, 8, dpToPx(context, 15)); // 8dp padding and 15dp bottom padding
        transactionItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
        transactionItem.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create the ImageView for the transaction icon
        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(context, 40), dpToPx(context, 40)); // Consistent 40dp width and height
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.add); // Replace with your icon
        transactionItem.addView(icon);

        // Create the LinearLayout for the text content (date, label, source)
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1); // Weight of 1
        textContainer.setLayoutParams(textContainerParams);
        textContainer.setPadding(dpToPx(context, 8), 0, 0, 0); // 8dp paddingStart

        // Create a TextView for the transaction date
        TextView transactionDate = new TextView(context);
        transactionDate.setText(date);
        transactionDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 16sp text size
        textContainer.addView(transactionDate);

        // Create a TextView for the transaction label
        TextView transactionLabel = new TextView(context);
        transactionLabel.setText(label);
        transactionLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 16sp text size
        transactionLabel.setTypeface(null, android.graphics.Typeface.BOLD); // Bold
        textContainer.addView(transactionLabel);

        // Create a TextView for the transaction source
        TextView transactionSource = new TextView(context);
        transactionSource.setText(source);
        transactionSource.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); // 14sp text size
        textContainer.addView(transactionSource);

        // Add the text container to the transaction item
        transactionItem.addView(textContainer);

        // Create a TextView for the transaction amount
        TextView transactionAmount = new TextView(context);
        transactionAmount.setText(amount);
        transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 16sp text size
        transactionAmount.setTextColor(Color.parseColor("#D32F2F")); // Red color for the amount
        transactionItem.addView(transactionAmount);

        // Add the entire transaction item to the parent layout
        parent.addView(transactionItem);
    }

    // Helper method to convert dp to pixels
    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
