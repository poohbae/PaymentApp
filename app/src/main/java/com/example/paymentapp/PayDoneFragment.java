package com.example.paymentapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class PayDoneFragment extends Fragment {

    private static final String CHANNEL_ID = "pay_notification_channel";
    private static final int PAY_NOTIFICATION_PERMISSION = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pay_done, container, false);

        // Retrieve data passed from the previous fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String amount = arguments.getString("amount", "0");
            String dateTime = getCurrentDateTime();
            int platformImageResId = arguments.getInt("platformImageRes", -1);
            String platformName = arguments.getString("platformName", "Default Platform");

            TextView totalAmountTextView = view.findViewById(R.id.total_amount);
            TextView totalAmount2TextView = view.findViewById(R.id.total_amount2);
            TextView dateTimeTextView = view.findViewById(R.id.date_time);
            ImageView platformImageView = view.findViewById(R.id.platform_image);
            TextView platformNameTextView = view.findViewById(R.id.platform_name);

            double amountValue = Double.parseDouble(amount);
            totalAmountTextView.setText(String.format("RM %.2f", amountValue));
            totalAmount2TextView.setText(String.format("RM %.2f", amountValue));
            dateTimeTextView.setText(dateTime);
            platformImageView.setImageResource(platformImageResId);
            platformNameTextView.setText(platformName);

            // Generate and display a random reference ID
            TextView referenceIdTextView = view.findViewById(R.id.reference_id);
            Random random = new Random();
            long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            String referenceId = String.valueOf(referenceNumber);
            referenceIdTextView.setText(referenceId);

            // Set up the "OK" button to update the wallet and save the transaction
            Button okButton = view.findViewById(R.id.ok_button);
            okButton.setOnClickListener(v -> {
                // Reference the user's wallet in Firebase
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId); // Removed extra "W"

                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Retrieve current wallet amount
                            double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                            double payAmt = Double.parseDouble(amount);
                            double updatedWalletAmt = currentWalletAmt - payAmt;

                            // Update the wallet amount in Firebase
                            walletRef.child("walletAmt").setValue(updatedWalletAmt).addOnCompleteListener(taskUpdate -> {
                                if (taskUpdate.isSuccessful()) {
                                    // Save the transaction to transaction history in Firebase
                                    DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                                    String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                                    // Create a Transaction object to save in the database
                                    Transaction transaction = new Transaction(transactionId, platformImageResId, dateTime, "Pay", referenceId, payAmt);
                                    transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("Transaction", "Transaction saved successfully");
                                            showPayNotification(payAmt, platformName, dateTime); // Show notification for payment completion
                                            navigateToHomeFragment(userId); // Navigate back to the home fragment
                                        } else {
                                            Log.e("Transaction", "Failed to save transaction", task1.getException());
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            });
        }
        return view;
    }

    // Displays a notification for the completed payment
    private void showPayNotification(double amount, String platformName, String dateTime) {
        createNotificationChannel();  // Create notification channel for Android 8.0+

        // Build notification content
        @SuppressLint("DefaultLocale") String notificationContent = String.format("You have successfully paid RM %.2f for %s on %s", amount, platformName, dateTime);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("Pay Completed Successfully")
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Check and request notification permission if not granted
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PAY_NOTIFICATION_PERMISSION);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Creates a notification channel for payment notifications (required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pay Notification";
            String description = "Notification sent after a pay action has been completed";
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

    // Navigates back to the HomeFragment after payment completion
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
