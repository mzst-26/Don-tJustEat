package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dontjusteat.models.User;
import com.example.dontjusteat.repositories.LoginRepository;
import com.example.dontjusteat.repositories.CreateAccountRepository;
import com.example.dontjusteat.security.PasswordValidator;
import com.example.dontjusteat.security.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class customer_login extends AppCompatActivity {
    Button signInWithEmailButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check session first (faster than Firebase check)
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            // User has active session, skip login
            startActivity(new Intent(this, customer_booking.class));
            finish();
            return;
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
        emailEditText = findViewById(R.id.customerEmail);
        customerPassword = findViewById(R.id.customer_password);
        customerNameEditText = findViewById(R.id.customerName);
        customerPhoneEditText = findViewById(R.id.customer_phone);
        switchLink = findViewById(R.id.create_an_account);



        //set the name and phone default visibility to GONE
        customerNameEditText.setVisibility(TextView.GONE);
        customerPhoneEditText.setVisibility(TextView.GONE);
        titleTextView = findViewById(R.id.pageTitle);
        pageDescriptionTextView = findViewById(R.id.page_description);


        // Set up sign-in button listener for login mode
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

    private void setupMainButtonListener() {
        signInWithEmailButton.setOnClickListener(v -> {
            if (isLoginMode) {
                handleSignIn();
            } else {
                handleCreateAccount();
            }
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
