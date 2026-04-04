package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.network.TokenStore;

public class settings extends AppCompatActivity {

    private TokenStore tokenStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tokenStore = new TokenStore(this);

        findViewById(R.id.btn_settings_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_settings_profile).setOnClickListener(v ->
                startActivity(new Intent(settings.this, profile.class))
        );
        findViewById(R.id.btn_settings_dashboard).setOnClickListener(v ->
                startActivity(new Intent(settings.this, Home_dashboard.class))
        );
        findViewById(R.id.btn_settings_logout).setOnClickListener(v -> logout());
    }

    private void logout() {
        tokenStore.clearToken();
        Intent intent = new Intent(this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
