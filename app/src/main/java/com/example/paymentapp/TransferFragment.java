package com.example.paymentapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class TransferFragment extends Fragment {

    String userId, userImageUrl;
    double walletAmt;

    private DatabaseReference userListRef;
    private List<HashMap<String, String>> userList = new ArrayList<>();
    private List<HashMap<String, String>> filteredUserList = new ArrayList<>();

    private RecyclerView cardRecyclerView;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        // Back button to navigate back to the previous fragment
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView balanceAmountTextView = view.findViewById(R.id.balance_amount);
        EditText searchInput = view.findViewById(R.id.search);

        // Retrieve data from arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            userImageUrl = arguments.getString("userImageUrl");
            walletAmt = arguments.getDouble("walletAmt", 0.0);
            balanceAmountTextView.setText(String.format("RM %.2f", walletAmt));
        }

        // Initialize database reference for user data
        userListRef = FirebaseDatabase.getInstance().getReference("Users");
        loadUsers();

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

        userAdapter = new UserAdapter(userList, getContext(), this, UserAdapter.FragmentType.TRANSFER, userId, userImageUrl, walletAmt);
        cardRecyclerView.setAdapter(userAdapter);

        userAdapter = new UserAdapter(filteredUserList, getContext(), this, UserAdapter.FragmentType.TRANSFER, userId, userImageUrl, walletAmt);
        cardRecyclerView.setAdapter(userAdapter);

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
