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

    String userId, userName;

    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    Toolbar toolbar;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Scan scan;

    // Declare permission launcher
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

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance("https://paymentapp-1f1bf-default-rtdb.firebaseio.com/").getReference("Users");

        scan = new Scan(this);

        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        if (getSupportActionBar() != null && userName != null) {
            getSupportActionBar().setTitle("  Good Day, " + userName);
        }

        // Check and request notifications permission
        checkAndRequestNotificationPermission();

        // Load default fragment if there is no saved instance state
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
        }

        // Set up bottom navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Uncheck all bottom navigation items
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
                menuItem.setChecked(false);
            }
            item.setChecked(true);

            if (item.getItemId() == R.id.home) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);

                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
            } else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
            }
            return true;
        });

        // Set up Floating Action Button click listener
        fab.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            SelectPaymentMethodFragment selectPaymentMethodFragment = new SelectPaymentMethodFragment();
            selectPaymentMethodFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectPaymentMethodFragment).commit();

        });
        // fab.setOnClickListener(view -> scan.startScan());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }

        item.setChecked(true);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        scan.handleActivityResult(requestCode, resultCode, data);
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionDialog();
            }
        }
    }

    private void showNotificationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Allow Android Notifications")
                .setMessage("This app would like to send you notifications. Would you like to allow it?")
                .setPositiveButton("Allow", (dialog, which) -> requestNotificationPermission())
                .setNegativeButton("Don't allow", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void requestNotificationPermission() {
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }
}
