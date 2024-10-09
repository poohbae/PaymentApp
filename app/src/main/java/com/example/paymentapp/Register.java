package com.example.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Register extends AppCompatActivity {

    EditText editTextName, editTextMobileNumber, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button registerButton;
    TextView signIn;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://paymentapp-1f1bf-default-rtdb.firebaseio.com/").getReference("Users");

        editTextName = findViewById(R.id.name);
        editTextMobileNumber = findViewById(R.id.mobile_number);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        signIn = findViewById(R.id.sign_in);

        registerButton.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();

            if (password.equals(confirmPassword)) {
                // Encode the password before passing to the registration method
                String encodedPassword = encodePassword(password);
                if (encodedPassword != null) {
                    registerUserWithEmailPassword(email, encodedPassword);  // Register with encoded password
                } else {
                    Toast.makeText(Register.this, "Error encoding password", Toast.LENGTH_SHORT).show();
                }
            } else {
                editTextConfirmPassword.setError("Passwords do not match");
            }
        });

        signIn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
    }

    // Function to register the user with email and encoded password
    private void registerUserWithEmailPassword(String email, String encodedPassword) {
        String name = editTextName.getText().toString(); // Get the name from input field
        String mobileNumber = editTextMobileNumber.getText().toString().trim(); // Get the mobile number from input field

        mAuth.createUserWithEmailAndPassword(email, encodedPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // User registered successfully, now send verification email
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(Register.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Call addUserToDatabase to save user details with the encoded password
                    addUserToDatabase(name, mobileNumber, email, encodedPassword); // Save the user with encoded password
                }

                // Optionally, direct the user to the login screen
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to add user to Firebase Realtime Database with the encoded password
    private void addUserToDatabase(String name, String mobileNumber, String email, String encodedPassword) {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long userCount = task.getResult().getChildrenCount(); // Number of users
                String userId = String.format("U%03d", userCount + 1); // Generate user ID like U001, U002

                // Create a user object with the encoded password
                User user = new User(userId, name, "+6" + mobileNumber, email, encodedPassword);

                // Add the user data to Firebase Realtime Database
                databaseReference.child(userId).setValue(user).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(Register.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Function to encode the password using SHA-256 (same as in Login.java)
    private String encodePassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP); // Encode as Base64 string
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // User class to store user details
    public static class User {
        public String userId;
        public String name;
        public String mobileNumber;
        public String email;
        public String password; // This will store the encoded password

        public User(String userId, String name, String mobileNumber, String email, String password) {
            this.userId = userId;
            this.name = name;
            this.mobileNumber = mobileNumber;
            this.email = email;
            this.password = password; // Store the encoded password
        }
    }
}