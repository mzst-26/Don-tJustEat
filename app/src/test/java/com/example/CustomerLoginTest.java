package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.dontjusteat.security.PasswordValidator;

import org.junit.Test;


 // Unit tests for customer login functionality

public class CustomerLoginTest {

    // Testing the password validation logic

    @Test
    public void testPasswordValidation_StrongPassword() {
        // Test a strong password that meets all requirements
        String strongPassword = "SecurePass123!";

        assertTrue("Strong password should be valid",
                PasswordValidator.isPasswordStrong(strongPassword));
    }

    @Test
    public void testPasswordValidation_TooShort() {
        // Test password shorter than 8 characters
        String shortPassword = "Pass1!";

        assertFalse("Password shorter than 8 characters should be invalid",
                PasswordValidator.isPasswordStrong(shortPassword));
    }

    @Test
    public void testPasswordValidation_NoUppercase() {
        // Test password without uppercase letter
        String noUppercase = "password123!";

        assertFalse("Password without uppercase should be invalid",
                PasswordValidator.isPasswordStrong(noUppercase));
    }


    @Test
    public void testPasswordValidation_NoLowercase() {
        // Test password without lowercase letter
        String noLowercase = "PASSWORD123!";
        assertFalse("Password without lowercase should be invalid",
                PasswordValidator.isPasswordStrong(noLowercase));
    }

    @Test
    public void testPasswordValidation_NoDigit() {
        // Test password without digit
        String noDigit = "Password!";
        assertFalse("Password without digit should be invalid",
                PasswordValidator.isPasswordStrong(noDigit));
    }

    @Test
    public void testPasswordValidation_NoSpecialCharacter() {
        // Test password without special character
        String noSpecial = "Password123";
        assertFalse("Password without special character should be invalid",
                PasswordValidator.isPasswordStrong(noSpecial));
    }

    @Test
    public void testPasswordValidation_NullPassword() {
        // Test null password
        assertFalse("Null password should be invalid",
                PasswordValidator.isPasswordStrong(null));
    }

    @Test
    public void testPasswordValidation_EmptyPassword() {
        // Test empty password
        assertFalse("Empty password should be invalid",
                PasswordValidator.isPasswordStrong(""));
    }

    @Test
    public void testPasswordValidation_TooLong() {
        // Test password longer than 128 characters
        String tooLong = "A".repeat(130) + "a1!";
        assertFalse("Password longer than 128 characters should be invalid",
                PasswordValidator.isPasswordStrong(tooLong));
    }

    @Test
    public void testPasswordValidation_ExactMinLength() {
        // Test password at exactly 8 characters with all requirements
        String exactMin = "Pass123!";
        assertTrue("Password at exactly 8 characters should be valid",
                PasswordValidator.isPasswordStrong(exactMin));
    }

    @Test
    public void testPasswordValidation_VariousSpecialCharacters() {
        // Test different special characters are accepted
        String[] specialChars = {"@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "+"};
        for (String special : specialChars) {
            String password = "Password1" + special;
            assertTrue("Password with special character '" + special + "' should be valid",
                    PasswordValidator.isPasswordStrong(password));
        }
    }
}