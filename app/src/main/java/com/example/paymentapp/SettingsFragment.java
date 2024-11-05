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
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    String userImageUrl, userName, userMobileNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ImageView userImageView = view.findViewById(R.id.user_image);
        TextView userNameTextView = view.findViewById(R.id.user_name);
        TextView mobileNumberTextView = view.findViewById(R.id.mobile_number);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userImageUrl = bundle.getString("userImageUrl");
            userName = bundle.getString("userName");
            userMobileNumber = bundle.getString("userMobileNumber");

            Glide.with(getContext())
                    .load(userImageUrl)
                    .placeholder(R.drawable.person)
                    .into(userImageView);
            userNameTextView.setText(userName);
            mobileNumberTextView.setText(userMobileNumber);
        }

        LinearLayout about = view.findViewById(R.id.about);
        about.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, new AboutFragment())
                .addToBackStack(null)
                .commit());

        LinearLayout helpAndSupport = view.findViewById(R.id.help_and_support);
        helpAndSupport.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, new HelpAndSupportFragment())
                .addToBackStack(null)
                .commit());

        LinearLayout logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }
}