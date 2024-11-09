package com.example.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    String userImageUrl, userName, userMobileNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ImageView userImageView = view.findViewById(R.id.user_image);
        TextView userNameTextView = view.findViewById(R.id.user_name);
        TextView mobileNumberTextView = view.findViewById(R.id.mobile_number);

        // Retrieve arguments passed to this fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            userImageUrl = bundle.getString("userImageUrl");
            userName = bundle.getString("userName");
            userMobileNumber = bundle.getString("userMobileNumber");

            // Load and display the person's image using Glide
            Glide.with(getContext())
                    .load(userImageUrl)  // URL of the person’s image
                    .placeholder(R.drawable.person)  // Placeholder image if loading fails
                    .into(userImageView);
            userNameTextView.setText(userName);
            mobileNumberTextView.setText(userMobileNumber);
        }

        // Set up About section to navigate to AboutFragment on click
        LinearLayout about = view.findViewById(R.id.about);
        about.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, new AboutFragment())
                .addToBackStack(null)
                .commit());

        // Set up Help and Support section to navigate to HelpAndSupportFragment on click
        LinearLayout helpAndSupport = view.findViewById(R.id.help_and_support);
        helpAndSupport.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, new HelpAndSupportFragment())
                .addToBackStack(null)
                .commit());

        // Set up Logout functionality
        LinearLayout logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            // Sign out from Firebase authentication
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to Login activity and clear back stack
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    // Hide the ActionBar in this fragment
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    // Show the ActionBar when leaving this fragment
    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }
}