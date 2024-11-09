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

public class ReloadDoneFragment extends Fragment {

    private static final String CHANNEL_ID = "reload_notification_channel";
    private static final int RELOAD_NOTIFICATION_PERMISSION = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reload_done, container, false);

        // Retrieve data from arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String amount = arguments.getString("amount", "0");
            String dateTime = getCurrentDateTime();
            int bankImageResId = arguments.getInt("bankImageRes", -1);
            String bankName = arguments.getString("bankName", "Default Bank");

            TextView totalAmountTextView = view.findViewById(R.id.total_amount);
            TextView totalAmount2TextView = view.findViewById(R.id.total_amount2);
            TextView dateTimeTextView = view.findViewById(R.id.date_time);
            ImageView bankImageView = view.findViewById(R.id.bank_image);
            TextView bankNameTextView = view.findViewById(R.id.bank_name);

            double amountValue = Double.parseDouble(amount);
            totalAmountTextView.setText(String.format("RM %.2f", amountValue));
            totalAmount2TextView.setText(String.format("RM %.2f", amountValue));
            dateTimeTextView.setText(dateTime);
            bankImageView.setImageResource(bankImageResId);
            bankNameTextView.setText(bankName);

            // Generate and display a random reference ID
            TextView referenceIdTextView = view.findViewById(R.id.reference_id);
            Random random = new Random();
            long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            String referenceId = String.valueOf(referenceNumber);
            referenceIdTextView.setText(referenceId);

            // Set up the "OK" button to update wallet balance and save transaction history
            Button okButton = view.findViewById(R.id.ok_button);
            okButton.setOnClickListener(v -> {
                // Reference user's wallet in Firebase
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId); // Removed extra "W"

                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Fetch current wallet amount and update with reload amount
                            double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                            double reloadAmt = Double.parseDouble(amount);
                            double updatedWalletAmt = currentWalletAmt + reloadAmt;

                            // Update wallet amount in Firebase
                            walletRef.child("walletAmt").setValue(updatedWalletAmt).addOnCompleteListener(taskUpdate -> {
                                if (taskUpdate.isSuccessful()) {
                                    // Save transaction details in transaction history
                                    DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                                    String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                                    // Create a Transaction object
                                    Transaction transaction = new Transaction(transactionId, bankImageResId, dateTime, "Reload", referenceId, reloadAmt);
                                    transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("Transaction", "Transaction saved successfully");
                                            showReloadNotification(reloadAmt, bankName, dateTime);
                                            navigateToHomeFragment(userId);
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

    // Displays a notification for the reload action completion
    private void showReloadNotification(double amount, String bankName, String dateTime) {
        createNotificationChannel();  // Create notification channel for Android 8.0+

        // Build notification content
        @SuppressLint("DefaultLocale") String notificationContent = String.format("You have successfully reloaded RM %.2f via %s on %s", amount, bankName, dateTime);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("Reload Completed Successfully")
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Check and request notification permission if not granted
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, RELOAD_NOTIFICATION_PERMISSION);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Creates a notification channel for reload notifications (required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reload Notification";
            String description = "Notification sent after a reload action has been completed";
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

    // Navigates to HomeFragment after reload completion
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
