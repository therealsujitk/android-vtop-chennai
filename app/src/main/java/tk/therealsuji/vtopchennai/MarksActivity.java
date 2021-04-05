package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MarksActivity extends AppCompatActivity {
    ScrollView marks;
    HorizontalScrollView markTitlesContainer;
    ArrayList<TextView> buttons = new ArrayList<>();
    ArrayList<LinearLayout> markViews = new ArrayList<>();
    LinearLayout markButtons;
    Context context;
    float pixelDensity;
    int screenWidth, index;
    SharedPreferences sharedPreferences;
    JSONObject newMarks;

    boolean terminateThread;

    public void setMarks(View view) {
        int selectedIndex = Integer.parseInt(view.getTag().toString());
        if (index == selectedIndex) {
            return;
        } else {
            index = selectedIndex;
        }

        marks.scrollTo(0, 0);
        marks.removeAllViews();
        marks.setAlpha(0);
        marks.addView(markViews.get(index));
        marks.animate().alpha(1);

        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        buttons.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        int halfWidth = screenWidth / 2;
        float location = 0;
        for (int i = 0; i < index; ++i) {
            location += 10 * pixelDensity + (float) buttons.get(i).getWidth();
        }
        location += 20 * pixelDensity + (float) buttons.get(index).getWidth() / 2;
        markTitlesContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    public void filterByCourse(MenuItem menuItem) {
        if (menuItem != null && sharedPreferences.getBoolean("filterByCourse", true)) {
            return;
        }

        /*
            The immediate change in visibility may seem useless, but it is done to reset the progress bar
         */
        FrameLayout loading = findViewById(R.id.loading);
        loading.setVisibility(View.INVISIBLE);
        loading.setAlpha(1);
        loading.setVisibility(View.VISIBLE);

        sharedPreferences.edit().remove("filterByCourse").apply();

        markButtons.removeAllViews();
        marks.removeAllViews();

        buttons.clear();
        markViews.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT course FROM marks GROUP BY course", null);

                int courseIndex = c.getColumnIndex("course");
                c.moveToFirst();

                LayoutGenerator myLayout = new LayoutGenerator(context);
                ButtonGenerator myButton = new ButtonGenerator(context);
                CardGenerator myMark = new CardGenerator(context, CardGenerator.CARD_MARK);

                NotificationDotGenerator myNotification = new NotificationDotGenerator(context);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    String course = c.getString(courseIndex);

                    /*
                        Creating a the mark view
                     */
                    final LinearLayout markView = myLayout.generateLayout();

                    markViews.add(markView);    //Storing the view

                    Cursor s = myDatabase.rawQuery("SELECT * FROM marks WHERE course = '" + course + "' ORDER BY type DESC, title", null);

                    int idIndex = s.getColumnIndex("id");
                    int titleIndex = s.getColumnIndex("title");
                    int typeIndex = s.getColumnIndex("type");
                    int scoreIndex = s.getColumnIndex("score");
                    int statusIndex = s.getColumnIndex("status");
                    int weightageIndex = s.getColumnIndex("weightage");
                    int averageIndex = s.getColumnIndex("average");

                    s.moveToFirst();

                    final ArrayList<String> readMarks = new ArrayList<>();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String title = s.getString(titleIndex);
                        String type = s.getString(typeIndex);
                        String score = s.getString(scoreIndex);
                        String weightage = s.getString(weightageIndex);
                        String average = s.getString(averageIndex);
                        String status = s.getString(statusIndex);

                        final LinearLayout card = myMark.generateCard(title, type, score, weightage, average, status);

                        /*
                            Adding the card / container to the view
                         */
                        String id = s.getString(idIndex);
                        if (newMarks.has(id)) {
                            RelativeLayout container = myNotification.generateNotificationContainer();
                            container.addView(card);

                            int marginStart = (int) (screenWidth - 30 * pixelDensity);
                            ImageView notification = myNotification.generateNotificationDot(marginStart, NotificationDotGenerator.NOTIFICATION_DEFAULT);
                            notification.setPadding(0, (int) (5 * pixelDensity), 0, 0);
                            container.addView(notification);

                            markView.addView(container);
                            readMarks.add(id);
                        } else {
                            markView.addView(card);
                        }
                    }

                    /*
                        Creating the course button
                     */
                    final TextView markButton = myButton.generateButton(course);
                    if (i == 0) {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));

                        for (int j = 0; j < readMarks.size(); ++j) {
                            String id = readMarks.get(j);
                            if (newMarks.has(id)) {
                                newMarks.remove(id);
                            }
                        }

                        sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();
                    }
                    markButton.setTag(i);
                    markButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMarks(markButton);

                            for (int i = 0; i < readMarks.size(); ++i) {
                                String id = readMarks.get(i);
                                if (newMarks.has(id)) {
                                    newMarks.remove(id);
                                }
                            }

                            sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();
                        }
                    });
                    markButton.setAlpha(0);
                    markButton.animate().alpha(1);

                    buttons.add(markButton);    //Storing the button

                    /*
                        Adding the button to the HorizontalScrollView
                     */
                    if (readMarks.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markButtons.addView(markButton);
                            }
                        });
                    } else {
                        final RelativeLayout container = myNotification.generateNotificationContainer();
                        container.addView(markButton);

                        final ImageView notification = myNotification.generateNotificationDot((int) (3 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                        notification.setPadding(0, (int) (20 * pixelDensity), 0, 0);
                        container.addView(notification);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markButtons.addView(container);
                                notification.animate().scaleX(1).scaleY(1);
                            }
                        });
                    }

                    if (i == index) {
                        markView.setAlpha(0);
                        markView.animate().alpha(1);
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();
            }
        }).start();
    }

    public void filterByTitle(MenuItem menuItem) {
        if (menuItem != null && !sharedPreferences.getBoolean("filterByCourse", true)) {
            return;
        }

        /*
            The immediate change in visibility may seem useless, but it is done to reset the progress bar
         */
        FrameLayout loading = findViewById(R.id.loading);
        loading.setVisibility(View.INVISIBLE);
        loading.setAlpha(1);
        loading.setVisibility(View.VISIBLE);

        sharedPreferences.edit().putBoolean("filterByCourse", false).apply();

        markButtons.removeAllViews();
        marks.removeAllViews();

        buttons.clear();
        markViews.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT title FROM marks GROUP BY title", null);

                int titleIndex = c.getColumnIndex("title");
                c.moveToFirst();

                LayoutGenerator myLayout = new LayoutGenerator(context);
                ButtonGenerator myButton = new ButtonGenerator(context);
                CardGenerator myMark = new CardGenerator(context, CardGenerator.CARD_MARK);

                NotificationDotGenerator myNotification = new NotificationDotGenerator(context);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    String markTitle = c.getString(titleIndex);

                    /*
                        Creating a the mark view
                     */
                    final LinearLayout markView = myLayout.generateLayout();

                    markViews.add(markView);    //Storing the view

                    Cursor s = myDatabase.rawQuery("SELECT * FROM marks WHERE title = '" + markTitle + "' ORDER BY course, type DESC", null);

                    int idIndex = s.getColumnIndex("id");
                    int courseIndex = s.getColumnIndex("course");
                    int typeIndex = s.getColumnIndex("type");
                    int scoreIndex = s.getColumnIndex("score");
                    int statusIndex = s.getColumnIndex("status");
                    int weightageIndex = s.getColumnIndex("weightage");
                    int averageIndex = s.getColumnIndex("average");

                    s.moveToFirst();

                    final ArrayList<String> readMarks = new ArrayList<>();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String course = s.getString(courseIndex);
                        String type = s.getString(typeIndex);
                        String score = s.getString(scoreIndex);
                        String weightage = s.getString(weightageIndex);
                        String average = s.getString(averageIndex);
                        String status = s.getString(statusIndex);

                        final LinearLayout card = myMark.generateCard(course, type, score, weightage, average, status);

                        /*
                            Adding the card / container to the view
                         */
                        String id = s.getString(idIndex);
                        if (newMarks.has(id)) {
                            RelativeLayout container = myNotification.generateNotificationContainer();
                            container.addView(card);

                            int marginStart = (int) (screenWidth - 30 * pixelDensity);
                            ImageView notification = myNotification.generateNotificationDot(marginStart, NotificationDotGenerator.NOTIFICATION_DEFAULT);
                            notification.setPadding(0, (int) (5 * pixelDensity), 0, 0);
                            container.addView(notification);

                            markView.addView(container);
                            readMarks.add(id);
                        } else {
                            markView.addView(card);
                        }
                    }

                    /*
                        Creating the markTitle button
                     */
                    final TextView markButton = myButton.generateButton(markTitle);
                    if (i == 0) {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));

                        for (int j = 0; j < readMarks.size(); ++j) {
                            String id = readMarks.get(j);
                            if (newMarks.has(id)) {
                                newMarks.remove(id);
                            }
                        }

                        sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();
                    }
                    markButton.setTag(i);
                    markButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMarks(markButton);

                            for (int i = 0; i < readMarks.size(); ++i) {
                                String id = readMarks.get(i);
                                if (newMarks.has(id)) {
                                    newMarks.remove(id);
                                }
                            }

                            sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();
                        }
                    });
                    markButton.setAlpha(0);
                    markButton.animate().alpha(1);

                    buttons.add(markButton);    //Storing the button

                    /*
                        Adding the button to the HorizontalScrollView
                     */
                    if (readMarks.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markButtons.addView(markButton);
                            }
                        });
                    } else {
                        final RelativeLayout container = myNotification.generateNotificationContainer();
                        container.addView(markButton);

                        final ImageView notification = myNotification.generateNotificationDot((int) (3 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                        notification.setPadding(0, (int) (20 * pixelDensity), 0, 0);
                        container.addView(notification);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markButtons.addView(container);
                                notification.animate().scaleX(1).scaleY(1);
                            }
                        });
                    }

                    if (i == index) {
                        markView.setAlpha(0);
                        markView.animate().alpha(1);
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        markButtons = findViewById(R.id.markTitles);
        pixelDensity = context.getResources().getDisplayMetrics().density;
        marks = findViewById(R.id.marks);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", MODE_PRIVATE);
        newMarks = new JSONObject();
        try {
            newMarks = new JSONObject(sharedPreferences.getString("newMarks", "{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (sharedPreferences.getBoolean("filterByCourse", true)) {
            filterByCourse(null);
        } else {
            filterByTitle(null);
        }

        markTitlesContainer = findViewById(R.id.markTitlesContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.marks_menu, menu);

        return true;
    }
}