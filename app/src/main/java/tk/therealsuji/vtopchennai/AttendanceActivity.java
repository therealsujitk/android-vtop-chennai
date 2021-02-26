package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class AttendanceActivity extends AppCompatActivity {
    boolean terminateThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final LinearLayout attendance = findViewById(R.id.attendance);
        final Context context = this;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int screenWidth = displayMetrics.widthPixels;

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS attendance (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, attended VARCHAR, total VARCHAR, percent VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM attendance", null);

                int courseIndex = c.getColumnIndex("course");
                int typeIndex = c.getColumnIndex("type");
                int attendedIndex = c.getColumnIndex("attended");
                int totalIndex = c.getColumnIndex("total");
                int percentIndex = c.getColumnIndex("percent");
                c.moveToFirst();

                CardGenerator myAttendance = new CardGenerator(context, CardGenerator.CARD_ATTENDANCE);

                NotificationDotGenerator myNotification = new NotificationDotGenerator(context);

                for (int i = 0; i < c.getCount(); ++i) {
                    if (terminateThread) {
                        return;
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    String course = c.getString(courseIndex);
                    String percentage = c.getString(percentIndex);
                    String type = c.getString(typeIndex);
                    String attended = c.getString(attendedIndex) + " | " + c.getString(totalIndex);

                    final LinearLayout card = myAttendance.generateCard(course, percentage + "%", type, attended);
                    card.setAlpha(0);
                    card.animate().alpha(1);

                    /*
                        Adding the card to the activity
                     */
                    if (Integer.parseInt(percentage) <= 75) {
                        final RelativeLayout container = myNotification.generateNotificationContainer();
                        container.addView(card);

                        int marginStart = (int) (screenWidth - 30 * pixelDensity);

                        final ImageView notification = myNotification.generateNotificationDot(marginStart, NotificationDotGenerator.NOTIFICATION_URGENT);
                        notification.setPadding(0, (int) (5 * pixelDensity), 0, 0);
                        container.addView(notification);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendance.addView(container);
                                notification.animate().scaleX(1).scaleY(1);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendance.addView(card);
                            }
                        });
                    }

                    c.moveToNext();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}