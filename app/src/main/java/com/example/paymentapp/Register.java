package com.example.paymentapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Register extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 100;
    private static final int PICK_IMAGE_REQUEST = 1;

    EditText nameEditText, mobileNumberEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility, userProfileImage;
    Button uploadImageButton, registerButton, verifiedButton;
    TextView signIn;
    FirebaseAuth mAuth;
    DatabaseReference databaseReferenceUsers, databaseReferenceWallets;
    Uri imageUri;
    String imageFileName;
    String imageUrl;


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
        nameEditText = findViewById(R.id.name);
        mobileNumberEditText = findViewById(R.id.mobile_number);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        togglePasswordVisibility = findViewById(R.id.toggle_password);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggle_confirm_password);
        userProfileImage = findViewById(R.id.user_profile_image);
        uploadImageButton = findViewById(R.id.upload_image_button);
        registerButton = findViewById(R.id.register_button);
        verifiedButton = findViewById(R.id.verified_button);
        signIn = findViewById(R.id.sign_in);

        // Password visibility toggle logic
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
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

                passwordEditText.setTypeface(currentTypeface);
                passwordEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
                passwordEditText.setSelection(cursorPosition);

                isPasswordVisible = !isPasswordVisible;
            }
        });

        toggleConfirmPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            boolean isConfirmPasswordVisible = false;
            @Override
            public void onClick(View v) {
                int cursorPosition = passwordEditText.getSelectionStart();
                Typeface currentTypeface = passwordEditText.getTypeface();
                float currentTextSize = passwordEditText.getTextSize();

                if (isConfirmPasswordVisible) {
                    // Hide confirm password
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleConfirmPasswordVisibility.setImageResource(R.drawable.visibility_on);
                } else {
                    // Show confirm password
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleConfirmPasswordVisibility.setImageResource(R.drawable.visibility_off);
                }

                confirmPasswordEditText.setTypeface(currentTypeface);
                confirmPasswordEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
                confirmPasswordEditText.setSelection(cursorPosition);

                isConfirmPasswordVisible = !isConfirmPasswordVisible;
            }
        });

        uploadImageButton.setOnClickListener(v -> {
            String nameInput = nameEditText.getText().toString().trim();

            if (nameInput.isEmpty()) {
                // Show a message if the name field is empty
                Toast.makeText(Register.this, "Please enter your name before uploading the image", Toast.LENGTH_SHORT).show();
            } else {
                // Allow image upload if the name is entered
                openImageChooser();
            }
        });

        // Set up registration button click event
        registerButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String mobileNumber = mobileNumberEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim().toLowerCase();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (validateInput(name, mobileNumber, email, password, confirmPassword)) {
                registerUserWithEmailPassword(email, password);
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
                        storeUserDataWithImage();
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

    // Function to validate inputs
    private boolean validateInput(String name, String mobileNumber, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            nameEditText.requestFocus();
            return false;
        }
        if (mobileNumber.isEmpty()) {
            mobileNumberEditText.setError("Mobile number cannot be empty");
            mobileNumberEditText.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            emailEditText.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            passwordEditText.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }
        return true;
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            // If permission is granted, proceed with the image chooser
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Permission to access media was denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            userProfileImage.setImageURI(imageUri);  // Display the selected image
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            imageFileName = nameEditText.getText().toString().trim().toLowerCase();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profileImages/" + imageFileName);

            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    Toast.makeText(Register.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(Register.this, "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(Register.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(Register.this, "No image selected for upload.", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to register user with Firebase Authentication
    private void registerUserWithEmailPassword(String email, String password) {
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
    private void storeUserDataWithImage() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            String name = nameEditText.getText().toString().trim();
            String mobileNumber = mobileNumberEditText.getText().toString().trim();
            String email = firebaseUser.getEmail();

            // Create a user object with the image URL
            User user = new User(name, mobileNumber, email, imageUrl);

            // Create a wallet object for the user, linked to the userId
            String walletId = "W" + userId; // Wallet ID linked to user
            Wallet wallet = new Wallet(0.0);

            // Save user data to Realtime Database
            databaseReferenceUsers.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Save wallet data after user data
                    databaseReferenceWallets.child(walletId).setValue(wallet).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            databaseReferenceWallets.child(walletId).child("transactionHistory").setValue(wallet.transactionHistory).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(Register.this, "User and wallet data saved to the database", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Register.this, "Failed to save wallet transaction history", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(Register.this, "Failed to save wallet data", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // User class to store user details
    public static class User {
        public String name;
        public String mobileNumber;
        public String email;
        public String image;

        public User(String name, String mobileNumber, String email, String image) {
            this.name = name;
            this.mobileNumber = mobileNumber;
            this.email = email;
            this.image = image;
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

    public static class Transaction {
        public String transactionId;
        public int status;  // request
        public int iconResId; // reload
        public String imageUrl;  // request and transfer
        public String datetime;
        public String source;
        public String note;  // request and transfe
        public String refId;
        public double amount;

        // For reload
        public Transaction(String transactionId, int iconResId, String datetime, String source, String refId, double amount) {
            this.transactionId = transactionId;
            this.iconResId = iconResId;
            this.datetime = datetime;
            this.source = source;
            this.refId = refId;
            this.amount = amount;
        }

        // For request
        public Transaction(String transactionId, int status, String imageUrl, String datetime, String source, String note, String refId, double amount) {
            this.transactionId = transactionId;
            this.status = status;
            this.imageUrl = imageUrl;
            this.datetime = datetime;
            this.source = source;
            this.note = note;
            this.refId = refId;
            this.amount = amount;
        }

        // For transfer
        public Transaction(String transactionId, String imageUrl, String datetime, String source, String note, String refId, double amount) {
            this.transactionId = transactionId;
            this.imageUrl = imageUrl;
            this.datetime = datetime;
            this.source = source;
            this.note = note;
            this.refId = refId;
            this.amount = amount;
        }
    }
}
