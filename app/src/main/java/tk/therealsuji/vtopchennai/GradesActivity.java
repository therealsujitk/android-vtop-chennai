package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class GradesActivity extends AppCompatActivity {

    public void openGradeHistory(MenuItem item) {
        startActivity(new Intent(this, GradeHistoryActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout grades = findViewById(R.id.grades);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, grade_type VARCHAR, total VARCHAR, grade VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM grades", null);

                int courseIndex = c.getColumnIndex("course");
                int typeIndex = c.getColumnIndex("type");
                int gradeTypeIndex = c.getColumnIndex("grade_type");
                int totalIndex = c.getColumnIndex("total");
                int gradeIndex = c.getColumnIndex("grade");
                c.moveToFirst();

                CardGenerator myGrade = new CardGenerator(context, CardGenerator.CARD_GRADE);

                int i;
                for (i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    String course = c.getString(courseIndex);
                    String type = c.getString(typeIndex);
                    String gradeType = c.getString(gradeTypeIndex);
                    String total = c.getString(totalIndex);
                    String grade = c.getString(gradeIndex);

                    final LinearLayout card = myGrade.generateCard(course, type, gradeType, total, grade);
                    card.setAlpha(0);
                    card.animate().alpha(1);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            grades.addView(card);
                        }
                    });
                }

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                if (i > 0) {
                    String gpa = sharedPreferences.getString("gpa", "0.0");
                    final LinearLayout card = new CardGenerator(context, CardGenerator.CARD_GPA).generateCard("Your GPA", gpa);
                    card.setAlpha(0);
                    card.animate().alpha(1);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            grades.addView(card);
                        }
                    });
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();

                sharedPreferences.edit().remove("newGrades").apply();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grades_menu, menu);

        return true;
    }
}