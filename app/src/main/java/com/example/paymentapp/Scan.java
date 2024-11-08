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

    // Constructor to accept the current activity context and userId
    public Scan(Activity activity, String userId) {
        this.activity = activity;
        this.userId = userId;
    }

    // Method to start the scan and navigate automatically after 3 seconds
    public void startScan() {
        // Start the scanning activity
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);  // Use the back camera
        integrator.setBeepEnabled(true);  // Enable beep on successful scan
        integrator.setBarcodeImageEnabled(true);  // Save the barcode image
        integrator.setOrientationLocked(true);  // Lock orientation to avoid restarting
        integrator.initiateScan();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(activity, "Scan completed", Toast.LENGTH_LONG).show();
            navigateToFragment();
        }, 5000); // 5-second delay
    }

    // Handle scan results
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            navigateToFragment();
        }
    }

    private void navigateToFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);

        SelectPaymentMethodFragment selectPaymentMethodFragment = new SelectPaymentMethodFragment();
        selectPaymentMethodFragment.setArguments(bundle);

        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, selectPaymentMethodFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}