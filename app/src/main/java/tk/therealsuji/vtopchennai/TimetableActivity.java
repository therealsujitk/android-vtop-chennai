package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class TimetableActivity extends AppCompatActivity {
    ScrollView timetable;
    LinearLayout sunday, monday, tuesday, wednesday, thursday, friday, saturday;

    public void setSunday(View view) {
        timetable.removeAllViews();
        timetable.addView(sunday);
    }

    public void setMonday(View view) {
        timetable.removeAllViews();
        timetable.addView(monday);
    }

    public void setTuesday(View view) {
        timetable.removeAllViews();
        timetable.addView(tuesday);
    }

    public void setWednesday(View view) {
        timetable.removeAllViews();
        timetable.addView(wednesday);
    }

    public void setThursday(View view) {
        timetable.removeAllViews();
        timetable.addView(thursday);
    }

    public void setFriday(View view) {
        timetable.removeAllViews();
        timetable.addView(friday);
    }

    public void setSaturday(View view) {
        timetable.removeAllViews();
        timetable.addView(saturday);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        timetable = findViewById(R.id.timetable);

        float pixelDensity = this.getResources().getDisplayMetrics().density;

        SQLiteDatabase myDatabase = getApplicationContext().openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");

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

        sunday = new LinearLayout(this);
        sunday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        sunday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        sunday.setOrientation(LinearLayout.VERTICAL);

        monday = new LinearLayout(this);
        monday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        monday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        monday.setOrientation(LinearLayout.VERTICAL);

        tuesday = new LinearLayout(this);
        tuesday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tuesday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        tuesday.setOrientation(LinearLayout.VERTICAL);

        wednesday = new LinearLayout(this);
        wednesday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        wednesday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        wednesday.setOrientation(LinearLayout.VERTICAL);

        thursday = new LinearLayout(this);
        thursday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        thursday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        thursday.setOrientation(LinearLayout.VERTICAL);

        friday = new LinearLayout(this);
        friday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        friday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        friday.setOrientation(LinearLayout.VERTICAL);

        saturday = new LinearLayout(this);
        saturday.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        saturday.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        saturday.setOrientation(LinearLayout.VERTICAL);

        theory.moveToFirst();
        lab.moveToFirst();

        for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
            LinearLayout[] days = {sunday, monday, tuesday, wednesday, thursday, friday, saturday};
            int[] theoryIndexes = {sundayTheory, mondayTheory, tuesdayTheory, wednesdayTheory, thursdayTheory, fridayTheory, saturdayTheory};
            int[] labIndexes = {sundayLab, mondayLab, tuesdayLab, wednesdayLab, thursdayLab, fridayLab, saturdayLab};

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
                TextView period = new TextView(this);
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
                    period.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    theoryFlag = true;
                }

                /*
                    The outer block for theory (Initialized later to make the code faster)
                 */
                if (theoryFlag) {
                    LinearLayout block = new LinearLayout(this);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(this, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    String timings = startTimeTheory + " - " + endTimeTheory;

                    TextView time = new TextView(this);
                    TableRow.LayoutParams timeParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    timeParams.setMarginStart((int) (20 * pixelDensity));
                    timeParams.setMarginEnd((int) (20 * pixelDensity));
                    timeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    time.setLayoutParams(timeParams);
                    time.setText(timings);
                    time.setTextColor(getColor(R.color.colorPrimary));
                    time.setTextSize(16);
                    time.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    innerBlock.addView(time);

                    TextView theoryText = new TextView(this);
                    TableRow.LayoutParams theoryParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    theoryParams.setMarginStart((int) (20 * pixelDensity));
                    theoryParams.setMarginEnd((int) (20 * pixelDensity));
                    theoryParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    theoryText.setLayoutParams(theoryParams);
                    theoryText.setText(getString(R.string.theory));
                    theoryText.setTextColor(getColor(R.color.colorPrimary));
                    theoryText.setTextSize(16);
                    theoryText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    theoryText.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    innerBlock.addView(theoryText);

                    block.addView(period);
                    block.addView(innerBlock);
                    days[j].addView(block);
                }

                /*
                    The period TextView for lab
                 */
                period = new TextView(this);
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
                    period.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    labFlag = true;
                }

                /*
                    The outer block for lab (Initialized later to make the code faster)
                 */
                if (labFlag) {
                    LinearLayout block = new LinearLayout(this);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(this, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    String timings = startTimeLab + " - " + endTimeLab;

                    TextView time = new TextView(this);
                    TableRow.LayoutParams timeParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    timeParams.setMarginStart((int) (20 * pixelDensity));
                    timeParams.setMarginEnd((int) (20 * pixelDensity));
                    timeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    time.setLayoutParams(timeParams);
                    time.setText(timings);
                    time.setTextColor(getColor(R.color.colorPrimary));
                    time.setTextSize(16);
                    time.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    innerBlock.addView(time);

                    TextView labText = new TextView(this);
                    TableRow.LayoutParams labParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    labParams.setMarginStart((int) (20 * pixelDensity));
                    labParams.setMarginEnd((int) (20 * pixelDensity));
                    labParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    labText.setLayoutParams(labParams);
                    labText.setText(getString(R.string.lab));
                    labText.setTextColor(getColor(R.color.colorPrimary));
                    labText.setTextSize(16);
                    labText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    labText.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    innerBlock.addView(labText);

                    block.addView(period);
                    block.addView(innerBlock);
                    days[j].addView(block);
                }
            }
        }

        timetable.addView(thursday);

        theory.close();
        lab.close();
        myDatabase.close();
    }
}