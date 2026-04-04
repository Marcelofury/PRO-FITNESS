package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.network.TokenStore;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TokenStore tokenStore = new TokenStore(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> target = (tokenStore.getToken() != null && !tokenStore.getToken().isEmpty())
                    ? Home_dashboard.class
                    : welcome_screen.class;

            startActivity(new Intent(SplashActivity.this, target));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
