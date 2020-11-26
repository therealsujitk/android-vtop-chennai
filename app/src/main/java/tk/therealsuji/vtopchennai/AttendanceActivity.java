package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class AttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LinearLayout attendance = findViewById(R.id.attendance);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS attendance (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, attended VARCHAR, total VARCHAR, percent VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM attendance", null);

        int courseIndex = c.getColumnIndex("course");
        int typeIndex = c.getColumnIndex("type");
        int attendedIndex = c.getColumnIndex("attended");
        int totalIndex = c.getColumnIndex("total");
        int percentIndex = c.getColumnIndex("percent");
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
            block.setOrientation(LinearLayout.VERTICAL);

            /*
                The inner LinearLayout
             */
            LinearLayout innerBlock = new LinearLayout(this);
            innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

            /*
                The course code TextView
             */
            TextView course = new TextView(this);
            TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            courseParams.setMarginStart((int) (20 * pixelDensity));
            courseParams.setMarginEnd((int) (20 * pixelDensity));
            courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
            course.setLayoutParams(courseParams);
            course.setText(c.getString(courseIndex));
            course.setTextColor(getColor(R.color.colorPrimary));
            course.setTextSize(20);
            course.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            innerBlock.addView(course); //Adding course code to innerBlock

            /*
                The attendance percentage TextView
             */
            String percentString = c.getString(percentIndex) + "%";

            TextView percent = new TextView(this);
            TableRow.LayoutParams percentParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            percentParams.setMarginStart((int) (20 * pixelDensity));
            percentParams.setMarginEnd((int) (20 * pixelDensity));
            percentParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
            percent.setLayoutParams(percentParams);
            percent.setText(percentString);
            percent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            percent.setTextColor(getColor(R.color.colorPrimary));
            percent.setTextSize(20);
            percent.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

            innerBlock.addView(percent); //Adding percentage to innerBlock

            LinearLayout secondInnerBlock = new LinearLayout(this);
            secondInnerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            secondInnerBlock.setOrientation(LinearLayout.HORIZONTAL);

            /*
                The course type code TextView
             */
            TextView type = new TextView(this);
            TableRow.LayoutParams typeParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            typeParams.setMarginStart((int) (20 * pixelDensity));
            typeParams.setMarginEnd((int) (20 * pixelDensity));
            typeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            type.setLayoutParams(typeParams);
            type.setText(c.getString(typeIndex));
            type.setTextColor(getColor(R.color.colorPrimary));
            type.setTextSize(16);
            type.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            secondInnerBlock.addView(type);   //Adding type to innerBlock

            /*
                The attended classes TextView
             */
            String attendedString = c.getString(attendedIndex) + " | " + c.getString(totalIndex);

            TextView attended = new TextView(this);
            TableRow.LayoutParams attendedParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            attendedParams.setMarginStart((int) (20 * pixelDensity));
            attendedParams.setMarginEnd((int) (20 * pixelDensity));
            attendedParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            attended.setLayoutParams(attendedParams);
            attended.setText(attendedString);
            attended.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            attended.setTextColor(getColor(R.color.colorPrimary));
            attended.setTextSize(16);
            attended.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

            secondInnerBlock.addView(attended);   //Adding attended classes to innerBlock

            block.addView(innerBlock);   //Adding innerBlock to block
            block.addView(secondInnerBlock);    //Adding secondInnerBlock to block

            /*
                Finally adding the block to the activity
             */
            attendance.addView(block);

            c.moveToNext();
        }

        c.close();
        myDatabase.close();
    }
}