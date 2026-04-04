package com.example.profitness;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.navigation.BottomNavHelper;
import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

public class active_workout extends AppCompatActivity {

    private ProFitnessApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout);

        api = new ProFitnessApi(new TokenStore(this));

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_workout);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        findViewById(R.id.btn_save_workout).setOnClickListener(v -> saveWorkout());
    }

    private void saveWorkout() {
        TextView titleView = findViewById(R.id.tv_exercise_title);
        String workoutName = titleView.getText().toString().trim();

        if (workoutName.isEmpty()) {
            workoutName = "Workout Session";
        }

        int durationMinutes = 35;
        int caloriesBurned = 280;

        api.createWorkout(workoutName, durationMinutes, caloriesBurned, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> Toast.makeText(active_workout.this, "Workout saved", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(active_workout.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }
}
