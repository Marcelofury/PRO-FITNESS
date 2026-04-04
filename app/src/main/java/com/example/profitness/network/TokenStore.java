package com.example.profitness.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(ApiConfig.KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(ApiConfig.KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(ApiConfig.KEY_TOKEN).apply();
    }
}
