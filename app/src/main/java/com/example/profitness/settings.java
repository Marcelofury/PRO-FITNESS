package com.example.profitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.profitness.network.ApiConfig;
import com.example.profitness.network.TokenStore;

public class settings extends AppCompatActivity {

    private static final String KEY_REMINDER_WORKOUT = "reminder_workout";
    private static final String KEY_REMINDER_HYDRATION = "reminder_hydration";

    private TokenStore tokenStore;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tokenStore = new TokenStore(this);
        prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);

        SwitchCompat switchWorkoutReminder = findViewById(R.id.switch_settings_workout_reminder);
        SwitchCompat switchHydrationReminder = findViewById(R.id.switch_settings_hydration_reminder);
        TextView tvAppVersion = findViewById(R.id.tv_settings_version);

        switchWorkoutReminder.setChecked(prefs.getBoolean(KEY_REMINDER_WORKOUT, true));
        switchHydrationReminder.setChecked(prefs.getBoolean(KEY_REMINDER_HYDRATION, true));

        switchWorkoutReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean(KEY_REMINDER_WORKOUT, isChecked).apply()
        );
        switchHydrationReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean(KEY_REMINDER_HYDRATION, isChecked).apply()
        );

        tvAppVersion.setText("App version " + getAppVersionName());

        findViewById(R.id.btn_settings_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_settings_profile).setOnClickListener(v ->
                startActivity(new Intent(settings.this, profile.class))
        );
        findViewById(R.id.btn_settings_dashboard).setOnClickListener(v ->
                startActivity(new Intent(settings.this, Home_dashboard.class))
        );
        findViewById(R.id.btn_settings_logout).setOnClickListener(v -> logout());
    }

    private void logout() {
        tokenStore.clearToken();
        Intent intent = new Intent(this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String getAppVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            return "1.0";
        }
    }
}
