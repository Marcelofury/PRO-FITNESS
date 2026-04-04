package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.navigation.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class exercise_details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_details);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_workout);

        TextView tvTitle = findViewById(R.id.exercise_title);

        String exerciseName = getIntent().getStringExtra("exercise_name");
        String muscle = getIntent().getStringExtra("exercise_muscle");
        String difficulty = getIntent().getStringExtra("exercise_difficulty");
        int duration = getIntent().getIntExtra("exercise_duration", 15);

        if (exerciseName != null && !exerciseName.trim().isEmpty()) {
            tvTitle.setText(exerciseName);
        }

        findViewById(R.id.fab_add_to_workout).setOnClickListener(v -> {
            Intent intent = new Intent(exercise_details.this, active_workout.class);
            intent.putExtra("prefill_workout_name", tvTitle.getText().toString());
            startActivity(intent);
        });

        TextView tagsText = findViewById(R.id.exercise_title);
        tagsText.setContentDescription((muscle != null ? muscle : "General") + " • "
                + (difficulty != null ? difficulty : "Beginner") + " • " + duration + " min");
    }
}
