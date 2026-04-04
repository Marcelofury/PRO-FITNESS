package com.example.profitness.network;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingStore {
    private static final String PREFS = "profitness_onboarding";
    private static final String KEY_GOAL = "goal";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_AGE = "age";
    private static final String KEY_HEIGHT = "height_cm";
    private static final String KEY_WEIGHT = "weight_kg";

    private final SharedPreferences prefs;

    public OnboardingStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void setGoal(String goal) {
        prefs.edit().putString(KEY_GOAL, goal).apply();
    }

    public String getGoal() {
        return prefs.getString(KEY_GOAL, "Build Muscle");
    }

    public void setActivity(String activity) {
        prefs.edit().putString(KEY_ACTIVITY, activity).apply();
    }

    public String getActivity() {
        return prefs.getString(KEY_ACTIVITY, "Lightly Active");
    }

    public void setAge(int age) {
        prefs.edit().putInt(KEY_AGE, age).apply();
    }

    public int getAge() {
        return prefs.getInt(KEY_AGE, 28);
    }

    public void setHeightCm(int heightCm) {
        prefs.edit().putInt(KEY_HEIGHT, heightCm).apply();
    }

    public int getHeightCm() {
        return prefs.getInt(KEY_HEIGHT, 182);
    }

    public void setWeightKg(int weightKg) {
        prefs.edit().putInt(KEY_WEIGHT, weightKg).apply();
    }

    public int getWeightKg() {
        return prefs.getInt(KEY_WEIGHT, 75);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
