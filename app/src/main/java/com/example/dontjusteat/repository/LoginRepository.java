package com.example.dontjusteat.repository;

import android.content.Intent;
import android.widget.Toast;

import com.example.dontjusteat.customer_booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public LoginRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void signIn(String email, String password, android.app.Activity activity) {
        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(result -> {
                    // Check if authentication was successful
                    if (result.isSuccessful()) {
                        // check user role from database
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        // Retrieve user document
                        db.collection("users").document(userId).get()
                                // check if the document retrieval was successful
                                .addOnCompleteListener(document -> {
                                    if (document.isSuccessful() && document.getResult() != null) {
                                        // Check if the user is a customer
                                        if (Boolean.TRUE.equals(document.getResult().getBoolean("roleCustomer"))) {
                                            // navigate to customer booking activity and give them a notification message
                                            Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(activity, customer_booking.class);
                                            activity.startActivity(intent);
                                            activity.finish();
                                        } else {
                                            // notify user of incorrect role and sign them out
                                            Toast.makeText(activity, "This account is not a customer account", Toast.LENGTH_LONG).show();
                                            auth.signOut();
                                        }
                                    } else {
                                        Toast.makeText(activity, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        auth.signOut();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(activity, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                });
                    } else {
                        // generic error message for security - don't expose Firebase error details
                        Toast.makeText(activity, "Login failed. Please check your email and password.", Toast.LENGTH_LONG).show();
                    }
                })
                // handle failure in sign-in process
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                });
    }
}

