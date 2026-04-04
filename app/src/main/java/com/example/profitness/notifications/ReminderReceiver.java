package com.example.profitness.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.profitness.Home_dashboard;
import com.example.profitness.R;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_SHOW_REMINDER = "com.example.profitness.action.SHOW_REMINDER";
    public static final String EXTRA_REMINDER_TYPE = "reminder_type";

    private static final String CHANNEL_ID = "profitness_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_SHOW_REMINDER.equals(intent.getAction())) {
            return;
        }

        ensureChannel(context);

        String type = intent.getStringExtra(EXTRA_REMINDER_TYPE);
        String title = ReminderScheduler.TYPE_WORKOUT.equals(type) ? "Workout reminder" : "Hydration reminder";
        String body = ReminderScheduler.TYPE_WORKOUT.equals(type)
                ? "Time to plan a training session for today."
                : "Drink some water and stay on track.";

        Intent openIntent = new Intent(context, Home_dashboard.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                ReminderScheduler.TYPE_WORKOUT.equals(type) ? 2001 : 2002,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context)
                .notify(ReminderScheduler.TYPE_WORKOUT.equals(type) ? 3001 : 3002, builder.build());
    }

    private void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "ProFitness Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Daily workout and hydration reminders");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
