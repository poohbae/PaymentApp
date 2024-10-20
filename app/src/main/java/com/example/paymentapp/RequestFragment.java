package com.example.paymentapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.MutableData;

import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RequestFragment extends Fragment {

    String userId, userMobileNumber, userImageUrl;

    private DatabaseReference userListRef;
    private List<HashMap<String, String>> userList = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private List<String> userMobileNumbers = new ArrayList<>();

    private LinearLayout cardContainer;

    private List<Register.Transaction> pendingRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        cardContainer = view.findViewById(R.id.card_container);
        EditText searchInput = view.findViewById(R.id.search);

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            userMobileNumber = arguments.getString("userMobileNumber");
            userImageUrl = arguments.getString("userImageUrl");
        }

        userListRef = FirebaseDatabase.getInstance().getReference("Users");
        loadUsers();

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

        TextView seeAllButton = view.findViewById(R.id.see_all_button);
        seeAllButton.setOnClickListener(v -> showBottomDialog());

        pendingRequests = new ArrayList<>();
        fetchAndPopulatePendingRequests(view.findViewById(R.id.request_list));

        return view;
    }

    private void loadUsers() {
        // Clear the lists before loading to avoid duplicates
        userList.clear();
        userNames.clear();
        userMobileNumbers.clear();

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot methodSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> user = (HashMap<String, String>) methodSnapshot.getValue();
                        String userIdFromDB = methodSnapshot.getKey();  // Get the user's ID from Firebase

                        // Skip the current user from being populated
                        if (!userIdFromDB.equals(userId)) {
                            userList.add(user);
                            userNames.add(user.get("name"));
                            userMobileNumbers.add(user.get("mobileNumber"));
                        }
                    }
                    // Sort the userList by the user name in ascending order
                    userList.sort((user1, user2) -> user1.get("name").compareToIgnoreCase(user2.get("name")));

                    populateUserCards();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void populateUserCards() {
        if (userList.isEmpty()) {
            return;
        }

        // Loop through the userList and create a card for each user
        for (int i = 0; i < userList.size(); i++) {
            HashMap<String, String> user = userList.get(i);
            String id = user.get("userId");
            String name = user.get("name");
            String mobileNumber = user.get("mobileNumber");
            String imageUrl = user.get("image");

            // Create a new CardView
            CardView cardView = new CardView(getContext());
            LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardLayoutParams.setMargins(dpToPx(getContext(), 0), dpToPx(getContext(), 0), dpToPx(getContext(), 0), dpToPx(getContext(), 5));
            cardView.setLayoutParams(cardLayoutParams);
            cardView.setRadius(12);
            cardView.setCardElevation(0);
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));

            cardView.setTag(id);

            LinearLayout cardLayout = new LinearLayout(getContext());
            cardLayout.setOrientation(LinearLayout.HORIZONTAL);
            cardLayout.setPadding(dpToPx(getContext(), 8), dpToPx(getContext(), 15), dpToPx(getContext(), 8), dpToPx(getContext(), 15));
            cardLayout.setGravity(Gravity.CENTER_VERTICAL);

            ImageView personImageView = new ImageView(getContext());
            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(dpToPx(getContext(), 50), dpToPx(getContext(), 50));
            personImageView.setLayoutParams(imageLayoutParams);

            // Load the image using Glide from the Firebase URL
            Glide.with(getContext())
                    .load(imageUrl)  // Load the image from Firebase Storage
                    .placeholder(R.drawable.person)  // Optional placeholder image
                    .into(personImageView);

            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setPadding(dpToPx(getContext(), 15), 0, 0, 0);

            TextView personNameTextView = new TextView(getContext());
            personNameTextView.setText(name);
            personNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            personNameTextView.setTextColor(getResources().getColor(R.color.black));
            personNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView mobileNumberTextView = new TextView(getContext());
            mobileNumberTextView.setText(mobileNumber);
            mobileNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            mobileNumberTextView.setTextColor(getResources().getColor(R.color.black));

            textLayout.addView(personNameTextView);
            textLayout.addView(mobileNumberTextView);
            cardLayout.addView(personImageView);
            cardLayout.addView(textLayout);
            cardView.addView(cardLayout);
            cardContainer.addView(cardView);

            cardView.setOnClickListener(v -> {
                RequestMoneyFragment requestMoneyFragment = new RequestMoneyFragment();

                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("personImageUrl", imageUrl);
                bundle.putString("personName", name);
                bundle.putString("personMobileNumber", mobileNumber);

                requestMoneyFragment.setArguments(bundle);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, requestMoneyFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    // Filter users based on the search query
    private void filterUsers(String query) {
        cardContainer.removeAllViews();  // Clear the card container before repopulating it

        if (query.isEmpty()) {
            populateUserCards();  // If no query, show all users
            return;
        }

        // Filter the userList based on the search query
        List<HashMap<String, String>> filteredList = new ArrayList<>();
        for (HashMap<String, String> user : userList) {
            String userName = user.get("name").toLowerCase();
            String mobileNumber = user.get("mobileNumber").toLowerCase();
            if (userName.contains(query.toLowerCase()) || mobileNumber.contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }

        // Populate the filtered user list
        populateFilteredUserCards(filteredList);
    }

    // Populate the filtered user cards
    private void populateFilteredUserCards(List<HashMap<String, String>> filteredList) {
        if (filteredList.isEmpty()) {
            return;
        }

        for (int i = 0; i < filteredList.size(); i++) {
            HashMap<String, String> user = filteredList.get(i);
            String name = user.get("name");
            String mobileNumber = user.get("mobileNumber");
            String imageUrl = user.get("image");

            // Create a new CardView
            CardView cardView = new CardView(getContext());
            LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardLayoutParams.setMargins(dpToPx(getContext(), 0), dpToPx(getContext(), 0), dpToPx(getContext(), 0), dpToPx(getContext(), 5));
            cardView.setLayoutParams(cardLayoutParams);
            cardView.setRadius(12);
            cardView.setCardElevation(0);
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));

            LinearLayout cardLayout = new LinearLayout(getContext());
            cardLayout.setOrientation(LinearLayout.HORIZONTAL);
            cardLayout.setPadding(dpToPx(getContext(), 8), dpToPx(getContext(), 15), dpToPx(getContext(), 8), dpToPx(getContext(), 15));
            cardLayout.setGravity(Gravity.CENTER_VERTICAL);

            ImageView personImageView = new ImageView(getContext());
            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(dpToPx(getContext(), 50), dpToPx(getContext(), 50));
            personImageView.setLayoutParams(imageLayoutParams);

            // Load the image using Glide from the Firebase URL
            Glide.with(getContext())
                    .load(imageUrl)  // Load the image from Firebase Storage
                    .placeholder(R.drawable.person)  // Optional placeholder image
                    .into(personImageView);

            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setPadding(dpToPx(getContext(), 15), 0, 0, 0);

            TextView personNameTextView = new TextView(getContext());
            personNameTextView.setText(name);
            personNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            personNameTextView.setTextColor(getResources().getColor(R.color.black));
            personNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView mobileNumberTextView = new TextView(getContext());
            mobileNumberTextView.setText(mobileNumber);
            mobileNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            mobileNumberTextView.setTextColor(getResources().getColor(R.color.black));

            textLayout.addView(personNameTextView);
            textLayout.addView(mobileNumberTextView);
            cardLayout.addView(personImageView);
            cardLayout.addView(textLayout);
            cardView.addView(cardLayout);
            cardContainer.addView(cardView);

            cardView.setOnClickListener(v -> {
                RequestMoneyFragment requestMoneyFragment = new RequestMoneyFragment();

                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("personImageUrl", imageUrl);
                bundle.putString("personName", name);
                bundle.putString("personMobileNumber", mobileNumber);

                requestMoneyFragment.setArguments(bundle);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, requestMoneyFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void fetchAndPopulatePendingRequests(LinearLayout pendingRequestList) {
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("Wallets");

        // Iterate through all wallets in the database
        walletsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingRequestList.removeAllViews();  // Clear old views before adding new ones
                pendingRequests.clear();  // Clear the old transaction list

                for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                    String walletOwnerId = walletSnapshot.getKey(); // Get wallet owner's ID

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

                        if (status != 0) continue;

                        // Scenario 1: Someone is requesting money from the logged-in user
                        if (mobileNumber.equals(userMobileNumber)) {
                            // Show "Accept" icon
                            Register.Transaction transaction = new Register.Transaction(transactionId, recipientImageUrl, null, datetime, source, note, refId, status, mobileNumber, recipientId, amount);
                            pendingRequests.add(transaction);
                        }

                        // Scenario 2: Logged-in user is requesting money from others
                        if (walletOwnerId.equals("W" + userId)) {
                            // Show "Call" icon because the logged-in user is the requester
                            Register.Transaction transaction = new Register.Transaction(transactionId, null, senderImageUrl, datetime, source, note, refId, status, mobileNumber, recipientId, amount);
                            pendingRequests.add(transaction);
                        }
                    }
                }

                // Populate the pending requests list
                for (Register.Transaction transaction : pendingRequests) {
                    String note = transaction.note.equals("N/A") ? "Ref ID: " + transaction.refId : transaction.note + " (Ref ID: " + transaction.refId + ")";

                    // Show "Accept" icon if the logged-in user is the recipient
                    if (transaction.mobileNumber.equals(userMobileNumber)) {
                        addPendingTransactionItem(pendingRequestList, transaction.recipientImageUrl, null, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, true);
                    }
                    // Show "Call" icon if the logged-in user is the requester
                    else {
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

        String imageToLoad;
        if (isAcceptIcon) {
            // If the logged-in user is the recipient, show the requester's image (userImageUrl)
            imageToLoad = userImageUrl;
        } else {
            // If the logged-in user is the requester, show the recipient's image (imageUrl)
            imageToLoad = imageUrl;
        }
        Glide.with(context).load(imageToLoad).into(icon);  // Load the image for pending requests
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
            // Show "Accept" icon if the current user is being requested
            ImageView acceptIcon = new ImageView(context);
            LinearLayout.LayoutParams acceptIconParams = new LinearLayout.LayoutParams(dpToPx(context, 35), dpToPx(context, 35));
            acceptIcon.setLayoutParams(acceptIconParams);
            acceptIcon.setImageResource(R.drawable.send);  // Replace with your accept icon resource
            acceptIcon.setPadding(dpToPx(context, 0), dpToPx(context, 5), dpToPx(context, 15), dpToPx(context, 5));
            transactionItem.addView(acceptIcon);

            acceptIcon.setOnClickListener(v -> handleAcceptTransaction(transactionId, context, parent, userId, recipientId, amount));

            TextView transactionAmount = new TextView(context);
            transactionAmount.setText(String.format("- %s", amount));
            transactionAmount.setTextColor(Color.parseColor("#FF9800"));  // Orange color for pending request
            transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            transactionItem.addView(transactionAmount);

        } else {
            ImageView cancelIcon = new ImageView(context);
            LinearLayout.LayoutParams cancelIconParams = new LinearLayout.LayoutParams(dpToPx(context, 35), dpToPx(context, 35));
            cancelIcon.setLayoutParams(cancelIconParams);
            cancelIcon.setImageResource(R.drawable.cancel);
            cancelIcon.setPadding(dpToPx(context, 0), dpToPx(context, 5), dpToPx(context, 15), dpToPx(context, 5));
            transactionItem.addView(cancelIcon);

            cancelIcon.setOnClickListener(v -> {
                showConfirmationDialog(transactionId, context, parent);
            });

            ImageView callIcon = new ImageView(context);
            LinearLayout.LayoutParams callIconParams = new LinearLayout.LayoutParams(dpToPx(context, 35), dpToPx(context, 35));
            callIcon.setLayoutParams(callIconParams);
            callIcon.setImageResource(R.drawable.call);
            callIcon.setPadding(dpToPx(context, 0), dpToPx(context, 5), dpToPx(context, 15), dpToPx(context, 5));
            transactionItem.addView(callIcon);

            callIcon.setOnClickListener(v -> {
                Toast.makeText(context, "Calling: " + mobileNumber, Toast.LENGTH_SHORT).show();

                // Open the dialer with the number
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mobileNumber));
                context.startActivity(intent);
            });

            TextView transactionAmount = new TextView(context);
            transactionAmount.setText(String.format("+ %s", amount));
            transactionAmount.setTextColor(Color.parseColor("#FF9800"));  // Orange color for pending request
            transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            transactionItem.addView(transactionAmount);
        }

        parent.addView(transactionItem);
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.request_see_all);

        LinearLayout requestHistory = dialog.findViewById(R.id.pending_request);
        ImageView closeButton = dialog.findViewById(R.id.close_button);

        for (Register.Transaction transaction : pendingRequests) {
            String note = transaction.note.equals("N/A") ? "Ref ID: " + transaction.refId : transaction.note + " (Ref ID: " + transaction.refId + ")";

            // Show "Accept" icon if the logged-in user is the recipient
            if (transaction.mobileNumber.equals(userMobileNumber)) {
                addPendingTransactionItem(requestHistory, transaction.recipientImageUrl, null, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, true);
            }
            // Show "Call" icon if the logged-in user is the requester
            else {
                addPendingTransactionItem(requestHistory, null, transaction.senderImageUrl, transaction.datetime, transaction.source, note, transaction.mobileNumber, transaction.recipientId, String.format("RM %.2f", transaction.amount), getActivity(), transaction.transactionId, false);
            }
        }

        closeButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
    }

    private void showConfirmationDialog(String transactionId, Context context, LinearLayout parent) {
        final View dialogView = getLayoutInflater().inflate(R.layout.confirmation_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setNegativeButton("Back", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Confirm", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Handle confirm button click
        Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        confirmButton.setOnClickListener(view -> {
            // Delete the respective transaction from Firebase
            DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("Wallets")
                    .child("W" + userId)
                    .child("transactionHistory")
                    .child(transactionId);

            transactionRef.removeValue().addOnSuccessListener(aVoid -> {
                // Remove the view from parent container and refresh pending requests
                Toast.makeText(context, "Request canceled successfully.", Toast.LENGTH_SHORT).show();
                fetchAndPopulatePendingRequests(parent);
                dialog.dismiss(); // Close the confirmation dialog
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to cancel the request.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void handleAcceptTransaction(String transactionId, Context context, LinearLayout parent, String userId, String recipientId, String amount) {
        DatabaseReference currentUserWalletRef = FirebaseDatabase.getInstance().getReference("Wallets")
                .child("W" + userId).child("walletAmt");

        currentUserWalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot currentUserSnapshot) {
                if (currentUserSnapshot.exists() && currentUserSnapshot.getValue() != null) {
                    try {
                        // Fetch current wallet amount
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

                        // Step 1: Fetch the recipient's wallet amount and update it
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
                                                                                                    Toast.makeText(context, "Request accepted. Wallets and transaction updated successfully.", Toast.LENGTH_SHORT).show();
                                                                                                    fetchAndPopulatePendingRequests(parent);  // Refresh pending requests
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

        // Re-fetch pending requests when the fragment is resumed
        fetchAndPopulatePendingRequests(getView().findViewById(R.id.request_list));
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
