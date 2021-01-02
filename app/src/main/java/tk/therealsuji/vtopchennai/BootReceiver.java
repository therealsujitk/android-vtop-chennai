package tk.therealsuji.vtopchennai;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        boolean isSignedIn = sharedPreferences.getBoolean("isSignedIn", false);

        if (!isSignedIn) {
            return;
        }

        SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

        Cursor theory = myDatabase.rawQuery("SELECT * FROM timetable_theory", null);
        Cursor lab = myDatabase.rawQuery("SELECT * FROM timetable_lab", null);

        int startTheory = theory.getColumnIndex("start_time");
        int sundayTheory = theory.getColumnIndex("sun");
        int mondayTheory = theory.getColumnIndex("mon");
        int tuesdayTheory = theory.getColumnIndex("tue");
        int wednesdayTheory = theory.getColumnIndex("wed");
        int thursdayTheory = theory.getColumnIndex("thu");
        int fridayTheory = theory.getColumnIndex("fri");
        int saturdayTheory = theory.getColumnIndex("sat");

        int startLab = lab.getColumnIndex("start_time");
        int sundayLab = lab.getColumnIndex("sun");
        int mondayLab = lab.getColumnIndex("mon");
        int tuesdayLab = lab.getColumnIndex("tue");
        int wednesdayLab = lab.getColumnIndex("wed");
        int thursdayLab = lab.getColumnIndex("thu");
        int fridayLab = lab.getColumnIndex("fri");
        int saturdayLab = lab.getColumnIndex("sat");

        theory.moveToFirst();
        lab.moveToFirst();

        int[] theoryIndexes = {sundayTheory, mondayTheory, tuesdayTheory, wednesdayTheory, thursdayTheory, fridayTheory, saturdayTheory};
        int[] labIndexes = {sundayLab, mondayLab, tuesdayLab, wednesdayLab, thursdayLab, fridayLab, saturdayLab};

        Calendar c = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Date today = null;
        try {
            today = dateFormat.parse(dateFormat.format(c.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Date now = null;
        try {
            now = timeFormat.parse(timeFormat.format(c.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int day = c.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
            String start_time_lab = lab.getString(startLab);
            String start_time_theory = theory.getString(startTheory);

            for (int j = 0; j < 7; ++j) {
                if (!lab.getString(labIndexes[j]).equals("null")) {
                    if (j == day) {
                        Date current = null;
                        try {
                            current = timeFormat.parse(start_time_lab);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        assert current != null;
                        if (current.after(now) || current.equals(now)) {
                            assert today != null;
                            c.setTime(today);
                        } else {
                            assert today != null;
                            c.setTime(today);
                            c.add(Calendar.DATE, 7);
                        }
                    } else if (j > day) {
                        assert today != null;
                        c.setTime(today);
                        c.add(Calendar.DATE, j - day);
                    } else {
                        assert today != null;
                        c.setTime(today);
                        c.add(Calendar.DATE, 7 - day + j);
                    }

                    Date date = null;
                    try {
                        date = df.parse(dateFormat.format(c.getTime()) + " " + start_time_lab);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert date != null;
                    c.setTime(date);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

                    c.add(Calendar.MINUTE, -30);
                    pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                }

                if (!theory.getString(theoryIndexes[j]).equals("null")) {
                    if (j == day) {
                        Date current = null;
                        try {
                            current = timeFormat.parse(start_time_theory);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        assert current != null;
                        if (current.after(now) || current.equals(now)) {
                            assert today != null;
                            c.setTime(today);
                        } else {
                            assert today != null;
                            c.setTime(today);
                            c.add(Calendar.DATE, 7);
                        }
                    } else if (j > day) {
                        assert today != null;
                        c.setTime(today);
                        c.add(Calendar.DATE, j - day);
                    } else {
                        assert today != null;
                        c.setTime(today);
                        c.add(Calendar.DATE, 7 - day + j);
                    }

                    Date date = null;
                    try {
                        date = df.parse(dateFormat.format(c.getTime()) + " " + start_time_theory);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert date != null;
                    c.setTime(date);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

                    c.add(Calendar.MINUTE, -30);
                    pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                }
            }
        }

        theory.close();
        lab.close();
        myDatabase.close();
    }
}
