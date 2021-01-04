package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ExamsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout schedule = findViewById(R.id.schedule);

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INT(3) PRIMARY KEY, course VARCHAR, date VARCHAR, start_time VARCHAR, end_time VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM exams", null);

                int courseIndex = c.getColumnIndex("course");
                int dateIndex = c.getColumnIndex("date");
                int startTimeIndex = c.getColumnIndex("start_time");
                int endTimeIndex = c.getColumnIndex("end_time");
                c.moveToFirst();

                SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

                for (int i = 0; i < c.getCount(); ++i) {
                   /*
                        The outer block
                     */
                    final LinearLayout block = new LinearLayout(context);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    if (i == 0) {
                        findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                        blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    }
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setGravity(Gravity.CENTER_VERTICAL);
                    block.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The exam course code TextView
                     */
                    TextView course = new TextView(context);
                    TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    courseParams.setMarginStart((int) (20 * pixelDensity));
                    courseParams.setMarginEnd((int) (20 * pixelDensity));
                    courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    course.setLayoutParams(courseParams);
                    course.setText(c.getString(courseIndex));
                    course.setTextColor(getColor(R.color.colorPrimary));
                    course.setTextSize(20);
                    course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(course); //Adding exam course code to block

                    /*
                        The inner LinearLayout
                     */
                    LinearLayout innerBlock = new LinearLayout(context);
                    innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    /*
                        Making a proper string of the timings
                     */
                    String startTimeString = c.getString(startTimeIndex);
                    String endTimeString = c.getString(endTimeIndex);

                    if (startTimeString.charAt(0) == '0') {
                        startTimeString = startTimeString.substring(1);
                    }
                    if (endTimeString.charAt(0) == '0') {
                        endTimeString = endTimeString.substring(1);
                    }

                    String timings = startTimeString + " - " + endTimeString;

                    if (DateFormat.is24HourFormat(context)) {
                        try {
                            Date startTime = hour12.parse(startTimeString);
                            Date endTime = hour12.parse(endTimeString);
                            assert startTime != null;
                            assert endTime != null;
                            timings = hour24.format(startTime) + " - " + hour24.format(endTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    /*
                        The time TextView
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

                    innerBlock.addView(course); //Adding time to innerBlock

                    /*
                        The date TextView
                     */
                    TextView date = new TextView(context);
                    TableRow.LayoutParams dateParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    dateParams.setMarginEnd((int) (20 * pixelDensity));
                    dateParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    date.setLayoutParams(dateParams);
                    date.setText(c.getString(dateIndex));
                    date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    date.setTextColor(getColor(R.color.colorPrimary));
                    date.setTextSize(16);
                    date.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(date);   //Adding date to innerBlock

                    block.addView(innerBlock);  //Adding innerBlock to block

                    /*
                        Finally adding the block to the activity
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            schedule.addView(block);
                        }
                    });

                    c.moveToNext();
                }

                c.close();
                myDatabase.close();
            }
        }).start();
    }
}