package com.example.dontjusteat.repositories;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.dontjusteat.security.InputValidator;
import com.example.dontjusteat.security.SessionManager;
import com.example.dontjusteat.security.PasswordValidator;
import com.example.dontjusteat.security.email_verification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StaffLoginRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public StaffLoginRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void signIn(String email, String password, Activity activity, Runnable onSuccess) {
        // Sanitize email input
        String sanitizedEmail = InputValidator.sanitize(email);


        // check for empty input
        if (sanitizedEmail.isEmpty() || password == null || password.isEmpty()) {
            Toast.makeText(activity, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate email format
        if (!InputValidator.isValidEmail(sanitizedEmail)) {
            String error = InputValidator.getValidationError("email", sanitizedEmail);
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Use PasswordValidator for login-time checks as well
        if (!PasswordValidator.isPasswordStrong(password)) {
            String feedback = PasswordValidator.getPasswordFeedback(password);
            Toast.makeText(activity, feedback, Toast.LENGTH_SHORT).show();
            return;
        }

        if (PasswordValidator.isPasswordCompromised(password)) {
            Toast.makeText(activity, "That password is commonly used. Please choose another.", Toast.LENGTH_SHORT).show();
            return;
        }

        // attempt Firebase sign-in
        auth.signInWithEmailAndPassword(sanitizedEmail, password)
            .addOnSuccessListener(result -> {
                // sign-in successful now verify email and admin status
                FirebaseUser user = auth.getCurrentUser();

                if (user == null) {
                    Toast.makeText(activity, "You are not authorised to access admin panel.", Toast.LENGTH_LONG).show();
                    return;
                }
                // reload to ensure latest emailVerified status
                user.reload().addOnCompleteListener(reload -> {

                    // check if email is verified
                    if (!user.isEmailVerified()) {
                        Toast.makeText(activity, "Please verify your email first. A link has been sent to you.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(activity, email_verification.class);
                        activity.startActivity(intent);
                        return;
                    }
                    // check admin status from database
                    String uid = user.getUid();

                    db.collection("admins").document(uid).get()
                      .addOnSuccessListener(adminDoc -> {
                          // check if adminDoc authorizes this user as an active admin
                          if (isAuthorizedAdminDoc(adminDoc)) {
                              proceed(activity, uid, user.getEmail(), onSuccess);
                          } else {
                              Toast.makeText(activity, "Not authorized as admin", Toast.LENGTH_LONG).show();
                              auth.signOut();
                          }
                      })
                      .addOnFailureListener(e -> {
                          Toast.makeText(activity, "Admin lookup failed.", Toast.LENGTH_LONG).show();
                          auth.signOut();
                      });
                });
            })
            .addOnFailureListener(e -> {
                String msg = "Login failed";
                // for debugging
                if (e instanceof FirebaseAuthException) {
                    String code = ((FirebaseAuthException) e).getErrorCode();
                    msg = "Login failed: " + code.replace('_', ' ').toLowerCase();
                }

                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            });
    }

    // check if admin document authorizes user
    private boolean isAuthorizedAdminDoc(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return false;
        Boolean isActive = doc.getBoolean("isActive");
        return Boolean.TRUE.equals(isActive);
    }

    // proceed with session saving and onSuccess callback
    private void proceed(Activity activity, String uid, String email, Runnable onSuccess) {
        SessionManager sm = new SessionManager(activity);
        sm.saveSession(uid, email, false, true);
        onSuccess.run();
    }

    // send password reset email
    public void sendPasswordReset(String email, Activity activity) {
        String sanitizedEmail = InputValidator.sanitize(email);
        // check for empty input
        if (sanitizedEmail.isEmpty()) {
            Toast.makeText(activity, "Enter your email above", Toast.LENGTH_SHORT).show();
            return;
        }
        // validate email format
        if (!InputValidator.isValidEmail(sanitizedEmail)) {
            String error = InputValidator.getValidationError("email", sanitizedEmail);
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        // send reset email via Firebase
        auth.sendPasswordResetEmail(sanitizedEmail)
            .addOnSuccessListener(aVoid -> Toast.makeText(activity, "Password reset email sent", Toast.LENGTH_LONG).show())
            .addOnFailureListener(e -> Toast.makeText(activity, "Failed to send reset email", Toast.LENGTH_LONG).show());
    }
}
