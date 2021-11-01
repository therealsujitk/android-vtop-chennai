package tk.therealsuji.vtopchennai.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import tk.therealsuji.vtopchennai.R;

public class NotificationHelper extends ContextWrapper {
    public static final String CHANNEL_ID_APPLICATION = "application";
    public static final String CHANNEL_NAME_APPLICATION = "Application";

    public static final String CHANNEL_ID_UPCOMING = "upcoming";
    public static final String CHANNEL_NAME_UPCOMING = "Upcoming Classes";

    public static final String CHANNEL_ID_ONGOING = "ongoing";
    public static final String CHANNEL_NAME_ONGOING = "Ongoing Classes";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel application = new NotificationChannel(CHANNEL_ID_APPLICATION, CHANNEL_NAME_APPLICATION, NotificationManager.IMPORTANCE_LOW);
            application.enableVibration(false);
            application.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getManager().createNotificationChannel(application);

            NotificationChannel upcoming = new NotificationChannel(CHANNEL_ID_UPCOMING, CHANNEL_NAME_UPCOMING, NotificationManager.IMPORTANCE_HIGH);
            upcoming.enableVibration(true);
            upcoming.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(upcoming);

            NotificationChannel ongoing = new NotificationChannel(CHANNEL_ID_ONGOING, CHANNEL_NAME_ONGOING, NotificationManager.IMPORTANCE_HIGH);
            ongoing.enableVibration(true);
            ongoing.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(ongoing);
        }
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    public NotificationCompat.Builder notifyApplication(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_APPLICATION)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logo_light)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
    }

    public NotificationCompat.Builder notifyUpcoming(Context context, String title, String message) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent("tk.therealsuji.vtopchennai.LAUNCH_TIMETABLE"), 0);

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_UPCOMING)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_timetable)
                .setColor(getColor(R.color.colorPrimary))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public NotificationCompat.Builder notifyOngoing(Context context, String title, String message) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent("tk.therealsuji.vtopchennai.LAUNCH_TIMETABLE"), 0);

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_ONGOING)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_timetable)
                .setColor(getColor(R.color.colorPrimary))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }
}
