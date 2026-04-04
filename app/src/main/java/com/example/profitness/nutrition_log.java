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
import com.google.gson.JsonObject;

public class nutrition_log extends AppCompatActivity {

    private static final int DAILY_CALORIES_GOAL = 2450;
    private static final int DAILY_WATER_GOAL_ML = 2500;

    private ProFitnessApi api;
    private TextView valCal;
    private TextView valProtein;
    private TextView tvWaterTotal;
    private ProgressBar progressWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_log);

        api = new ProFitnessApi(new TokenStore(this));

        valCal = findViewById(R.id.val_cal);
        valProtein = findViewById(R.id.val_protein);
        tvWaterTotal = findViewById(R.id.tv_water_total);
        progressWater = findViewById(R.id.progress_water_total);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_nutrition);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Button btnAddFood = findViewById(R.id.btn_add_food);
        btnAddFood.setOnClickListener(v -> showAddFoodDialog());

        findViewById(R.id.btn_add_250ml).setOnClickListener(v -> addWater(250));
        findViewById(R.id.btn_add_500ml).setOnClickListener(v -> addWater(500));

        loadNutritionSummary();
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
                    valCal.setText(String.format(java.util.Locale.US, "%,d", remaining));
                    valProtein.setText(protein + "g");
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
