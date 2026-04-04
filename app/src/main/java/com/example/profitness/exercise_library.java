package com.example.profitness;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.profitness.navigation.BottomNavHelper;
import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.AuthSessionHelper;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class exercise_library extends AppCompatActivity {

    private final List<ExerciseAdapter.ExerciseItem> allExercises = new ArrayList<>();
    private ExerciseAdapter adapter;
    private ProFitnessApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_library);

        api = new ProFitnessApi(new TokenStore(this));

        RecyclerView recyclerView = findViewById(R.id.exerciseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter();
        recyclerView.setAdapter(adapter);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BottomNavHelper.setup(this, nav, R.id.nav_workout);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExercises(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadExercises();
    }

    private void loadExercises() {
        api.getExerciseLibrary(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    JsonArray data = result.getAsJsonArray("data");
                    allExercises.clear();

                    if (data != null) {
                        for (JsonElement element : data) {
                            JsonObject obj = element.getAsJsonObject();
                            String name = obj.has("name") ? obj.get("name").getAsString() : "Exercise";
                            String muscle = obj.has("muscleGroup") ? obj.get("muscleGroup").getAsString() : "General";
                            String difficulty = obj.has("difficulty") ? obj.get("difficulty").getAsString() : "Beginner";
                            int duration = obj.has("defaultDurationMinutes") ? obj.get("defaultDurationMinutes").getAsInt() : 15;

                            allExercises.add(new ExerciseAdapter.ExerciseItem(name, muscle, difficulty, duration));
                        }
                    }

                    adapter.submit(allExercises);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (AuthSessionHelper.handleIfAuthExpired(exercise_library.this, errorMessage)) {
                        return;
                    }
                    Toast.makeText(exercise_library.this, "Failed to load exercises", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterExercises(String query) {
        String normalized = query.toLowerCase(Locale.US).trim();
        if (normalized.isEmpty()) {
            adapter.submit(allExercises);
            return;
        }

        List<ExerciseAdapter.ExerciseItem> filtered = new ArrayList<>();
        for (ExerciseAdapter.ExerciseItem item : allExercises) {
            if (item.name.toLowerCase(Locale.US).contains(normalized) || item.muscleGroup.toLowerCase(Locale.US).contains(normalized)) {
                filtered.add(item);
            }
        }

        adapter.submit(filtered);
    }
}
