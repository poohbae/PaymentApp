package com.example.paymentapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<HashMap<String, String>> ordersList;
    private List<Boolean> checkedStates;  // Track the checked state for each item
    private boolean initialCheckedState; // Determines if checkboxes are checked initially
    private boolean checkBoxEnabled; // Determines if checkboxes are enabled
    private boolean isAllChecked = false; // Track if all items are checked or not

    // Interface for communicating item check status changes
    public interface OnItemCheckedChangeListener {
        void onItemCheckedChange(double totalCheckedTax, double totalCheckedPrice);
    }

    private OnItemCheckedChangeListener itemCheckedChangeListener;

    public OrderAdapter(List<HashMap<String, String>> ordersList, boolean initialCheckedState, boolean checkBoxEnabled) {
        this.ordersList = ordersList;
        this.initialCheckedState = initialCheckedState;
        this.checkBoxEnabled = checkBoxEnabled;
        this.checkedStates = new ArrayList<>();
        initializeCheckedStates(ordersList.size());
    }

    // Sets a listener for item checked state changes
    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        this.itemCheckedChangeListener = listener;
    }

    // Initializes or resets the checked states for each item based on initialCheckedState
    public void initializeCheckedStates(int size) {
        checkedStates = new ArrayList<>(Collections.nCopies(size, initialCheckedState));
        notifyDataSetChanged();
    }

    // Returns the list of keys for items that are checked
    public List<String> getCheckedItemKeys() {
        List<String> checkedItemKeys = new ArrayList<>();
        for (int i = 0; i < ordersList.size(); i++) {
            if (checkedStates.get(i)) {
                checkedItemKeys.add(ordersList.get(i).get("key"));
            }
        }
        return checkedItemKeys;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        // Retrieve data for the current order item
        HashMap<String, String> order = ordersList.get(position);

        // Set text for food name and price
        holder.foodNameTextView.setText(order.get("name"));
        holder.foodPriceTextView.setText("RM " + order.get("price"));

        // Set image for the order item
        String imageName = order.get("image");
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(imageName, "drawable", holder.itemView.getContext().getPackageName());
        holder.foodImageView.setImageResource(imageResId);

        // Set the checked state and enable/disable checkbox based on checkBoxEnabled
        holder.checkBox.setChecked(checkedStates.get(position));
        holder.checkBox.setEnabled(checkBoxEnabled);

        // Listen for checkbox state changes
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStates.set(position, isChecked);  // Update checked state in the list
            if (itemCheckedChangeListener != null) {
                // Notify listener with updated total tax and price for checked items
                itemCheckedChangeListener.onItemCheckedChange(getTotalCheckedTax(), getTotalCheckedPrice());
            }
        });
    }

    // Calculates the total tax for all checked items
    public double getTotalCheckedTax() {
        double totalTax = 0.0;
        for (int i = 0; i < ordersList.size(); i++) {
            if (checkedStates.get(i)) {
                String priceStr = ordersList.get(i).get("price");
                double price = Double.parseDouble(priceStr);
                totalTax += price * 0.1;
            }
        }
        return totalTax;
    }

    // Calculates the total price for all checked items
    public double getTotalCheckedPrice() {
        double total = 0.0;
        for (int i = 0; i < ordersList.size(); i++) {
            if (checkedStates.get(i)) {
                String priceStr = ordersList.get(i).get("price");
                total += Double.parseDouble(priceStr);
            }
        }
        return total;
    }

    @Override
    public int getItemCount() {
        return ordersList.size();  // Return the total number of orders
    }

    // Toggles the checked state for all items
    public void checkUncheckAll() {
        isAllChecked = !isAllChecked;

        for (int i = 0; i < checkedStates.size(); i++) {
            checkedStates.set(i, isAllChecked);
        }

        notifyDataSetChanged();
    }

    // ViewHolder class to manage individual order item views in the RecyclerView
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView foodNameTextView, foodPriceTextView;
        CheckBox checkBox;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize item views from the layout
            foodImageView = itemView.findViewById(R.id.food_image);
            foodNameTextView = itemView.findViewById(R.id.food_name);
            foodPriceTextView = itemView.findViewById(R.id.food_price);
            checkBox = itemView.findViewById(R.id.check);
        }
    }
}
