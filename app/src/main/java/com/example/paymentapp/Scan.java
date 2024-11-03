package com.example.paymentapp;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Scan {
    private Activity activity;

    // Constructor to accept the current activity context
    public Scan(Activity activity) {
        this.activity = activity;
    }

    // Method to start the scan
    public void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);  // Use the back camera
        integrator.setBeepEnabled(true);  // Enable beep on successful scan
        integrator.setBarcodeImageEnabled(true);  // Save the barcode image
        integrator.initiateScan();
    }

    // Method to handle scan result in the calling activity
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(activity, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // Process the scan result
                String scannedData = result.getContents();
                Toast.makeText(activity, "Scanned: " + scannedData, Toast.LENGTH_LONG).show();
                // Optionally: Pass scannedData to another activity or method
            }
        }
    }
}
