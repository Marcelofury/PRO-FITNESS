package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        View cardSedentary = findViewById(R.id.cardActivitySedentary);
        View cardLight = findViewById(R.id.cardActivityLight);
        View cardModerate = findViewById(R.id.cardActivityModerate);
        View cardAthlete = findViewById(R.id.cardActivityAthlete);

        updateSelectionUI(cardSedentary, cardLight, cardModerate, cardAthlete, selectedActivity);

        cardSedentary.setOnClickListener(v -> {
            selectedActivity = "Sedentary";
            updateSelectionUI(cardSedentary, cardLight, cardModerate, cardAthlete, selectedActivity);
        });
        cardLight.setOnClickListener(v -> {
            selectedActivity = "Lightly Active";
            updateSelectionUI(cardSedentary, cardLight, cardModerate, cardAthlete, selectedActivity);
        });
        cardModerate.setOnClickListener(v -> {
            selectedActivity = "Moderately Active";
            updateSelectionUI(cardSedentary, cardLight, cardModerate, cardAthlete, selectedActivity);
        });
        cardAthlete.setOnClickListener(v -> {
            selectedActivity = "Athlete";
            updateSelectionUI(cardSedentary, cardLight, cardModerate, cardAthlete, selectedActivity);
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            store.setActivity(selectedActivity);
            startActivity(new Intent(this, register4.class));
        });
    }

    private void updateSelectionUI(View cardSedentary, View cardLight, View cardModerate, View cardAthlete, String selected) {
        cardSedentary.setAlpha("Sedentary".equals(selected) ? 1f : 0.75f);
        cardLight.setAlpha("Lightly Active".equals(selected) ? 1f : 0.75f);
        cardModerate.setAlpha("Moderately Active".equals(selected) ? 1f : 0.75f);
        cardAthlete.setAlpha("Athlete".equals(selected) ? 1f : 0.75f);
    }
}
