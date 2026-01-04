package com.example.dontjusteat.security;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dontjusteat.R;
import com.example.dontjusteat.customer_login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class email_verification extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_verification);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String email = getIntent().getStringExtra("email");
        TextView tvInfo = findViewById(R.id.verification_info);
        if (tvInfo != null) {
            tvInfo.setText(email != null ? "A verification link was sent to: " + email : "A verification link was sent to your email.");
        }

        Button resendButton = findViewById(R.id.resend_button);
        if (resendButton != null) {
            resendButton.setOnClickListener(v -> resendVerificationEmail());
        }

        Button checkButton = findViewById(R.id.check_verified_button);
        if (checkButton != null) {
            checkButton.setOnClickListener(v -> checkAndProceed());
        }
    }

    // Resend verification email
    private void resendVerificationEmail() {
        // get the current user
        FirebaseUser user = auth.getCurrentUser();
        // if no user, prompt to login
        if (user == null) {
            Toast.makeText(this, "No user found. Please login.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }
        // resend email
        user.sendEmailVerification()

                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email resent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed: Please wait for 1 minute then retry.", Toast.LENGTH_LONG).show();
                    }
                })

                .addOnFailureListener(e -> Toast.makeText(this, "Failed to resend email.", Toast.LENGTH_LONG).show());
    }

    // check verification status
    private void checkAndProceed() {
        // get current user
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // no user, prompt to login
            Toast.makeText(this, "No user found. Please login.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }
        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // check if email is verified
                if (user.isEmailVerified()) {
                    // update Firestore isVerified
                    db.collection("users").document(user.getUid()).update("isVerified", true)
                            .addOnCompleteListener(update -> {
                                // proceed regardless of update result
                                Toast.makeText(this, "Email verified!", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // even if update fails proceed
                                navigateToLogin();
                                finish();
                            });
                } else {
                    Toast.makeText(this, "Email not verified yet.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Failed to check verification.", Toast.LENGTH_LONG).show();
            }
        });
    }

    //handle navigation to customer login
    private void navigateToLogin() {
        // navigate to customer login activity
        Intent intent = new Intent(this, customer_login.class);
        startActivity(intent);
    }
}
