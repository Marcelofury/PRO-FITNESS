package com.example.profitness.navigation;

import android.app.Activity;
import android.content.Intent;

import com.example.profitness.Home_dashboard;
import com.example.profitness.R;
import com.example.profitness.Your_progress;
import com.example.profitness.active_workout;
import com.example.profitness.nutrition_log;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class BottomNavHelper {
    private BottomNavHelper() {}

    public static void setup(Activity activity, BottomNavigationView nav, int selectedItemId) {
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == selectedItemId) {
                return true;
            }

            Intent intent;
            if (id == R.id.nav_home) {
                intent = new Intent(activity, Home_dashboard.class);
            } else if (id == R.id.nav_workout) {
                intent = new Intent(activity, active_workout.class);
            } else if (id == R.id.nav_nutrition) {
                intent = new Intent(activity, nutrition_log.class);
            } else if (id == R.id.nav_progress) {
                intent = new Intent(activity, Your_progress.class);
            } else {
                return false;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            return true;
        });
    }
}
