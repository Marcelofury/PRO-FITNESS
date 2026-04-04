package com.example.profitness;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.profitness.network.ApiConfig;
import com.example.profitness.network.TokenStore;
import com.example.profitness.notifications.ReminderScheduler;

public class settings extends AppCompatActivity {

    private static final String KEY_REMINDER_WORKOUT = "reminder_workout";
    private static final String KEY_REMINDER_HYDRATION = "reminder_hydration";
    private static final int REQ_NOTIFICATIONS = 401;

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
        EditText etBackendUrl = findViewById(R.id.et_settings_backend_url);
        TextView tvAppVersion = findViewById(R.id.tv_settings_version);

        String savedBaseUrl = prefs.getString(ApiConfig.KEY_BASE_URL, ApiConfig.DEFAULT_BASE_URL);
        etBackendUrl.setText(savedBaseUrl);

        switchWorkoutReminder.setChecked(prefs.getBoolean(KEY_REMINDER_WORKOUT, true));
        switchHydrationReminder.setChecked(prefs.getBoolean(KEY_REMINDER_HYDRATION, true));

        switchWorkoutReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            prefs.edit().putBoolean(KEY_REMINDER_WORKOUT, isChecked).apply();
            if (isChecked) {
                ensureNotificationPermission();
                ReminderScheduler.scheduleWorkoutReminder(settings.this);
            } else {
                ReminderScheduler.cancelWorkoutReminder(settings.this);
            }
        });
        switchHydrationReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            prefs.edit().putBoolean(KEY_REMINDER_HYDRATION, isChecked).apply();
            if (isChecked) {
                ensureNotificationPermission();
                ReminderScheduler.scheduleHydrationReminder(settings.this);
            } else {
                ReminderScheduler.cancelHydrationReminder(settings.this);
            }
        });

        tvAppVersion.setText("App version " + getAppVersionName());

        // Ensure schedules are aligned with persisted toggle states.
        syncReminderSchedules(
            switchWorkoutReminder.isChecked(),
            switchHydrationReminder.isChecked()
        );

        findViewById(R.id.btn_settings_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_settings_profile).setOnClickListener(v ->
                startActivity(new Intent(settings.this, profile.class))
        );
        findViewById(R.id.btn_settings_dashboard).setOnClickListener(v ->
                startActivity(new Intent(settings.this, Home_dashboard.class))
        );
        findViewById(R.id.btn_settings_save_backend).setOnClickListener(v -> {
            String normalized = ApiConfig.normalizeBaseUrl(etBackendUrl.getText().toString());
            prefs.edit().putString(ApiConfig.KEY_BASE_URL, normalized).apply();
            etBackendUrl.setText(normalized);
            Toast.makeText(settings.this, "Backend URL saved", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btn_settings_logout).setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> logout())
                .show();
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

    private void syncReminderSchedules(boolean workoutEnabled, boolean hydrationEnabled) {
        if (workoutEnabled) {
            ReminderScheduler.scheduleWorkoutReminder(this);
        } else {
            ReminderScheduler.cancelWorkoutReminder(this);
        }

        if (hydrationEnabled) {
            ReminderScheduler.scheduleHydrationReminder(this);
        } else {
            ReminderScheduler.cancelHydrationReminder(this);
        }
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQ_NOTIFICATIONS
        );
    }
}
