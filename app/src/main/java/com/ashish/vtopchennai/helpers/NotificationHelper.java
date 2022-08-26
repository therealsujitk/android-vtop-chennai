package com.ashish.vtopchennai.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.color.MaterialColors;

import java.text.ParseException;
import java.util.Calendar;

import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.activities.MainActivity;
import com.ashish.vtopchennai.models.Timetable;

public class NotificationHelper extends ContextWrapper {
    public static final String CHANNEL_ID_APPLICATION = "application";
    public static final String CHANNEL_NAME_APPLICATION = "Application";

    public static final String CHANNEL_ID_UPCOMING = "upcoming";
    public static final String CHANNEL_NAME_UPCOMING = "Upcoming Classes";

    public static final String CHANNEL_ID_ONGOING = "ongoing";
    public static final String CHANNEL_NAME_ONGOING = "Ongoing Classes";

    private NotificationManager manager;

    public NotificationHelper(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel application = new NotificationChannel(
                    CHANNEL_ID_APPLICATION,
                    CHANNEL_NAME_APPLICATION,
                    NotificationManager.IMPORTANCE_LOW
            );
            application.enableVibration(false);
            application.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getManager().createNotificationChannel(application);

            NotificationChannel upcoming = new NotificationChannel(
                    CHANNEL_ID_UPCOMING,
                    CHANNEL_NAME_UPCOMING,
                    NotificationManager.IMPORTANCE_HIGH
            );
            upcoming.enableVibration(true);
            upcoming.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(upcoming);

            NotificationChannel ongoing = new NotificationChannel(
                    CHANNEL_ID_ONGOING,
                    CHANNEL_NAME_ONGOING,
                    NotificationManager.IMPORTANCE_HIGH
            );
            ongoing.enableVibration(true);
            ongoing.setBypassDnd(true);
            ongoing.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(ongoing);
        }

        this.setTheme(R.style.Base_Theme_VTOP);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    public NotificationCompat.Builder notifySync(String title, String message) {
        int colorPrimary = MaterialColors.getColor(this, R.attr.colorPrimary, 0);

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_APPLICATION)
                .setColor(colorPrimary)
                .setContentText(message)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_sync)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
    }

    public NotificationCompat.Builder notifyUpcoming(Timetable.AllData timetableItem) throws ParseException {
        Drawable largeIcon;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );
        String title = "Upcoming: " +
                SettingsRepository.getSystemFormattedTime(this, timetableItem.startTime) + " - " +
                SettingsRepository.getSystemFormattedTime(this, timetableItem.endTime);
        String message = timetableItem.courseCode + " - " + timetableItem.courseTitle;

        if (timetableItem.courseType.equals("lab")) {
            largeIcon = ContextCompat.getDrawable(this, R.drawable.ic_lab);
        } else {
            largeIcon = ContextCompat.getDrawable(this, R.drawable.ic_theory);
        }

        int colorPrimary = MaterialColors.getColor(this, R.attr.colorPrimary, 0);

        assert largeIcon != null;
        largeIcon = DrawableCompat.wrap(largeIcon);
        DrawableCompat.setTint(largeIcon, colorPrimary);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.startTime.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timetableItem.startTime.split(":")[1]));

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_UPCOMING)
                .setColor(colorPrimary)
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_timetable)
                .setLargeIcon(SettingsRepository.getBitmapFromVectorDrawable(largeIcon))
                .setTimeoutAfter(0L)    // Resetting the timeout duration
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(calendar.getTimeInMillis());
    }

    public NotificationCompat.Builder notifyOngoing(Timetable.AllData timetableItem) throws ParseException {
        Drawable largeIcon;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );
        String title = "Ongoing: " +
                SettingsRepository.getSystemFormattedTime(this, timetableItem.startTime) + " - " +
                SettingsRepository.getSystemFormattedTime(this, timetableItem.endTime);
        String message = timetableItem.courseCode + " - " + timetableItem.courseTitle;

        if (timetableItem.courseType.equals("lab")) {
            largeIcon = ContextCompat.getDrawable(this, R.drawable.ic_lab);
        } else {
            largeIcon = ContextCompat.getDrawable(this, R.drawable.ic_theory);
        }

        int colorPrimary = MaterialColors.getColor(this, R.attr.colorPrimary, 0);

        assert largeIcon != null;
        largeIcon = DrawableCompat.wrap(largeIcon);
        DrawableCompat.setTint(largeIcon, colorPrimary);

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.startTime.split(":")[0]));
        calendarStart.set(Calendar.MINUTE, Integer.parseInt(timetableItem.startTime.split(":")[1]));

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.endTime.split(":")[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.parseInt(timetableItem.endTime.split(":")[1]));

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_ONGOING)
                .setColor(colorPrimary)
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setLargeIcon(SettingsRepository.getBitmapFromVectorDrawable(largeIcon))
                .setSmallIcon(R.drawable.ic_timetable)
                .setTimeoutAfter(calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(calendarStart.getTimeInMillis());
    }
}
