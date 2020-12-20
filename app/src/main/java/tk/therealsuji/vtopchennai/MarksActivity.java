package tk.therealsuji.vtopchennai;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Objects;

public class MarksActivity extends AppCompatActivity {
    ScrollView marks;
    ArrayList<Button> buttons = new ArrayList<>();
    ArrayList<LinearLayout> markViews = new ArrayList<>();
    float pixelDensity;

    public void setMarks(View view) {
        marks.scrollTo(0, 0);
        marks.removeAllViews();

        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        int index = Integer.parseInt(view.getTag().toString());
        buttons.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));
        marks.addView(markViews.get(index));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int halfWidth = displayMetrics.widthPixels / 2;
        float location = 0;
        for (int i = 0; i < index; ++i) {
            location += 5 * pixelDensity + (float) buttons.get(i).getWidth();
        }
        location += 5 * pixelDensity + (float) buttons.get(index).getWidth() / 2;
        ((HorizontalScrollView) findViewById(R.id.markTitlesContainer)).smoothScrollTo((int) location - halfWidth + (int) (15 * pixelDensity), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout markButtons = findViewById(R.id.markTitles);
        pixelDensity = context.getResources().getDisplayMetrics().density;
        marks = findViewById(R.id.marks);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, percent VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR, remark VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT DISTINCT title FROM marks", null);

                int titleIndex = c.getColumnIndex("title");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    String markTitle = c.getString(titleIndex);

                    /*
                        Creating a the mark view
                     */
                    final LinearLayout markView = new LinearLayout(context);
                    LinearLayout.LayoutParams markViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    markView.setLayoutParams(markViewParams);
                    markView.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
                    markView.setOrientation(LinearLayout.VERTICAL);

                    markViews.add(markView);    //Storing the view

                    /*
                        Creating the markTitle button
                     */
                    final Button markButton = new Button(context);
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) (25 * pixelDensity)
                    );
                    buttonParams.setMarginStart((int) (5 * pixelDensity));
                    buttonParams.setMarginEnd((int) (5 * pixelDensity));
                    buttonParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    markButton.setLayoutParams(buttonParams);
                    markButton.setPadding(0, 0, 0, 0);
                    if (i == 0) {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                        findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                    } else {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
                    }
                    markButton.setTag(i);
                    markButton.setText(markTitle);
                    markButton.setTextColor(getColor(R.color.colorPrimary));
                    markButton.setTextSize(12);
                    markButton.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
                    markButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMarks(markButton);
                        }
                    });

                    /*
                        Finally adding the button to the HorizontalScrollView
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            markButtons.addView(markButton);
                        }
                    });

                    buttons.add(markButton);    //Storing the button

                    Cursor s = myDatabase.rawQuery("SELECT * FROM marks WHERE title = '" + markTitle + "'", null);

                    int courseIndex = s.getColumnIndex("course");
                    int typeIndex = s.getColumnIndex("type");
                    int scoreIndex = s.getColumnIndex("score");
                    int percentIndex = s.getColumnIndex("percent");
                    int statusIndex = s.getColumnIndex("status");
                    int weightageIndex = s.getColumnIndex("weightage");
                    int averageIndex = s.getColumnIndex("average");
                    int remarkIndex = s.getColumnIndex("remark");

                    int[] indexes = {typeIndex, scoreIndex, weightageIndex, percentIndex, averageIndex, statusIndex, remarkIndex};
                    String[] titles = {getString(R.string.type), getString(R.string.score), getString(R.string.weightage), getString(R.string.percent), getString(R.string.average), getString(R.string.status), getString(R.string.remark)};

                    s.moveToFirst();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        /*
                            The outer block for the mark
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

                        /*
                            The course TextView
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
                        course.setText(s.getString(courseIndex));
                        course.setTextColor(getColor(R.color.colorPrimary));
                        course.setTextSize(20);
                        course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(course);  //Adding the course to block

                        for (int k = 0; k < 7; ++k) {
                            String valueString = s.getString(indexes[k]);
                            if (!valueString.equals("")) {
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
                            Finally adding the main block to the view
                         */
                        markView.addView(block);
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                marks.addView(markView);
                            }
                        });
                    }

                    s.close();
                }

                c.close();
                myDatabase.close();
            }
        }).start();
    }
}