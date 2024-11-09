package com.example.paymentapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SplitBillDoneFragment extends Fragment {

    private static final String CHANNEL_ID = "split_bill_notification_channel";
    private static final int SPLIT_BILL_NOTIFICATION_PERMISSION = 101;

    String userId;
    double finalRoundedSplitPrice;
    int quantity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_split_bill_done, container, false);

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            String billNo = arguments.getString("billNo");
            quantity = arguments.getInt("quantity", 1);
            finalRoundedSplitPrice = arguments.getDouble("finalRoundedSplitPrice", 0.0);

            Button backToHomeButton = view.findViewById(R.id.back_button);
            backToHomeButton.setOnClickListener(v -> {
                // Show a notification after payment and navigate to the home fragment
                showSplitBillNotification(finalRoundedSplitPrice, billNo, getCurrentDateTime());
                navigateToHomeFragment(userId);
            });
        }

        // Add guest views based on the quantity
        LinearLayout guestContainer = view.findViewById(R.id.guest_container);
        for (int i = 0; i < quantity - 1; i++) {
            String guestName = "Guest " + (i + 1);

            // Create and add guest view for each guest
            View guestView = createGuestView(guestName, "guest", finalRoundedSplitPrice);
            guestContainer.addView(guestView);
        }

        return view;
    }

    // Create a view for each guest, showing their name and split bill amount
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

    // Show a notification after a successful "Split Bill" action
    private void showSplitBillNotification(double amount, String billNo, String dateTime) {
        createNotificationChannel();  // Create notification channel for Android 8.0+

        // Build the notification
        @SuppressLint("DefaultLocale") String notificationContent = String.format("You have successfully paid RM %.2f for Bill No: #%s on %s", amount, billNo, dateTime);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("Split Bill Completed Successfully")
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Check and request notification permission if not granted
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, SPLIT_BILL_NOTIFICATION_PERMISSION);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Creates a notification channel for payment notifications (required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Split Bill Notification";
            String description = "Notifications for completed split bill action";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Navigate back to the HomeFragment
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

    // Returns the current date and time formatted as "dd MMM yyyy, hh:mma"
    private String getCurrentDateTime() {
        return new SimpleDateFormat("dd MMM yyyy, hh:mma", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    // Converts a value in dp to px based on the device's screen density
    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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