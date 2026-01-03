package com.example.dontjusteat.security;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "dontjusteat_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE_CUSTOMER = "role_customer";
    private static final String KEY_ROLE_STAFF = "role_staff";

    private final SharedPreferences prefs;

    public static class SessionData {
        public final String userId;
        public final String email;
        public final boolean isCustomer;
        public final boolean isStaff;

        public SessionData(String userId, String email, boolean isCustomer, boolean isStaff) {
            this.userId = userId;
            this.email = email;
            this.isCustomer = isCustomer;
            this.isStaff = isStaff;
        }
    }

    public SessionManager(Context context) {
        // private mode keeps prefs sandboxed to this app process
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String userId, String email, boolean isCustomer) {
        // store only non-sensitive data
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_ROLE_CUSTOMER, isCustomer)
                .apply();
    }

    public boolean isLoggedIn() {
        // user is login if we have a stored user ID
        return prefs.contains(KEY_USER_ID) && prefs.getString(KEY_USER_ID, null) != null;
    }

    public SessionData getSession() {
        if (!isLoggedIn()) {
            return null;
        }
        // Read back lightweight session attributes; callers should handle nulls defensively
        String userId = prefs.getString(KEY_USER_ID, "");
        String email = prefs.getString(KEY_EMAIL, "");
        boolean isCustomer = prefs.getBoolean(KEY_ROLE_CUSTOMER, false);
        boolean isStaff = prefs.getBoolean(KEY_ROLE_STAFF, false);
        return new SessionData(userId, email, isCustomer, isStaff);
    }

    public void clearSession() {
        // Clears all stored session fields (used on logout)
        prefs.edit().clear().apply();
    }


}
