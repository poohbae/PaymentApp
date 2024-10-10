package com.example.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    DatabaseReference databaseReferenceUsers;
    FirebaseAuth mAuth;
    TextView signUp, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication and Database
        mAuth = FirebaseAuth.getInstance();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signUp = findViewById(R.id.sign_up);
        forgotPassword = findViewById(R.id.forgot_password);

        // Sign-up redirect
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        // Login button click listener
        loginButton.setOnClickListener(view -> {
            String email = loginEmail.getText().toString().trim().toLowerCase(); // Convert email to lowercase
            String password = loginPassword.getText().toString();

            if (email.isEmpty()) {
                loginEmail.setError("Email cannot be empty");
                loginEmail.requestFocus();
            } else if (password.isEmpty()) {
                loginPassword.setError("Password cannot be empty");
                loginPassword.requestFocus();
            } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Proceed to login if the input is a valid email format
                checkIfEmailExists(email, password);
            } else {
                loginEmail.setError("Invalid email format");
                loginEmail.requestFocus();
            }
        });

        forgotPassword.setOnClickListener(view -> showForgotPasswordDialog());
    }

    // Check if the email exists in Firebase Realtime Database
    private void checkIfEmailExists(String email, String password) {
        databaseReferenceUsers.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Email exists, proceed to login
                loginUserByEmail(email, password);
            } else {
                loginEmail.setError("Email not found");
                loginEmail.requestFocus();
            }
        });
    }

    // Function to log in user by email and pass the user's name to MainActivity
    private void loginUserByEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    // Check if the user's email is verified
                    if (user.isEmailVerified()) {
                        // Retrieve the user name from the database
                        databaseReferenceUsers.child(user.getUid()).get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                String userName = userTask.getResult().child("name").getValue(String.class);

                                // Pass the user name to MainActivity
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("userName", userName);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();  // Sign out the user if the email is not verified
                    }
                }
            } else {
                loginPassword.setError("Incorrect password. Please try again.");
                loginPassword.requestFocus();
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

            // Check if email exists in the Firebase Realtime Database
            databaseReferenceUsers.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // If the email exists, send the password reset email
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
}
