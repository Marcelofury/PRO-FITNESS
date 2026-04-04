package com.example.profitness.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class ReminderScheduler {
    public static final String TYPE_WORKOUT = "workout";
    public static final String TYPE_HYDRATION = "hydration";

    private static final int REQ_WORKOUT = 1001;
    private static final int REQ_HYDRATION = 1002;

    private ReminderScheduler() {}

    public static void scheduleWorkoutReminder(Context context) {
        scheduleDaily(context, TYPE_WORKOUT, REQ_WORKOUT, 9, 0);
    }

    public static void scheduleHydrationReminder(Context context) {
        scheduleDaily(context, TYPE_HYDRATION, REQ_HYDRATION, 12, 0);
    }

    public static void cancelWorkoutReminder(Context context) {
        cancel(context, TYPE_WORKOUT, REQ_WORKOUT);
    }

    public static void cancelHydrationReminder(Context context) {
        cancel(context, TYPE_HYDRATION, REQ_HYDRATION);
    }

    private static void scheduleDaily(Context context, String type, int requestCode, int hourOfDay, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildReminderPendingIntent(context, type, requestCode);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private static void cancel(Context context, String type, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildReminderPendingIntent(context, type, requestCode);
        alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent buildReminderPendingIntent(Context context, String type, int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_SHOW_REMINDER);
        intent.putExtra(ReminderReceiver.EXTRA_REMINDER_TYPE, type);

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
