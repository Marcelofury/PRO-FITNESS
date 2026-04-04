package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.OnboardingStore;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import androidx.appcompat.app.AppCompatActivity;

public class register5 extends AppCompatActivity {

    private ProFitnessApi api;
    private OnboardingStore store;
    private MaterialButton btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register5);

        api = new ProFitnessApi(new TokenStore(this));
        store = new OnboardingStore(this);
        btnFinish = findViewById(R.id.btnFinish);

        TextView tvGoal = findViewById(R.id.tvSummaryGoal);
        TextView tvHeight = findViewById(R.id.tvSummaryHeight);
        TextView tvWeight = findViewById(R.id.tvSummaryWeight);
        TextView tvActivity = findViewById(R.id.tvSummaryActivity);

        String goal = store.getGoal();
        String activity = store.getActivity();
        int height = store.getHeightCm();
        int weight = store.getWeightKg();

        tvGoal.setText(goal);
        tvHeight.setText(height + " cm");
        tvWeight.setText(weight + " kg");
        tvActivity.setText(activity);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        btnFinish.setOnClickListener(v -> submitProfile(goal, activity));
    }

    private void submitProfile(String goal, String activity) {
        btnFinish.setEnabled(false);
        btnFinish.setText("SETTING UP...");

        api.updateProfile(
                null,
                store.getAge(),
                store.getHeightCm(),
                store.getWeightKg(),
                goal + " | " + activity,
                new ApiCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        runOnUiThread(() -> {
                            store.clear();
                            Toast.makeText(register5.this, "Profile setup complete", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(register5.this, Home_dashboard.class));
                            finishAffinity();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            btnFinish.setEnabled(true);
                            btnFinish.setText("FINISH & SETUP MY DASHBOARD");
                            Toast.makeText(register5.this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }
}
