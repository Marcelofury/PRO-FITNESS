package com.example.profitness;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.profitness.navigation.BottomNavHelper;
import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.JsonObject;

public class intensity_focus extends AppCompatActivity {

    private ProFitnessApi api;
    private TextView tvCurrentFocus;
    private TextView tvFocusPhase;
    private TextView tvPeakStatus;
    private TextView tvIntensityValue;
    private TextView tvDeloadHint;
    private ProgressBar intensityProgress;
    private SwitchMaterial switchDeload;
    private TextView chipRest30;
    private TextView chipRest60;
    private TextView chipRest90;
    private TextView chipRest120;
    private int baseIntensity = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intensity_focus);

        api = new ProFitnessApi(new TokenStore(this));
        tvCurrentFocus = findViewById(R.id.tv_current_focus);
        tvFocusPhase = findViewById(R.id.tv_focus_phase);
        tvPeakStatus = findViewById(R.id.tv_peak_status);
        tvIntensityValue = findViewById(R.id.tv_intensity_value);
        tvDeloadHint = findViewById(R.id.tv_deload_hint);
        intensityProgress = findViewById(R.id.intensity_progress);
        switchDeload = findViewById(R.id.switch_deload);
        chipRest30 = findViewById(R.id.chip_rest_30);
        chipRest60 = findViewById(R.id.chip_rest_60);
        chipRest90 = findViewById(R.id.chip_rest_90);
        chipRest120 = findViewById(R.id.chip_rest_120);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupRestChipInteractions();
        switchDeload.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String suffix = isChecked ? "Deload active: reduced working intensity." : "Recommended every 4-6 weeks to prevent overtraining.";
            tvDeloadHint.setText(suffix);
            applyIntensityUi(baseIntensity);
            Toast.makeText(this, isChecked ? "Deload enabled" : "Deload disabled", Toast.LENGTH_SHORT).show();
        });

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

                    int weeklyMinutes = optInt(data, "weeklyWorkoutMinutes", 0);
                    int streakDays = optInt(data, "streakDays", 0);

                    baseIntensity = Math.max(10, Math.min(100, (weeklyMinutes / 3) + (streakDays * 5)));
                    applyIntensityUi(baseIntensity);
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

    private void setupRestChipInteractions() {
        chipRest30.setOnClickListener(v -> selectRestChip(chipRest30, 30));
        chipRest60.setOnClickListener(v -> selectRestChip(chipRest60, 60));
        chipRest90.setOnClickListener(v -> selectRestChip(chipRest90, 90));
        chipRest120.setOnClickListener(v -> selectRestChip(chipRest120, 120));
    }

    private void selectRestChip(TextView selectedChip, int seconds) {
        TextView[] chips = new TextView[]{chipRest30, chipRest60, chipRest90, chipRest120};
        for (TextView chip : chips) {
            boolean isSelected = chip == selectedChip;
            chip.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(this, isSelected ? android.R.color.black : R.color.white));
            chip.setTypeface(null, isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        tvFocusPhase.setText("Rest interval: " + seconds + "s • adaptive phase ongoing");
    }

    private void applyIntensityUi(int intensity) {
        int adjustedIntensity = switchDeload.isChecked() ? Math.max(10, intensity - 15) : intensity;
        intensityProgress.setProgress(adjustedIntensity);
        tvIntensityValue.setText(adjustedIntensity + "%");

        if (adjustedIntensity >= 80) {
            tvPeakStatus.setText("PEAK READY");
            tvPeakStatus.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
        } else if (adjustedIntensity >= 50) {
            tvPeakStatus.setText("OPTIMAL");
            tvPeakStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tvPeakStatus.setText("RECOVERY");
            tvPeakStatus.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        }
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
}
