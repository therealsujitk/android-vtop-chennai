package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TimetableActivity extends AppCompatActivity {
    ScrollView timetable;
    HorizontalScrollView daysContainer;
    LinearLayout[] dayViews = new LinearLayout[7];
    TextView[] buttons = new TextView[7];
    Context context;
    float pixelDensity;
    int halfWidth, day;

    boolean terminateThread = false;

    public void setTimetable(View view) {
        if (view != null) {
            int selectedDay = Integer.parseInt(view.getTag().toString());

            if (selectedDay == day) {
                return;
            } else {
                day = selectedDay;
            }

            timetable.scrollTo(0, 0);
            timetable.removeAllViews();

            if (dayViews[day].getChildCount() > 0) {
                findViewById(R.id.noData).setVisibility(View.GONE);
                timetable.setAlpha(0);
                timetable.addView(dayViews[day]);
                timetable.animate().alpha(1);
            } else {
                findViewById(R.id.noData).setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < 7; ++i) {
                buttons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
            }
        }

        buttons[day].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float location = (50 + 70 * day) * pixelDensity;
        daysContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getAction() != null && getIntent().getAction().equals("tk.therealsuji.vtopchennai.LAUNCH_TIMETABLE")) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
            String theme = sharedPreferences.getString("appearance", "system");

            if (theme.equals("light")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (theme.equals("dark")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        timetable = findViewById(R.id.timetable);
        pixelDensity = context.getResources().getDisplayMetrics().density;

        daysContainer = findViewById(R.id.daysContainer);

        buttons[0] = findViewById(R.id.sunday);
        buttons[1] = findViewById(R.id.monday);
        buttons[2] = findViewById(R.id.tuesday);
        buttons[3] = findViewById(R.id.wednesday);
        buttons[4] = findViewById(R.id.thursday);
        buttons[5] = findViewById(R.id.friday);
        buttons[6] = findViewById(R.id.saturday);

        daysContainer.animate().alpha(1);

        LayoutGenerator myLayout = new LayoutGenerator(this);

        for (int i = 0; i < 7; ++i) {
            dayViews[i] = myLayout.generateLayout();
        }

        Calendar c = Calendar.getInstance();
        day = c.get(Calendar.DAY_OF_WEEK) - 1;

        timetable.addView(dayViews[day]);

        daysContainer.post(new Runnable() {
            @Override
            public void run() {
                setTimetable(null);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*
                    Displaying the timetable
                 */

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                Cursor theory = myDatabase.rawQuery("SELECT * FROM timetable_theory", null);
                Cursor lab = myDatabase.rawQuery("SELECT * FROM timetable_lab", null);

                int startTheory = theory.getColumnIndex("start_time");
                int endTheory = theory.getColumnIndex("end_time");
                int sundayTheory = theory.getColumnIndex("sun");
                int mondayTheory = theory.getColumnIndex("mon");
                int tuesdayTheory = theory.getColumnIndex("tue");
                int wednesdayTheory = theory.getColumnIndex("wed");
                int thursdayTheory = theory.getColumnIndex("thu");
                int fridayTheory = theory.getColumnIndex("fri");
                int saturdayTheory = theory.getColumnIndex("sat");

                int startLab = lab.getColumnIndex("start_time");
                int endLab = lab.getColumnIndex("end_time");
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

                SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

                CardGenerator myPeriod = new CardGenerator(context, CardGenerator.CARD_TIMETABLE);

                for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    /*
                        The starting and ending times
                     */
                    String startTimeTheory = theory.getString(startTheory);
                    String endTimeTheory = theory.getString(endTheory);
                    String startTimeLab = lab.getString(startLab);
                    String endTimeLab = lab.getString(endLab);

                    for (int j = 0; j < 7; ++j) {
                        if (terminateThread) {
                            return;
                        }

                        /*
                            The period TextView for theory
                         */
                        if (!theory.getString(theoryIndexes[j]).equals("null")) {
                            String[] rawPeriod = theory.getString(theoryIndexes[j]).split("-");

                            String course = rawPeriod[1].trim();
                            String venue = rawPeriod[rawPeriod.length - 3] + " - " + rawPeriod[rawPeriod.length - 2];
                            /*
                                Making a proper string of the timings depending on the system settings
                             */
                            String timings = startTimeTheory + " - " + endTimeTheory;
                            if (!DateFormat.is24HourFormat(context)) {
                                try {
                                    Date startTime = hour24.parse(startTimeTheory);
                                    Date endTime = hour24.parse(endTimeTheory);
                                    if (startTime != null && endTime != null) {
                                        timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            String type = getString(R.string.theory);

                            final LinearLayout card = myPeriod.generateCard(course, venue, timings, type);

                            /*
                                Adding card to the day view
                             */
                            if (j == day) {
                                card.setAlpha(0);
                                card.animate().alpha(1);

                                if (dayViews[day].getChildCount() == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.noData).setVisibility(View.GONE);
                                        }
                                    });
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dayViews[day].addView(card);
                                    }
                                });
                            } else {
                                dayViews[j].addView(card);
                            }
                        }

                        /*
                            The period TextView for lab
                         */
                        if (!lab.getString(labIndexes[j]).equals("null")) {
                            String[] rawPeriod = lab.getString(theoryIndexes[j]).split("-");

                            String course = rawPeriod[1].trim();
                            String venue = rawPeriod[rawPeriod.length - 3] + " - " + rawPeriod[rawPeriod.length - 2];
                            /*
                                Making a proper string of the timings depending on the system settings
                             */
                            String timings = startTimeLab + " - " + endTimeLab;
                            if (!DateFormat.is24HourFormat(context)) {
                                try {
                                    Date startTime = hour24.parse(startTimeLab);
                                    Date endTime = hour24.parse(endTimeLab);
                                    if (startTime != null && endTime != null) {
                                        timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            String type = getString(R.string.lab);

                            final LinearLayout card = myPeriod.generateCard(course, venue, timings, type);

                            /*
                                Adding card to the day view
                             */
                            if (j == day) {
                                card.setAlpha(0);
                                card.animate().alpha(1);

                                if (dayViews[day].getChildCount() == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.noData).setVisibility(View.GONE);
                                        }
                                    });
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dayViews[day].addView(card);
                                    }
                                });
                            } else {
                                dayViews[j].addView(card);
                            }
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                theory.close();
                lab.close();
                myDatabase.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newTimetable").apply();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}