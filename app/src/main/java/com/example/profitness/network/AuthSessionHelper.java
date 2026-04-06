package com.example.profitness.network;

import android.app.Activity;
import android.content.Intent;

import com.example.profitness.login;

public final class AuthSessionHelper {
    private AuthSessionHelper() {}

    public static boolean isAuthExpiredMessage(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        String msg = errorMessage.toLowerCase().trim();
        return msg.contains("invalid or expired token")
                || msg.contains("invalid token user")
                || msg.contains("missing or invalid authorization header")
                || msg.startsWith("request failed with status 401")
                || msg.contains("\"status\":401")
                || msg.contains("\"statuscode\":401")
                || msg.contains("unauthorized");
    }

    public static boolean handleIfAuthExpired(Activity activity, String errorMessage) {
        if (!isAuthExpiredMessage(errorMessage)) {
            return false;
        }

        TokenStore tokenStore = new TokenStore(activity);
        tokenStore.clearToken();

        Intent intent = new Intent(activity, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        return true;
    }
}
