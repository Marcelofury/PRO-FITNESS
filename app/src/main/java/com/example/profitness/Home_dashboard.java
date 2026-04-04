package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.example.profitness.navigation.BottomNavHelper;
import com.google.gson.JsonObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Home_dashboard extends AppCompatActivity {

    private static final int DAILY_CALORIES_GOAL = 2450;
    private static final int DAILY_PROTEIN_GOAL = 150;
    private static final int DAILY_CARBS_GOAL = 250;
    private static final int DAILY_FAT_GOAL = 60;
    private static final int DAILY_WATER_GOAL_ML = 2500;

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
        tvHomeWater = findViewById(R.id.tv_home_water);
        tvFocusSubtitle = findViewById(R.id.tv_focus_subtitle);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_home);

        findViewById(R.id.btn_start_workout).setOnClickListener(v -> startActivity(new Intent(this, active_workout.class)));

        tvTodayDate.setText(new SimpleDateFormat("EEEE, MMM d", Locale.US).format(new Date()));

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
                    JsonObject data = result.getAsJsonObject("data");
                    if (data != null && data.has("name")) {
                        tvWelcomeName.setText(data.get("name").getAsString() + "!");
                    }
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
                    JsonObject data = result.getAsJsonObject("data");

                    int calories = data != null && data.has("calories") ? data.get("calories").getAsInt() : 0;
                    int protein = data != null && data.has("proteinGrams") ? data.get("proteinGrams").getAsInt() : 0;
                    int carbs = data != null && data.has("carbsGrams") ? data.get("carbsGrams").getAsInt() : 0;
                    int fat = data != null && data.has("fatGrams") ? data.get("fatGrams").getAsInt() : 0;

                    int remaining = Math.max(0, DAILY_CALORIES_GOAL - calories);
                    tvKcalRemaining.setText(String.format(Locale.US, "%,d", remaining));
                    tvKcalGoal.setText("/ " + DAILY_CALORIES_GOAL + " KCAL");
                    tvMacroProtein.setText("PROTEIN   " + protein + "G / " + DAILY_PROTEIN_GOAL + "G");
                    tvMacroCarbs.setText("CARBS   " + carbs + "G / " + DAILY_CARBS_GOAL + "G");
                    tvMacroFat.setText("FAT   " + fat + "G / " + DAILY_FAT_GOAL + "G");
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
                    JsonObject data = result.getAsJsonObject("data");
                    int totalMl = data != null && data.has("totalMl") ? data.get("totalMl").getAsInt() : 0;

                    int glasses = Math.max(0, Math.round(totalMl / 250f));
                    int goalGlasses = DAILY_WATER_GOAL_ML / 250;
                    tvHomeWater.setText(glasses + "/" + goalGlasses + " glasses today");
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
                    JsonObject data = result.getAsJsonObject("data");
                    int workoutsCount = data != null && data.has("workoutsCount") ? data.get("workoutsCount").getAsInt() : 0;
                    lblFocus.setText("Today's Focus (" + workoutsCount + " workouts)");
                    tvFocusSubtitle.setText(workoutsCount > 0 ? "Keep your streak alive" : "Start your first workout today");
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
}
