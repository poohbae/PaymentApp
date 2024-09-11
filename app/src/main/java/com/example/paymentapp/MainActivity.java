package com.example.paymentapp;

import android.app.Dialog;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawerLayout);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        NavigationView sideNavigationView = findViewById(R.id.sideNavigationView);
        sideNavigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load default fragment if there is no saved instance state
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
        }

        // Set up bottom navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.dashboard) {
            Toast.makeText(MainActivity.this, "Dashboard is clicked", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DashboardFragment()).commit();
        } else if (item.getItemId() == R.id.audience) {
            Toast.makeText(MainActivity.this, "Audience is clicked", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AudienceFragment()).commit();
        } else if (item.getItemId() == R.id.logout) {
            Toast.makeText(MainActivity.this, "Logout is clicked", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);  // Close the drawer after selection
        return true;
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
        scanArea.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(MainActivity.this, "Scan Area is clicked", Toast.LENGTH_SHORT).show();
        });

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
}
