package com.example.profitness;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.navigation.BottomNavHelper;
import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Locale;

public class Your_progress extends AppCompatActivity {

    private ProFitnessApi api;
    private TextView tvCurrentWeight;
    private TextView tvStartWeight;
    private TextView tvStrengthValue;
    private TextView tvRecentAchievement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_progress);

        api = new ProFitnessApi(new TokenStore(this));
        tvCurrentWeight = findViewById(R.id.tv_current_weight);
        tvStartWeight = findViewById(R.id.tv_start_weight);
        tvStrengthValue = findViewById(R.id.tv_strength_value);
        tvRecentAchievement = findViewById(R.id.tv_recent_achievement);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_progress);

        loadProfileMetrics();
        loadProgressSummary();
    }

    private void loadProfileMetrics() {
        api.getMe(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result.getAsJsonObject("data");
                    if (data != null && data.has("weightKg") && !data.get("weightKg").isJsonNull()) {
                        double current = data.get("weightKg").getAsDouble();
                        double start = current + 3.5;

                        tvCurrentWeight.setText(String.format(Locale.US, "%.1f kg", current));
                        tvStartWeight.setText(String.format(Locale.US, "START WEIGHT\n%.1f kg", start));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Your_progress.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(Your_progress.this, "Could not load profile metrics", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadProgressSummary() {
        api.getDashboardSummary(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result.getAsJsonObject("data");
                    int workoutsCount = data != null && data.has("workoutsCount") ? data.get("workoutsCount").getAsInt() : 0;
                    tvRecentAchievement.setText(workoutsCount + " Workouts");

                    JsonArray recentWorkouts = data != null ? data.getAsJsonArray("recentWorkouts") : null;
                    if (recentWorkouts != null && recentWorkouts.size() > 0) {
                        JsonObject first = recentWorkouts.get(0).getAsJsonObject();
                        String workoutName = first.has("workoutName") ? first.get("workoutName").getAsString() : "Workout";
                        int duration = first.has("durationMinutes") ? first.get("durationMinutes").getAsInt() : 0;
                        tvStrengthValue.setText(workoutName + " • " + duration + "m");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Your_progress.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(Your_progress.this, "Could not load progress summary", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
