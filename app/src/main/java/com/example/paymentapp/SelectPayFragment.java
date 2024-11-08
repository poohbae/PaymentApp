package com.example.paymentapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SelectPayFragment extends Fragment {

    String userId, billNo;
    double walletAmt, totalChecked;

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<HashMap<String, String>> ordersList = new ArrayList<>();
    private DatabaseReference ordersRef;

    private TextView billNoTextView, taxAmountTextView, totalAmountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_pay, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView checkUncheckTextView = view.findViewById(R.id.uncheck);
        checkUncheckTextView.setOnClickListener(v -> orderAdapter.checkUncheckAll());

        billNoTextView = view.findViewById(R.id.bill_no);
        taxAmountTextView = view.findViewById(R.id.tax_amount);
        taxAmountTextView.setText("RM 0.00");
        totalAmountTextView = view.findViewById(R.id.total_amount);
        totalAmountTextView.setText("RM 0.00");

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            walletAmt = arguments.getDouble("walletAmt");
        }

        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderAdapter = new OrderAdapter(ordersList, false, true);
        ordersRecyclerView.setAdapter(orderAdapter);

        orderAdapter.setOnItemCheckedChangeListener((totalCheckedTax, totalCheckedPrice) -> {
            totalChecked = totalCheckedPrice + totalCheckedTax;
            taxAmountTextView.setText(String.format("RM %.2f", totalCheckedTax));
            totalAmountTextView.setText(String.format("RM %.2f", totalChecked));
        });

        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        loadOrders();

        Button payButton = view.findViewById(R.id.pay_button);
        payButton.setOnClickListener(v -> {
            List<String> checkedItemKeys = orderAdapter.getCheckedItemKeys();

            // Validation: Check if the amount exceeds wallet balance
            if (totalChecked > walletAmt) {
                Toast.makeText(getContext(), "Amount exceeds wallet balance.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkedItemKeys.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one item to proceed.", Toast.LENGTH_SHORT).show();
            } else {
                // Update the status of each checked item in the database
                for (String key : checkedItemKeys) {
                    ordersRef.child(key).child("status").setValue(1);
                }

                // Reference to the user's wallet amount in the "Wallets" node
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId);

                // Fetch the current wallet balance
                walletRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                        double updatedWalletAmt = currentWalletAmt - totalChecked;
                        String dateTime = getCurrentDateTime();

                        // Update the new wallet balance in the database
                        walletRef.child("walletAmt").setValue(updatedWalletAmt).addOnCompleteListener(taskUpdate -> {
                            if (taskUpdate.isSuccessful()) {
                                DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                                String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                                Transaction transaction = new Transaction(transactionId, R.drawable.select_pay, dateTime, "Select & Pay", billNo, totalChecked);
                                transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("Transaction", "Transaction saved successfully");

                                        // Navigate to SelectPayDoneFragment
                                        Bundle bundle = new Bundle();
                                        bundle.putString("userId", userId);
                                        bundle.putString("billNo", billNo);
                                        bundle.putDouble("totalChecked", totalChecked);

                                        SelectPayDoneFragment selectPayDoneFragment = new SelectPayDoneFragment();
                                        selectPayDoneFragment.setArguments(bundle);
                                        getParentFragmentManager().beginTransaction()
                                                .replace(R.id.frameLayout, selectPayDoneFragment)
                                                .addToBackStack(null)
                                                .commit();
                                    } else {
                                        Log.e("Transaction", "Failed to save transaction", task1.getException());
                                    }
                                });

                            } else {
                                Toast.makeText(getContext(), "Failed to update wallet balance.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve wallet balance.", Toast.LENGTH_SHORT).show();
                    }
                });
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

                    if (dataSnapshot.child("billNo").getValue() != null) {
                        billNo = dataSnapshot.child("billNo").getValue(String.class);
                        billNoTextView.setText(getString(R.string.bill_no) + billNo);
                    }

                    // Iterate through each order item
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        if (!orderSnapshot.getKey().equals("billNo")) {
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