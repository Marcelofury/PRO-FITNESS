package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.profitness.network.OnboardingStore;

import androidx.appcompat.app.AppCompatActivity;

public class register4 extends AppCompatActivity {

    private int weightKg = 75;
    private int heightCm = 182;
    private int age = 28;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register4);

        OnboardingStore store = new OnboardingStore(this);

        TextView tvWeightValue = findViewById(R.id.tvWeightValue);
        TextView tvHeightValue = findViewById(R.id.tvHeightValue);
        TextView tvAgeDisplay = findViewById(R.id.tvAgeDisplay);
        TextView tvAgeTop = findViewById(R.id.tvAgeValueTop);
        TextView tvBmiValue = findViewById(R.id.tvBmiValue);
        TextView tvBmiStatus = findViewById(R.id.tvBmiStatus);
        SeekBar weightSeekBar = findViewById(R.id.weightSeekBar);
        SeekBar heightSeekBar = findViewById(R.id.heightSeekBar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        weightKg = store.getWeightKg();
        heightCm = store.getHeightCm();
        age = store.getAge();

        weightSeekBar.setProgress(Math.max(0, weightKg - 30));
        heightSeekBar.setProgress(Math.max(0, heightCm - 120));

        refreshUI(tvWeightValue, tvHeightValue, tvAgeDisplay, tvAgeTop, tvBmiValue, tvBmiStatus);

        weightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                weightKg = 30 + progress;
                refreshUI(tvWeightValue, tvHeightValue, tvAgeDisplay, tvAgeTop, tvBmiValue, tvBmiStatus);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                heightCm = 120 + progress;
                refreshUI(tvWeightValue, tvHeightValue, tvAgeDisplay, tvAgeTop, tvBmiValue, tvBmiStatus);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btnMinus).setOnClickListener(v -> {
            age = Math.max(12, age - 1);
            refreshUI(tvWeightValue, tvHeightValue, tvAgeDisplay, tvAgeTop, tvBmiValue, tvBmiStatus);
        });

        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            age = Math.min(90, age + 1);
            refreshUI(tvWeightValue, tvHeightValue, tvAgeDisplay, tvAgeTop, tvBmiValue, tvBmiStatus);
        });

        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            store.setAge(age);
            store.setHeightCm(heightCm);
            store.setWeightKg(weightKg);
            startActivity(new Intent(this, register5.class));
        });
    }

    private void refreshUI(TextView tvWeight, TextView tvHeight, TextView tvAge, TextView tvAgeTop, TextView tvBmiValue, TextView tvBmiStatus) {
        tvWeight.setText(weightKg + " kg");
        tvHeight.setText(heightCm + " cm");
        tvAge.setText(String.valueOf(age));
        tvAgeTop.setText(age + " yrs");

        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        tvBmiValue.setText(String.format(java.util.Locale.US, "%.1f", bmi));

        if (bmi < 18.5) {
            tvBmiStatus.setText("UNDER");
        } else if (bmi < 25) {
            tvBmiStatus.setText("NORMAL");
        } else if (bmi < 30) {
            tvBmiStatus.setText("OVER");
        } else {
            tvBmiStatus.setText("OBESE");
        }
    }
}
