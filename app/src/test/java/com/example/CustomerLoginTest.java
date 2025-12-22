package com.example;

import static org.junit.Assert.assertNotNull;

import android.widget.Button;
import android.widget.EditText;

import com.example.dontjusteat.R;
import com.example.dontjusteat.customer_login;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class CustomerLoginTest {

    @Test
    public void testLoginButtonClick() {
        // Create the activity using Robolectric
        customer_login activity = Robolectric.setupActivity(customer_login.class);

        // Set the authorized credentials
        EditText emailInput = activity.findViewById(R.id.customerEmail);
        
        emailInput.setText("ngu.zaki2021@gmail.com");

        Button signInButton = activity.findViewById(R.id.sign_in_with_email);

        // Verify the button is present and perform a click
        assertNotNull("Sign-in button should be present", signInButton);
        signInButton.performClick();
    }
}
