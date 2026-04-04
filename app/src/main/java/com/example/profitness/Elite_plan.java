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
import com.google.gson.JsonObject;

public class Elite_plan extends AppCompatActivity {

    private ProFitnessApi api;
    private TextView tvPlanTitle;
    private TextView tvPlanPhase;
    private TextView tvDayFocusTitle;
    private TextView tvDayFocusDuration;
    private TextView tvPlanExercise;
    private TextView tvPlanSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elite_plan);

        api = new ProFitnessApi(new TokenStore(this));
        tvPlanTitle = findViewById(R.id.tv_plan_title);
        tvPlanPhase = findViewById(R.id.tv_plan_phase);
        tvDayFocusTitle = findViewById(R.id.tv_day_focus_title);
        tvDayFocusDuration = findViewById(R.id.tv_day_focus_duration);
        tvPlanExercise = findViewById(R.id.tv_plan_exercise);
        tvPlanSets = findViewById(R.id.tv_plan_sets);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_start_workout).setOnClickListener(v ->
                startActivity(new Intent(Elite_plan.this, active_workout.class))
        );
        findViewById(R.id.card_plan_exercise).setOnClickListener(v -> {
            Intent intent = new Intent(Elite_plan.this, exercise_details.class);
            intent.putExtra("exercise_name", tvPlanExercise.getText().toString());
            startActivity(intent);
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_progress);

        loadPlanData();
    }

    private void loadPlanData() {
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

                    tvPlanTitle.setText(goal);
                    tvPlanPhase.setText("Current Phase: Adaptive Progression");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Elite_plan.this, errorMessage)) {
                        return;
                    }
                    tvPlanTitle.setText("Training Plan");
                    tvPlanPhase.setText("Plan goal unavailable");
                    Toast.makeText(Elite_plan.this, "Could not load plan goal", Toast.LENGTH_SHORT).show();
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
                    int weeklyWorkouts = data != null && data.has("weeklyWorkoutsCount") ? data.get("weeklyWorkoutsCount").getAsInt() : 0;

                    if (weeklyWorkouts > 0) {
                        int estimatedMinutes = Math.max(1, weeklyMinutes / weeklyWorkouts);
                        tvDayFocusTitle.setText("Next Session - Progressive Focus");
                        tvDayFocusDuration.setText("Estimated Duration: " + estimatedMinutes + " mins");
                    } else {
                        tvDayFocusTitle.setText("Start your first workout to build a plan");
                        tvDayFocusDuration.setText("Estimated Duration: --");
                    }

                    JsonArray recentWorkouts = data != null && data.has("recentWorkouts") && data.get("recentWorkouts").isJsonArray()
                            ? data.getAsJsonArray("recentWorkouts")
                            : null;
                    if (recentWorkouts != null && recentWorkouts.size() > 0 && recentWorkouts.get(0).isJsonObject()) {
                        JsonObject first = recentWorkouts.get(0).getAsJsonObject();
                        String name = first.has("workoutName") && !first.get("workoutName").isJsonNull()
                                ? first.get("workoutName").getAsString()
                                : "Workout";
                        int duration = first.has("durationMinutes") && !first.get("durationMinutes").isJsonNull()
                                ? first.get("durationMinutes").getAsInt()
                                : 30;

                        tvPlanExercise.setText(name);
                        tvPlanSets.setText("Duration: " + duration + " mins");
                    } else {
                        tvPlanExercise.setText("No recent workout");
                        tvPlanSets.setText("Log a workout to see details");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Elite_plan.this, errorMessage)) {
                        return;
                    }
                    tvDayFocusTitle.setText("Plan metrics unavailable");
                    tvDayFocusDuration.setText("Estimated Duration: --");
                    tvPlanExercise.setText("No recent workout");
                    tvPlanSets.setText("Log a workout to see details");
                    Toast.makeText(Elite_plan.this, "Could not load plan metrics", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
