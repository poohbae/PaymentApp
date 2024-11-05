package com.example.paymentapp;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SplitBillDoneFragment extends Fragment {

    String userId;
    double unitPrice;
    int quantity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_split_bill_done, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            quantity = arguments.getInt("quantity", 1);
            unitPrice = arguments.getDouble("unitPrice", 0.0);
        }

        LinearLayout guestContainer = view.findViewById(R.id.guest_container);
        for (int i = 0; i < quantity - 1; i++) {
            String guestName = "Guest " + (i + 1);

            View guestView = createGuestView(guestName, "guest", unitPrice);
            guestContainer.addView(guestView);
        }

        Button backToHomeButton = view.findViewById(R.id.back_button);
        backToHomeButton.setOnClickListener(v -> navigateToHomeFragment(userId));

        return view;
    }

    private View createGuestView(String name, String image, Double price) {
        Context context = getContext();

        LinearLayout itemLayout = new LinearLayout(context);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 0, 0, dpToPx(context, 15));
        itemLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);

        ImageView itemImageView = new ImageView(context);
        int imageResId = getResources().getIdentifier(image, "drawable", getActivity().getPackageName());
        itemImageView.setImageResource(imageResId);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(context, 50), dpToPx(context, 50));
        imageParams.setMargins(0, 0, dpToPx(context, 10), 0);
        itemImageView.setLayoutParams(imageParams);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textContainerParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        textContainer.setLayoutParams(textContainerParams);

        TextView itemNameTextView = new TextView(context);
        itemNameTextView.setText(name);
        itemNameTextView.setTextColor(getResources().getColor(R.color.black));
        itemNameTextView.setTextSize(18);
        itemNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        itemNameTextView.setLayoutParams(nameParams);

        TextView itemPriceTextView = new TextView(context);
        itemPriceTextView.setText(String.format("RM %.2f", price));
        itemPriceTextView.setTextColor(getResources().getColor(R.color.black));
        itemPriceTextView.setTextSize(15);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemPriceTextView.setLayoutParams(priceParams);

        textContainer.addView(itemNameTextView);
        textContainer.addView(itemPriceTextView);

        itemLayout.addView(itemImageView);
        itemLayout.addView(textContainer);

        return itemLayout;
    }

    private void navigateToHomeFragment(String userId) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        homeFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, homeFragment)
                .addToBackStack(null)
                .commit();
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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