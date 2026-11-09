package in.sahityraj.ashwaniai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ashwani_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");

        if (title == null || title.trim().isEmpty()) {
            title = "आपका Reminder";
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ashwani AI Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Planner reminders");
            manager.createNotificationChannel(channel);
        }

        Intent openApp = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                500,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Ashwani AI Reminder")
                .setContentText(title)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(title))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            manager.notify(
                    (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                    builder.build()
            );
        } catch (SecurityException e) {
            // Notification permission may be denied.
        }
    }
}
