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
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ExamsActivity extends AppCompatActivity {
    ScrollView exams;
    HorizontalScrollView examTitlesContainer;
    ArrayList<TextView> buttons = new ArrayList<>();
    ArrayList<LinearLayout> examViews = new ArrayList<>();
    LinearLayout examButtons;
    Context context;
    float pixelDensity;
    int index, halfWidth;

    boolean terminateThread = false;

    public void setExams(View view) {
        int selectedIndex = Integer.parseInt(view.getTag().toString());
        if (index == selectedIndex) {
            return;
        }

        index = selectedIndex;

        exams.scrollTo(0, 0);
        exams.removeAllViews();

        if (exams.getChildCount() == 0) {
            exams.setAlpha(0);
            exams.addView(examViews.get(index));
            exams.animate().alpha(1);
        }

        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        buttons.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float location = 0;
        for (int i = 0; i < index; ++i) {
            location += 10 * pixelDensity + (float) buttons.get(i).getWidth();
        }
        location += 20 * pixelDensity + (float) buttons.get(index).getWidth() / 2;
        examTitlesContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        exams = findViewById(R.id.exams);
        examButtons = findViewById(R.id.examTitles);
        pixelDensity = context.getResources().getDisplayMetrics().density;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        examTitlesContainer = findViewById(R.id.examTitlesContainer);

        final SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        final SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INTEGER PRIMARY KEY, exam VARCHAR, course VARCHAR, title VARCHAR, slot VARCHAR, date VARCHAR, reporting VARCHAR, start_time VARCHAR, end_time VARCHAR, venue VARCHAR, location VARCHAR, seat VARCHAR)");

                Cursor c = myDatabase.rawQuery("SELECT exam FROM exams GROUP BY exam", null);

                int examIndex = c.getColumnIndex("exam");
                c.moveToFirst();

                LayoutGenerator myLayout = new LayoutGenerator(context);
                ButtonGenerator myButton = new ButtonGenerator(context);
                CardGenerator myExam = new CardGenerator(context, CardGenerator.CARD_EXAM);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
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

                    String exam = c.getString(examIndex);

                    /*
                        Creating a the mark view
                     */
                    final LinearLayout examView = myLayout.generateLayout();

                    examViews.add(examView);    //Storing the view

                    /*
                        Creating the markTitle button
                     */
                    final TextView examButton = myButton.generateButton(exam);
                    if (i == 0 && i == index) {
                        examButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                    }
                    examButton.setTag(i);
                    examButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setExams(examButton);
                        }
                    });

                    buttons.add(examButton);    //Storing the button

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            examButtons.addView(examButton);
                        }
                    });

                    if (i == 0 && i == index && exams.getChildCount() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                exams.addView(examView);
                            }
                        });
                    }

                    Cursor s = myDatabase.rawQuery("SELECT * FROM exams WHERE exam = '" + exam + "' ORDER BY id", null);

                    int courseIndex = s.getColumnIndex("course");
                    int titleIndex = s.getColumnIndex("title");
                    int slotIndex = s.getColumnIndex("slot");
                    int dateIndex = s.getColumnIndex("date");
                    int reportingIndex = s.getColumnIndex("reporting");
                    int startIndex = s.getColumnIndex("start_time");
                    int endIndex = s.getColumnIndex("end_time");
                    int venueIndex = s.getColumnIndex("venue");
                    int locationIndex = s.getColumnIndex("location");
                    int seatIndex = s.getColumnIndex("seat");

                    s.moveToFirst();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String course = s.getString(courseIndex);
                        String title = s.getString(titleIndex);

                        String slot = s.getString(slotIndex);
                        String date = s.getString(dateIndex);
                        String reporting = s.getString(reportingIndex);
                        if (!reporting.equals("") && !DateFormat.is24HourFormat(context)) {
                            try {
                                Date reportingTime = hour24.parse(reporting);
                                if (reportingTime != null) {
                                    reporting = hour12.format(reportingTime);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        String startTime = s.getString(startIndex);
                        String endTime = s.getString(endIndex);
                        String timings = startTime + " - " + endTime;
                        if (!startTime.equals("") && !DateFormat.is24HourFormat(context)) {
                            try {
                                Date reportingTime = hour24.parse(reporting);
                                if (reportingTime != null) {
                                    reporting = hour12.format(reportingTime);
                                }

                                Date startTimeDate = hour24.parse(startTime);
                                Date endTimeDate = hour24.parse(endTime);
                                if (startTimeDate != null && endTimeDate != null) {
                                    reporting = hour12.format(startTimeDate) + " - " + hour12.format(endTimeDate);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        timings = timings.trim();

                        String venue = s.getString(venueIndex);
                        String location = s.getString(locationIndex);
                        String seat = s.getString(seatIndex);

                        final LinearLayout card = myExam.generateCard(course, title, slot, date, reporting, timings, venue, location, seat);

                        /*
                            Adding the card to the view
                         */
                        if (i == index) {
                            card.setAlpha(0);
                            card.animate().alpha(1);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    examView.addView(card);
                                }
                            });
                        } else {
                            examView.addView(card);
                        }
                    }

                    s.close();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newExams").apply();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}