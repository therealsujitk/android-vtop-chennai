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

public class AttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LinearLayout attendance = findViewById(R.id.attendance);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        SQLiteDatabase myDatabase = getApplicationContext().openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS attendance (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, percent VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM attendance", null);

        int courseIndex = c.getColumnIndex("course");
        int typeIndex = c.getColumnIndex("type");
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
                blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (10 * pixelDensity));
            } else if (i == c.getCount() - 1) {
                blockParams.setMargins(0, (int) (10 * pixelDensity), 0, (int) (20 * pixelDensity));
            } else {
                blockParams.setMargins(0, (int) (10 * pixelDensity), 0, (int) (10 * pixelDensity));
            }
            block.setLayoutParams(blockParams);
            block.setBackground(ContextCompat.getDrawable(this, R.drawable.button_card));
            block.setGravity(Gravity.CENTER_VERTICAL);
            block.setOrientation(LinearLayout.HORIZONTAL);

            /*
                The inner LinearLayout
             */
            LinearLayout innerBlock = new LinearLayout(this);
            innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            innerBlock.setOrientation(LinearLayout.VERTICAL);

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
            courseParams.setMargins(0, (int) (20 * pixelDensity), (int) (20 * pixelDensity), (int) (5 * pixelDensity));
            course.setLayoutParams(courseParams);
            course.setText(c.getString(courseIndex));
            course.setTextColor(getColor(R.color.colorPrimary));
            course.setTextSize(20);
            course.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            innerBlock.addView(course); //Adding course code to innerBlock

            /*
                The course code TextView
             */
            TextView type = new TextView(this);
            TableRow.LayoutParams typeParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            typeParams.setMarginStart((int) (20 * pixelDensity));
            typeParams.setMarginEnd((int) (20 * pixelDensity));
            typeParams.setMargins(0, (int) (5 * pixelDensity), (int) (20 * pixelDensity), (int) (20 * pixelDensity));
            type.setLayoutParams(typeParams);
            type.setText(c.getString(typeIndex));
            type.setTextColor(getColor(R.color.colorPrimary));
            type.setTextSize(16);
            type.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            innerBlock.addView(type);   //Adding course type to innerBlock

            block.addView(innerBlock);  //Adding innerBlock to block

            /*
                The attendance percentage TextView
             */
            TextView percent = new TextView(this);
            TableRow.LayoutParams percentParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            percentParams.setMarginStart((int) (20 * pixelDensity));
            percentParams.setMarginEnd((int) (20 * pixelDensity));
            percentParams.setMargins(0, (int) (20 * pixelDensity), (int) (20 * pixelDensity), (int) (20 * pixelDensity));
            percent.setLayoutParams(percentParams);
            percent.setText(c.getString(percentIndex));
            percent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            percent.setTextColor(getColor(R.color.colorPrimary));
            percent.setTextSize(20);
            percent.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

            block.addView(percent); //Adding percentage to block

            /*
                Finally adding the block to the activity
             */
            attendance.addView(block);

            c.moveToNext();
        }

        c.close();
    }
}