package com.example.profitness;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.navigation.BottomNavHelper;
import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

public class intensity_focus extends AppCompatActivity {

    private ProFitnessApi api;
    private TextView tvCurrentFocus;
    private TextView tvFocusPhase;
    private TextView tvPeakStatus;
    private ProgressBar intensityProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intensity_focus);

        api = new ProFitnessApi(new TokenStore(this));
        tvCurrentFocus = findViewById(R.id.tv_current_focus);
        tvFocusPhase = findViewById(R.id.tv_focus_phase);
        tvPeakStatus = findViewById(R.id.tv_peak_status);
        intensityProgress = findViewById(R.id.intensity_progress);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.switch_deload).setOnClickListener(v ->
                Toast.makeText(this, "Deload preference updated", Toast.LENGTH_SHORT).show()
        );

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_progress);

        loadIntensityData();
    }

    private void loadIntensityData() {
        api.getMe(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result != null && result.has("data") && result.get("data").isJsonObject()
                            ? result.getAsJsonObject("data")
                            : null;
                    String goal = data != null && data.has("goal") && !data.get("goal").isJsonNull()
                            ? data.get("goal").getAsString()
                            : "General Fitness";

                    tvCurrentFocus.setText("Current Focus: " + goal);
                    tvFocusPhase.setText("Adaptive phase based on your recent workouts");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(intensity_focus.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(intensity_focus.this, "Could not load profile focus", Toast.LENGTH_SHORT).show();
                });
            }
        });

        api.getDashboardSummary(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result != null && result.has("data") && result.get("data").isJsonObject()
                            ? result.getAsJsonObject("data")
                            : null;

                    int weeklyMinutes = data != null && data.has("weeklyWorkoutMinutes") ? data.get("weeklyWorkoutMinutes").getAsInt() : 0;
                    int streakDays = data != null && data.has("streakDays") ? data.get("streakDays").getAsInt() : 0;

                    int intensity = Math.max(10, Math.min(100, (weeklyMinutes / 3) + (streakDays * 5)));
                    intensityProgress.setProgress(intensity);

                    if (intensity >= 80) {
                        tvPeakStatus.setText("PEAK READY");
                    } else if (intensity >= 50) {
                        tvPeakStatus.setText("OPTIMAL");
                    } else {
                        tvPeakStatus.setText("RECOVERY");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(intensity_focus.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(intensity_focus.this, "Could not load intensity metrics", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
