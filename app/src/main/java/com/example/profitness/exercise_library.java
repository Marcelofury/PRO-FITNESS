package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
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
    private String selectedCategory = "All";
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_library);

        api = new ProFitnessApi(new TokenStore(this));

        RecyclerView recyclerView = findViewById(R.id.exerciseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(item -> {
            Intent intent = new Intent(exercise_library.this, exercise_details.class);
            intent.putExtra("exercise_name", item.name);
            intent.putExtra("exercise_muscle", item.muscleGroup);
            intent.putExtra("exercise_difficulty", item.difficulty);
            intent.putExtra("exercise_duration", item.defaultDurationMinutes);
            startActivity(intent);
        });
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
                currentSearch = s.toString();
                filterExercises();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextView chipAll = findViewById(R.id.chip_all);
        TextView chipChest = findViewById(R.id.chip_chest);
        TextView chipLegs = findViewById(R.id.chip_legs);
        TextView chipCardio = findViewById(R.id.chip_cardio);
        TextView chipBack = findViewById(R.id.chip_back);

        chipAll.setOnClickListener(v -> {
            selectedCategory = "All";
            updateChipStyles(chipAll, chipChest, chipLegs, chipCardio, chipBack);
            filterExercises();
        });
        chipChest.setOnClickListener(v -> {
            selectedCategory = "Chest";
            updateChipStyles(chipAll, chipChest, chipLegs, chipCardio, chipBack);
            filterExercises();
        });
        chipLegs.setOnClickListener(v -> {
            selectedCategory = "Legs";
            updateChipStyles(chipAll, chipChest, chipLegs, chipCardio, chipBack);
            filterExercises();
        });
        chipCardio.setOnClickListener(v -> {
            selectedCategory = "Cardio";
            updateChipStyles(chipAll, chipChest, chipLegs, chipCardio, chipBack);
            filterExercises();
        });
        chipBack.setOnClickListener(v -> {
            selectedCategory = "Back";
            updateChipStyles(chipAll, chipChest, chipLegs, chipCardio, chipBack);
            filterExercises();
        });

        updateChipStyles(chipAll, chipChest, chipLegs, chipCardio, chipBack);

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
                            if (!element.isJsonObject()) {
                                continue;
                            }
                            JsonObject obj = element.getAsJsonObject();
                            String name = obj.has("name") ? obj.get("name").getAsString() : "Exercise";
                            String muscle = obj.has("muscleGroup") ? obj.get("muscleGroup").getAsString() : "General";
                            String difficulty = obj.has("difficulty") ? obj.get("difficulty").getAsString() : "Beginner";
                            int duration = obj.has("defaultDurationMinutes") ? obj.get("defaultDurationMinutes").getAsInt() : 15;

                            allExercises.add(new ExerciseAdapter.ExerciseItem(name, muscle, difficulty, duration));
                        }
                    }

                    filterExercises();
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

    private void filterExercises() {
        String normalized = currentSearch.toLowerCase(Locale.US).trim();
        List<ExerciseAdapter.ExerciseItem> filtered = new ArrayList<>();
        for (ExerciseAdapter.ExerciseItem item : allExercises) {
            boolean matchesCategory = "All".equals(selectedCategory)
                    || item.muscleGroup.equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = normalized.isEmpty()
                    || item.name.toLowerCase(Locale.US).contains(normalized)
                    || item.muscleGroup.toLowerCase(Locale.US).contains(normalized);

            if (matchesCategory && matchesSearch) {
                filtered.add(item);
            }
        }

        adapter.submit(filtered);
    }

    private void updateChipStyles(TextView chipAll, TextView chipChest, TextView chipLegs, TextView chipCardio, TextView chipBack) {
        styleChip(chipAll, "All".equals(selectedCategory));
        styleChip(chipChest, "Chest".equals(selectedCategory));
        styleChip(chipLegs, "Legs".equals(selectedCategory));
        styleChip(chipCardio, "Cardio".equals(selectedCategory));
        styleChip(chipBack, "Back".equals(selectedCategory));
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(getColor(selected ? android.R.color.black : android.R.color.white));
    }
}
