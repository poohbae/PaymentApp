package com.example.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {

    EditText loginInput, loginPassword;
    Button loginButton;
    DatabaseReference databaseReference; // Reference to Firebase Realtime Database
    FirebaseAuth mAuth; // Firebase Authentication
    TextView signUp, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication and Realtime Database reference to "Users" table
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://paymentapp-1f1bf-default-rtdb.firebaseio.com/").getReference("Users");

        loginInput = findViewById(R.id.login_input);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signUp = findViewById(R.id.sign_up);
        forgotPassword = findViewById(R.id.forgot_password);

        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        forgotPassword.setOnClickListener(view -> showForgotPasswordDialog());

        loginButton.setOnClickListener(view -> {
            String input = loginInput.getText().toString().trim();
            String password = loginPassword.getText().toString();

            if (input.isEmpty()) {
                loginInput.setError("Email or Mobile number cannot be empty");
                loginInput.requestFocus();
            } else if (password.isEmpty()) {
                loginPassword.setError("Password cannot be empty");
                loginPassword.requestFocus();
            } else {
                // Check whether input is an email or mobile number
                if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                    // Login by email
                    loginUserByEmail(input, password);
                } else {
                    // Otherwise, treat it as a mobile number
                    loginUserByMobileNumber(input, password);
                }
            }
        });
    }

    private void showForgotPasswordDialog() {
        final View dialogView = getLayoutInflater().inflate(R.layout.forgot_password_dialog, null);
        final EditText input = dialogView.findViewById(R.id.email_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Send", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        sendButton.setOnClickListener(view -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                input.setError("Email cannot be empty");
                input.requestFocus();
                return;
            }

            databaseReference.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(resetTask -> {
                        if (resetTask.isSuccessful()) {
                            Toast.makeText(Login.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(Login.this, resetTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    input.setError("Email not found");
                    input.requestFocus();
                }
            });
        });
    }

    // Function to log in the user by email
    private void loginUserByEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        databaseReference.child(user.getUid()).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful() && task1.getResult().exists()) {
                                String userName = task1.getResult().child("name").getValue(String.class);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("userName", userName);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                }
            } else {
                Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to log in the user by mobile number
    private void loginUserByMobileNumber(String mobileNumber, String password) {
        if (mobileNumber.startsWith("0")) {
            mobileNumber = "+60" + mobileNumber.substring(1);
        }

        databaseReference.orderByChild("mobileNumber").equalTo(mobileNumber).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot userSnapshot = task.getResult().getChildren().iterator().next();
                String email = userSnapshot.child("email").getValue(String.class); // Get the email

                // Now sign in using email in Firebase Authentication
                loginUserByEmailAndPassword(email, password);
            } else {
                Toast.makeText(Login.this, "Mobile number not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to log in with the email retrieved from mobile number and the encoded password
    private void loginUserByEmailAndPassword(String email, String password) {
        String encodedPassword = encodePassword(password);
        mAuth.signInWithEmailAndPassword(email, encodedPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // Password and email match, navigate to the main activity
                    databaseReference.child(user.getUid()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful() && task1.getResult().exists()) {
                            String userName = task1.getResult().child("name").getValue(String.class);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("userName", userName);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(Login.this, "Email not verified or incorrect password", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
            } else {
                Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to encode the password using SHA-256
    private String encodePassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}