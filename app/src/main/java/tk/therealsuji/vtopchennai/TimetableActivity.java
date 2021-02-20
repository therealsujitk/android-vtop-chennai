package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
    boolean isViewEmpty;

    public void setTimetable(View view) {
        if (view != null) {
            isViewEmpty = false;

            int selectedDay = Integer.parseInt(view.getTag().toString());

            if (selectedDay == day) {
                return;
            } else {
                day = selectedDay;
            }

            timetable.scrollTo(0, 0);
            timetable.removeAllViews();
            timetable.setAlpha(0);
            timetable.animate().alpha(1);

            if (dayViews[day].getChildCount() > 0) {
                findViewById(R.id.noData).setVisibility(View.GONE);
                timetable.addView(dayViews[day]);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        timetable = findViewById(R.id.timetable);
        pixelDensity = context.getResources().getDisplayMetrics().density;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        daysContainer = findViewById(R.id.daysContainer);

        for (int i = 0; i < 7; ++i) {
            dayViews[i] = new LinearLayout(context);
            dayViews[i].setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            dayViews[i].setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
            dayViews[i].setOrientation(LinearLayout.VERTICAL);
        }

        buttons[0] = findViewById(R.id.sunday);
        buttons[1] = findViewById(R.id.monday);
        buttons[2] = findViewById(R.id.tuesday);
        buttons[3] = findViewById(R.id.wednesday);
        buttons[4] = findViewById(R.id.thursday);
        buttons[5] = findViewById(R.id.friday);
        buttons[6] = findViewById(R.id.saturday);

        Calendar c = Calendar.getInstance();
        day = c.get(Calendar.DAY_OF_WEEK) - 1;

        daysContainer.animate().alpha(1);

        daysContainer.post(new Runnable() {
            @Override
            public void run() {
                setTimetable(null);
            }
        });

        isViewEmpty = true;

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

                boolean isNoDataVisible = true;

                for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
                    /*
                        The starting and ending times
                     */
                    String startTimeTheory = theory.getString(startTheory);
                    String endTimeTheory = theory.getString(endTheory);
                    String startTimeLab = lab.getString(startLab);
                    String endTimeLab = lab.getString(endLab);

                    for (int j = 0; j < 7; ++j) {
                        boolean theoryFlag = false, labFlag = false;

                        /*
                            The period TextView for theory
                         */
                        TextView period = new TextView(context);
                        if (!theory.getString(theoryIndexes[j]).equals("null")) {
                            String course = theory.getString(theoryIndexes[j]).split("-")[1].trim();

                            TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            courseParams.setMarginStart((int) (20 * pixelDensity));
                            courseParams.setMarginEnd((int) (20 * pixelDensity));
                            courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                            period.setLayoutParams(courseParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(20);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            theoryFlag = true;
                        }

                        /*
                            The outer block for theory (Initialized later to make the code faster)
                         */
                        if (theoryFlag) {
                            if (isNoDataVisible && j == day && dayViews[j].getChildCount() == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                });

                                isNoDataVisible = false;
                            }

                            final LinearLayout block = new LinearLayout(context);
                            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            blockParams.setMarginStart((int) (20 * pixelDensity));
                            blockParams.setMarginEnd((int) (20 * pixelDensity));
                            blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                            block.setLayoutParams(blockParams);
                            block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                            block.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                Making a proper string of the timings
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

                            TextView time = new TextView(context);
                            TableRow.LayoutParams timeParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timeParams.setMarginStart((int) (20 * pixelDensity));
                            timeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            time.setLayoutParams(timeParams);
                            time.setText(timings);
                            time.setTextColor(getColor(R.color.colorPrimary));
                            time.setTextSize(16);
                            time.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(time);   //Adding the timings to innerBlock

                            TextView theoryText = new TextView(context);
                            TableRow.LayoutParams theoryParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            theoryParams.setMarginEnd((int) (20 * pixelDensity));
                            theoryParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            theoryText.setLayoutParams(theoryParams);
                            theoryText.setText(getString(R.string.theory));
                            theoryText.setTextColor(getColor(R.color.colorPrimary));
                            theoryText.setTextSize(16);
                            theoryText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            theoryText.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(theoryText); //Adding the theory text to innerBlock

                            /*
                                Adding period and other details to block
                             */
                            block.addView(period);
                            block.addView(innerBlock);

                            if (j == day) {
                                block.setAlpha(0);
                                block.animate().alpha(1);
                            }

                            if (isViewEmpty) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        timetable.addView(dayViews[day]);
                                    }
                                });

                                isViewEmpty = false;
                            }

                            /*
                                Finally adding block to the main sections
                             */
                            final LinearLayout dayBlock = dayViews[j];
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dayBlock.addView(block);
                                }
                            });
                        }

                        /*
                            The period TextView for lab
                         */
                        period = new TextView(context);
                        if (!lab.getString(labIndexes[j]).equals("null")) {
                            String course = lab.getString(labIndexes[j]).split("-")[1].trim();

                            TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            courseParams.setMarginStart((int) (20 * pixelDensity));
                            courseParams.setMarginEnd((int) (20 * pixelDensity));
                            courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                            period.setLayoutParams(courseParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(20);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            labFlag = true;
                        }

                        /*
                            The outer block for lab (Initialized later to make the code faster)
                         */
                        if (labFlag) {
                            if (isNoDataVisible && j == day && dayViews[j].getChildCount() == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                });

                                isNoDataVisible = false;
                            }

                            final LinearLayout block = new LinearLayout(context);
                            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            blockParams.setMarginStart((int) (20 * pixelDensity));
                            blockParams.setMarginEnd((int) (20 * pixelDensity));
                            blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                            block.setLayoutParams(blockParams);
                            block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                            block.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                Making a proper string of the timings
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

                            /*
                                The timings TextView
                             */
                            TextView time = new TextView(context);
                            TableRow.LayoutParams timeParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timeParams.setMarginStart((int) (20 * pixelDensity));
                            timeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            time.setLayoutParams(timeParams);
                            time.setText(timings);
                            time.setTextColor(getColor(R.color.colorPrimary));
                            time.setTextSize(16);
                            time.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(time);   //Adding the timings to innerBlock

                            /*
                                The lab text TextView
                             */
                            TextView labText = new TextView(context);
                            TableRow.LayoutParams labParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            labParams.setMarginEnd((int) (20 * pixelDensity));
                            labParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            labText.setLayoutParams(labParams);
                            labText.setText(getString(R.string.lab));
                            labText.setTextColor(getColor(R.color.colorPrimary));
                            labText.setTextSize(16);
                            labText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            labText.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(labText);    //Adding the lab text to innerBlock

                            /*
                                Adding period and other details to block
                             */
                            block.addView(period);
                            block.addView(innerBlock);

                            if (j == day) {
                                block.setAlpha(0);
                                block.animate().alpha(1);
                            }

                            if (isViewEmpty) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        timetable.addView(dayViews[day]);
                                    }
                                });

                                isViewEmpty = false;
                            }

                            /*
                                Finally adding block to the main sections
                             */
                            final LinearLayout dayBlock = dayViews[j];
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dayBlock.addView(block);
                                }
                            });
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
}