package com.example.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Register extends AppCompatActivity {

    EditText editTextName, editTextMobileNumber, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button registerButton, verifiedButton;
    TextView signIn;
    FirebaseAuth mAuth;
    DatabaseReference databaseReferenceUsers, databaseReferenceWallets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database references
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseReferenceWallets = FirebaseDatabase.getInstance().getReference("Wallets");

        // Initialize UI elements
        editTextName = findViewById(R.id.name);
        editTextMobileNumber = findViewById(R.id.mobile_number);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        verifiedButton = findViewById(R.id.verified_button);
        signIn = findViewById(R.id.sign_in);

        // Set up registration button click event
        registerButton.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            String mobileNumber = editTextMobileNumber.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim().toLowerCase();
            String password = editTextPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();

            if (password.equals(confirmPassword)) {
                registerUserWithEmailPassword(email, password, name, mobileNumber);  // Pass mobile number during registration
            } else {
                editTextConfirmPassword.setError("Passwords do not match");
            }
        });

        // Set up sign-in redirect
        signIn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // Set up verified button click event
        verifiedButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                // Reload the user to check if their email is verified
                user.reload().addOnCompleteListener(reloadTask -> {
                    if (user.isEmailVerified()) {
                        // Save the user data to the database
                        storeUserDataAndWallet(user, editTextName.getText().toString(), editTextMobileNumber.getText().toString().trim());

                        // Redirect to the login page
                        Intent intent = new Intent(Register.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Register.this, "Please verify your email before proceeding.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Register.this, "User not found. Please register again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to register user with Firebase Authentication
    private void registerUserWithEmailPassword(String email, String password, String name, String mobileNumber) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Send verification email
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(Register.this, "Verification email sent. Please verify your email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to store user data and wallet in the Firebase Database after verification
    private void storeUserDataAndWallet(FirebaseUser firebaseUser, String name, String mobileNumber) {
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        // Create a user object
        User user = new User(name, mobileNumber, email);

        // Create a wallet object for the user, linked to the userId
        String walletId = "W" + userId; // Wallet ID linked to user
        Wallet wallet = new Wallet(0.0);

        // Store the user and wallet in the Firebase Database
        databaseReferenceUsers.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Store the wallet in the separate Wallets table, linked by userId
                databaseReferenceWallets.child(walletId).setValue(wallet).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(Register.this, "User and wallet data saved to the database", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Failed to save wallet data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // User class to store user details
    public static class User {
        public String name;
        public String mobileNumber;
        public String email;

        public User(String name, String mobileNumber, String email) {
            this.name = name;
            this.mobileNumber = mobileNumber;
            this.email = email;
        }
    }

    // Wallet class to store wallet details linked with userId
    public static class Wallet {
        public double walletAmt;
        public ArrayList<Transaction> transactionHistory;

        public Wallet(double walletAmt) {
            this.walletAmt = walletAmt;
            this.transactionHistory = new ArrayList<>(); // Ensure transactionHistory is initialized
        }
    }


}
