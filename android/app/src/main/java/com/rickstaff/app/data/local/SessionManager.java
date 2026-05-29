package com.rickstaff.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "AppSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_IMAGE = "img_char_";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) instance = new SessionManager(context);
        return instance;
    }

    public void saveSession(int userId, String token, String userName) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_NAME, userName)
                .putInt(KEY_USER_ID, userId)
                .apply();
    }

    public void saveCapturedImageUri(int characterId, String uri) {
        prefs.edit().putString(KEY_IMAGE + getUserId()+ "_" + characterId, uri).apply();
    }

    public String getCapturedImageUri(int characterId) {
        return prefs.getString(KEY_IMAGE + getUserId()+ "_" + characterId, null);
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, null); }
    public boolean isLoggedIn() { return getToken() != null; }

    public int getUserId() { return  prefs.getInt(KEY_USER_ID, -1); }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
