package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class GradeHistoryActivity extends AppCompatActivity {
    ScrollView grades;
    TextView[] gradeButtons = new TextView[4];
    LinearLayout[] gradeViews = new LinearLayout[4];
    HorizontalScrollView gradeButtonsContainer;
    float pixelDensity;
    int grade, halfWidth;

    boolean terminateThread = false;

    public void setGrades(View view) {
        int index = Integer.parseInt(view.getTag().toString());
        if (index == grade) {
            return;
        }

        grade = index;

        grades.scrollTo(0, 0);
        grades.removeAllViews();

        if (gradeViews[grade].getChildCount() > 0) {
            findViewById(R.id.noData).setVisibility(View.GONE);
            gradeViews[grade].setAlpha(0);
            grades.addView(gradeViews[grade]);
            gradeViews[grade].animate().alpha(1);
        } else {
            findViewById(R.id.noData).setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < 4; ++i) {
            gradeButtons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        gradeButtons[grade].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float location = 0;
        for (int i = 0; i < grade; ++i) {
            location += 10 * pixelDensity + (float) gradeButtons[i].getWidth();
        }
        location += 20 * pixelDensity + (float) gradeButtons[grade].getWidth() / 2;

        gradeButtonsContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_history);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        pixelDensity = context.getResources().getDisplayMetrics().density;
        grades = findViewById(R.id.grades);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        gradeButtonsContainer = findViewById(R.id.grade_buttons);

        gradeButtonsContainer.animate().alpha(1);

        gradeButtons[0] = findViewById(R.id.summary);
        gradeButtons[1] = findViewById(R.id.effective);
        gradeButtons[2] = findViewById(R.id.curriculum);
        gradeButtons[3] = findViewById(R.id.basket);

        LayoutGenerator myLayout = new LayoutGenerator(this);

        for (int i = 0; i < 4; ++i) {
            gradeViews[i] = myLayout.generateLayout();
        }

        grades.addView(gradeViews[0]);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                /*
                    The Grade History Summary
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_summary (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM grades_summary", null);

                int keyIndex = c.getColumnIndex("column1");
                int valueIndex = c.getColumnIndex("column2");

                c.moveToFirst();

                CardGenerator myGradeHistory = new CardGenerator(context, CardGenerator.CARD_GRADE_HISTORY_A);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    String key = c.getString(keyIndex);
                    String value = c.getString(valueIndex);

                    if (i == 0) {
                        ++i;
                        c.moveToNext();

                        key = "Total Credits";
                        value = c.getString(valueIndex) + " / " + value;
                    }

                    final LinearLayout card = myGradeHistory.generateCard(key, value);

                    if (i <= 2 && grade == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 0) {
                        card.setAlpha(0);
                        card.animate().alpha(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gradeViews[0].addView(card);
                            }
                        });
                    } else {
                        gradeViews[0].addView(card);
                    }
                }

                c.close();

                /*
                    The Grade History Effective Grades
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_effective (id INTEGER PRIMARY KEY, course VARCHAR, title VARCHAR, credits VARCHAR, grade VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM grades_effective", null);

                int courseIndex = c.getColumnIndex("course");
                int titleIndex = c.getColumnIndex("title");
                int creditsIndex = c.getColumnIndex("credits");
                int gradeIndex = c.getColumnIndex("grade");

                c.moveToFirst();

                myGradeHistory = new CardGenerator(context, CardGenerator.CARD_GRADE_HISTORY_B);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    String course = c.getString(courseIndex);
                    String title = c.getString(titleIndex);
                    String credits = c.getString(creditsIndex);
                    String gradeString = c.getString(gradeIndex);

                    final LinearLayout card = myGradeHistory.generateCard(course, title, credits, gradeString);

                    if (i <= 1 && grade == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 1) {
                        card.setAlpha(0);
                        card.animate().alpha(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gradeViews[1].addView(card);
                            }
                        });
                    } else {
                        gradeViews[1].addView(card);
                    }
                }

                c.close();

                /*
                    The Grade History Curriculum Details
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_curriculum (id INTEGER PRIMARY KEY, type VARCHAR, credits VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM grades_curriculum", null);

                int typeIndex = c.getColumnIndex("type");
                creditsIndex = c.getColumnIndex("credits");

                c.moveToFirst();

                myGradeHistory = new CardGenerator(context, CardGenerator.CARD_GRADE_HISTORY_A);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    String type = c.getString(typeIndex);
                    String credits = c.getString(creditsIndex);

                    final LinearLayout card = myGradeHistory.generateCard(type, credits);

                    if (i <= 1 && grade == 2) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 2) {
                        card.setAlpha(0);
                        card.animate().alpha(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gradeViews[2].addView(card);
                            }
                        });
                    } else {
                        gradeViews[2].addView(card);
                    }
                }

                c.close();

                /*
                    The Grade History Basket Details
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_basket (id INTEGER PRIMARY KEY, title VARCHAR, credits VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM grades_basket", null);

                titleIndex = c.getColumnIndex("title");
                creditsIndex = c.getColumnIndex("credits");

                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    String title = c.getString(titleIndex);
                    String credits = c.getString(creditsIndex);

                    final LinearLayout card = myGradeHistory.generateCard(title, credits);

                    if (i <= 1 && grade == 3) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 3) {
                        card.setAlpha(0);
                        card.animate().alpha(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gradeViews[3].addView(card);
                            }
                        });
                    } else {
                        gradeViews[3].addView(card);
                    }
                }

                c.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}