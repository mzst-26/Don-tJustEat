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

    //Password feedback tests

    @Test
    public void testPasswordFeedback_NullPassword() {
        // test feedback for null password
        String feedback = PasswordValidator.getPasswordFeedback(null);
        assertEquals("Should return 'Password is required' for null",
                "Password is required", feedback);
    }


    @Test
    public void testPasswordFeedback_EmptyPassword() {
        // test feedback for empty password
        String feedback = PasswordValidator.getPasswordFeedback("");
        assertEquals("Should return 'Password is required' for empty string",
                "Password is required", feedback);
    }


    @Test
    public void testPasswordFeedback_ShortPassword() {
        // test feedback for password that's too short
        String feedback = PasswordValidator.getPasswordFeedback("Pass1!");
        assertTrue("Should mention minimum length",
                feedback.contains("8"));
    }


    //email validation tests


    @Test
    public void testEmailValidation_ValidEmail() {
        // Test valid email formats
        assertTrue("Valid email should pass", isValidEmail("test@example.com"));
        assertTrue("Valid email with subdomain should pass", isValidEmail("test@mail.example.com"));
        assertTrue("Valid email with numbers should pass", isValidEmail("test123@example.com"));
        assertTrue("Valid email with dots should pass", isValidEmail("test.user@example.com"));
    }


    @Test
    public void testEmailValidation_InvalidEmail() {
        // Test invalid email formats
        assertFalse("Email without @ should fail", isValidEmail("testexample.com"));
        assertFalse("Email without domain should fail", isValidEmail("test@"));
        assertFalse("Email without username should fail", isValidEmail("@example.com"));
        assertFalse("Empty email should fail", isValidEmail(""));
        assertFalse("Null email should fail", isValidEmail(null));
        assertFalse("Email with spaces should fail", isValidEmail("test @example.com"));
    }

    
    //Helper methods

    // validate email format
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }

}
