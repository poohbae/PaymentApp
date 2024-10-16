package com.example.paymentapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestFragment extends Fragment {

    String userId;

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
            String userName = user.get("name");
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
            personNameTextView.setText(userName);
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
                bundle.putString("personImageUrl", imageUrl);
                bundle.putString("personName", userName);
                bundle.putString("mobileNumber", mobileNumber);

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
            String userName = user.get("name");
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
            personNameTextView.setText(userName);
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
                TransferMoneyFragment transferMoneyFragment = new TransferMoneyFragment();

                Bundle bundle = new Bundle();
                bundle.putString("personImageUrl", imageUrl);
                bundle.putString("personName", userName);
                bundle.putString("mobileNumber", mobileNumber);

                transferMoneyFragment.setArguments(bundle);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, transferMoneyFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void fetchAndPopulatePendingRequests(LinearLayout pendingRequestList) {
        DatabaseReference transactionHistoryRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId).child("transactionHistory");

        transactionHistoryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                pendingRequestList.removeAllViews();  // Clear old views before adding new ones
                pendingRequests.clear();  // Clear the old transaction list

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String transactionId = snapshot.child("transactionId").getValue(String.class);
                    int status = snapshot.child("status").getValue(Integer.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String datetime = snapshot.child("datetime").getValue(String.class);
                    String source = snapshot.child("source").getValue(String.class);
                    String refId = snapshot.child("refId").getValue(String.class);
                    String note = snapshot.child("note").getValue(String.class);
                    double amount = snapshot.child("amount").getValue(Double.class);

                    // Only include transactions where status == 0 (pending)
                    if (status != 0) {
                        continue;
                    }

                    // Create a transaction object specifically for pending requests
                    Register.Transaction transaction = new Register.Transaction(transactionId, status, imageUrl, datetime, source, note, refId, amount);
                    pendingRequests.add(transaction);
                }

                // Sort pending requests in descending order by datetime
                pendingRequests.sort((t1, t2) -> t2.datetime.compareTo(t1.datetime));

                // Add pending transactions (status == 0) to the list
                for (Register.Transaction transaction : pendingRequests) {
                    if (transaction.note.equals("N/A")) {
                        addPendingTransactionItem(pendingRequestList, transaction.imageUrl, transaction.datetime, transaction.source, "Ref ID: " + transaction.refId, String.format("RM %.2f", transaction.amount), getActivity());
                    } else {
                        addPendingTransactionItem(pendingRequestList, transaction.imageUrl, transaction.datetime, transaction.source, transaction.note + " (Ref ID: " + transaction.refId + ")", String.format("RM %.2f", transaction.amount), getActivity());
                    }                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to retrieve transactions.", Toast.LENGTH_SHORT).show();
        });
    }

    private void addPendingTransactionItem(LinearLayout parent, String imageUrl, String date, String source, String note, String amount, Context context) {
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
        Glide.with(context).load(imageUrl).into(icon);  // Load the image for pending requests
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

        TextView transactionAmount = new TextView(context);
        transactionAmount.setText(String.format(" %s", amount));  // No "+" or "-" for pending
        transactionAmount.setTextColor(Color.parseColor("#FF9800"));  // Orange color for pending request
        transactionAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        transactionItem.addView(transactionAmount);

        parent.addView(transactionItem);
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.request_see_all);

        LinearLayout requestHistory = dialog.findViewById(R.id.pending_request);
        ImageView closeButton = dialog.findViewById(R.id.close_button);

        // Populate transactionHistory using only pending transactions (status == 0)
        for (Register.Transaction transaction : pendingRequests) {
            if (transaction.note.equals("N/A")) {
                addPendingTransactionItem(requestHistory, transaction.imageUrl, transaction.datetime, transaction.source, "Ref ID: " + transaction.refId, String.format("RM %.2f", transaction.amount), getActivity());
            } else {
                addPendingTransactionItem(requestHistory, transaction.imageUrl, transaction.datetime, transaction.source, transaction.note + " (Ref ID: " + transaction.refId + ")", String.format("RM %.2f", transaction.amount), getActivity());
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
