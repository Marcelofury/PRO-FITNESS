package com.example.profitness;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.example.profitness.navigation.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class nutrition_log extends AppCompatActivity {

    private static final int DAILY_CALORIES_GOAL = 2450;
    private static final int DAILY_WATER_GOAL_ML = 2500;

    private ProFitnessApi api;
    private TextView tvDayLabel;
    private TextView valCal;
    private TextView valProtein;
    private TextView tvFoodName;
    private TextView tvFoodMeta;
    private TextView tvFoodKcal;
    private TextView tvWaterTotal;
    private ProgressBar progressCalories;
    private ProgressBar progressProtein;
    private ProgressBar progressWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_log);

        api = new ProFitnessApi(new TokenStore(this));

        tvDayLabel = findViewById(R.id.tv_day_label);
        valCal = findViewById(R.id.val_cal);
        valProtein = findViewById(R.id.val_protein);
        tvFoodName = findViewById(R.id.food1);
        tvFoodMeta = findViewById(R.id.tv_food_meta);
        tvFoodKcal = findViewById(R.id.tv_food_kcal);
        tvWaterTotal = findViewById(R.id.tv_water_total);
        progressCalories = findViewById(R.id.progress_calories);
        progressProtein = findViewById(R.id.progress_protein);
        progressWater = findViewById(R.id.progress_water_total);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_nutrition);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Button btnAddFood = findViewById(R.id.btn_add_food);
        btnAddFood.setOnClickListener(v -> showAddFoodDialog());

        findViewById(R.id.btn_add_250ml).setOnClickListener(v -> addWater(250));
        findViewById(R.id.btn_add_500ml).setOnClickListener(v -> addWater(500));

        tvDayLabel.setText("Today, " + new SimpleDateFormat("MMM d", Locale.US).format(new Date()));

        loadNutritionSummary();
        loadLatestMeal();
        loadHydrationSummary();
    }

    private void showAddFoodDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etMeal = new EditText(this);
        etMeal.setHint("Meal name");

        EditText etCalories = new EditText(this);
        etCalories.setHint("Calories");
        etCalories.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etProtein = new EditText(this);
        etProtein.setHint("Protein (g)");
        etProtein.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etCarbs = new EditText(this);
        etCarbs.setHint("Carbs (g)");
        etCarbs.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etFat = new EditText(this);
        etFat.setHint("Fat (g)");
        etFat.setInputType(InputType.TYPE_CLASS_NUMBER);

        container.addView(etMeal);
        container.addView(etCalories);
        container.addView(etProtein);
        container.addView(etCarbs);
        container.addView(etFat);

        new AlertDialog.Builder(this)
                .setTitle("Add Food")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String mealName = etMeal.getText().toString().trim();
                    int calories = parseOrZero(etCalories.getText().toString());
                    int protein = parseOrZero(etProtein.getText().toString());
                    int carbs = parseOrZero(etCarbs.getText().toString());
                    int fat = parseOrZero(etFat.getText().toString());

                    if (mealName.isEmpty() || calories <= 0) {
                        Toast.makeText(this, "Enter meal name and calories", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    api.addNutrition(mealName, calories, protein, carbs, fat, new ApiCallback<JsonObject>() {
                        @Override
                        public void onSuccess(JsonObject result) {
                            runOnUiThread(() -> {
                                Toast.makeText(nutrition_log.this, "Food added", Toast.LENGTH_SHORT).show();
                                loadNutritionSummary();
                                loadLatestMeal();
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> {
                                if (AuthSessionHelper.handleIfAuthExpired(nutrition_log.this, errorMessage)) {
                                    return;
                                }
                                Toast.makeText(nutrition_log.this, errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .show();
    }

    private void addWater(int amountMl) {
        api.addHydration(amountMl, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    Toast.makeText(nutrition_log.this, "+" + amountMl + "ml", Toast.LENGTH_SHORT).show();
                    loadHydrationSummary();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(nutrition_log.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(nutrition_log.this, errorMessage, Toast.LENGTH_LONG).show();
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

                    int remaining = Math.max(0, DAILY_CALORIES_GOAL - calories);
                    valCal.setText(String.format(Locale.US, "%,d", remaining));
                    valProtein.setText(protein + "g");
                    progressCalories.setMax(100);
                    progressCalories.setProgress(Math.min(100, (int) ((calories * 100f) / DAILY_CALORIES_GOAL)));
                    progressProtein.setMax(100);
                    progressProtein.setProgress(Math.min(100, (int) ((protein * 100f) / 150f)));
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(nutrition_log.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(nutrition_log.this, "Failed to load nutrition summary", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadLatestMeal() {
        api.getNutritionLogs(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonArray data = result != null && result.has("data") && result.get("data").isJsonArray()
                            ? result.getAsJsonArray("data")
                            : null;

                    if (data == null || data.size() == 0) {
                        tvFoodName.setText("No meals logged yet");
                        tvFoodMeta.setText("Tap + Add Food to log your first meal");
                        tvFoodKcal.setText("0 kcal");
                        return;
                    }

                    JsonElement first = data.get(0);
                    if (!first.isJsonObject()) {
                        return;
                    }

                    JsonObject latest = first.getAsJsonObject();
                    String mealName = latest.has("mealName") && !latest.get("mealName").isJsonNull()
                            ? latest.get("mealName").getAsString()
                            : "Meal";
                    int calories = latest.has("calories") && !latest.get("calories").isJsonNull()
                            ? latest.get("calories").getAsInt()
                            : 0;
                    int protein = latest.has("proteinGrams") && !latest.get("proteinGrams").isJsonNull()
                            ? latest.get("proteinGrams").getAsInt()
                            : 0;
                    int carbs = latest.has("carbsGrams") && !latest.get("carbsGrams").isJsonNull()
                            ? latest.get("carbsGrams").getAsInt()
                            : 0;
                    int fat = latest.has("fatGrams") && !latest.get("fatGrams").isJsonNull()
                            ? latest.get("fatGrams").getAsInt()
                            : 0;

                    tvFoodName.setText(mealName);
                    tvFoodMeta.setText("P " + protein + "g • C " + carbs + "g • F " + fat + "g");
                    tvFoodKcal.setText(calories + " kcal");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(nutrition_log.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(nutrition_log.this, "Failed to load meal logs", Toast.LENGTH_SHORT).show();
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
                    int totalMl = (data != null && data.has("totalMl")) ? data.get("totalMl").getAsInt() : 0;
                    int percent = Math.min(100, (int) ((totalMl * 100.0f) / DAILY_WATER_GOAL_ML));

                    tvWaterTotal.setText(String.format(java.util.Locale.US, "%.1fL / %.1fL", totalMl / 1000f, DAILY_WATER_GOAL_ML / 1000f));
                    progressWater.setProgress(percent);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(nutrition_log.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(nutrition_log.this, "Failed to load hydration data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private int parseOrZero(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
