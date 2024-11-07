package com.example.paymentapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SelectPayFragment extends Fragment {

    String userId;
    double totalPrice;

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<HashMap<String, String>> ordersList = new ArrayList<>();
    private DatabaseReference ordersRef;

    private TextView totalTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_pay, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView checkUncheckTextView = view.findViewById(R.id.uncheck);
        checkUncheckTextView.setOnClickListener(v -> orderAdapter.checkUncheckAll());

        totalTextView = view.findViewById(R.id.total_amount);
        totalTextView.setText("RM 0.00");

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
        }

        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderAdapter = new OrderAdapter(ordersList, false, true);
        ordersRecyclerView.setAdapter(orderAdapter);

        orderAdapter.setOnItemCheckedChangeListener(totalCheckedPrice ->
                totalTextView.setText(String.format("RM %.2f", totalCheckedPrice))
        );

        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        loadOrders();

        Button payButton = view.findViewById(R.id.pay_button);
        payButton.setOnClickListener(v -> {
            List<String> checkedItemKeys = orderAdapter.getCheckedItemKeys();

            if (checkedItemKeys.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one item to proceed.", Toast.LENGTH_SHORT).show();
            } else {
                for (String key : checkedItemKeys) {
                    ordersRef.child(key).child("status").setValue(1);
                }

                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);

                SelectPayDoneFragment selectPayDoneFragment = new SelectPayDoneFragment();
                selectPayDoneFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, selectPayDoneFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void loadOrders() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ordersList.clear();

                    // Get the total count from the database
                    if (dataSnapshot.child("total").getValue() != null) {
                        totalPrice = dataSnapshot.child("total").getValue(Double.class);
                    }

                    // Iterate through each order item
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        if (!orderSnapshot.getKey().equals("total")) { // Skip the "total" field
                            Long status = orderSnapshot.child("status").getValue(Long.class);  // Use Long for integer

                            // Only add orders with status = 0
                            if (status != null && status == 0) {
                                HashMap<String, String> order = new HashMap<>();

                                // Add database key for reference
                                order.put("key", orderSnapshot.getKey()); // Save Firebase key

                                String name = orderSnapshot.child("name").getValue(String.class);
                                String image = orderSnapshot.child("image").getValue(String.class);
                                Double price = orderSnapshot.child("price").getValue(Double.class);  // Use Double for decimal

                                order.put("name", name);
                                order.put("image", image);

                                String formattedPrice = price != null ? String.format("%.2f", price) : "0.00";
                                order.put("price", formattedPrice);
                                order.put("status", status.toString());

                                ordersList.add(order);
                            }
                        }
                    }
                    orderAdapter.initializeCheckedStates(ordersList.size());
                    orderAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
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