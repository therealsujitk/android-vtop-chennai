package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.Objects;

public class CoursesActivity extends AppCompatActivity {
    ScrollView courses;
    HorizontalScrollView courseCodesContainer;
    ArrayList<TextView> buttons = new ArrayList<>();
    ArrayList<LinearLayout> courseViews = new ArrayList<>();
    LinearLayout courseButtons;
    Context context;
    float pixelDensity;
    int index, halfWidth;

    boolean terminateThread;

    public void setCourse(View view) {
        int selectedIndex = Integer.parseInt(view.getTag().toString());
        if (index == selectedIndex) {
            return;
        }

        index = selectedIndex;

        courses.scrollTo(0, 0);
        courses.removeAllViews();

        if (courses.getChildCount() == 0) {
            courses.setAlpha(0);
            courses.addView(courseViews.get(index));
            courses.animate().alpha(1);
        }

        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        buttons.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float location = 0;
        for (int i = 0; i < index; ++i) {
            location += 10 * pixelDensity + (float) buttons.get(i).getWidth();
        }
        location += 20 * pixelDensity + (float) buttons.get(index).getWidth() / 2;
        courseCodesContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        courses = findViewById(R.id.courses);
        courseButtons = findViewById(R.id.courseCodes);
        pixelDensity = context.getResources().getDisplayMetrics().density;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        courseCodesContainer = findViewById(R.id.courseCodesContainer);

        new Thread(() -> {
            SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS courses (id INTEGER PRIMARY KEY, course_code VARCHAR, course VARCHAR, course_type VARCHAR, slot , venue VARCHAR, faculty VARCHAR, school VARCHAR)");

            Cursor c = myDatabase.rawQuery("SELECT course_code, course FROM courses GROUP BY course_code", null);

            int courseCodeIndex = c.getColumnIndex("course_code");
            int courseIndex = c.getColumnIndex("course");
            c.moveToFirst();

            LayoutGenerator myLayout = new LayoutGenerator(context);
            ButtonGenerator myButton = new ButtonGenerator(context);
            CardGenerator myCourse = new CardGenerator(context, CardGenerator.CARD_COURSE);
            CardGenerator myCourseTitle = new CardGenerator(context, CardGenerator.CARD_COURSE_TITLE);

            for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                if (terminateThread) {
                    return;
                }

                if (i == 0) {
                    runOnUiThread(() -> findViewById(R.id.noData).setVisibility(View.GONE));
                }

                String courseCode = c.getString(courseCodeIndex);
                String course = c.getString(courseIndex);

                /*
                    Creating a the course view
                 */
                final LinearLayout courseView = myLayout.generateLayout();

                courseViews.add(courseView);    //Storing the view

                /*
                    Creating the courseCode button
                 */
                final TextView courseButton = myButton.generateButton(courseCode);
                if (i == 0 && i == index) {
                    courseButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                }
                courseButton.setTag(i);
                courseButton.setOnClickListener(v -> setCourse(courseButton));

                buttons.add(courseButton);  //Storing the button

                /*
                    Creating & adding the course title card
                 */
                final LinearLayout titleCard = myCourseTitle.generateCard(getString(R.string.course_title), course);
                courseView.addView(titleCard);

                runOnUiThread(() -> {
                    courseButton.setAlpha(0);
                    courseButtons.addView(courseButton);
                    courseButton.animate().alpha(1);
                });

                if (i == 0 && i == index && courses.getChildCount() == 0) {
                    runOnUiThread(() -> {
                        titleCard.setAlpha(0);
                        courses.addView(courseView);
                        titleCard.animate().alpha(1);
                    });
                }

                Cursor s = myDatabase.rawQuery("SELECT * FROM courses WHERE course_code = '" + courseCode + "'", null);

                int courseTypeIndex = s.getColumnIndex("course_type");
                int slotIndex = s.getColumnIndex("slot");
                int venueIndex = s.getColumnIndex("venue");
                int facultyIndex = s.getColumnIndex("faculty");
                int schoolIndex = s.getColumnIndex("school");

                s.moveToFirst();

                for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    String courseType = s.getString(courseTypeIndex);
                    String slot = s.getString(slotIndex);
                    String venue = s.getString(venueIndex);
                    String faculty = s.getString(facultyIndex);
                    String school = s.getString(schoolIndex);

                    final LinearLayout card = myCourse.generateCard(faculty, courseType, school, slot, venue);

                    /*
                        Adding the card to the view
                     */
                    if (i == index) {
                        runOnUiThread(() -> {
                            card.setAlpha(0);
                            courseView.addView(card);
                            card.animate().alpha(1);
                        });
                    } else {
                        courseView.addView(card);
                    }
                }

                s.close();
            }

            runOnUiThread(() -> findViewById(R.id.loading).animate().alpha(0));

            c.close();
            myDatabase.close();

            SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove("newCourses").apply();
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}
