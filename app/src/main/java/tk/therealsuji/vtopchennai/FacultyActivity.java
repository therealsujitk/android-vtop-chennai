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

public class FacultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout facultyInfo = findViewById(R.id.faculty);

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS faculty (id INT(3) PRIMARY KEY, course VARCHAR, faculty VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM faculty", null);

                int courseIndex = c.getColumnIndex("course");
                int facultyIndex = c.getColumnIndex("faculty");
                c.moveToFirst();

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
                        Extracting the name of the faculty
                     */
                    String facultyName = c.getString(facultyIndex).split("-")[0].trim();

                    /*
                        The faculty name TextView
                     */
                    TextView faculty = new TextView(context);
                    TableRow.LayoutParams facultyParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    facultyParams.setMarginStart((int) (20 * pixelDensity));
                    facultyParams.setMarginEnd((int) (20 * pixelDensity));
                    facultyParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    faculty.setLayoutParams(facultyParams);
                    faculty.setText(facultyName);
                    faculty.setTextColor(getColor(R.color.colorPrimary));
                    faculty.setTextSize(20);
                    faculty.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(faculty); //Adding course code to block

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
                        Extracting the course and type
                     */
                    String[] courseData = c.getString(courseIndex).split("-");

                    /*
                        The course code TextView
                     */
                    TextView course = new TextView(context);
                    TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    courseParams.setMarginStart((int) (20 * pixelDensity));
                    courseParams.setMarginEnd((int) (20 * pixelDensity));
                    courseParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    course.setLayoutParams(courseParams);
                    course.setText(courseData[0].trim());
                    course.setTextColor(getColor(R.color.colorPrimary));
                    course.setTextSize(16);
                    course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(course); //Adding course code to innerBlock

                    /*
                        The course type code TextView
                     */
                    TextView type = new TextView(context);
                    TableRow.LayoutParams typeParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    typeParams.setMarginStart((int) (20 * pixelDensity));
                    typeParams.setMarginEnd((int) (20 * pixelDensity));
                    typeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    type.setLayoutParams(typeParams);
                    type.setText(courseData[courseData.length - 1].trim());
                    type.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    type.setTextColor(getColor(R.color.colorPrimary));
                    type.setTextSize(16);
                    type.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(type);   //Adding course type to innerBlock

                    block.addView(innerBlock);  //Adding innerBlock to block

                    /*
                        Finally adding the block to the activity
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            facultyInfo.addView(block);
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