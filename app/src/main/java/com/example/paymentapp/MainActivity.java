package com.example.paymentapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    NavigationView sideNavigationView;
    Toolbar toolbar;
    DatabaseReference databaseReference; // Firebase Realtime Database reference
    FirebaseAuth mAuth;  // Firebase Authentication
    FirebaseUser currentUser;  // Store the current logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance("https://paymentapp-1f1bf-default-rtdb.firebaseio.com/").getReference("Users");

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawerLayout);
        sideNavigationView = findViewById(R.id.sideNavigationView);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // Retrieve the user's name from the intent and set it on the toolbar
        String userName = getIntent().getStringExtra("userName");
        if (userName != null) {
            toolbar.setTitle("Good Day, " + userName);
        }

        sideNavigationView.setNavigationItemSelectedListener(this);  // Set listener for side navigation

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load default fragment if there is no saved instance state
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
        }

        // Set up bottom navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Uncheck all bottom navigation items
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
                menuItem.setChecked(false); // Uncheck all items
            }

            // Check only the clicked item
            item.setChecked(true);

            // Uncheck all items in the side navigation when a bottom nav item is selected
            sideNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < sideNavigationView.getMenu().size(); i++) {
                sideNavigationView.getMenu().getItem(i).setChecked(false);
            }

            // Using if-else statements instead of switch cases
            if (item.getItemId() == R.id.home) {
                Toast.makeText(MainActivity.this, "Home is clicked", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
            } else if (item.getItemId() == R.id.portfolio) {
                Toast.makeText(MainActivity.this, "Portfolio is clicked", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PortfolioFragment()).commit();
            } else if (item.getItemId() == R.id.notifications) {
                Toast.makeText(MainActivity.this, "Notifications is clicked", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new NotificationsFragment()).commit();
            } else if (item.getItemId() == R.id.settings) {
                Toast.makeText(MainActivity.this, "Settings is clicked", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
            }
            return true;
        });

        // Set up Floating Action Button click listener
        fab.setOnClickListener(view -> showBottomDialog());
    }

    // Method to show a bottom dialog
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.scan_dialog);

        // Initialize dialog views
        LinearLayout scanArea = dialog.findViewById(R.id.scanArea);
        ImageView closeButton = dialog.findViewById(R.id.closeButton);

        // Set up click listener for scan area
        scanArea.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Scan Area is clicked", Toast.LENGTH_SHORT).show());

        // Set up click listener for close button
        closeButton.setOnClickListener(view -> dialog.dismiss());

        // Show the dialog and set its properties
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Uncheck all bottom navigation items when a side nav item is selected
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }

        // Using if-else instead of switch case
        if (item.getItemId() == R.id.dashboard) {
            Toast.makeText(MainActivity.this, "Dashboard is clicked", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DashboardFragment()).commit();
        } else if (item.getItemId() == R.id.audience) {
            Toast.makeText(MainActivity.this, "Audience is clicked", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AudienceFragment()).commit();
        } else if (item.getItemId() == R.id.logout) {
            // Log out the user using FirebaseAuth signOut method
            FirebaseAuth.getInstance().signOut();

            // Show a toast message
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to the Login activity
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish(); // Close the current activity so the user can't go back to it
        }

        drawerLayout.closeDrawer(GravityCompat.START);  // Close the drawer after selection
        item.setChecked(true);  // Ensure the selected item in the side navigation is checked
        return true;
    }
}