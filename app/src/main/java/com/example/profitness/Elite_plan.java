package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.navigation.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Elite_plan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elite_plan);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_start_workout).setOnClickListener(v ->
                startActivity(new Intent(Elite_plan.this, active_workout.class))
        );

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_progress);
    }
}
