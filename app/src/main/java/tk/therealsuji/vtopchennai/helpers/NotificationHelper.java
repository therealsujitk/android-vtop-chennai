package tk.therealsuji.vtopchennai.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.LauncherActivity;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;
import tk.therealsuji.vtopchennai.models.Exam;
import tk.therealsuji.vtopchennai.models.Timetable;

public class NotificationHelper extends ContextWrapper {
    public static final String CHANNEL_ID_APPLICATION = "application";
    public static final String CHANNEL_NAME_APPLICATION = "Application";

    public static final String CHANNEL_ID_UPCOMING = "upcoming";
    public static final String CHANNEL_NAME_UPCOMING = "Upcoming Classes";

    public static final String CHANNEL_ID_ONGOING = "ongoing";
    public static final String CHANNEL_NAME_ONGOING = "Ongoing Classes";

    public static final String CHANNEL_ID_EXAMS = "exams";
    public static final String CHANNEL_NAME_EXAMS = "Exam Schedule";

    private NotificationManager manager;
    private final int notificationColor;

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
            ongoing.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(ongoing);

            NotificationChannel exams = new NotificationChannel(
                    CHANNEL_ID_EXAMS,
                    CHANNEL_NAME_EXAMS,
                    NotificationManager.IMPORTANCE_HIGH
            );
            exams.enableVibration(true);
            exams.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(exams);
        }

        TypedValue typedValue = new TypedValue();
        ContextThemeWrapper contextThemeWrapper;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contextThemeWrapper = new ContextThemeWrapper(this.getApplicationContext(), android.R.style.Theme_DeviceDefault_DayNight);
            contextThemeWrapper.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        } else {
            contextThemeWrapper = new ContextThemeWrapper(this.getApplicationContext(), R.style.Theme_Material3_DayNight);
            contextThemeWrapper.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        }

        this.notificationColor = typedValue.data;
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    public NotificationCompat.Builder notifySync(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_APPLICATION)
                .setColor(this.getNotificationColor(true))
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
                new Intent(this, LauncherActivity.class)
                        .putExtra("launchFragment", HomeFragment.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
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

        assert largeIcon != null;
        largeIcon = DrawableCompat.wrap(largeIcon);
        DrawableCompat.setTint(largeIcon, this.getNotificationColor(false));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.startTime.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timetableItem.startTime.split(":")[1]));

        this.manager.cancel(SettingsRepository.NOTIFICATION_ID_TIMETABLE);
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_UPCOMING)
                .setColor(this.getNotificationColor(true))
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_timetable)
                .setLargeIcon(SettingsRepository.getBitmapFromVectorDrawable(largeIcon))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(calendar.getTimeInMillis());
    }

    public NotificationCompat.Builder notifyOngoing(Timetable.AllData timetableItem) throws ParseException {
        Drawable largeIcon;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, LauncherActivity.class)
                        .putExtra("launchFragment", HomeFragment.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
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

        assert largeIcon != null;
        largeIcon = DrawableCompat.wrap(largeIcon);
        DrawableCompat.setTint(largeIcon, this.getNotificationColor(false));

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.startTime.split(":")[0]));
        calendarStart.set(Calendar.MINUTE, Integer.parseInt(timetableItem.startTime.split(":")[1]));

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.endTime.split(":")[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.parseInt(timetableItem.endTime.split(":")[1]));

        this.manager.cancel(SettingsRepository.NOTIFICATION_ID_TIMETABLE);
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_ONGOING)
                .setColor(this.getNotificationColor(true))
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setLargeIcon(SettingsRepository.getBitmapFromVectorDrawable(largeIcon))
                .setSmallIcon(R.drawable.ic_timetable)
                .setTimeoutAfter(calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(calendarStart.getTimeInMillis());
    }

    public NotificationCompat.Builder notifyExam(Exam.AllData examItem) {
        Drawable largeIcon = ContextCompat.getDrawable(this, R.drawable.ic_theory);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, LauncherActivity.class)
                        .putExtra("launchFragment", ProfileFragment.class)
                        .putExtra("launchSubFragment", "ExamSchedule"),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        String title = "All the best for your exam!";
        String message = examItem.courseCode + " - " + examItem.courseTitle;

        assert largeIcon != null;
        largeIcon = DrawableCompat.wrap(largeIcon);
        DrawableCompat.setTint(largeIcon, this.getNotificationColor(false));

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(new Date(examItem.startTime));

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(new Date(examItem.endTime));

        this.manager.cancel(SettingsRepository.NOTIFICATION_ID_EXAMS);
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_EXAMS)
                .setColor(this.getNotificationColor(true))
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setLargeIcon(SettingsRepository.getBitmapFromVectorDrawable(largeIcon))
                .setSmallIcon(R.drawable.ic_exams)
                .setTimeoutAfter(calendarEnd.getTimeInMillis() - Calendar.getInstance().getTimeInMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(calendarStart.getTimeInMillis());
    }

    private int getNotificationColor(boolean checkBuild) {
        if (checkBuild && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return NotificationCompat.COLOR_DEFAULT;
        }

        return this.notificationColor;
    }
}
