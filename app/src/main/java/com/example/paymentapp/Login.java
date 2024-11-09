package com.example.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.util.TypedValue;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    ImageView togglePasswordVisibility;
    Button loginButton;
    TextView signUp, forgotPassword;
    DatabaseReference databaseReferenceUsers;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication and Realtime Database references
        mAuth = FirebaseAuth.getInstance();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        togglePasswordVisibility = findViewById(R.id.toggle_password);
        loginButton = findViewById(R.id.login_button);
        signUp = findViewById(R.id.sign_up);
        forgotPassword = findViewById(R.id.forgot_password);

        // Toggle password visibility
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                // Save current text style and position to restore after visibility change
                int cursorPosition = passwordEditText.getSelectionStart();
                Typeface currentTypeface = passwordEditText.getTypeface();
                float currentTextSize = passwordEditText.getTextSize();

                if (isPasswordVisible) {
                    // Hide password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.visibility_on);
                } else {
                    // Show password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.visibility_off);
                }

                // Restore text style and cursor position
                passwordEditText.setTypeface(currentTypeface);
                passwordEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
                passwordEditText.setSelection(cursorPosition);

                // Toggle the password visibility flag
                isPasswordVisible = !isPasswordVisible;
            }
        });

        // Redirect to sign-up page when "Sign Up" is clicked
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        // Handle login button click
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim().toLowerCase();
            String password = passwordEditText.getText().toString();

            // Validate email and password inputs
            if (email.isEmpty()) {
                emailEditText.setError("Email cannot be empty");
                emailEditText.requestFocus();
            } else if (password.isEmpty()) {
                passwordEditText.setError("Password cannot be empty");
                passwordEditText.requestFocus();
            } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // If email format is valid, proceed with login
                loginUserByEmail(email, password);
            } else {
                emailEditText.setError("Invalid email format");
                emailEditText.requestFocus();
            }
        });

        // Show forgot password dialog when clicked
        forgotPassword.setOnClickListener(view -> showForgotPasswordDialog());
    }

    // Logs in the user using email and password, then navigates to MainActivity if successful
    private void loginUserByEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    // Check if the user's email is verified
                    if (user.isEmailVerified()) {
                        // Retrieve additional user details from the database
                        databaseReferenceUsers.child(user.getUid()).get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                String userImageUrl = userTask.getResult().child("image").getValue(String.class);
                                String userName = userTask.getResult().child("name").getValue(String.class);
                                String userMobileNumber = userTask.getResult().child("mobileNumber").getValue(String.class);

                                // Pass user details to MainActivity
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("userImageUrl", userImageUrl);
                                intent.putExtra("userName", userName);
                                intent.putExtra("userMobileNumber", userMobileNumber);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        // Notify user to verify their email if not verified
                        Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();  // Sign out if email is not verified
                    }
                }
            } else {
                // Show error if password is incorrect
                passwordEditText.setError("Incorrect password. Please try again.");
                passwordEditText.requestFocus();
            }
        });
    }

    // Displays a dialog for password reset via email
    private void showForgotPasswordDialog() {
        final View dialogView = getLayoutInflater().inflate(R.layout.forgot_password_dialog, null);
        final EditText input = dialogView.findViewById(R.id.email_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Send", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set up "Send" button click logic for sending password reset email
        Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        sendButton.setOnClickListener(view -> {
            String email = input.getText().toString().trim();

            // Validate email input
            if (email.isEmpty()) {
                input.setError("Email cannot be empty");
                input.requestFocus();
                return;
            }

            // Check if email exists in Firebase Realtime Database
            databaseReferenceUsers.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // If email exists, send reset email
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(resetTask -> {
                        if (resetTask.isSuccessful()) {
                            Toast.makeText(Login.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            // Show error if reset email failed
                            Toast.makeText(Login.this, resetTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Show error if email not found in the database
                    input.setError("Email not found");
                    input.requestFocus();
                }
            });
        });
    }
}
