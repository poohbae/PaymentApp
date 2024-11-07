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

    public interface OnItemCheckedChangeListener {
        void onItemCheckedChange(double totalCheckedPrice);
    }

    private OnItemCheckedChangeListener itemCheckedChangeListener;

    public OrderAdapter(List<HashMap<String, String>> ordersList, boolean initialCheckedState, boolean checkBoxEnabled) {
        this.ordersList = ordersList;
        this.initialCheckedState = initialCheckedState;
        this.checkBoxEnabled = checkBoxEnabled;
        this.checkedStates = new ArrayList<>();
        initializeCheckedStates(ordersList.size());
    }

    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        this.itemCheckedChangeListener = listener;
    }

    // Method to initialize or reset checkedStates based on initialCheckedState
    public void initializeCheckedStates(int size) {
        checkedStates = new ArrayList<>(Collections.nCopies(size, initialCheckedState));
        notifyDataSetChanged();
    }

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        HashMap<String, String> order = ordersList.get(position);

        holder.foodNameTextView.setText(order.get("name"));
        holder.foodPriceTextView.setText("RM " + order.get("price"));

        String imageName = order.get("image");
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(imageName, "drawable", holder.itemView.getContext().getPackageName());
        holder.foodImageView.setImageResource(imageResId);

        holder.checkBox.setChecked(checkedStates.get(position));
        holder.checkBox.setEnabled(checkBoxEnabled);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStates.set(position, isChecked);
            if (itemCheckedChangeListener != null) {
                itemCheckedChangeListener.onItemCheckedChange(getTotalCheckedPrice());
            }
        });
    }

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
        return ordersList.size();
    }

    // Method to toggle check/uncheck all CheckBoxes
    public void checkUncheckAll() {
        isAllChecked = !isAllChecked;

        for (int i = 0; i < checkedStates.size(); i++) {
            checkedStates.set(i, isAllChecked);
        }

        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView foodNameTextView, foodPriceTextView;
        CheckBox checkBox;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.food_image);
            foodNameTextView = itemView.findViewById(R.id.food_name);
            foodPriceTextView = itemView.findViewById(R.id.food_price);
            checkBox = itemView.findViewById(R.id.check);  // Make sure your CheckBox id is correct
        }
    }
}
