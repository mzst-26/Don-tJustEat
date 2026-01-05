package com.example.dontjusteat;

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

import com.example.dontjusteat.models.User;
import com.example.dontjusteat.repositories.LoginRepository;
import com.example.dontjusteat.repositories.CreateAccountRepository;
import com.example.dontjusteat.security.PasswordValidator;
import com.example.dontjusteat.security.SessionManager;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;


public class customer_login extends AppCompatActivity {
    Button signInWithEmailButton;
    TextView forgotPasswordLinkButton;
    EditText emailEditText;
    EditText customerPassword;
    EditText customerNameEditText;
    EditText customerPhoneEditText;

    TextView switchLink;
    TextView titleTextView;
    TextView pageDescriptionTextView;

    private LoginRepository loginRepository;
    private CreateAccountRepository createAccountRepository;

    private User currentUser;
    private boolean isLoginMode = true; // Track current mode


    // google sign in
    private ImageView googleSignInButton;
    private FirebaseAuth auth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    // handle Google Identity sign-in result
    private final ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher =
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
        // Check session first (faster than Firebase check)
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.getSession().isStaff) {
                startActivity(new Intent(this, admin_dashboard.class));
                finish();
                return;
            } else if (sessionManager.getSession().isCustomer) {
                // already logged in as a customer; send to customer home
                startActivity(new Intent(this, customer_booking.class));
                finish();
                return;
            }
        }

        super.onCreate(savedInstanceState);
        // This line loads the XML layout and displays it on the screen
        setContentView(R.layout.customer_login);

        // initialize repositories
        loginRepository = new LoginRepository();
        createAccountRepository = new CreateAccountRepository();

        //Back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        //Sign in with email button (for testing only)
        signInWithEmailButton = findViewById(R.id.sign_in_with_email);
        forgotPasswordLinkButton = findViewById(R.id.forgot_password);
        emailEditText = findViewById(R.id.customerEmail);
        customerPassword = findViewById(R.id.customer_password);
        customerNameEditText = findViewById(R.id.customerName);
        customerPhoneEditText = findViewById(R.id.customer_phone);
        switchLink = findViewById(R.id.create_an_account);

        // google sing in button
        googleSignInButton = findViewById(R.id.customer_google_sign_in);

        //set the name and phone default visibility to GONE
        customerNameEditText.setVisibility(TextView.GONE);
        customerPhoneEditText.setVisibility(TextView.GONE);
        titleTextView = findViewById(R.id.pageTitle);
        pageDescriptionTextView = findViewById(R.id.page_description);

        // initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // set up onetap login
        oneTapClient = Identity.getSignInClient(this);
        // configure sign in request
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(new BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Show all accounts; set true to filter by previously authorized accounts
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        // set up google sign in button listener
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v -> {
                // start the one tap sign in flow
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(result -> {
                            // launch the intent
                            try {
                                IntentSenderRequest request = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                                googleSignInLauncher.launch(request);
                            } catch (Exception e) {
                                Toast.makeText(this, "Failed to launch Google sign-in.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "No Google accounts available or sign-in failed.", Toast.LENGTH_LONG).show());
            });
        }

        // Set up sign in button listener for login mode
        setupMainButtonListener();

        //Create an account toggle button
        switchLink.setOnClickListener(v -> {
            if (isLoginMode) {
                switchToCreateAccountMode();
            } else {
                switchToLoginMode();
            }
        });
    }



    // authenticate with Firebase using Google ID token
    private void firebaseAuthWithGoogle(String idToken) {
        // get credential
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        // sign in with credential
        auth.signInWithCredential(credential)
                // on success
                .addOnSuccessListener(result -> {
                    // get current user
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser == null) {
                        Toast.makeText(this, "No Firebase user after sign-in.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Save lightweight session
                    new SessionManager(this).saveSession(fbUser.getUid(), fbUser.getEmail(), true, false);

                    // Check if user document already exists
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    // Retrieve user document
                    db.collection("users").document(fbUser.getUid()).get()
                            .addOnSuccessListener((DocumentSnapshot doc) -> {
                                if (doc != null && doc.exists()) {
                                    // Existing user, just proceed
                                    startActivity(new Intent(this, customer_booking.class));
                                    finish();
                                } else {
                                    // new user, auto-create from Google profile
                                    String googleName = fbUser.getDisplayName();
                                    String googleEmail = fbUser.getEmail();
                                    String googlePhoto = fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : null;
                                    String phone = "";

                                    // save to Firestore
                                    saveUserToFirestore(
                                            fbUser.getUid(),
                                            googleEmail,
                                            googleName,
                                            phone,
                                            googlePhoto,
                                            fbUser.isEmailVerified()
                                    );

                                    // proceed to booking
                                    startActivity(new Intent(this, customer_booking.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to check user profile.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase auth failed.", Toast.LENGTH_LONG).show()
                );
    }

    // build a user model and write it to firestore
    private void saveUserToFirestore(String uid, String email, String name, String phone, String photoUrl, boolean isVerified) {
        // build user model
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // default roles
        boolean isCustomer = true;
        boolean isActive = true;
        // current timestamp
        Timestamp createdAt = Timestamp.now();
        // create user model
        User userModel = new User(uid, email != null ? email : "", name != null ? name : "", phone != null ? phone : "", createdAt, isCustomer, isActive, photoUrl);
        // set isVerified
        userModel.setIsVerified(isVerified);
        // save to firestore
        db.collection("users").document(uid)
                .set(userModel)
                .addOnSuccessListener(aVoid -> { /* saved */ })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user.", Toast.LENGTH_SHORT).show());
    }

    private void setupMainButtonListener() {
        signInWithEmailButton.setOnClickListener(v -> {
            if (isLoginMode) {
                handleSignIn();
            } else {
                handleCreateAccount();
            }
        });

        // forgot password link listener
        forgotPasswordLinkButton.setOnClickListener(v -> {
            // read email from input; trigger reset if present
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email above", Toast.LENGTH_SHORT).show();
                return;
            }
            loginRepository.sendPasswordReset(email, this);
        });

    }

    private void handleSignIn() {
        String email = emailEditText.getText().toString();
        String password = customerPassword.getText().toString();

        // validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PasswordValidator.isPasswordStrong(password)) {
            String feedback = PasswordValidator.getPasswordFeedback(password);
            customerPassword.setError(feedback);
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
            return;
        }

        if (PasswordValidator.isPasswordCompromised(password)) {
            String feedback = "That password is commonly used. Please choose another.";
            customerPassword.setError(feedback);
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
            return;
        }

        // call signIn method from LoginRepository
        loginRepository.signIn(email, password, this);
    }

    private void handleCreateAccount() {
        String email = emailEditText.getText().toString();
        String password = customerPassword.getText().toString();
        String name = customerNameEditText.getText().toString();
        String phone = customerPhoneEditText.getText().toString();

        // validate inputs
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PasswordValidator.isPasswordStrong(password)) {
            String feedback = PasswordValidator.getPasswordFeedback(password);
            customerPassword.setError(feedback);
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
            return;
        }

        if (PasswordValidator.isPasswordCompromised(password)) {
            String feedback = "That password is commonly used. Please choose another.";
            customerPassword.setError(feedback);
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
            return;
        }

        // call createAccount method from CreateAccountRepository
        createAccountRepository.createAccount(email, password, name, phone, this);
    }

    private void switchToCreateAccountMode() {
        isLoginMode = false;
        signInWithEmailButton.setText("Create an Account");
        titleTextView.setText("Create your Customer Account");
        pageDescriptionTextView.setText("Please enter your email and create a password to set up your customer account.");

        customerNameEditText.setVisibility(EditText.VISIBLE);
        customerPhoneEditText.setVisibility(EditText.VISIBLE);

        switchLink.setText("Back to Login");

        // Clear fields
        clearInputFields();
    }

    private void switchToLoginMode() {
        isLoginMode = true;
        signInWithEmailButton.setText("Sign in with email");
        titleTextView.setText("Sign in to your Customer Account");
        pageDescriptionTextView.setText("Please Enter your email and password to sign in.");

        customerNameEditText.setVisibility(EditText.GONE);
        customerPhoneEditText.setVisibility(EditText.GONE);

        switchLink.setText("Create an account");

        // Clear fields
        clearInputFields();
    }

    private void clearInputFields() {
        emailEditText.setText("");
        customerPassword.setText("");
        customerNameEditText.setText("");
        customerPhoneEditText.setText("");
    }
}
