package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
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

    public void setExams(View view) {
        int selectedIndex = Integer.parseInt(view.getTag().toString());
        if (index == selectedIndex) {
            return;
        } else {
            index = selectedIndex;
        }

        exams.scrollTo(0, 0);
        exams.removeAllViews();
        exams.setAlpha(0);
        exams.animate().alpha(1);

        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        buttons.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));
        exams.addView(examViews.get(index));

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

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                                findViewById(R.id.loading).setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    String exam = c.getString(examIndex);

                    /*
                        Creating a the mark view
                     */
                    final LinearLayout examView = new LinearLayout(context);
                    LinearLayout.LayoutParams examViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    examView.setLayoutParams(examViewParams);
                    examView.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
                    examView.setOrientation(LinearLayout.VERTICAL);

                    examViews.add(examView);    //Storing the view

                    /*
                        Creating the markTitle button
                     */
                    final TextView examButton = new TextView(context);
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) (25 * pixelDensity)
                    );
                    buttonParams.setMarginStart((int) (5 * pixelDensity));
                    buttonParams.setMarginEnd((int) (5 * pixelDensity));
                    buttonParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    examButton.setLayoutParams(buttonParams);
                    examButton.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), 0);
                    if (i == 0) {
                        index = 0;
                        examButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                    } else {
                        examButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
                    }
                    examButton.setTag(i);
                    examButton.setText(exam);
                    examButton.setTextColor(getColor(R.color.colorPrimary));
                    examButton.setTextSize(12);
                    examButton.setGravity(Gravity.CENTER_VERTICAL);
                    examButton.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
                    examButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setExams(examButton);
                        }
                    });
                    examButton.setAlpha(0);
                    examButton.animate().alpha(1);

                    buttons.add(examButton);    //Storing the button

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            examButtons.addView(examButton);
                        }
                    });

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

                    int[] indexes = {titleIndex, slotIndex, dateIndex, reportingIndex, startIndex, venueIndex, locationIndex, seatIndex};
                    String[] titles = {getString(R.string.title), getString(R.string.slot), getString(R.string.date), getString(R.string.reporting), getString(R.string.timings), getString(R.string.venue), getString(R.string.location), getString(R.string.seat)};

                    s.moveToFirst();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        /*
                            The outer block for the exam
                         */
                        final LinearLayout block = new LinearLayout(context);
                        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        blockParams.setMarginStart((int) (20 * pixelDensity));
                        blockParams.setMarginEnd((int) (20 * pixelDensity));
                        blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        block.setPadding(0, 0, 0, (int) (17 * pixelDensity));
                        block.setLayoutParams(blockParams);
                        block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                        block.setOrientation(LinearLayout.VERTICAL);
                        block.setAlpha(0);
                        block.animate().alpha(1);

                        /*
                            The course TextView
                         */
                        TextView course = new TextView(context);
                        TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        courseParams.setMarginStart((int) (20 * pixelDensity));
                        courseParams.setMarginEnd((int) (20 * pixelDensity));
                        courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                        course.setLayoutParams(courseParams);
                        course.setText(s.getString(courseIndex));
                        course.setTextColor(getColor(R.color.colorPrimary));
                        course.setTextSize(20);
                        course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(course);  //Adding the course to block

                        for (int k = 0; k < 8; ++k) {
                            String valueString = s.getString(indexes[k]);
                            if (k == 0) {
                                /*
                                    For the time being i'm removing the course title.
                                    I might add this back later if I think of a better design
                                 */
                                continue;
                            } else if (k == 3 && !DateFormat.is24HourFormat(context)) {
                                try {
                                    Date reportingTime = hour24.parse(valueString);
                                    if (reportingTime != null) {
                                        valueString = hour12.format(reportingTime);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (k == 4 && !valueString.equals("")) {
                                String endTime = s.getString(endIndex);
                                if (!DateFormat.is24HourFormat(context)) {
                                    try {
                                        Date startTimeDate = hour24.parse(valueString);
                                        Date endTimeDate = hour24.parse(endTime);
                                        if (startTimeDate != null) {
                                            valueString = hour12.format(startTimeDate);
                                        }
                                        if (endTimeDate != null) {
                                            endTime = hour12.format(endTimeDate);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                valueString = valueString + " - " + endTime;
                            }

                            if (!valueString.equals("") && !valueString.equals("-")) {
                                /*
                                    The inner block
                                 */
                                LinearLayout innerBlock = new LinearLayout(context);
                                innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                                /*
                                    The title TextView
                                 */
                                TextView title = new TextView(context);
                                TableRow.LayoutParams titleParams = new TableRow.LayoutParams(
                                        TableRow.LayoutParams.WRAP_CONTENT,
                                        TableRow.LayoutParams.WRAP_CONTENT
                                );
                                titleParams.setMarginStart((int) (20 * pixelDensity));
                                titleParams.setMargins(0, (int) (3 * pixelDensity), 0, (int) (3 * pixelDensity));
                                title.setLayoutParams(titleParams);
                                title.setText(titles[k]);
                                title.setTextColor(getColor(R.color.colorPrimary));
                                title.setTextSize(16);
                                title.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                                innerBlock.addView(title);  //Adding the title to the inner block

                                /*
                                    The value TextView
                                 */
                                TextView value = new TextView(context);
                                TableRow.LayoutParams valueParams = new TableRow.LayoutParams(
                                        TableRow.LayoutParams.MATCH_PARENT,
                                        TableRow.LayoutParams.WRAP_CONTENT
                                );
                                valueParams.setMarginEnd((int) (20 * pixelDensity));
                                valueParams.setMargins(0, (int) (3 * pixelDensity), 0, (int) (3 * pixelDensity));
                                value.setLayoutParams(valueParams);
                                value.setText(valueString);
                                value.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                                value.setTextColor(getColor(R.color.colorPrimary));
                                value.setTextSize(16);
                                value.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                                innerBlock.addView(value);  //Adding the value to the inner block

                                /*
                                    Finally adding the inner block to the main block
                                 */
                                block.addView(innerBlock);
                            }
                        }

                        /*
                            Adding the block to the view
                         */
                        examView.addView(block);
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loading).setVisibility(View.GONE);
                                exams.addView(examView);
                            }
                        });
                    }

                    s.close();
                }

                c.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newExams").apply();
            }
        }).start();
    }
}