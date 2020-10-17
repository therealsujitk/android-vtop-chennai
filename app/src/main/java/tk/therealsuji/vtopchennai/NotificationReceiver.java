package tk.therealsuji.vtopchennai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        Calendar calFuture = Calendar.getInstance();
        calFuture.add(Calendar.MINUTE, 30);
        int dayCode = cal.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        String day;

        if (dayCode == 1) {
            day = "sun";
        } else if (dayCode == 2) {
            day = "mon";
        } else if (dayCode == 3) {
            day = "tue";
        } else if (dayCode == 4) {
            day = "wed";
        } else if (dayCode == 5) {
            day = "thu";
        } else if (dayCode == 6) {
            day = "fri";
        } else {
            day = "sat";
        }

        SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");

        Cursor theory = myDatabase.rawQuery("SELECT start_time, end_time, " + day + " FROM timetable_theory", null);
        Cursor lab = myDatabase.rawQuery("SELECT start_time, end_time, " + day + " FROM timetable_lab", null);

        int startTheory = theory.getColumnIndex("start_time");
        int endTheory = theory.getColumnIndex("end_time");
        int dayTheory = theory.getColumnIndex(day);

        int startLab = lab.getColumnIndex("start_time");
        int endLab = lab.getColumnIndex("end_time");
        int dayLab = lab.getColumnIndex(day);

        theory.moveToFirst();
        lab.moveToFirst();

        for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
            boolean flag = false;

            String startTimeTheory = theory.getString(startTheory);
            String endTimeTheory = theory.getString(endTheory);
            String startTimeLab = lab.getString(startLab);
            String endTimeLab = lab.getString(endLab);

            try {
                Date currentTime = df.parse(df.format(cal.getTime()));
                Date futureTime = df.parse(df.format(calFuture.getTime()));

                assert currentTime != null;
                assert futureTime != null;

                if ((futureTime.after(df.parse(startTimeTheory)) || futureTime.equals(df.parse(startTimeTheory))) && currentTime.before(df.parse(startTimeTheory)) && !theory.getString(dayTheory).equals("null")) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    NotificationCompat.Builder n = notificationHelper.notifyUpcoming("Upcoming: " + startTimeTheory + " - " + endTimeTheory, theory.getString(dayTheory).split("-")[1].trim() + " - Theory");
                    notificationHelper.getManager().notify(1, n.build());

                    flag = true;
                }

                if ((futureTime.after(df.parse(startTimeLab)) || futureTime.equals(df.parse(startTimeLab))) && currentTime.before(df.parse(startTimeLab)) && !lab.getString(dayLab).equals("null")) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    NotificationCompat.Builder n = notificationHelper.notifyUpcoming("Upcoming: " + startTimeLab + " - " + endTimeLab, lab.getString(dayLab).split("-")[1].trim() + " - Lab");
                    notificationHelper.getManager().notify(1, n.build());

                    flag = true;
                }

                if (flag) {
                    break;
                }

                if ((currentTime.after(df.parse(startTimeTheory)) || currentTime.equals(df.parse(startTimeTheory))) && (currentTime.before(df.parse(endTimeTheory)) || currentTime.equals(df.parse(endTimeTheory))) && !theory.getString(dayTheory).equals("null")) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    NotificationCompat.Builder n = notificationHelper.notifyOngoing("Ongoing: " + startTimeTheory + " - " + endTimeTheory, theory.getString(dayTheory).split("-")[1].trim() + " - Theory");
                    notificationHelper.getManager().notify(1, n.build());
                }

                if ((currentTime.after(df.parse(startTimeLab)) || currentTime.equals(df.parse(startTimeLab))) && (currentTime.before(df.parse(endTimeLab)) || currentTime.equals(df.parse(endTimeLab))) && !lab.getString(dayLab).equals("null")) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    NotificationCompat.Builder n = notificationHelper.notifyOngoing("Ongoing: " + startTimeLab + " - " + endTimeLab, lab.getString(dayLab).split("-")[1].trim() + " - Lab");
                    notificationHelper.getManager().notify(1, n.build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        theory.close();
        lab.close();
        myDatabase.close();
    }
}
