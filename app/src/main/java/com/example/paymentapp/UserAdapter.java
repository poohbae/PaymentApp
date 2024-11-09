package com.example.paymentapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.HashMap;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    // Enum for defining the type of fragment that the adapter will handle
    public enum FragmentType {
        TRANSFER,
        REQUEST
    }

    private List<HashMap<String, String>> userList;
    private final Context context;
    private final Fragment fragment;
    private final FragmentType fragmentType;
    private final String userId;
    private final String userImageUrl;
    private final double walletAmt;

    // Constructor to initialize UserAdapter with necessary data
    public UserAdapter(List<HashMap<String, String>> userList, Context context, Fragment fragment, FragmentType fragmentType, String userId, String userImageUrl, double walletAmt) {
        this.userList = userList;
        this.context = context;
        this.fragment = fragment;
        this.fragmentType = fragmentType;
        this.userId = userId;
        this.userImageUrl = userImageUrl;
        this.walletAmt = walletAmt;
    }

    // Inflate the layout for each user card and return a ViewHolder for it
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    // Bind data to each ViewHolder, setting up user details and handling click actions
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Get the user at the current position
        HashMap<String, String> user = userList.get(position);
        String id = user.get("id");
        String name = user.get("name");
        String mobileNumber = user.get("mobileNumber");
        String imageUrl = user.get("image");

        // Set user details in the views
        holder.personNameTextView.setText(name);
        holder.mobileNumberTextView.setText(mobileNumber);

        // Load image with Glide
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.person)
                .into(holder.personImageView);

        // Set up click listener for the user card
        holder.cardView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            bundle.putString("userImageUrl", userImageUrl);
            bundle.putString("personName", name);
            bundle.putString("personMobileNumber", mobileNumber);
            bundle.putString("personImageUrl", imageUrl);

            // Add additional data if the fragment type is TRANSFER
            if (fragmentType == FragmentType.TRANSFER) {
                bundle.putString("personId", id);
                bundle.putDouble("walletAmt", walletAmt);
            }

            FragmentTransaction transaction = fragment.getParentFragmentManager().beginTransaction();

            // Replace the fragment based on the type (TRANSFER or REQUEST)
            if (fragmentType == FragmentType.TRANSFER) {
                TransferMoneyFragment transferMoneyFragment = new TransferMoneyFragment();
                transferMoneyFragment.setArguments(bundle);
                transaction.replace(R.id.frameLayout, transferMoneyFragment);
            } else if (fragmentType == FragmentType.REQUEST) {
                RequestMoneyFragment requestMoneyFragment = new RequestMoneyFragment();
                requestMoneyFragment.setArguments(bundle);
                transaction.replace(R.id.frameLayout, requestMoneyFragment);
            }

            transaction.addToBackStack(null).commit(); // Add the transaction to the back stack
        });
    }

    // Return the total number of users in the list
    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class for holding and managing the views of a single user item
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView personImageView;
        TextView personNameTextView;
        TextView mobileNumberTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.user_card_view);
            personImageView = itemView.findViewById(R.id.person_image);
            personNameTextView = itemView.findViewById(R.id.person_name);
            mobileNumberTextView = itemView.findViewById(R.id.mobile_number);
        }
    }
}