package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.profitness.network.OnboardingStore;

import androidx.appcompat.app.AppCompatActivity;

public class register3 extends AppCompatActivity {

    private String selectedActivity = "Lightly Active";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register3);

        OnboardingStore store = new OnboardingStore(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.cardActivitySedentary).setOnClickListener(v -> selectedActivity = "Sedentary");
        findViewById(R.id.cardActivityLight).setOnClickListener(v -> selectedActivity = "Lightly Active");
        findViewById(R.id.cardActivityModerate).setOnClickListener(v -> selectedActivity = "Moderately Active");
        findViewById(R.id.cardActivityAthlete).setOnClickListener(v -> selectedActivity = "Athlete");

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            store.setActivity(selectedActivity);
            startActivity(new Intent(this, register4.class));
        });
    }
}
