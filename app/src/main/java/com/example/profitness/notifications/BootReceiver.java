package com.example.profitness.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.profitness.network.ApiConfig;

public class BootReceiver extends BroadcastReceiver {
    private static final String KEY_REMINDER_WORKOUT = "reminder_workout";
    private static final String KEY_REMINDER_HYDRATION = "reminder_hydration";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);

        if (prefs.getBoolean(KEY_REMINDER_WORKOUT, true)) {
            ReminderScheduler.scheduleWorkoutReminder(context);
        }

        if (prefs.getBoolean(KEY_REMINDER_HYDRATION, true)) {
            ReminderScheduler.scheduleHydrationReminder(context);
        }
    }
}
