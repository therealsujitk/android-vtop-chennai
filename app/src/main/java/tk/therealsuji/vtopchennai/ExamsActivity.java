package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class ExamsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LinearLayout schedule = findViewById(R.id.schedule);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INT(3) PRIMARY KEY, course VARCHAR, date VARCHAR, start_time VARCHAR, end_time VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM exams", null);

        int courseIndex = c.getColumnIndex("course");
        int dateIndex = c.getColumnIndex("date");
        int startTimeIndex = c.getColumnIndex("start_time");
        int endTimeIndex = c.getColumnIndex("end_time");
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); ++i) {
            /*
                The outer block
             */
            LinearLayout block = new LinearLayout(this);
            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            blockParams.setMarginStart((int) (20 * pixelDensity));
            blockParams.setMarginEnd((int) (20 * pixelDensity));
            if (i == 0) {
                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
            } else if (i == c.getCount() - 1) {
                blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            } else {
                blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
            }
            block.setLayoutParams(blockParams);
            block.setBackground(ContextCompat.getDrawable(this, R.drawable.plain_card));
            block.setGravity(Gravity.CENTER_VERTICAL);
            block.setOrientation(LinearLayout.VERTICAL);

            /*
                The course code TextView
             */
            TextView course = new TextView(this);
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
            course.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

            block.addView(course); //Adding course code to block

            /*
                The inner LinearLayout
             */
            LinearLayout innerBlock = new LinearLayout(this);
            innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

            String timings = c.getString(startTimeIndex) + " - " + c.getString(endTimeIndex);

            /*
                The time TextView
             */
            TextView time = new TextView(this);
            TableRow.LayoutParams timeParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            timeParams.setMarginStart((int) (20 * pixelDensity));
            timeParams.setMarginEnd((int) (20 * pixelDensity));
            timeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            time.setLayoutParams(courseParams);
            time.setText(timings);
            time.setTextColor(getColor(R.color.colorPrimary));
            time.setTextSize(16);
            time.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            innerBlock.addView(course); //Adding time to innerBlock

            /*
                The date TextView
             */
            TextView date = new TextView(this);
            TableRow.LayoutParams dateParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            dateParams.setMarginStart((int) (20 * pixelDensity));
            dateParams.setMarginEnd((int) (20 * pixelDensity));
            dateParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            date.setLayoutParams(dateParams);
            date.setText(c.getString(dateIndex));
            date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            date.setTextColor(getColor(R.color.colorPrimary));
            date.setTextSize(16);
            date.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            innerBlock.addView(date);   //Adding course type to innerBlock

            block.addView(innerBlock);  //Adding innerBlock to block

            /*
                Finally adding the block to the activity
             */
            schedule.addView(block);

            c.moveToNext();
        }

        c.close();
        myDatabase.close();
    }
}