package com.example.paymentapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String userId, userImageUrl, userName, userMobileNumber;

    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    Toolbar toolbar;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Scan scan;

    // Declare permission launcher for handling notification permissions
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Set Firebase Realtime Database reference for Users data
        databaseReference = FirebaseDatabase.getInstance("https://paymentapp-1f1bf-default-rtdb.firebaseio.com/").getReference("Users");

        // Retrieve user details from Intent extras
        userId = getIntent().getStringExtra("userId");
        userImageUrl = getIntent().getStringExtra("userImageUrl");
        userName = getIntent().getStringExtra("userName");
        userMobileNumber = getIntent().getStringExtra("userMobileNumber");

        // Initialize scan functionality with the current userId
        scan = new Scan(this, userId);

        // Set toolbar title
        if (getSupportActionBar() != null && userName != null) {
            getSupportActionBar().setTitle("  Good Day, " + userName);
        }

        // Check and request notification permission
        checkAndRequestNotificationPermission();

        // Load default fragment when activity starts if there is no saved state
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
        }

        // Set up bottom navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Uncheck all items in bottom navigation to ensure only the selected item is checked
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
                menuItem.setChecked(false);
            }
            item.setChecked(true);  // Mark the selected item as checked

            // Navigate to the appropriate fragment based on selected item
            if (item.getItemId() == R.id.home) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);

                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
            } else if (item.getItemId() == R.id.settings) {
                Bundle bundle = new Bundle();
                bundle.putString("userImageUrl", userImageUrl);
                bundle.putString("userName", userName);
                bundle.putString("userMobileNumber", userMobileNumber);

                SettingsFragment settingsFragment = new SettingsFragment();
                settingsFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, settingsFragment).commit();}
            return true;
        });

        // Set up Floating Action Button to initiate scan functionality when clicked
        fab.setOnClickListener(view -> scan.startScan());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Uncheck all bottom navigation items when one is selected
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }

        // Mark the selected item as checked
        item.setChecked(true);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle scan results if scan is initialized
        if (scan != null) {
            scan.handleActivityResult(requestCode, resultCode, data);
        }
    }

    // Checks and requests notification permission for Android 13 (TIRAMISU) and above
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionDialog();
            }
        }
    }

    // Shows a dialog to ask the user for notification permission
    private void showNotificationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Allow Android Notifications")
                .setMessage("This app would like to send you notifications. Would you like to allow it?")
                .setPositiveButton("Allow", (dialog, which) -> requestNotificationPermission())
                .setNegativeButton("Don't allow", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Requests notification permission from the user
    private void requestNotificationPermission() {
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }
}
