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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class TransferDoneFragment extends Fragment {

    private static final String CHANNEL_ID = "transfer_notification_channel";
    private static final int TRANSFER_NOTIFICATION_PERMISSION = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer_done, container, false);

        // Get the passed arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String userId = arguments.getString("userId");
            String userImageUrl = arguments.getString("userImageUrl");
            String amount = arguments.getString("amount");
            String dateTime = getCurrentDateTime();
            String personImageUrl = arguments.getString("personImageUrl");
            String personName = arguments.getString("personName");
            String personMobileNumber = arguments.getString("personMobileNumber");
            String personId = arguments.getString("personId");
            String transferPurpose = arguments.getString("transferPurpose");

            // Set the values to TextViews
            TextView amountTextView = view.findViewById(R.id.total_amount);
            TextView dateTimeTextView = view.findViewById(R.id.date_time);
            ImageView personImageView = view.findViewById(R.id.person_image);
            TextView personNameTextView = view.findViewById(R.id.person_name);
            TextView transferPurposeTextView = view.findViewById(R.id.transfer_purpose);

            // Set the values retrieved from the bundle
            double amountValue = Double.parseDouble(amount); // Convert String to double
            amountTextView.setText(String.format("RM %.2f", amountValue));
            dateTimeTextView.setText(dateTime);
            Glide.with(getContext())
                    .load(personImageUrl)  // Load the image from Firebase Storage
                    .placeholder(R.drawable.person)  // Optional placeholder image
                    .into(personImageView);
            personNameTextView.setText(personName);
            transferPurposeTextView.setText(transferPurpose);

            TextView referenceIdTextView = view.findViewById(R.id.reference_id);
            Random random = new Random();
            long referenceNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            String referenceId = String.valueOf(referenceNumber);
            referenceIdTextView.setText(referenceId);

            Button okButton = view.findViewById(R.id.ok_button);
            okButton.setOnClickListener(v -> {
                // Fetch current wallet amount from Firebase and update
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId);

                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Fetch current wallet amount
                        Double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                        double transferAmt = Double.parseDouble(amount);

                        if (currentWalletAmt == null || currentWalletAmt < transferAmt) {
                            Toast.makeText(getContext(), "Insufficient wallet balance.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double updatedWalletAmt = currentWalletAmt - transferAmt;

                        // Update the wallet amount in Firebase
                        walletRef.child("walletAmt").setValue(updatedWalletAmt).addOnCompleteListener(taskUpdate -> {
                            if (taskUpdate.isSuccessful()) {
                                DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                                String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                                Transaction transaction = new Transaction(transactionId, personImageUrl, userImageUrl, dateTime, "Transfer", transferPurpose, referenceId, personMobileNumber, personId, transferAmt);
                                transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Now, update the recipient's wallet amount and add transaction to recipient's history
                                        DatabaseReference recipientWalletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + personId);
                                        recipientWalletRef.get().addOnCompleteListener(recipientTask -> {
                                            if (recipientTask.isSuccessful() && recipientTask.getResult().exists()) {
                                                // Fetch recipient's current wallet amount
                                                Double recipientWalletAmt = recipientTask.getResult().child("walletAmt").getValue(Double.class);
                                                if (recipientWalletAmt != null) {
                                                    // Update recipient's wallet amount
                                                    recipientWalletRef.child("walletAmt").setValue(recipientWalletAmt + transferAmt).addOnCompleteListener(updateTask -> {
                                                        if (updateTask.isSuccessful()) {
                                                            // Add transaction to recipient's transaction history
                                                            DatabaseReference recipientTransactionHistoryRef = recipientWalletRef.child("transactionHistory").child(transactionId);
                                                            recipientTransactionHistoryRef.setValue(transaction).addOnCompleteListener(historyTask -> {
                                                                if (historyTask.isSuccessful()) {
                                                                    Log.d("Transaction", "Transaction successfully saved to both users' histories");
                                                                    showTransferNotification(transferAmt, personName, dateTime);
                                                                    navigateToHomeFragment(userId);
                                                                } else {
                                                                    Log.e("Transaction", "Failed to save transaction to recipient's history", historyTask.getException());
                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(getContext(), "Failed to update recipient's wallet amount.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(getContext(), "Recipient's wallet amount is null.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Recipient's wallet not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.e("Transaction", "Failed to save transaction", task1.getException());
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Failed to update user's wallet amount.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch wallet details.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        return view;
    }

    private void showTransferNotification(double amount, String name, String dateTime) {
        createNotificationChannel();  // Create notification channel for Android 8.0+

        // Build the notification with expanded content
        @SuppressLint("DefaultLocale") String notificationContent = String.format("You have successfully transferred RM %.2f to %s on %s.", amount, name, dateTime);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("Transfer Completed Successfully")
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))  // Expanded text style
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, TRANSFER_NOTIFICATION_PERMISSION);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Transfer Notification";
            String description = "Notification sent after a transfer is completed";
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