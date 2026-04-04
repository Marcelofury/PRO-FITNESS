package com.example.profitness;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.example.profitness.navigation.BottomNavHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Home_dashboard extends AppCompatActivity {

    private static final int DEFAULT_DAILY_CALORIES_GOAL = 2450;
    private static final int DEFAULT_DAILY_PROTEIN_GOAL = 150;
    private static final int DEFAULT_DAILY_CARBS_GOAL = 250;
    private static final int DEFAULT_DAILY_FAT_GOAL = 60;
    private static final int DAILY_WATER_GOAL_ML = 2500;
    private static final int REQ_NOTIFICATIONS = 402;

    private ProFitnessApi api;
    private TextView lblFocus;
    private TextView tvWelcomeName;
    private TextView tvTodayDate;
    private TextView tvKcalRemaining;
    private TextView tvKcalGoal;
    private TextView tvMacroProtein;
    private TextView tvMacroCarbs;
    private TextView tvMacroFat;
    private TextView tvHomeWater;
    private TextView tvFocusSubtitle;
    private TextView tvHomeWeeklyWorkouts;
    private TextView tvHomeWeeklyMinutes;
    private TextView tvHomeMonthlyWorkouts;
    private TextView tvHomeRecentTitle;
    private TextView tvHomeRecentSubtitle;
    private ProgressBar progressKcalRing;
    private ProgressBar progressMacroProtein;
    private ProgressBar progressMacroCarbs;
    private ProgressBar progressMacroFat;
    private ProgressBar progressHomeWater;
    private int dailyCaloriesGoal = DEFAULT_DAILY_CALORIES_GOAL;
    private int dailyProteinGoal = DEFAULT_DAILY_PROTEIN_GOAL;
    private int dailyCarbsGoal = DEFAULT_DAILY_CARBS_GOAL;
    private int dailyFatGoal = DEFAULT_DAILY_FAT_GOAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        api = new ProFitnessApi(new TokenStore(this));
        lblFocus = findViewById(R.id.lbl_focus);
        tvWelcomeName = findViewById(R.id.tv_welcome_name);
        tvTodayDate = findViewById(R.id.tv_today_date);
        tvKcalRemaining = findViewById(R.id.tv_kcal_remaining);
        tvKcalGoal = findViewById(R.id.tv_kcal_goal);
        tvMacroProtein = findViewById(R.id.tv_macro_protein);
        tvMacroCarbs = findViewById(R.id.tv_macro_carbs);
        tvMacroFat = findViewById(R.id.tv_macro_fat);
        progressKcalRing = findViewById(R.id.progress_kcal_ring);
        progressMacroProtein = findViewById(R.id.progress_macro_protein);
        progressMacroCarbs = findViewById(R.id.progress_macro_carbs);
        progressMacroFat = findViewById(R.id.progress_macro_fat);
        progressHomeWater = findViewById(R.id.progress_home_water);
        tvHomeWater = findViewById(R.id.tv_home_water);
        tvFocusSubtitle = findViewById(R.id.tv_focus_subtitle);
        tvHomeWeeklyWorkouts = findViewById(R.id.tv_home_weekly_workouts);
        tvHomeWeeklyMinutes = findViewById(R.id.tv_home_weekly_minutes);
        tvHomeMonthlyWorkouts = findViewById(R.id.tv_home_monthly_workouts);
        tvHomeRecentTitle = findViewById(R.id.tv_home_recent_title);
        tvHomeRecentSubtitle = findViewById(R.id.tv_home_recent_subtitle);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_home);

        findViewById(R.id.btn_start_workout).setOnClickListener(v -> startActivity(new Intent(this, active_workout.class)));
        findViewById(R.id.card_home_workout).setOnClickListener(v -> startActivity(new Intent(this, active_workout.class)));
        findViewById(R.id.card_home_progress).setOnClickListener(v -> startActivity(new Intent(this, Your_progress.class)));
        findViewById(R.id.card_home_nutrition).setOnClickListener(v -> startActivity(new Intent(this, nutrition_log.class)));
        findViewById(R.id.card_home_hydration).setOnClickListener(v -> startActivity(new Intent(this, hydration_tracker.class)));
        findViewById(R.id.iv_home_settings).setOnClickListener(v -> startActivity(new Intent(this, settings.class)));

        tvTodayDate.setText(new SimpleDateFormat("EEEE, MMM d", Locale.US).format(new Date()));

        progressKcalRing.setMax(100);
        progressMacroProtein.setMax(100);
        progressMacroCarbs.setMax(100);
        progressMacroFat.setMax(100);
        progressHomeWater.setMax(100);

        ensureNotificationPermission();

        loadUserProfile();
        loadNutritionSummary();
        loadHydrationSummary();
        loadDashboardSummary();
    }

    private void loadUserProfile() {
        api.getMe(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = getDataObject(result);
                    String name = optString(data, "name", null);
                    if (name != null && !name.isEmpty()) {
                        tvWelcomeName.setText(name + "!");
                    }

                    applyProfileBasedGoals(data);
                    loadNutritionSummary();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Home_dashboard.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(Home_dashboard.this, "Could not load profile", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadNutritionSummary() {
        api.getNutritionTodaySummary(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = getDataObject(result);

                    int calories = optInt(data, "calories", 0);
                    int protein = optInt(data, "proteinGrams", 0);
                    int carbs = optInt(data, "carbsGrams", 0);
                    int fat = optInt(data, "fatGrams", 0);

                    int remaining = Math.max(0, dailyCaloriesGoal - calories);
                    tvKcalRemaining.setText(String.format(Locale.US, "%,d", remaining));
                    tvKcalGoal.setText("/ " + dailyCaloriesGoal + " KCAL");
                    tvMacroProtein.setText("PROTEIN   " + protein + "G / " + dailyProteinGoal + "G");
                    tvMacroCarbs.setText("CARBS   " + carbs + "G / " + dailyCarbsGoal + "G");
                    tvMacroFat.setText("FAT   " + fat + "G / " + dailyFatGoal + "G");

                    progressKcalRing.setProgress(toPercent(calories, dailyCaloriesGoal));
                    progressMacroProtein.setProgress(toPercent(protein, dailyProteinGoal));
                    progressMacroCarbs.setProgress(toPercent(carbs, dailyCarbsGoal));
                    progressMacroFat.setProgress(toPercent(fat, dailyFatGoal));
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Home_dashboard.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(Home_dashboard.this, "Could not load nutrition data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadHydrationSummary() {
        api.getHydrationTodayTotal(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = getDataObject(result);
                    int totalMl = optInt(data, "totalMl", 0);

                    int glasses = Math.max(0, Math.round(totalMl / 250f));
                    int goalGlasses = DAILY_WATER_GOAL_ML / 250;
                    tvHomeWater.setText(glasses + "/" + goalGlasses + " glasses today");
                    progressHomeWater.setProgress(toPercent(totalMl, DAILY_WATER_GOAL_ML));
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Home_dashboard.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(Home_dashboard.this, "Could not load hydration data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadDashboardSummary() {
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

                    lblFocus.setText("Today's Focus (" + workoutsCount + " workouts)");
                    tvFocusSubtitle.setText(streakDays > 0 ? streakDays + " day streak active" : "Start your first workout today");
                    tvHomeWeeklyWorkouts.setText(weeklyWorkouts + " workouts this week");
                    tvHomeWeeklyMinutes.setText(weeklyMinutes + " min this week");
                    tvHomeMonthlyWorkouts.setText(monthlyWorkouts + " workouts this month");

                    JsonArray recentWorkouts = optArray(data, "recentWorkouts");
                    if (recentWorkouts != null && recentWorkouts.size() > 0) {
                        JsonObject first = recentWorkouts.get(0).isJsonObject() ? recentWorkouts.get(0).getAsJsonObject() : null;
                        String workoutName = optString(first, "workoutName", "Latest Workout");
                        int duration = optInt(first, "durationMinutes", 0);
                        tvHomeRecentTitle.setText("Recent: " + workoutName);
                        tvHomeRecentSubtitle.setText(duration + " minutes completed • " + streakDays + " day streak");
                    } else {
                        tvHomeRecentTitle.setText("No workouts yet");
                        tvHomeRecentSubtitle.setText("Start a workout to build your streak");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(Home_dashboard.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(Home_dashboard.this, "Dashboard sync failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void applyProfileBasedGoals(JsonObject profileData) {
        if (profileData == null) {
            return;
        }

        double weightKg = optDouble(profileData, "weightKg", 0);
        String goalText = optString(profileData, "goal", "").toLowerCase(Locale.US);

        if (weightKg <= 0) {
            return;
        }

        int calories = (int) Math.round(weightKg * 33);
        if (goalText.contains("lose") || goalText.contains("fat loss") || goalText.contains("cut")) {
            calories -= 300;
        } else if (goalText.contains("gain") || goalText.contains("bulk")) {
            calories += 250;
        }

        int protein = (int) Math.max(110, Math.round(weightKg * 2.0));
        int fat = (int) Math.max(45, Math.round(weightKg * 0.8));
        int carbs = Math.max(100, (calories - (protein * 4) - (fat * 9)) / 4);

        dailyCaloriesGoal = Math.max(1600, calories);
        dailyProteinGoal = protein;
        dailyFatGoal = fat;
        dailyCarbsGoal = carbs;
    }

    private int toPercent(int current, int goal) {
        if (goal <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, Math.round((current * 100f) / goal)));
    }

    private JsonObject getDataObject(JsonObject result) {
        if (result == null || !result.has("data") || result.get("data").isJsonNull() || !result.get("data").isJsonObject()) {
            return null;
        }
        return result.getAsJsonObject("data");
    }

    private int optInt(JsonObject object, String key, int fallback) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return object.get(key).getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double optDouble(JsonObject object, String key, double fallback) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return object.get(key).getAsDouble();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String optString(JsonObject object, String key, String fallback) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private JsonArray optArray(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull() || !object.get(key).isJsonArray()) {
            return null;
        }
        return object.getAsJsonArray(key);
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
