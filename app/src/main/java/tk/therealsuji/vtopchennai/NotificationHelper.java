package tk.therealsuji.vtopchennai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    public static final String CHANNEL_ID_UPCOMING = "upcoming";
    public static final String CHANNEL_NAME_UPCOMING = "Upcoming Classes";

    public static final String CHANNEL_ID_ONGOING = "ongoing";
    public static final String CHANNEL_NAME_ONGOING = "Ongoing Classes";

    public static final String CHANNEL_ID_APPLICATION = "application";
    public static final String CHANNEL_NAME_APPLICATION = "Application";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel upcoming = new NotificationChannel(CHANNEL_ID_UPCOMING, CHANNEL_NAME_UPCOMING, NotificationManager.IMPORTANCE_HIGH);
            upcoming.enableLights(true);
            upcoming.enableVibration(true);
            upcoming.setLightColor(R.color.colorPrimary);
            upcoming.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(upcoming);

            NotificationChannel ongoing = new NotificationChannel(CHANNEL_ID_ONGOING, CHANNEL_NAME_ONGOING, NotificationManager.IMPORTANCE_HIGH);
            ongoing.enableLights(true);
            ongoing.enableVibration(true);
            ongoing.setLightColor(R.color.colorPrimary);
            ongoing.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(ongoing);

            NotificationChannel application = new NotificationChannel(CHANNEL_ID_APPLICATION, CHANNEL_NAME_APPLICATION, NotificationManager.IMPORTANCE_DEFAULT);
            application.enableLights(true);
            application.enableVibration(true);
            application.setLightColor(R.color.colorPrimary);
            application.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(application);
        }
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    public NotificationCompat.Builder notifyUpcoming(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_UPCOMING)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_timetable)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public NotificationCompat.Builder notifyOngoing(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_ONGOING)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_timetable)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public NotificationCompat.Builder notifyApplication(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_APPLICATION)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logo_square)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }
}
