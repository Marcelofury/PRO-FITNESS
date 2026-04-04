package com.example.profitness.network;

public final class ApiConfig {
    private ApiConfig() {}

    // Default local URL; override at runtime from Settings for Render or LAN use.
    public static final String DEFAULT_BASE_URL = "https://pro-fitness-m64i.onrender.com";
    public static final String PREFS_NAME = "profitness_prefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_BASE_URL = "api_base_url";

    public static String normalizeBaseUrl(String rawUrl) {
        if (rawUrl == null) {
            return DEFAULT_BASE_URL;
        }

        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_BASE_URL;
        }

        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }

        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed;
    }
}
