package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
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
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.ImageViewCompat;

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
        marks.animate().alpha(1);

        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        buttons.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));
        marks.addView(markViews.get(index));

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

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
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
                    final LinearLayout markView = new LinearLayout(context);
                    LinearLayout.LayoutParams markViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    markView.setLayoutParams(markViewParams);
                    markView.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
                    markView.setOrientation(LinearLayout.VERTICAL);

                    markViews.add(markView);    //Storing the view

                    Cursor s = myDatabase.rawQuery("SELECT * FROM marks WHERE course = '" + course + "' ORDER BY type DESC, title", null);

                    int idIndex = s.getColumnIndex("id");
                    int titleIndex = s.getColumnIndex("title");
                    int typeIndex = s.getColumnIndex("type");
                    int scoreIndex = s.getColumnIndex("score");
                    int statusIndex = s.getColumnIndex("status");
                    int weightageIndex = s.getColumnIndex("weightage");
                    int averageIndex = s.getColumnIndex("average");

                    int[] indexes = {typeIndex, scoreIndex, weightageIndex, averageIndex, statusIndex};
                    String[] titles = {getString(R.string.type), getString(R.string.score), getString(R.string.weightage), getString(R.string.average), getString(R.string.status)};

                    s.moveToFirst();

                    final ArrayList<String> readMarks = new ArrayList<>();

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
                            The mark title TextView
                         */
                        TextView markTitle = new TextView(context);
                        TableRow.LayoutParams markTitleParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        markTitleParams.setMarginStart((int) (20 * pixelDensity));
                        markTitleParams.setMarginEnd((int) (20 * pixelDensity));
                        markTitleParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                        markTitle.setLayoutParams(markTitleParams);
                        markTitle.setText(s.getString(titleIndex));
                        markTitle.setTextColor(getColor(R.color.colorPrimary));
                        markTitle.setTextSize(20);
                        markTitle.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(markTitle);  //Adding the title to block

                        for (int k = 0; k < 5; ++k) {
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
                        String id = s.getString(idIndex);
                        if (newMarks.has(id)) {
                            RelativeLayout container = new RelativeLayout(context);
                            RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                            );
                            container.setLayoutParams(containerParams);

                            container.addView(block);

                            int marginStart = (int) (screenWidth - 30 * pixelDensity);

                            ImageView notification = new ImageView(context);
                            LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            notificationParams.setMarginStart(marginStart);
                            notificationParams.setMargins(0, (int) (5 * pixelDensity), 0, 0);
                            notification.setLayoutParams(notificationParams);
                            notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                            ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                            notification.setScaleX(0);
                            notification.setScaleY(0);

                            notification.animate().scaleX(1).scaleY(1);

                            container.addView(notification);

                            markView.addView(container);

                            readMarks.add(id);
                        } else {
                            markView.addView(block);
                        }
                    }

                    /*
                        Creating the course button
                     */
                    final TextView markButton = new TextView(context);
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) (25 * pixelDensity)
                    );
                    buttonParams.setMarginStart((int) (5 * pixelDensity));
                    buttonParams.setMarginEnd((int) (5 * pixelDensity));
                    buttonParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    markButton.setLayoutParams(buttonParams);
                    markButton.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), 0);
                    if (i == 0) {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                        index = 0;

                        for (int j = 0; j < readMarks.size(); ++j) {
                            String id = readMarks.get(j);
                            if (newMarks.has(id)) {
                                newMarks.remove(id);
                            }
                        }

                        sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();
                    } else {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
                    }
                    markButton.setTag(i);
                    markButton.setText(course);
                    markButton.setTextColor(getColor(R.color.colorPrimary));
                    markButton.setTextSize(12);
                    markButton.setGravity(Gravity.CENTER_VERTICAL);
                    markButton.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
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
                        Finally adding the button to the HorizontalScrollView
                     */
                    if (readMarks.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markButtons.addView(markButton);
                            }
                        });
                    } else {
                        final RelativeLayout container = new RelativeLayout(context);
                        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );
                        container.setLayoutParams(containerParams);

                        container.addView(markButton);

                        final ImageView notification = new ImageView(context);
                        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        notificationParams.setMarginStart((int) (3 * pixelDensity));
                        notificationParams.setMargins(0, (int) (20 * pixelDensity), 0, 0);
                        notification.setLayoutParams(notificationParams);
                        notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                        ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                        notification.setScaleX(0);
                        notification.setScaleY(0);

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

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
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
                    final LinearLayout markView = new LinearLayout(context);
                    LinearLayout.LayoutParams markViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    markView.setLayoutParams(markViewParams);
                    markView.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
                    markView.setOrientation(LinearLayout.VERTICAL);

                    markViews.add(markView);    //Storing the view

                    Cursor s = myDatabase.rawQuery("SELECT * FROM marks WHERE title = '" + markTitle + "' ORDER BY course, type DESC", null);

                    int idIndex = s.getColumnIndex("id");
                    int courseIndex = s.getColumnIndex("course");
                    int typeIndex = s.getColumnIndex("type");
                    int scoreIndex = s.getColumnIndex("score");
                    int statusIndex = s.getColumnIndex("status");
                    int weightageIndex = s.getColumnIndex("weightage");
                    int averageIndex = s.getColumnIndex("average");

                    int[] indexes = {typeIndex, scoreIndex, weightageIndex, averageIndex, statusIndex};
                    String[] titles = {getString(R.string.type), getString(R.string.score), getString(R.string.weightage), getString(R.string.average), getString(R.string.status)};

                    s.moveToFirst();

                    final ArrayList<String> readMarks = new ArrayList<>();

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
                                TableRow.LayoutParams.WRAP_CONTENT,
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

                        for (int k = 0; k < 5; ++k) {
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
                        String id = s.getString(idIndex);
                        if (newMarks.has(id)) {
                            RelativeLayout container = new RelativeLayout(context);
                            RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                            );
                            container.setLayoutParams(containerParams);

                            container.addView(block);

                            int marginStart = (int) (screenWidth - 30 * pixelDensity);

                            ImageView notification = new ImageView(context);
                            LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            notificationParams.setMarginStart(marginStart);
                            notificationParams.setMargins(0, (int) (5 * pixelDensity), 0, 0);
                            notification.setLayoutParams(notificationParams);
                            notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                            ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                            notification.setScaleX(0);
                            notification.setScaleY(0);

                            notification.animate().scaleX(1).scaleY(1);

                            container.addView(notification);

                            markView.addView(container);

                            readMarks.add(id);
                        } else {
                            markView.addView(block);
                        }
                    }

                    /*
                        Creating the markTitle button
                     */
                    final TextView markButton = new TextView(context);
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) (25 * pixelDensity)
                    );
                    buttonParams.setMarginStart((int) (5 * pixelDensity));
                    buttonParams.setMarginEnd((int) (5 * pixelDensity));
                    buttonParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    markButton.setLayoutParams(buttonParams);
                    markButton.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), 0);
                    if (i == 0) {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                        index = 0;

                        for (int j = 0; j < readMarks.size(); ++j) {
                            String id = readMarks.get(j);
                            if (newMarks.has(id)) {
                                newMarks.remove(id);
                            }
                        }

                        sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();
                    } else {
                        markButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
                    }
                    markButton.setTag(i);
                    markButton.setText(markTitle);
                    markButton.setTextColor(getColor(R.color.colorPrimary));
                    markButton.setTextSize(12);
                    markButton.setGravity(Gravity.CENTER_VERTICAL);
                    markButton.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
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
                        Finally adding the button to the HorizontalScrollView
                     */
                    if (readMarks.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markButtons.addView(markButton);
                            }
                        });
                    } else {
                        final RelativeLayout container = new RelativeLayout(context);
                        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );
                        container.setLayoutParams(containerParams);

                        container.addView(markButton);

                        final ImageView notification = new ImageView(context);
                        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        notificationParams.setMarginStart((int) (3 * pixelDensity));
                        notificationParams.setMargins(0, (int) (20 * pixelDensity), 0, 0);
                        notification.setLayoutParams(notificationParams);
                        notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                        ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                        notification.setScaleX(0);
                        notification.setScaleY(0);

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