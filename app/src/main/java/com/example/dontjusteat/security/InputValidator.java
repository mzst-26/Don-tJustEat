package com.example.dontjusteat.security;

import android.util.Log;
import android.util.Patterns;




// this validates all the inputs to prevent attacks
// What does it check?

// Checks if email is in correct format
//checks if phone number is valid
// Checks if name contains only allowed characters
// Detects XSS attacks
// Cleans up user input to remove dangerous characters
// Provides user-friendly error messages

public class InputValidator {
    // tag is used for logging errors (helps with debugging)
    private static final String TAG = "InputValidator";

    // Email Validation

    /**
     *@param email - the email to validate
     *@return true if email is valid, false otherwise
     *
    */

    public static boolean isValidEmail(String email) {
        // check if email is null or empty

        if (email == null || email.trim().isEmpty()) {
            Log.w(TAG, "Email is empty");
            return false;
        }

        //  remove leading and trailing spaces (trim)
        email = email.trim();

        // Check maximum length, max lenght of 254 characters

        if (email.length() > 254) {
            Log.w(TAG, "Email exceeds maximum length");
            return false;
        }

        // use Android's built-in email pattern matcher
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.w(TAG, "Email format invalid");
            return false;
        }

        // all checks passed
        return true;
    }


    // PHONE VALIDATION

    // What it checks:
    // phone is not empty
    // Phone has reasonable length (7-15 digits)
    // phone contains mostly numbers

    /**
    * @param phone - the phone number to validate
    * @return true if phone is valid, false otherwise*
    */


    public static boolean isValidPhone(String phone) {
        // check if phone is null or empty

        if (phone == null || phone.trim().isEmpty()) {
            Log.w(TAG, "Phone is empty");
            return false;
        }

        // Remove leading/trailing spaces
        phone = phone.trim();

        //clean the phone number
        //remove common formatting

        String cleaned = phone.replaceAll("[^0-9+]", "");

        // check if cleaned phone has valid length
        // international phone numbers are typically 7-15 digits

        if (cleaned.length() < 7 || cleaned.length() > 15) {
            Log.w(TAG, "Phone number length invalid");
            return false;
        }

        // All checks passed
        return true;
    }

    // NAME VALIDATION

    /**
     * Validates name format
     *
     * What does it checks:
     * name is not empty
     * name has reasonable length (2-100 c)
     * name contains only letters, spaces, and hyphens
     * no numbers or characters (prevents injections)
     *
     * @param name - the name to validate
     * @return true if name is valid, false otherwise
     */
    public static boolean isValidName(String name) {
        //Check if name is null or empty
        if (name == null || name.trim().isEmpty()) {
            Log.w(TAG, "Name is empty");
            return false;
        }


        //trim the name
        name = name.trim();


        // check length
        // Names should be between 2 and 100 characters


        if (name.length() < 2 || name.length() > 100) {
            Log.w(TAG, "Name length invalid");
            return false;
        }

        // check allowed characters using regex pattern
        // letters (a-z, A-Z), spaces, hyphens, apostrophes, periods are allowed.
        if (!name.matches("^[a-zA-Z\\s\\-'.]+$")) {
            Log.w(TAG, "Name contains invalid characters");
            return false;
        }

        // all checks passed

        return true;
    }


    // DATA SANITIZATION

    /**
     * Cleans user input:
     * What it does:
     * - removes start/end spaces
     * - removes special characters that could cause issues
     * - Limits inputs
     *
     * @param input - the input to clean
     * @return cleaned input safe to use*
     */
    public static String sanitize(String input) {
        // check if input is null
        if (input == null) {
            return "";
        }

        // remove spaces
        input = input.trim();

        // Remove dangerous characters that could cause problems
        input = input.replaceAll("[<>\"'%;()&+]", "");

        // Limit input length
        // Maximum 500
        // cut it off if its longer
        if (input.length() > 500) {
            input = input.substring(0, 500);
        }

        //return cleaned input
        return input;
    }

    // USER-FRIENDLY ERROR MESSAGES

    /**
     * make the errors clear for user
     * What it does:
     * provides specific feedback
     * Helps user fix the problem without exposing
     *
     * @param fieldName - which field has the problem?
     * @param value - the invalid value
     * @return user-friendly error message
     */
    public static String getValidationError(String fieldName, String value) {

        if (fieldName.equalsIgnoreCase("email")) {
            if (value == null || value.isEmpty()) {
                return "Email is required";
            }

            if (value.length() > 254) {
                return "Email is too long";
            }

            // for any other email error
            return "Please enter a valid email address";
        }

        // phone error message
        if (fieldName.equalsIgnoreCase("phone")) {

            if (value == null || value.isEmpty()) {
                return "Phone number is required";
            }

            // for any other phone error
            return "Please enter a valid phone number (7-15 digits)";
        }

        // Name Error Message
        if (fieldName.equalsIgnoreCase("name")) {

            if (value == null || value.isEmpty()) {
                return "Name is required";
            }

            if (value.length() < 2) {
                return "Name must be at least 2 characters";
            }

            // For other errors
            return "Name can only contain letters, spaces, and hyphens";
        }

        // default error for all unknown fields
        return "Invalid input";
    }
}

