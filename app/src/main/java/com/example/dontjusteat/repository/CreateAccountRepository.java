package com.example.dontjusteat.repository;

import android.app.Activity;
import android.widget.Toast;

import com.example.dontjusteat.model.User;
import com.example.dontjusteat.security.InputValidator;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccountRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // Constructor
    public CreateAccountRepository() {
        // initialize Firebase Auth and Firestore instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void createAccount(String email, String password, String name, String phone, Activity activity) {
        //sanitize inputs first
        final String sanitizedEmail = InputValidator.sanitize(email);
        final String sanitizedName = InputValidator.sanitize(name);
        final String sanitizedPhone = InputValidator.sanitize(phone);

        // Check for empty fields
        if (sanitizedEmail.isEmpty() || password.isEmpty() || sanitizedName.isEmpty() || sanitizedPhone.isEmpty()) {
            Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email using InputValidator
        if (!InputValidator.isValidEmail(sanitizedEmail)) {
            String error = InputValidator.getValidationError("email", sanitizedEmail);
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate name using InputValidator
        if (!InputValidator.isValidName(sanitizedName)) {
            String error = InputValidator.getValidationError("name", sanitizedName);
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone using InputValidator
        if (!InputValidator.isValidPhone(sanitizedPhone)) {
            String error = InputValidator.getValidationError("phone", sanitizedPhone);
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Password validation (minimum 6 characters)
        if (password.length() < 6) {
            Toast.makeText(activity, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(sanitizedEmail, password)
                .addOnCompleteListener(result -> {
                    // Check if account creation was successful
                    if (result.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        // create user without password - Firebase Auth handles password security
                        User user = new User(userId, sanitizedEmail, sanitizedName, sanitizedPhone, Timestamp.now(), true, false, "");


                        // save the user data in Firestore
                        db.collection("users").document(userId).set(user)
                                .addOnCompleteListener(task -> {
                                    // check if saving user data was successful
                                    if (task.isSuccessful()) {
                                        Toast.makeText(activity, "Account created successfully", Toast.LENGTH_SHORT).show();
                                        activity.finish();
                                    } else {
                                        Toast.makeText(activity, "Error saving user data", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(activity, "An error occurred", Toast.LENGTH_LONG).show();
                                });
                    } else {
                        // generic error message for security
                        Toast.makeText(activity, "Account creation failed. Please try again.", Toast.LENGTH_LONG).show();
                    }


                })


                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "An error occurred. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

}
