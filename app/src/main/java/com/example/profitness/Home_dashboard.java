package com.example.profitness;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.gson.JsonObject;

import androidx.appcompat.app.AppCompatActivity;

public class Home_dashboard extends AppCompatActivity {

    private ProFitnessApi api;
    private TextView lblFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        api = new ProFitnessApi(new TokenStore(this));
        lblFocus = findViewById(R.id.lbl_focus);
        loadDashboardSummary();
    }

    private void loadDashboardSummary() {
        api.getDashboardSummary(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonObject data = result.getAsJsonObject("data");
                    int workoutsCount = data != null && data.has("workoutsCount") ? data.get("workoutsCount").getAsInt() : 0;
                    lblFocus.setText("Today's Focus (" + workoutsCount + " workouts)");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(Home_dashboard.this, "Dashboard sync failed", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
