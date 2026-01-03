package com.example.dontjusteat.security;

import android.util.Log;

/**
 *      password validator Class
 * What does it do?
 * - Checks if password is strong enough
 * - Provides feedback about whats missing
 * - Detects commonly used used passwords
s

 */
public class PasswordValidator {
    // tag is used for debugging logs
    private static final String TAG = "PasswordValidator";

    // password length
    // We require minimum 8 characters
    private static final int MIN_PASSWORD_LENGTH = 8;


    // Maximum 128 characters
    private static final int MAX_PASSWORD_LENGTH = 128;


    //check the strength

    /**
     * Validates if a password is strong enough
     * What makes a password strong?
     * Minimum 8 characters long
     * at least one upper case
     * one lower case
     * one digit
     * one special character
     *
     * @param password - the password to validate
     * @return true if password is strong, false if weak
     */
    public static boolean isPasswordStrong(String password) {
        // is password empty or null?
        if (password == null || password.isEmpty()) {
            Log.w(TAG, "Password is null or empty");
            return false;
        }

        // check length requirements
        // password must be between 8 and 128 characters

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            Log.w(TAG, "Password length invalid: " + password.length());
            return false;
        }

        // check if there is at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            Log.w(TAG, "Password missing uppercase letter");
            return false;
        }

        // check for one lower case

        if (!password.matches(".*[a-z].*")) {
            Log.w(TAG, "Password missing lowercase letter");
            return false;
        }

        //check for one digit
        if (!password.matches(".*\\d.*")) {
            Log.w(TAG, "Password missing digit");
            return false;
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*")) {

            Log.w(TAG, "Password missing special character");
            return false;
        }

        // if all checks passed, password is strong
        return true;
    }


    /**
     * Provides feedbacks to user
     *
     * @param password - the password to check
     * @return helpful message explaining the problem

     */
    public static String getPasswordFeedback(String password) {
        // is the password empty or null?
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }

        // Is the password to short?
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }

        // does it include upper case?
        // We check each requirement in order of importance
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain an uppercase letter";
        }

        // Does it include lower case?
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain a lowercase letter";
        }

        // Does password have any numbers in it?
        if (!password.matches(".*\\d.*")) {
            return "Password must contain a digit";
        }

        // check for special characters
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*")) {
            return "Password must contain a special character (!@#$%^&* etc)";
        }

        // if all checks passed
        return "Password is strong";
    }

    // detect commonly used passwords
    /**
     * Checks if a password is commonly used and should be avoided
     * What is a compromised password?
     * A password that appears in data breaches
     * commonly used (password123).
     * easy to guess

     * @param password - the password to check
     * @return true if password is commonly used, false if unique

     */
    public static boolean isPasswordCompromised(String password) {
        // List of commonly used passwords
        String[] commonPasswords = {
            "Password123!",
            "Admin123!",
            "Test123!",
            "Welcome123!",
            "Qwerty123!",
            "Letmein123!",
            "Dragon123!",
            "Master123!",
            "Welcome@123",
            "Password@123",
            "Admin@123",
            "Summer2024!",
            "Winter2024!",
            "Autumn2024!",
            "Spring2024!",
            "January1!",
            "February2!",
            "March3!",
            "April4!",
            "May2024!",
            "June2024!",
            "July2024!",
            "August2024!",
            "September1!",
            "October1!",
            "November1!",
            "December1!",
            "Football1!",
            "Baseball1!",
            "Hockey123!",
            "Soccer123!",
            "Basketball1!",
            "IloveYou1!",
            "Sunshine1!",
            "Princess1!",
            "Welcome1!",
            "Monkey123!",
            "Shadow123!",
            "Superman1!",
            "TrustNo1!",
            "Freedom1!",
            "Ninja123!",
            "Batman123!",
            "SpiderMan1!",
            "Pokemon1!",
            "Starwars1!",
            "HarryPotter1!",
            "LetMeIn1!",
            "QwertyUi1!",
            "Abcdef1!",
            "Qazwsx1!",
            "Passw0rd!",
            "P@ssword1",
            "Pa$$w0rd!",
            "Admin2024!",
            "Manager1!",
            "Office123!",
            "WorkFrom1!",
            "Remote123!",
            "Covid2020!",
            "Welcome2024!",
            "ChangeMe!1",
            "Default1!",
            "TempPass1!",
            "User1234!",
            "Guest123!",
            "Testing1!",
            "TestTest!",
            "Qwerty!1",
            "Password1!",
            "Password2@",
            "Password3#",
            "Password4$",
            "Password5%",
            "Password6^",
            "Password7&",
            "Password8*",
            "Password9!",
            "Welcome2@",
            "Welcome3#",
            "Welcome4$",
            "Welcome5%",
            "Welcome6^",
            "Welcome7&",
            "Welcome8*",
            "Welcome9!",
            "Admin1!@#",
            "Admin2@!#",
            "Admin3#!@",
            "Admin4$!@",
            "Admin5%!@",
            "Admin6^!@",
            "Admin7&!@",
            "Admin8*!@",
            "Admin9(!@",
            "Qwerty1!@",
            "Qwerty2@!",
            "Qwerty3#!",
            "Qwerty4$!",
            "Qwerty5%!",
            "Qwerty6^!",
            "Qwerty7&!",
            "Qwerty8*!",
            "Qwerty9(!",
            "Login123!",
            "Customer1!",
            "Client123!",
            "Service1!",
            "Support1!",
            "Helpdesk1!",
            "WelcomeHome1!",
            "Family123!",
            "Friends1!",
            "School123!",
            "College1!",
            "University1!",
            "Graduation1!",
            "Summer2023!",
            "Winter2023!",
            "Spring2023!",
            "Autumn2023!",
            "Summer2022!",
            "Winter2022!",
            "Spring2022!",
            "Autumn2022!",
            "William1!",
            "Anthony1!",
            "Jonathan1!",
            "Elizabeth1!",
            "Alexander1!",
            "Nicholas1!",
            "Samantha1!",
            "Rebecca1!",
            "Benjamin1!",
            "Isabella1!",
            "Victoria1!",
            "Katherine1!",
            "Catherine1!",
            "Christian1!",
            "Jonathan1.",
            "Elizabeth1.",
            "Alexander1.",
            "Nicholas1.",
            "Samantha1.",
            "Rebecca1.",
            "Benjamin1.",
            "Isabella1.",
            "Victoria1.",
            "Katherine1.",
            "Catherine1.",
            "Christian1.",
            "Aa123456!",
            "Ab123456!",
            "Ac123456!",
            "Ad123456!",
            "Ae123456!",
            "Af123456!",
            "Ag123456!",
            "Ah123456!",
            "Ai123456!",
            "Aj123456!",
            "Ak123456!",
            "Al123456!",
            "Am123456!",
            "An123456!",
            "Ao123456!",
            "Ap123456!",
            "Aq123456!",
            "Ar123456!",
            "As123456!",
            "At123456!",
            "Au123456!",
            "Av123456!",
            "Aw123456!",
            "Ax123456!",
            "Ay123456!",
            "Az123456!",
            "Aa123456.",
            "Ab123456.",
            "Ac123456.",
            "Ad123456.",
            "Ae123456.",
            "Af123456.",
            "Ag123456.",
            "Ah123456.",
            "Ai123456.",
            "Aj123456.",
            "Ak123456.",
            "Al123456.",
            "Am123456.",
            "An123456.",
            "Ao123456.",
            "Ap123456.",
            "Aq123456.",
            "Ar123456.",
            "As123456.",
            "At123456.",
            "Au123456.",
            "Av123456.",
            "Aw123456.",
            "Ax123456.",
            "Ay123456.",
            "Az123456.",
        };

        // Check each common password
        for (String commonPassword : commonPasswords) {
            if (password.equalsIgnoreCase(commonPassword)) {
                Log.w(TAG, "Password is commonly used");
                return true; // This password is compromised!
            }
        }

        // Password not found in common list. it is unique.
        return false;
    }


}
