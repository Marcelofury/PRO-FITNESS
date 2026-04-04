package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        api = new ProFitnessApi(new TokenStore(this));
        tokenStore = new TokenStore(this);

        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        tvProfileGoal = findViewById(R.id.tv_profile_goal);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_profile);

        Button btnRefresh = findViewById(R.id.btn_profile_refresh);
        Button btnLogout = findViewById(R.id.btn_profile_logout);

        btnRefresh.setOnClickListener(v -> loadProfile());
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
                    String goal = data.has("fitnessGoal") ? data.get("fitnessGoal").getAsString() : "Not set";

                    tvProfileName.setText(name);
                    tvProfileEmail.setText(email);
                    tvProfileGoal.setText(goal);
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
}
