package com.example.dontjusteat;

import static com.example.dontjusteat.admin_modules.isAuthorizedAdminDoc;
import static com.example.dontjusteat.security.InputValidator.getValidationError;
import static com.example.dontjusteat.security.InputValidator.isValidEmail;
import static com.example.dontjusteat.security.InputValidator.sanitize;
import static com.example.dontjusteat.security.PasswordValidator.getPasswordFeedback;
import static com.example.dontjusteat.security.PasswordValidator.isPasswordCompromised;
import static com.example.dontjusteat.security.PasswordValidator.isPasswordStrong;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dontjusteat.repositories.StaffLoginRepository;
import com.example.dontjusteat.security.InputValidator;
import com.example.dontjusteat.security.SessionManager;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class staff_login extends AppCompatActivity {

    private StaffLoginRepository staffRepo;
    private EditText emailEt;
    private EditText passwordEt; // assuming you'll add this to the layout later
    private Button signInButton;
    private TextView forgotPasswordLink;
    private ImageView googleSignInButton;

    private FirebaseAuth auth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    // activity result launcher for Google Identity (One Tap)
    private final ActivityResultLauncher<IntentSenderRequest> oneTapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    Toast.makeText(this, "Google sign-in cancelled.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();
                    if (idToken == null) {
                        Toast.makeText(this, "Failed to get Google ID token.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    firebaseAuthWithGoogle(idToken);
                } catch (Exception e) {
                    Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_login);
        // Check for existing session
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn() && sessionManager.getSession().isStaff) {
            // if user has active session, skip login
            startActivity(new Intent(this, admin_dashboard.class));
            finish();
            return;
        }

        staffRepo = new StaffLoginRepository();
        auth = FirebaseAuth.getInstance();

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // get UI elements
        emailEt = findViewById(R.id.customerEmail);
        passwordEt = findViewById(R.id.admin_password);

        signInButton = findViewById(R.id.admin_sign_in_with_email);
        forgotPasswordLink = findViewById(R.id.forgot_password);
        googleSignInButton = findViewById(R.id.customer_google_sign_in);


        // Initialize One Tap client and request
        oneTapClient = Identity.getSignInClient(this);
        // set up google one tap sign in
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(new BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in to this app
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(false)
                .build();
        // set up button listener
        signInButton.setOnClickListener(v -> {
            // get email and password
            String email = sanitize(emailEt.getText().toString());
            String password = passwordEt != null && passwordEt.getText() != null ? passwordEt.getText().toString() : "";

            // validate password strength
            if (!isPasswordStrong(password) || isPasswordCompromised(password)) {
                String feedback = getPasswordFeedback(password);
                Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
                return;
            }
            // attempt sign-in
            staffRepo.signIn(email, password, this, () -> {
                Intent intent = new Intent(staff_login.this, admin_dashboard.class);
                startActivity(intent);
                finish();
            });
        });

        // forgot password link listener
        forgotPasswordLink.setOnClickListener(v -> {
            // get email
            String email = emailEt != null && emailEt.getText() != null ? emailEt.getText().toString().trim() : "";
            // validate email
            if (!isValidEmail(sanitize(email))) {
                String message = getValidationError("email", email);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                return;
            }
            // send password reset
            staffRepo.sendPasswordReset(email, this);
        });

        //  google sign in button listener
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v -> {
                // one tap sign in
                oneTapClient.beginSignIn(signInRequest)
                        // on success, launch the intent
                        .addOnSuccessListener(result -> {
                            try {
                                //show the one tap dialog
                                IntentSenderRequest request = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                                // launch
                                oneTapLauncher.launch(request);
                            } catch (Exception e) {
                                Toast.makeText(this, "Failed to launch Google sign-in.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "No Google accounts available or sign-in failed.", Toast.LENGTH_LONG).show());
            });
        }
    }

    // authenticate with google token
    private void firebaseAuthWithGoogle(String idToken) {
        // get credential
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        // sign in with firebase
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    // check the status of the user
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser == null) {
                        Toast.makeText(this, "No Firebase user after sign-in.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String email = fbUser.getEmail();
                    String uid = fbUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    // check admin record in firestore
                    db.collection("admins").document(uid).get()
                            .addOnSuccessListener((DocumentSnapshot adminDoc) -> {
                                // check if admin is active
                                if (isAuthorizedAdminDoc(adminDoc)) {
                                    new SessionManager(this).saveSession(uid, email, false, true);
                                    startActivity(new Intent(this, admin_dashboard.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Not authorized as admin (no active admin record)", Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Admin lookup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                auth.signOut();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Firebase auth failed.", Toast.LENGTH_LONG).show());
    }
}
