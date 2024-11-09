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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SplitBillFragment extends Fragment {

    String userId, billNo;
    double walletAmt, taxAmount, totalPrice, splitPrice, finalRoundedSplitPrice;
    BigDecimal roundedSplitPrice;
    int quantity;

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<HashMap<String, String>> ordersList = new ArrayList<>();
    private DatabaseReference ordersRef;

    private TextView billNoTextView, taxAmountTextView, totalAmountTextView, splitIntoTextView, splitAmountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_split_bill, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        billNoTextView = view.findViewById(R.id.bill_no);
        taxAmountTextView = view.findViewById(R.id.tax_amount);
        totalAmountTextView = view.findViewById(R.id.total_amount);
        splitIntoTextView = view.findViewById(R.id.split_into);
        splitAmountTextView = view.findViewById(R.id.split_amount);

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            walletAmt = arguments.getDouble("walletAmt");
            quantity = arguments.getInt("quantity", 1);
            splitIntoTextView.setText(getString(R.string.split_into) + " 1/" + quantity);
        }

        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderAdapter = new OrderAdapter(ordersList, true, false);
        ordersRecyclerView.setAdapter(orderAdapter);

        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        loadOrders();

        Button payButton = view.findViewById(R.id.pay_button);
        payButton.setOnClickListener(v -> {
            // Validation: Check if the amount exceeds wallet balance
            if (finalRoundedSplitPrice > walletAmt) {
                Toast.makeText(getContext(), "Amount exceeds wallet balance.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reference to the user's wallet balance in the database
            DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallets").child("W" + userId);

            // Fetch the current wallet balance
            walletRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    double currentWalletAmt = task.getResult().child("walletAmt").getValue(Double.class);
                    double updatedWalletAmt = currentWalletAmt - finalRoundedSplitPrice;
                    String dateTime = getCurrentDateTime();

                    // Update the new wallet balance in the database
                    walletRef.child("walletAmt").setValue(updatedWalletAmt).addOnCompleteListener(taskUpdate -> {
                        if (taskUpdate.isSuccessful()) {
                            DatabaseReference transactionHistoryRef = walletRef.child("transactionHistory");
                            String transactionId = transactionHistoryRef.push().getKey(); // Generate transaction ID

                            Transaction transaction = new Transaction(transactionId, R.drawable.split_bill, dateTime, "Split Bill", billNo, finalRoundedSplitPrice);
                            transactionHistoryRef.child(transactionId).setValue(transaction).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d("Transaction", "Transaction saved successfully");

                                    // Navigate to SplitBillDoneFragment
                                    Bundle bundle = new Bundle();
                                    bundle.putString("userId", userId);
                                    bundle.putString("billNo", billNo);
                                    bundle.putInt("quantity", quantity);
                                    bundle.putDouble("finalRoundedSplitPrice", finalRoundedSplitPrice);

                                    SplitBillDoneFragment splitBillDoneFragment = new SplitBillDoneFragment();
                                    splitBillDoneFragment.setArguments(bundle);

                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.frameLayout, splitBillDoneFragment)
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
                            HashMap<String, String> order = new HashMap<>();

                            // Extract each field
                            String name = orderSnapshot.child("name").getValue(String.class);
                            String image = orderSnapshot.child("image").getValue(String.class);
                            Double price = orderSnapshot.child("price").getValue(Double.class);  // Use Double for decimal
                            Long status = orderSnapshot.child("status").getValue(Long.class);  // Use Long for integer

                            // Add the retrieved values to the order HashMap
                            order.put("name", name);
                            order.put("image", image);
                            totalPrice += price;

                            String formattedPrice = price != null ? String.format("%.2f", price) : "0.00";  // Format to 2 decimal places
                            order.put("price", formattedPrice);  // Store formatted price as a String

                            order.put("status", status != null ? status.toString() : "0");

                            // Add each order to the list
                            ordersList.add(order);
                        }
                    }
                    taxAmount = totalPrice * 0.1;
                    totalPrice += taxAmount;
                    splitPrice = totalPrice / quantity;

                    // Use BigDecimal to round splitPrice to 2 decimal places
                    roundedSplitPrice = BigDecimal.valueOf(splitPrice).setScale(2, RoundingMode.HALF_UP);
                    finalRoundedSplitPrice = roundedSplitPrice.doubleValue();

                    taxAmountTextView.setText(String.format("RM %.2f", taxAmount));
                    totalAmountTextView.setText(String.format("RM %.2f", totalPrice));
                    splitAmountTextView.setText(String.format("RM %.2f", finalRoundedSplitPrice));

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