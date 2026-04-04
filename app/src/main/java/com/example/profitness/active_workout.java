package com.example.profitness;

import android.content.Intent;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

public class active_workout extends AppCompatActivity {

    private ProFitnessApi api;
    private TextView tvExerciseTitle;
    private TextView tvTimerMinutes;
    private TextView tvTimerSeconds;
    private TextView tvDurationValue;
    private TextView tvTotalVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout);

        api = new ProFitnessApi(new TokenStore(this));
        tvExerciseTitle = findViewById(R.id.tv_exercise_title);
        tvTimerMinutes = findViewById(R.id.tv_timer_minutes);
        tvTimerSeconds = findViewById(R.id.tv_timer_seconds);
        tvDurationValue = findViewById(R.id.tv_duration_value);
        tvTotalVolume = findViewById(R.id.tv_total_volume);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_workout);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_library).setOnClickListener(v ->
            startActivity(new Intent(active_workout.this, exercise_library.class))
        );

        findViewById(R.id.btn_add_set).setOnClickListener(v -> addSetAndAdvanceTimer());
        findViewById(R.id.btn_save_workout).setOnClickListener(v -> saveWorkout());

        String prefillWorkoutName = getIntent().getStringExtra("prefill_workout_name");
        if (prefillWorkoutName != null && !prefillWorkoutName.trim().isEmpty()) {
            applyWorkoutToUi(prefillWorkoutName, 35, estimateCalories(35));
        } else {
            loadLatestWorkout();
        }
    }

    private void saveWorkout() {
        String workoutName = tvExerciseTitle.getText().toString().trim();

        if (workoutName.isEmpty()) {
            workoutName = "Workout Session";
        }

        int durationMinutes = parseDurationMinutesFromUi();
        int caloriesBurned = estimateCalories(durationMinutes);

        api.createWorkout(workoutName, durationMinutes, caloriesBurned, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    Toast.makeText(active_workout.this, "Workout saved", Toast.LENGTH_SHORT).show();
                    loadLatestWorkout();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(active_workout.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(active_workout.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadLatestWorkout() {
        api.getWorkouts(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonArray data = result != null && result.has("data") && result.get("data").isJsonArray()
                            ? result.getAsJsonArray("data")
                            : null;

                    if (data == null || data.size() == 0) {
                        applyWorkoutToUi("Workout Session", 35, 280);
                        return;
                    }

                    JsonElement firstItem = data.get(0);
                    if (!firstItem.isJsonObject()) {
                        applyWorkoutToUi("Workout Session", 35, 280);
                        return;
                    }

                    JsonObject latest = firstItem.getAsJsonObject();
                    String name = latest.has("workoutName") && !latest.get("workoutName").isJsonNull()
                            ? latest.get("workoutName").getAsString()
                            : "Workout Session";
                    int duration = latest.has("durationMinutes") && !latest.get("durationMinutes").isJsonNull()
                            ? Math.max(1, latest.get("durationMinutes").getAsInt())
                            : 35;
                    int calories = latest.has("caloriesBurned") && !latest.get("caloriesBurned").isJsonNull()
                            ? Math.max(1, latest.get("caloriesBurned").getAsInt())
                            : estimateCalories(duration);

                    applyWorkoutToUi(name, duration, calories);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(active_workout.this, errorMessage)) {
                        return;
                    }
                    applyWorkoutToUi("Workout Session", 35, 280);
                });
            }
        });
    }

    private void applyWorkoutToUi(String name, int durationMinutes, int caloriesBurned) {
        int safeDuration = Math.max(1, durationMinutes);
        int minutes = Math.max(0, safeDuration);

        tvExerciseTitle.setText(name);
        tvTimerMinutes.setText(String.format(Locale.US, "%02d", minutes));
        tvTimerSeconds.setText("00");
        tvDurationValue.setText(String.format(Locale.US, "%02d:00", minutes));
        tvTotalVolume.setText(caloriesBurned + " kcal");
    }

    private int parseDurationMinutesFromUi() {
        try {
            String raw = tvTimerMinutes.getText().toString().trim();
            int parsed = Integer.parseInt(raw);
            return Math.max(1, parsed);
        } catch (Exception ignored) {
            return 35;
        }
    }

    private int estimateCalories(int durationMinutes) {
        return Math.max(80, durationMinutes * 8);
    }

    private void addSetAndAdvanceTimer() {
        int currentMinutes = parseDurationMinutesFromUi();
        int updatedMinutes = currentMinutes + 5;
        tvTimerMinutes.setText(String.format(Locale.US, "%02d", updatedMinutes));
        tvDurationValue.setText(String.format(Locale.US, "%02d:00", updatedMinutes));
        tvTotalVolume.setText(estimateCalories(updatedMinutes) + " kcal");
        Toast.makeText(this, "Set added", Toast.LENGTH_SHORT).show();
    }
}
