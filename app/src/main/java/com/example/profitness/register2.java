package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.example.profitness.network.OnboardingStore;

import androidx.appcompat.app.AppCompatActivity;

public class register2 extends AppCompatActivity {

    private String selectedGoal = "Lose Weight";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        OnboardingStore store = new OnboardingStore(this);

        RelativeLayout cardLose = findViewById(R.id.cardGoalLose);
        RelativeLayout cardBuild = findViewById(R.id.cardGoalBuild);
        RelativeLayout cardKeep = findViewById(R.id.cardGoalKeep);
        RelativeLayout cardEndurance = findViewById(R.id.cardGoalEndurance);

        RadioButton rbLose = findViewById(R.id.rbGoalLose);
        RadioButton rbBuild = findViewById(R.id.rbGoalBuild);
        RadioButton rbKeep = findViewById(R.id.rbGoalKeep);
        RadioButton rbEndurance = findViewById(R.id.rbGoalEndurance);

        ImageButton btnBack = findViewById(R.id.btnBack);

        cardLose.setOnClickListener(v -> updateGoal("Lose Weight", rbLose, rbBuild, rbKeep, rbEndurance));
        cardBuild.setOnClickListener(v -> updateGoal("Build Muscle", rbLose, rbBuild, rbKeep, rbEndurance));
        cardKeep.setOnClickListener(v -> updateGoal("Keep Fit", rbLose, rbBuild, rbKeep, rbEndurance));
        cardEndurance.setOnClickListener(v -> updateGoal("Increase Endurance", rbLose, rbBuild, rbKeep, rbEndurance));

        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            store.setGoal(selectedGoal);
            startActivity(new Intent(this, register3.class));
        });
    }

    private void updateGoal(String goal, RadioButton rbLose, RadioButton rbBuild, RadioButton rbKeep, RadioButton rbEndurance) {
        selectedGoal = goal;
        rbLose.setChecked("Lose Weight".equals(goal));
        rbBuild.setChecked("Build Muscle".equals(goal));
        rbKeep.setChecked("Keep Fit".equals(goal));
        rbEndurance.setChecked("Increase Endurance".equals(goal));
    }
}
