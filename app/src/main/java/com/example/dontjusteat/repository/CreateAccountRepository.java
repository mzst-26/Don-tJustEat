package com.example.dontjusteat.repository;

import android.app.Activity;
import android.widget.Toast;

import com.example.dontjusteat.model.User;
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
        // basic input validation
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate the email format
        if (!isValidEmail(email)) {
            Toast.makeText(activity, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // basic password validation
        if (password.length() < 6) {
            Toast.makeText(activity, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(result -> {
                    // Check if account creation was successful
                    if (result.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        // create user without password - Firebase Auth handles password security
                        User user = new User(userId, email, name, phone, Timestamp.now(), true, false, "");


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

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

