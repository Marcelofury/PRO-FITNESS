package com.example.profitness.network;

public final class ApiConfig {
    private ApiConfig() {}

    // Emulator: 10.0.2.2, physical device: replace with your PC LAN IP.
    public static final String BASE_URL = "http://10.0.2.2:5000";
    public static final String PREFS_NAME = "profitness_prefs";
    public static final String KEY_TOKEN = "jwt_token";
}
