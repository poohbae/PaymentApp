package com.example.paymentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Scan {
    private final Activity activity;
    private final String userId;

    // Constructor to initialize Scan with the current activity context and userId
    public Scan(Activity activity, String userId) {
        this.activity = activity;
        this.userId = userId;
    }

    // Start the scan and automatically navigate to the next fragment after a delay
    public void startScan() {
        // Initialize the QR code scanner using IntentIntegrator
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES); // Allow all barcode formats
        integrator.setPrompt("Scan a QR code"); // Set a message prompt for the user
        integrator.setCameraId(0);  // Use the back camera for scanning
        integrator.setBeepEnabled(true);  // Enable beep sound on successful scan
        integrator.setBarcodeImageEnabled(true);  // Save a copy of the scanned barcode image
        integrator.setOrientationLocked(true);  // Lock screen orientation to avoid disruption
        integrator.initiateScan(); // Start the scanning process

        // Delay to simulate time for the scan and automatically navigate after 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(activity, "Scan completed", Toast.LENGTH_LONG).show();
            navigateToFragment();  // Call the navigation function after delay
        }, 5000); // Set to a 5-second delay
    }

    // Handle scan results from QR code scanner
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            navigateToFragment();
        }
    }

    // Navigate to SelectPaymentMethodFragment
    private void navigateToFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);

        SelectPaymentMethodFragment selectPaymentMethodFragment = new SelectPaymentMethodFragment();
        selectPaymentMethodFragment.setArguments(bundle);

        // Ensure the current activity is an AppCompatActivity to support fragment transactions
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, selectPaymentMethodFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
