package tk.therealsuji.vtopchennai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    public static final String CHANNEL_ID_TIMETABLE = "timetable";
    public static final String CHANNEL_NAME_TIMETABLE = "Timetable";

    public static final String CHANNEL_ID_APPLICATION = "application";
    public static final String CHANNEL_NAME_APPLICATION = "Application";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel timetable = new NotificationChannel(CHANNEL_ID_TIMETABLE, CHANNEL_NAME_TIMETABLE, NotificationManager.IMPORTANCE_HIGH);
            timetable.enableLights(true);
            timetable.enableVibration(true);
            timetable.setLightColor(R.color.colorPrimary);
            timetable.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(timetable);

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

    public NotificationCompat.Builder getTimetableNotification(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_TIMETABLE)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_timetable);
    }

    public NotificationCompat.Builder getApplicationNotification(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_TIMETABLE)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logo_square);
    }
}
