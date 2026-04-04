package com.example.profitness;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.example.profitness.navigation.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class hydration_tracker extends AppCompatActivity {

    private static final int DAILY_GOAL_ML = 2500;

    private ProFitnessApi api;
    private ProgressBar waterProgressBar;
    private TextView tvWaterProgress;
    private TextView tvWaterPercent;
    private TextView tvLastDrink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydration_tracker);

        api = new ProFitnessApi(new TokenStore(this));

        waterProgressBar = findViewById(R.id.water_progress_bar);
        tvWaterProgress = findViewById(R.id.tv_water_progress);
        tvWaterPercent = findViewById(R.id.tv_water_percent);
        tvLastDrink = findViewById(R.id.tv_last_drink);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_nutrition);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.quick_add_250).setOnClickListener(v -> addWater(250));
        findViewById(R.id.quick_add_500).setOnClickListener(v -> addWater(500));
        findViewById(R.id.quick_add_750).setOnClickListener(v -> addWater(750));
        findViewById(R.id.quick_add_custom).setOnClickListener(v -> showCustomWaterInputDialog());
        findViewById(R.id.tv_view_history).setOnClickListener(v -> showHydrationHistory());

        loadTodayTotal();
    }

    private void addWater(int amountMl) {
        api.addHydration(amountMl, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    Toast.makeText(hydration_tracker.this, "+" + amountMl + "ml added", Toast.LENGTH_SHORT).show();
                    tvLastDrink.setText("Last drink: just now");
                    loadTodayTotal();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(hydration_tracker.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(hydration_tracker.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadTodayTotal() {
        api.getHydrationTodayTotal(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result.getAsJsonObject("data");
                    int totalMl = (data != null && data.has("totalMl")) ? data.get("totalMl").getAsInt() : 0;

                    int percent = Math.min(100, (int) ((totalMl * 100.0f) / DAILY_GOAL_ML));
                    waterProgressBar.setProgress(percent);

                    String litersProgress = String.format(java.util.Locale.US, "%.1f / %.1fL", totalMl / 1000f, DAILY_GOAL_ML / 1000f);
                    tvWaterProgress.setText(litersProgress);
                    tvWaterPercent.setText(percent + "% of Daily Goal");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(hydration_tracker.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(hydration_tracker.this, "Failed to load hydration data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showCustomWaterInputDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter ml (e.g. 350)");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(getColor(R.color.white));
        input.setHintTextColor(getColor(R.color.text_gray));

        new AlertDialog.Builder(this)
                .setTitle("Custom Water Amount")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {
                    String raw = input.getText().toString().trim();
                    if (raw.isEmpty()) {
                        Toast.makeText(this, "Enter an amount in ml", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(raw);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0 || amount > 5000) {
                        Toast.makeText(this, "Enter between 1 and 5000 ml", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addWater(amount);
                })
                .show();
    }

    private void showHydrationHistory() {
        api.getHydrationLogs(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonArray data = result != null && result.has("data") && result.get("data").isJsonArray()
                            ? result.getAsJsonArray("data")
                            : null;

                    List<String> rows = new ArrayList<>();
                    if (data != null) {
                        int max = Math.min(20, data.size());
                        for (int i = 0; i < max; i++) {
                            JsonElement element = data.get(i);
                            if (!element.isJsonObject()) {
                                continue;
                            }
                            JsonObject log = element.getAsJsonObject();
                            int amount = log.has("amountMl") && !log.get("amountMl").isJsonNull()
                                    ? log.get("amountMl").getAsInt()
                                    : 0;
                            rows.add(String.format(Locale.US, "%d ml", amount));
                        }
                    }

                    if (rows.isEmpty()) {
                        rows.add("No hydration logs yet");
                    }

                    CharSequence[] items = rows.toArray(new CharSequence[0]);
                    new AlertDialog.Builder(hydration_tracker.this)
                            .setTitle("Hydration History")
                            .setItems(items, null)
                            .setPositiveButton("Close", null)
                            .show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(hydration_tracker.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(hydration_tracker.this, "Failed to load hydration history", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
