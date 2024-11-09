package com.example.paymentapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RequestFragment extends Fragment {

    private static final String CHANNEL_ID = "request_notification_channel";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    String userId, userMobileNumber, userImageUrl;

    private DatabaseReference userListRef;
    private List<HashMap<String, String>> userList = new ArrayList<>();
    private List<HashMap<String, String>> filteredUserList = new ArrayList<>();

    private RecyclerView cardRecyclerView;
    private UserAdapter userAdapter;

    private List<Transaction> pendingRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        // Initialize the back button for navigation
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        EditText searchInput = view.findViewById(R.id.search);

        // Retrieve user data passed through arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            userMobileNumber = arguments.getString("userMobileNumber");
            userImageUrl = arguments.getString("userImageUrl");
        }

        // Initialize database reference for user data
        userListRef = FirebaseDatabase.getInstance().getReference("Users");
        loadUsers();  // Load list of users

        // Set up text change listener for search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed before text change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUsers(charSequence.toString());  // Filter the user list as the user types
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed after text change
            }
        });

        // Set up RecyclerView to display the user list
        cardRecyclerView = view.findViewById(R.id.card_recycler_view);
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(userList, getContext(), this, UserAdapter.FragmentType.REQUEST, userId, userImageUrl, 0.0);
        cardRecyclerView.setAdapter(userAdapter);

        userAdapter = new UserAdapter(filteredUserList, getContext(), this, UserAdapter.FragmentType.REQUEST, userId, userImageUrl, 0.0);
        cardRecyclerView.setAdapter(userAdapter);

        // Set up 'See All' button to display all requests
        TextView seeAllButton = view.findViewById(R.id.see_all_button);
        seeAllButton.setOnClickListener(v -> showBottomDialog());

        // Initialize list to store pending requests
        pendingRequests = new ArrayList<>();
        fetchAndPopulatePendingRequests(view.findViewById(R.id.request_list));

        return view;
    }

    // Load users from Firebase and add them to the user list
    private void loadUsers() {
        userList.clear();

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userIdFromDB = userSnapshot.getKey();

                        if (!userIdFromDB.equals(userId)) {  // Exclude the logged-in user
                            HashMap<String, String> user = (HashMap<String, String>) userSnapshot.getValue();
                            if (user != null) {
                                user.put("id", userIdFromDB);
                                userList.add(user);
                            }
                        }
                    }

                    userList.sort((user1, user2) -> user1.get("name").compareToIgnoreCase(user2.get("name")));
                    filteredUserList.clear();
                    filteredUserList.addAll(userList);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Filter users based on the search query
    private void filterUsers(String query) {
        filteredUserList.clear();

        if (query.isEmpty()) {
            filteredUserList.addAll(userList);  // Show all users if query is empty
        } else {
            for (HashMap<String, String> user : userList) {
                String userName = user.get("name").toLowerCase();
                String mobileNumber = user.get("mobileNumber").toLowerCase();
                if (userName.contains(query.toLowerCase()) || mobileNumber.contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged();  // Notify adapter of the updated filtered list
    }

    // Fetch and populate pending requests from Firebase and display them
    private void fetchAndPopulatePendingRequests(LinearLayout pendingRequestList) {
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("Wallets");

        walletsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingRequestList.removeAllViews();
                pendingRequests.clear();

                for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                    String walletOwnerId = walletSnapshot.getKey();

                    // Iterate through each transaction in the wallet
                    DataSnapshot transactionHistory = walletSnapshot.child("transactionHistory");
                    for (DataSnapshot transactionSnapshot : transactionHistory.getChildren()) {
                        String transactionId = transactionSnapshot.child("transactionId").getValue(String.class);
                        String recipientImageUrl = transactionSnapshot.child("recipientImageUrl").getValue(String.class);
                        String senderImageUrl = transactionSnapshot.child("senderImageUrl").getValue(String.class);
                        String datetime = transactionSnapshot.child("datetime").getValue(String.class);
                        String source = transactionSnapshot.child("source").getValue(String.class);
                        String note = transactionSnapshot.child("note").getValue(String.class);
                        String refId = transactionSnapshot.child("refId").getValue(String.class);
                        int status = transactionSnapshot.child("status").getValue(Integer.class);
                        String mobileNumber = transactionSnapshot.child("mobileNumber").getValue(String.class);
                        String recipientId = transactionSnapshot.child("recipientId").getValue(String.class);
                        double amount = transactionSnapshot.child("amount").getValue(Double.class);

                        // Only display pending requests
                        if (status != 0) continue;

                        // Scenario 1: Someone is requesting money from the logged-in user
                        if (mobileNumber.equals(userMobileNumber)) {
                            Transaction transaction = new Transaction(transactionId, recipientImageUrl, null, datetime, source, note, refId, status, mobileNumber, recipientId, amount);
                            pendingRequests.add(transaction);
                        }

                        // Scenario 2: Logged-in user is requesting money from others
                        if (walletOwnerId.equals("W" + userId)) {
                            Transaction transaction = new Transaction(transactionId, null, senderImageUrl, datetime, source, note, refId, status, mobileNumber, recipientId, amount);
                            pendingRequests.add(transaction);
                        }
                    }
                }

                for (Transaction transaction : pendingRequests) {
                    String note = transaction.note.equals("N/A") ? "Ref ID: " + transaction.refId : transaction.note + " (Ref ID: " + transaction.refId + ")";

                    // Determine if the user is the requester or recipient and add to list
                    if (transaction.mobileNumber.equals(userMobileNumber)) {
                        addPendingTransactionItem(pendingRequestList, transaction.recipientImageUrl, null, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, true);
                    } else {
                        addPendingTransactionItem(pendingRequestList, null, transaction.senderImageUrl, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to retrieve transactions.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to add a pending transaction item to the list
    private void addPendingTransactionItem(LinearLayout parent, String userImageUrl, String imageUrl, String date, String source, String note, String mobileNumber, String recipientId, String amount, Context context, String transactionId, boolean isAcceptIcon) {
        LinearLayout transactionItem = new LinearLayout(context);
        transactionItem.setOrientation(LinearLayout.HORIZONTAL);
        transactionItem.setPadding(8, 8, 8, dpToPx(context, 15));
        transactionItem.setGravity(Gravity.CENTER_VERTICAL);
        transactionItem.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(context, 40), dpToPx(context, 40));
        icon.setLayoutParams(iconParams);

        String imageToLoad = isAcceptIcon ? userImageUrl : imageUrl;
        Glide.with(context).load(imageToLoad).into(icon);  // Load the image
        transactionItem.addView(icon);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textContainer.setLayoutParams(textContainerParams);
        textContainer.setPadding(dpToPx(context, 15), 0, 0, 0);

        TextView transactionDateTime = new TextView(context);
        transactionDateTime.setText(date);
        transactionDateTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textContainer.addView(transactionDateTime);

        TextView transactionSource = new TextView(context);
        transactionSource.setText(source);
        transactionSource.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        transactionSource.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(transactionSource);

        TextView transactionNote = new TextView(context);
        transactionNote.setText(note);
        transactionNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textContainer.addView(transactionNote);

        transactionItem.addView(textContainer);

        if (isAcceptIcon) {
            ImageView acceptIcon = new ImageView(context);
            LinearLayout.LayoutParams acceptIconParams = new LinearLayout.LayoutParams(dpToPx(context, 35), dpToPx(context, 35));
            acceptIcon.setLayoutParams(acceptIconParams);
            acceptIcon.setImageResource(R.drawable.send);
            acceptIcon.setPadding(dpToPx(context, 0), dpToPx(context, 5), dpToPx(context, 15), dpToPx(context, 5));
            acceptIcon.setOnClickListener(v -> handleAcceptTransaction(transactionId, context, parent, userId, recipientId, amount));
            transactionItem.addView(acceptIcon);

            TextView transactionAmount = new TextView(context);
            transactionAmount.setText(String.format("- %s", amount));
            transactionAmount.setTextColor(Color.parseColor("#FF9800"));
            transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            transactionItem.addView(transactionAmount);

        } else {
            ImageView cancelIcon = new ImageView(context);
            LinearLayout.LayoutParams cancelIconParams = new LinearLayout.LayoutParams(dpToPx(context, 35), dpToPx(context, 35));
            cancelIcon.setLayoutParams(cancelIconParams);
            cancelIcon.setImageResource(R.drawable.cancel);
            cancelIcon.setPadding(dpToPx(context, 0), dpToPx(context, 5), dpToPx(context, 15), dpToPx(context, 5));
            cancelIcon.setOnClickListener(v -> showConfirmationDialog(transactionId, context, parent));
            transactionItem.addView(cancelIcon);

            ImageView callIcon = new ImageView(context);
            LinearLayout.LayoutParams callIconParams = new LinearLayout.LayoutParams(dpToPx(context, 35), dpToPx(context, 35));
            callIcon.setLayoutParams(callIconParams);
            callIcon.setImageResource(R.drawable.call);
            callIcon.setPadding(dpToPx(context, 0), dpToPx(context, 5), dpToPx(context, 15), dpToPx(context, 5));
            callIcon.setOnClickListener(v -> {
                Toast.makeText(context, "Calling: " + mobileNumber, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mobileNumber));
                context.startActivity(intent);
            });
            transactionItem.addView(callIcon);

            TextView transactionAmount = new TextView(context);
            transactionAmount.setText(String.format("+ %s", amount));
            transactionAmount.setTextColor(Color.parseColor("#FF9800"));  // Orange color for pending request
            transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            transactionItem.addView(transactionAmount);
        }

        parent.addView(transactionItem);
    }

    // Show the bottom dialog displaying all pending requests
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.request_see_all);

        LinearLayout requestHistory = dialog.findViewById(R.id.pending_request);
        ImageView closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(view -> dialog.dismiss());

        for (Transaction transaction : pendingRequests) {
            String note = transaction.note.equals("N/A") ? "Ref ID: " + transaction.refId : transaction.note + " (Ref ID: " + transaction.refId + ")";

            // Show "Accept" icon if the logged-in user is the recipient
            if (transaction.mobileNumber.equals(userMobileNumber)) {
                addPendingTransactionItem(requestHistory, transaction.recipientImageUrl, null, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, true);
            } else {
                addPendingTransactionItem(requestHistory, null, transaction.senderImageUrl, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, false);
            }
        }

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
    }

    // Show confirmation dialog for cancelling a request
    private void showConfirmationDialog(String transactionId, Context context, LinearLayout parent) {
        final View dialogView = getLayoutInflater().inflate(R.layout.confirmation_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setNegativeButton("Back", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Confirm", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Handle confirm button click to cancel request in Firebase
        Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        confirmButton.setOnClickListener(view -> {
            DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("Wallets")
                    .child("W" + userId)
                    .child("transactionHistory")
                    .child(transactionId);

            transactionRef.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Request canceled successfully.", Toast.LENGTH_SHORT).show();
                fetchAndPopulatePendingRequests(parent);
                dialog.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(context, "Failed to cancel the request.", Toast.LENGTH_SHORT).show());
        });
    }

    // Accepts a transaction request and updates Firebase records accordingly
    private void handleAcceptTransaction(String transactionId, Context context, LinearLayout parent, String userId, String recipientId, String amount) {
        DatabaseReference currentUserWalletRef = FirebaseDatabase.getInstance().getReference("Wallets")
                .child("W" + userId).child("walletAmt");

        currentUserWalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot currentUserSnapshot) {
                if (currentUserSnapshot.exists() && currentUserSnapshot.getValue() != null) {
                    try {
                        Double currentUserWalletAmt = currentUserSnapshot.getValue(Double.class);
                        double amountValue = Double.parseDouble(amount.replace("RM", "").trim());

                        // Check if the current user's wallet has enough balance
                        if (currentUserWalletAmt == null || currentUserWalletAmt < amountValue) {
                            Toast.makeText(context, "Insufficient wallet balance to accept this request.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Reference for the recipient's wallet amount and the transaction history
                        DatabaseReference recipientWalletRef = FirebaseDatabase.getInstance().getReference("Wallets")
                                .child("W" + recipientId).child("walletAmt");
                        DatabaseReference recipientTransactionRef = FirebaseDatabase.getInstance().getReference("Wallets")
                                .child("W" + recipientId).child("transactionHistory").child(transactionId);

                        // Step 1: Fetch the recipient's wallet amount
                        recipientWalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot recipientSnapshot) {
                                if (recipientSnapshot.exists() && recipientSnapshot.getValue() != null) {
                                    Double recipientWalletAmt = recipientSnapshot.getValue(Double.class);
                                    if (recipientWalletAmt != null) {
                                        // Step 2: Update the recipient's wallet amount
                                        recipientWalletRef.setValue(recipientWalletAmt + amountValue)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Step 3: Update the current user's wallet amount
                                                    currentUserWalletRef.setValue(currentUserWalletAmt - amountValue)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Step 4: Update the transaction status to 1 (accepted) and set the current datetime
                                                                String currentDatetime = new SimpleDateFormat("dd MMM yyyy, hh:mma", Locale.getDefault()).format(Calendar.getInstance().getTime());
                                                                recipientTransactionRef.child("status").setValue(1);
                                                                recipientTransactionRef.child("datetime").setValue(currentDatetime)
                                                                        .addOnSuccessListener(aVoid2 -> {
                                                                            // Step 5: Copy the transaction to the current user's transaction history
                                                                            DatabaseReference userTransactionHistoryRef = FirebaseDatabase.getInstance().getReference("Wallets")
                                                                                    .child("W" + userId).child("transactionHistory").child(transactionId);

                                                                            recipientTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot recipientTransactionSnapshot) {
                                                                                    if (recipientTransactionSnapshot.exists()) {
                                                                                        // Copy the transaction details to user's transaction history
                                                                                        userTransactionHistoryRef.setValue(recipientTransactionSnapshot.getValue())
                                                                                                .addOnSuccessListener(aVoid3 -> {
                                                                                                    DatabaseReference recipientUserRef = FirebaseDatabase.getInstance().getReference("Users").child(recipientId).child("name");
                                                                                                    recipientUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(DataSnapshot nameSnapshot) {
                                                                                                            String personName = nameSnapshot.getValue(String.class);
                                                                                                            if (personName != null) {
                                                                                                                Toast.makeText(context, "Request accepted. Wallets and transaction updated successfully.", Toast.LENGTH_SHORT).show();
                                                                                                                showRequestNotification(amountValue, personName, currentDatetime);
                                                                                                                fetchAndPopulatePendingRequests(parent);  // Refresh pending requests
                                                                                                            } else {
                                                                                                                Toast.makeText(context, "Failed to retrieve recipient's name.", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(DatabaseError error) {
                                                                                                            Toast.makeText(context, "Failed to retrieve recipient's name.", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                                })
                                                                                                .addOnFailureListener(e -> {
                                                                                                    Toast.makeText(context, "Failed to copy transaction to user's history.", Toast.LENGTH_SHORT).show();
                                                                                                });
                                                                                    } else {
                                                                                        Toast.makeText(context, "Failed to fetch recipient's transaction details.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError error) {
                                                                                    Toast.makeText(context, "Failed to fetch recipient's transaction details.", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            Toast.makeText(context, "Failed to update transaction status or datetime.", Toast.LENGTH_SHORT).show();
                                                                        });
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(context, "Failed to update user's wallet amount.", Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to update recipient's wallet amount.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(context, "Recipient's wallet amount is null.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "Recipient's wallet not found.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(context, "Failed to retrieve recipient's wallet amount.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid amount format.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Current user's wallet not found or empty.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Failed to retrieve current user's wallet amount.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Displays a notification for the request approval completion
    private void showRequestNotification(double amount, String name, String dateTime) {
        createNotificationChannel();  // Create notification channel for Android 8.0+

        // Build notification content
        @SuppressLint("DefaultLocale") String notificationContent = String.format("You have successfully approved the request for RM %.2f from %s on %s", amount, name, dateTime);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("Request Approved Successfully")
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Check and request notification permission if not granted
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Creates a notification channel for request notifications (required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Request Notification";
            String description = "Notification triggered upon completion of an approval request.";
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

        // Re-fetch pending requests when the fragment is resumed
        fetchAndPopulatePendingRequests(getView().findViewById(R.id.request_list));
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
