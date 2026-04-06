package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
    private TextView tvMetricValue;
    private TextView tvMetricDelta;
    private TextView tvActivitySummary;
    private TextView tvActivityDetail;
    private View barWeek1;
    private View barWeek2;
    private View barWeek3;
    private View barWeek4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_progress);

        api = new ProFitnessApi(new TokenStore(this));
        tvCurrentWeight = findViewById(R.id.tv_current_weight);
        tvStartWeight = findViewById(R.id.tv_start_weight);
        tvStrengthValue = findViewById(R.id.tv_strength_value);
        tvRecentAchievement = findViewById(R.id.tv_recent_achievement);
        tvMetricValue = findViewById(R.id.tv_metric_value);
        tvMetricDelta = findViewById(R.id.tv_metric_delta);
        tvActivitySummary = findViewById(R.id.tv_activity_summary);
        tvActivityDetail = findViewById(R.id.tv_activity_detail);
        barWeek1 = findViewById(R.id.bar_week_1);
        barWeek2 = findViewById(R.id.bar_week_2);
        barWeek3 = findViewById(R.id.bar_week_3);
        barWeek4 = findViewById(R.id.bar_week_4);

        tvStrengthValue.setText("No sessions yet");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_progress);

        findViewById(R.id.card_strength_focus).setOnClickListener(v ->
            startActivity(new Intent(Your_progress.this, intensity_focus.class))
        );
        findViewById(R.id.card_elite_plan).setOnClickListener(v ->
            startActivity(new Intent(Your_progress.this, Elite_plan.class))
        );

        loadProfileMetrics();
        loadProgressSummary();
    }

    private void loadProfileMetrics() {
        api.getMe(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = getDataObject(result);
                    Double currentWeight = optDouble(data, "weightKg");
                    if (currentWeight != null) {
                        double current = currentWeight;
                        double baseline = current;

                        tvCurrentWeight.setText(String.format(Locale.US, "%.1f kg", current));
                        tvStartWeight.setText(String.format(Locale.US, "BASELINE\n%.1f kg", baseline));

                        Double heightCmValue = optDouble(data, "heightCm");
                        if (heightCmValue != null) {
                            double heightCm = heightCmValue;
                            if (heightCm > 0) {
                                double heightM = heightCm / 100.0;
                                double bmi = current / (heightM * heightM);
                                tvMetricValue.setText(String.format(Locale.US, "%.1f", bmi));
                                tvMetricDelta.setText("Calculated from profile");
                            }
                        }
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
                    JsonObject data = getDataObject(result);
                    int workoutsCount = optInt(data, "workoutsCount", 0);
                    int weeklyWorkouts = optInt(data, "weeklyWorkoutsCount", 0);
                    int weeklyMinutes = optInt(data, "weeklyWorkoutMinutes", 0);
                    int monthlyWorkouts = optInt(data, "monthlyWorkoutsCount", 0);
                    int streakDays = optInt(data, "streakDays", 0);
                    tvRecentAchievement.setText(workoutsCount + " Workouts");
                    tvActivitySummary.setText(weeklyWorkouts + " workouts • " + weeklyMinutes + " minutes this week");
                    tvActivityDetail.setText(monthlyWorkouts + " workouts this month • " + streakDays + " day streak");

                    updateWeeklyBars(weeklyWorkouts, weeklyMinutes, monthlyWorkouts, streakDays);

                    JsonArray recentWorkouts = optArray(data, "recentWorkouts");
                    if (recentWorkouts != null && recentWorkouts.size() > 0 && recentWorkouts.get(0).isJsonObject()) {
                        JsonObject first = recentWorkouts.get(0).getAsJsonObject();
                        String workoutName = optString(first, "workoutName", "Workout");
                        int duration = optInt(first, "durationMinutes", 0);
                        tvStrengthValue.setText(workoutName + " • " + duration + "m");
                    } else {
                        tvStrengthValue.setText("No sessions yet");
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

    private void updateWeeklyBars(int weeklyWorkouts, int weeklyMinutes, int monthlyWorkouts, int streakDays) {
        updateBarHeight(barWeek1, clampBarHeight(30 + (weeklyWorkouts * 12)));
        updateBarHeight(barWeek2, clampBarHeight(30 + (weeklyMinutes / 3)));
        updateBarHeight(barWeek3, clampBarHeight(30 + (monthlyWorkouts * 8)));
        updateBarHeight(barWeek4, clampBarHeight(30 + (streakDays * 10)));
    }

    private int clampBarHeight(int heightDp) {
        return Math.max(30, Math.min(120, heightDp));
    }

    private void updateBarHeight(View bar, int heightDp) {
        if (bar == null) {
            return;
        }
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = (int) (heightDp * getResources().getDisplayMetrics().density);
        bar.setLayoutParams(params);
    }

    private JsonObject getDataObject(JsonObject result) {
        if (result == null || !result.has("data") || result.get("data").isJsonNull() || !result.get("data").isJsonObject()) {
            return null;
        }
        return result.getAsJsonObject("data");
    }

    private int optInt(JsonObject source, String key, int fallback) {
        if (source == null || !source.has(key) || source.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return source.get(key).getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String optString(JsonObject source, String key, String fallback) {
        if (source == null || !source.has(key) || source.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return source.get(key).getAsString();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private JsonArray optArray(JsonObject source, String key) {
        if (source == null || !source.has(key) || source.get(key).isJsonNull() || !source.get(key).isJsonArray()) {
            return null;
        }
        return source.getAsJsonArray(key);
    }

    private Double optDouble(JsonObject source, String key) {
        if (source == null || !source.has(key) || source.get(key).isJsonNull()) {
            return null;
        }
        try {
            return source.get(key).getAsDouble();
        } catch (Exception ignored) {
            return null;
        }
    }
}
