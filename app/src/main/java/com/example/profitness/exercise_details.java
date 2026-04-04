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
        TextView tvTagMuscle = findViewById(R.id.tv_tag_muscle);
        TextView tvTagDifficulty = findViewById(R.id.tv_tag_difficulty);
        TextView tvTagDuration = findViewById(R.id.tv_tag_duration);
        TextView tvInstructions = findViewById(R.id.tv_instructions_body);
        TextView tvMistakes = findViewById(R.id.tv_common_mistakes);

        String exerciseName = getIntent().getStringExtra("exercise_name");
        String muscle = getIntent().getStringExtra("exercise_muscle");
        String difficulty = getIntent().getStringExtra("exercise_difficulty");
        int duration = getIntent().getIntExtra("exercise_duration", 15);

        if (exerciseName != null && !exerciseName.trim().isEmpty()) {
            tvTitle.setText(exerciseName);
        }

        tvTagMuscle.setText(muscle != null && !muscle.trim().isEmpty() ? muscle : "General");
        tvTagDifficulty.setText(difficulty != null && !difficulty.trim().isEmpty() ? difficulty : "Beginner");
        tvTagDuration.setText(duration + " min");

        tvInstructions.setText(buildInstructions(exerciseName, muscle));
        tvMistakes.setText(buildCommonMistakes(muscle));

        findViewById(R.id.fab_add_to_workout).setOnClickListener(v -> {
            Intent intent = new Intent(exercise_details.this, active_workout.class);
            intent.putExtra("prefill_workout_name", tvTitle.getText().toString());
            startActivity(intent);
        });

        TextView tagsText = findViewById(R.id.exercise_title);
        tagsText.setContentDescription((muscle != null ? muscle : "General") + " • "
                + (difficulty != null ? difficulty : "Beginner") + " • " + duration + " min");
    }

    private String buildInstructions(String exerciseName, String muscle) {
        String displayName = exerciseName != null && !exerciseName.trim().isEmpty() ? exerciseName : "this exercise";
        String primary = muscle != null && !muscle.trim().isEmpty() ? muscle.toLowerCase() : "target";

        return "1. Set up with stable posture before starting " + displayName + ".\n\n"
                + "2. Control the lowering phase and keep tension on your " + primary + " muscles.\n\n"
                + "3. Exhale during the effort phase and maintain smooth reps.\n\n"
                + "4. Stop the set if form breaks and reset before continuing.";
    }

    private String buildCommonMistakes(String muscle) {
        String primary = muscle != null && !muscle.trim().isEmpty() ? muscle.toLowerCase() : "target";
        return "Using momentum instead of controlled movement, shortening range of motion, and shifting load away from "
                + primary + " by poor posture.";
    }
}
