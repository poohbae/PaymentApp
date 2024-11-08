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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SelectPayDoneFragment extends Fragment {

    private static final String CHANNEL_ID = "select_pay_notification_channel";
    private static final int SELECT_PAY_NOTIFICATION_PERMISSION = 101;

    String userId;

    private DatabaseReference ordersRef;
    private LinearLayout unpaidItemsContainer;
    private TextView unpaidItemLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_pay_done, container, false);

        unpaidItemsContainer = view.findViewById(R.id.unpaid_item_container);
        unpaidItemLabel = view.findViewById(R.id.unpaid_item_label);

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            String billNo = arguments.getString("billNo");
            double totalChecked = arguments.getDouble("totalChecked");

            Button backToHomeButton = view.findViewById(R.id.back_button);
            backToHomeButton.setOnClickListener(v -> {
                showSelectPayNotification(totalChecked, billNo,  getCurrentDateTime());
                navigateToHomeFragment(userId);
            });
        }

        // Initialize Firebase Database reference
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        // Load unpaid orders
        loadUnpaidOrders();

        return view;
    }

    private void loadUnpaidOrders() {
        ordersRef.orderByChild("status").equalTo(0).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unpaidItemsContainer.removeAllViews(); // Clear previous views

                if (dataSnapshot.exists()) {
                    unpaidItemLabel.setText("Unpaid Items"); // Reset label to "Unpaid Item"

                    // Loop through each unpaid order and add to the container
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        String name = orderSnapshot.child("name").getValue(String.class);
                        String image = orderSnapshot.child("image").getValue(String.class);
                        Double price = orderSnapshot.child("price").getValue(Double.class);

                        // Dynamically create a layout for each order item
                        View orderView = createOrderView(name, image, price);
                        unpaidItemsContainer.addView(orderView);
                    }
                } else {
                    // Update label to "No unpaid item" if no unpaid orders are found
                    unpaidItemLabel.setText("No unpaid item");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private View createOrderView(String name, String image, Double price) {
        Context context = getContext();

        LinearLayout itemLayout = new LinearLayout(context);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 0, 0, dpToPx(context, 15));
        itemLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);

        ImageView itemImageView = new ImageView(context);
        int imageResId = getResources().getIdentifier(image, "drawable", getActivity().getPackageName());
        itemImageView.setImageResource(imageResId);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(context, 50), dpToPx(context, 50));
        imageParams.setMargins(0, 0, dpToPx(context, 20), 0);
        itemImageView.setLayoutParams(imageParams);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textContainerParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        textContainer.setLayoutParams(textContainerParams);

        TextView itemNameTextView = new TextView(context);
        itemNameTextView.setText(name);
        itemNameTextView.setTextColor(getResources().getColor(R.color.black));
        itemNameTextView.setTextSize(18);
        itemNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        itemNameTextView.setPadding(0, 0, 0, dpToPx(context, 5));

        TextView itemPriceTextView = new TextView(context);
        itemPriceTextView.setText(String.format("RM %.2f", price));
        itemPriceTextView.setTextColor(getResources().getColor(R.color.black));
        itemPriceTextView.setTextSize(15);

        textContainer.addView(itemNameTextView);
        textContainer.addView(itemPriceTextView);

        itemLayout.addView(itemImageView);
        itemLayout.addView(textContainer);

        return itemLayout;
    }

    private void showSelectPayNotification(double amount, String billNo, String dateTime) {
        createNotificationChannel();  // Create notification channel for Android 8.0+

        // Build the notification with expanded content
        @SuppressLint("DefaultLocale") String notificationContent = String.format("You have successfully paid RM %.2f for Bill No: #%s on %s", amount, billNo, dateTime);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("Select and Pay Completed Successfully")
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))  // Expanded text style
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, SELECT_PAY_NOTIFICATION_PERMISSION);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Select and Pay Notification";
            String description = "Notifications for completed select and pay actions";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
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

    private String getCurrentDateTime() {
        return new SimpleDateFormat("dd MMM yyyy, hh:mma", Locale.getDefault()).format(Calendar.getInstance().getTime());
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