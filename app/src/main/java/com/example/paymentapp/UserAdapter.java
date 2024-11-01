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

    public enum FragmentType {
        TRANSFER,
        REQUEST
    }

    private List<HashMap<String, String>> userList;
    private final Context context;
    private final Fragment fragment;
    private final FragmentType fragmentType;
    private final double walletAmt;

    public UserAdapter(List<HashMap<String, String>> userList, Context context, Fragment fragment, FragmentType fragmentType, double walletAmt) {
        this.userList = userList;
        this.context = context;
        this.fragment = fragment;
        this.fragmentType = fragmentType;
        this.walletAmt = walletAmt;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        HashMap<String, String> user = userList.get(position);
        String id = user.get("id");
        String name = user.get("name");
        String mobileNumber = user.get("mobileNumber");
        String imageUrl = user.get("image");

        holder.personNameTextView.setText(name);
        holder.mobileNumberTextView.setText(mobileNumber);

        // Load image with Glide
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.person)
                .into(holder.personImageView);

        // Handle card click
        holder.cardView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("personId", id);
            bundle.putString("personName", name);
            bundle.putString("personMobileNumber", mobileNumber);
            bundle.putString("personImageUrl", imageUrl);
            if (fragmentType == FragmentType.TRANSFER) {
                bundle.putDouble("walletAmt", walletAmt);
            }

            FragmentTransaction transaction = fragment.getParentFragmentManager().beginTransaction();

            if (fragmentType == FragmentType.TRANSFER) {
                TransferMoneyFragment transferMoneyFragment = new TransferMoneyFragment();
                transferMoneyFragment.setArguments(bundle);
                transaction.replace(R.id.frameLayout, transferMoneyFragment);
            } else if (fragmentType == FragmentType.REQUEST) {
                RequestMoneyFragment requestMoneyFragment = new RequestMoneyFragment();
                requestMoneyFragment.setArguments(bundle);
                transaction.replace(R.id.frameLayout, requestMoneyFragment);
            }

            transaction.addToBackStack(null).commit();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

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