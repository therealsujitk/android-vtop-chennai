package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class GradeHistoryActivity extends AppCompatActivity {
    ScrollView grades;
    TextView[] gradeButtons = new TextView[4];
    LinearLayout[] gradeViews = new LinearLayout[4];
    HorizontalScrollView gradeButtonsContainer;
    float pixelDensity;
    int grade;

    public void setGrades(View view) {
        int index = Integer.parseInt(view.getTag().toString());
        if (index == grade) {
            return;
        }

        grade = index;

        grades.scrollTo(0, 0);
        for (int i = 0; i < 4; ++i) {
            gradeViews[i].setVisibility(View.GONE);
            gradeButtons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        if (gradeViews[grade].getChildCount() > 0) {
            findViewById(R.id.noData).setVisibility(View.GONE);
            gradeViews[grade].setAlpha(0);
            gradeViews[grade].setVisibility(View.VISIBLE);
            gradeViews[grade].animate().alpha(1);
        } else {
            findViewById(R.id.noData).setVisibility(View.VISIBLE);
        }

        gradeButtons[grade].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int halfWidth = displayMetrics.widthPixels / 2;
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

        gradeButtonsContainer = findViewById(R.id.grade_buttons);

        gradeButtonsContainer.animate().alpha(1);

        gradeButtons[0] = findViewById(R.id.summary);
        gradeButtons[1] = findViewById(R.id.effective);
        gradeButtons[2] = findViewById(R.id.curriculum);
        gradeButtons[3] = findViewById(R.id.basket);

        gradeViews[0] = findViewById(R.id.summary_view);
        gradeViews[1] = findViewById(R.id.effective_view);
        gradeViews[2] = findViewById(R.id.curriculum_view);
        gradeViews[3] = findViewById(R.id.basket_view);

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

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    String key = c.getString(keyIndex);
                    String value = c.getString(valueIndex);

                    if (i == 0) {
                        ++i;
                        c.moveToNext();

                        key = "Total Credits";
                        value = c.getString(valueIndex) + " / " + value;
                    }

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
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The inner LinearLayout
                     */
                    LinearLayout innerBlock = new LinearLayout(context);
                    innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    innerBlock.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The value TextView
                     */
                    TextView valueView = new TextView(context);
                    TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    valueViewParams.setMarginStart((int) (20 * pixelDensity));
                    valueViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    valueView.setLayoutParams(valueViewParams);
                    valueView.setText(value);
                    valueView.setTextColor(getColor(R.color.colorPrimary));
                    valueView.setTextSize(20);
                    valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(valueView); //Adding value to innerBlock

                    /*
                        The key TextView
                     */
                    TextView keyView = new TextView(context);
                    TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    keyViewParams.setMarginStart((int) (20 * pixelDensity));
                    keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    keyView.setLayoutParams(keyViewParams);
                    keyView.setText(key);
                    keyView.setTextColor(getColor(R.color.colorPrimary));
                    keyView.setTextSize(16);
                    keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(keyView); //Adding key to innerBlock

                    block.addView(innerBlock);  //Adding the innerBlock to block

                    if (i <= 2 && grade == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 0) {
                        block.setAlpha(0);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gradeViews[0].addView(block);
                            block.animate().alpha(1);
                        }
                    });
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

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    /*
                        The outer block
                     */
                    final LinearLayout block = new LinearLayout(context);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    block.setPadding(0, 0, 0, (int) (15 * pixelDensity));
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
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
                    courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    course.setLayoutParams(courseParams);
                    course.setText(c.getString(titleIndex));
                    course.setTextColor(getColor(R.color.colorPrimary));
                    course.setTextSize(20);
                    course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(course); //Adding course to block

                    /*
                        The title TextView
                     */
                    TextView title = new TextView(context);
                    TableRow.LayoutParams titleParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    titleParams.setMarginStart((int) (20 * pixelDensity));
                    titleParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    title.setLayoutParams(titleParams);
                    title.setText(c.getString(courseIndex));
                    title.setTextColor(getColor(R.color.colorPrimary));
                    title.setTextSize(16);
                    title.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(title); //Adding title to block

                    /*
                        The innerBlocks
                     */
                    String[] keys = {getString(R.string.credits), getString(R.string.grade)};
                    String[] values = {c.getString(creditsIndex), c.getString(gradeIndex)};
                    for (int j = 0; j < 2; ++j) {
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
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMarginStart((int) (20 * pixelDensity));
                        keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(keys[j]);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        innerBlock.addView(keyView); //Adding key to innerBlock

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMarginEnd((int) (20 * pixelDensity));
                        valueViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        valueView.setText(values[j]);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(16);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        innerBlock.addView(valueView); //Adding value to innerBlock

                        block.addView(innerBlock);  //Adding the innerBlock to block
                    }

                    if (i <= 1 && grade == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 1) {
                        block.setAlpha(0);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gradeViews[1].addView(block);
                            block.animate().alpha(1);
                        }
                    });
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

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
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
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);
                    block.setAlpha(0);
                    block.animate().alpha(1);

                    /*
                        The inner LinearLayout
                     */
                    LinearLayout innerBlock = new LinearLayout(context);
                    innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    innerBlock.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The credits TextView
                     */
                    TextView creditsView = new TextView(context);
                    TableRow.LayoutParams creditsViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    creditsViewParams.setMarginStart((int) (20 * pixelDensity));
                    creditsViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    creditsView.setLayoutParams(creditsViewParams);
                    creditsView.setText(c.getString(creditsIndex));
                    creditsView.setTextColor(getColor(R.color.colorPrimary));
                    creditsView.setTextSize(20);
                    creditsView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(creditsView); //Adding key to innerBlock

                    /*
                        The type TextView
                     */
                    TextView typeView = new TextView(context);
                    TableRow.LayoutParams typeViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    typeViewParams.setMarginStart((int) (20 * pixelDensity));
                    typeViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    typeView.setLayoutParams(typeViewParams);
                    typeView.setText(c.getString(typeIndex));
                    typeView.setTextColor(getColor(R.color.colorPrimary));
                    typeView.setTextSize(16);
                    typeView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(typeView); //Adding value to innerBlock

                    block.addView(innerBlock);  //Adding the innerBlock to block

                    if (i <= 1 && grade == 2) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 2) {
                        block.setAlpha(0);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gradeViews[2].addView(block);
                            block.animate().alpha(1);
                        }
                    });
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
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);
                    block.setAlpha(0);
                    block.animate().alpha(1);

                    /*
                        The inner LinearLayout
                     */
                    LinearLayout innerBlock = new LinearLayout(context);
                    innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    innerBlock.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The credits TextView
                     */
                    TextView creditsView = new TextView(context);
                    TableRow.LayoutParams creditsViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    creditsViewParams.setMarginStart((int) (20 * pixelDensity));
                    creditsViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    creditsView.setLayoutParams(creditsViewParams);
                    creditsView.setText(c.getString(creditsIndex));
                    creditsView.setTextColor(getColor(R.color.colorPrimary));
                    creditsView.setTextSize(20);
                    creditsView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(creditsView); //Adding key to innerBlock

                    /*
                        The title TextView
                     */
                    TextView titleView = new TextView(context);
                    TableRow.LayoutParams titleViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    titleViewParams.setMarginStart((int) (20 * pixelDensity));
                    titleViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    titleView.setLayoutParams(titleViewParams);
                    titleView.setText(c.getString(titleIndex));
                    titleView.setTextColor(getColor(R.color.colorPrimary));
                    titleView.setTextSize(16);
                    titleView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(titleView); //Adding value to innerBlock

                    block.addView(innerBlock);  //Adding the innerBlock to block

                    if (i <= 1 && grade == 3) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    if (grade == 3) {
                        block.setAlpha(0);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gradeViews[3].addView(block);
                            block.animate().alpha(1);
                        }
                    });
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
}