package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.navigation.BottomNavHelper;
import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

public class profile extends AppCompatActivity {

    private ProFitnessApi api;
    private TokenStore tokenStore;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileGoal;
    private TextView tvProfileAge;
    private TextView tvProfileHeight;
    private TextView tvProfileWeight;
    private Integer currentAge;
    private Integer currentHeightCm;
    private Integer currentWeightKg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        api = new ProFitnessApi(new TokenStore(this));
        tokenStore = new TokenStore(this);

        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        tvProfileGoal = findViewById(R.id.tv_profile_goal);
        tvProfileAge = findViewById(R.id.tv_profile_age);
        tvProfileHeight = findViewById(R.id.tv_profile_height);
        tvProfileWeight = findViewById(R.id.tv_profile_weight);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_profile);

        Button btnRefresh = findViewById(R.id.btn_profile_refresh);
        Button btnEdit = findViewById(R.id.btn_profile_edit);
        Button btnLogout = findViewById(R.id.btn_profile_logout);

        btnRefresh.setOnClickListener(v -> loadProfile());
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    private void loadProfile() {
        api.getMe(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result.getAsJsonObject("data");
                    if (data == null) {
                        Toast.makeText(profile.this, "Profile data unavailable", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = data.has("name") ? data.get("name").getAsString() : "No name";
                    String email = data.has("email") ? data.get("email").getAsString() : "No email";
                    String goal = data.has("goal") ? data.get("goal").getAsString() : "Not set";

                    currentAge = parseNullableInt(data, "age");
                    currentHeightCm = parseNullableInt(data, "heightCm");
                    currentWeightKg = parseNullableInt(data, "weightKg");

                    tvProfileName.setText(name);
                    tvProfileEmail.setText(email);
                    tvProfileGoal.setText(goal);
                    tvProfileAge.setText(currentAge != null ? currentAge + " years" : "Not set");
                    tvProfileHeight.setText(currentHeightCm != null ? currentHeightCm + " cm" : "Not set");
                    tvProfileWeight.setText(currentWeightKg != null ? currentWeightKg + " kg" : "Not set");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(profile.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(profile.this, "Could not load profile", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void logout() {
        tokenStore.clearToken();
        Intent intent = new Intent(this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showEditDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(tvProfileName.getText().toString());

        EditText etAge = new EditText(this);
        etAge.setHint("Age");
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        etAge.setText(currentAge != null ? String.valueOf(currentAge) : "");

        EditText etHeight = new EditText(this);
        etHeight.setHint("Height (cm)");
        etHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
        etHeight.setText(currentHeightCm != null ? String.valueOf(currentHeightCm) : "");

        EditText etWeight = new EditText(this);
        etWeight.setHint("Weight (kg)");
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER);
        etWeight.setText(currentWeightKg != null ? String.valueOf(currentWeightKg) : "");

        EditText etGoal = new EditText(this);
        etGoal.setHint("Goal");
        etGoal.setText(tvProfileGoal.getText().toString());

        container.addView(etName);
        container.addView(etAge);
        container.addView(etHeight);
        container.addView(etWeight);
        container.addView(etGoal);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> submitProfileUpdate(
                        etName.getText().toString().trim(),
                        parseIntegerOrNull(etAge.getText().toString()),
                        parseIntegerOrNull(etHeight.getText().toString()),
                        parseIntegerOrNull(etWeight.getText().toString()),
                        etGoal.getText().toString().trim()
                ))
                .show();
    }

    private void submitProfileUpdate(String name, Integer age, Integer heightCm, Integer weightKg, String goal) {
        if (!isValidProfileInput(name, age, heightCm, weightKg)) {
            return;
        }

        api.updateProfile(name, age, heightCm, weightKg, goal, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    Toast.makeText(profile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    loadProfile();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(profile.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(profile.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean isValidProfileInput(String name, Integer age, Integer heightCm, Integer weightKg) {
        if (name != null && !name.isEmpty() && name.length() < 2) {
            Toast.makeText(this, "Name must be at least 2 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (age != null && (age < 1 || age > 120)) {
            Toast.makeText(this, "Age must be between 1 and 120", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (heightCm != null && (heightCm < 50 || heightCm > 300)) {
            Toast.makeText(this, "Height must be between 50 and 300 cm", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (weightKg != null && (weightKg < 10 || weightKg > 500)) {
            Toast.makeText(this, "Weight must be between 10 and 500 kg", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseNullableInt(JsonObject data, String field) {
        if (!data.has(field) || data.get(field).isJsonNull()) {
            return null;
        }

        try {
            return data.get(field).getAsInt();
        } catch (Exception ignored) {
            return null;
        }
    }
}
