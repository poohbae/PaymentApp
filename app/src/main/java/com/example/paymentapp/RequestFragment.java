package com.example.paymentapp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
