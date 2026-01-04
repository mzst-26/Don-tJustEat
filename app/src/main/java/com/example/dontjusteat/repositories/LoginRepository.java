package com.example.dontjusteat.repositories;

import android.content.Intent;
import android.widget.Toast;

import com.example.dontjusteat.customer_booking;
import com.example.dontjusteat.security.InputValidator;
import com.example.dontjusteat.security.SessionManager;
import com.example.dontjusteat.security.email_verification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        //sanitize email input
        final String sanitizedEmail = InputValidator.sanitize(email);

        // Check for empty input
        if (password.isEmpty()) {
            Toast.makeText(activity, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate email format
        if (!InputValidator.isValidEmail(sanitizedEmail)) {
            String error = InputValidator.getValidationError("email", sanitizedEmail);
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(sanitizedEmail, password)
                .addOnCompleteListener(result -> {
                    // Check if authentication was successful
                    if (result.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) {
                            Toast.makeText(activity, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // Reload to ensure latest emailVerified
                        firebaseUser.reload().addOnCompleteListener(reloadTask -> {
                            if (reloadTask.isSuccessful()) {
                                // check if email is verified
                                if (!firebaseUser.isEmailVerified()) {
                                    Toast.makeText(activity, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(activity, email_verification.class);
                                    activity.startActivity(intent);
                                    return;
                                }


                                // check user role from database
                                String userId = Objects.requireNonNull(firebaseUser.getUid());
                                // Retrieve user document
                                db.collection("users").document(userId).get()
                                        // check if the document retrieval was successful
                                        .addOnCompleteListener(document -> {
                                            if (document.isSuccessful() && document.getResult() != null) {
                                                // Check verified flag in Firestore; if missing, treat as verified when Firebase says verified
                                                Boolean isVerified = document.getResult().getBoolean("isVerified");

                                                if (isVerified == null || !isVerified) {
                                                    // update Firestore isVerified if Firebase says verified
                                                    db.collection("users").document(userId).update("isVerified", true);
                                                }

                                                // Check if the user is a customer
                                                Boolean roleCustomer = document.getResult().getBoolean("roleCustomer");
                                                if (Boolean.TRUE.equals(roleCustomer)) {
                                                    SessionManager sessionManager = new SessionManager(activity);
                                                    sessionManager.saveSession(
                                                            userId,
                                                            sanitizedEmail,
                                                            true,
                                                            false
                                                    );
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
                                Toast.makeText(activity, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                                auth.signOut();
                            }
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



    public void sendPasswordReset(String email, android.app.Activity activity) {
        String sanitizedEmail = InputValidator.sanitize(email);


        // Check for empty input
        if (sanitizedEmail.isEmpty()) {
            android.widget.Toast.makeText(activity, "Please enter your email", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // validate email format
        if (!InputValidator.isValidEmail(sanitizedEmail)) {
            String error = InputValidator.getValidationError("email", sanitizedEmail);
            android.widget.Toast.makeText(activity, error, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }


        // send password reset email
        auth.sendPasswordResetEmail(sanitizedEmail)
            .addOnSuccessListener(aVoid -> android.widget.Toast.makeText(activity, "Password reset email sent.", android.widget.Toast.LENGTH_LONG).show())
            .addOnFailureListener(e -> android.widget.Toast.makeText(activity, "Failed: Please wait 1 minute and try again!", android.widget.Toast.LENGTH_LONG).show());
    }
}
