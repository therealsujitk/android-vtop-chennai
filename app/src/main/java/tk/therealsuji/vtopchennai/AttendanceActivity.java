package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.ImageViewCompat;

import java.util.Objects;

public class AttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final LinearLayout attendance = findViewById(R.id.attendance);
        final Context context = this;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int screenWidth = displayMetrics.widthPixels;

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
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
                    final LinearLayout block = new LinearLayout(context);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    if (i == 0) {
                        findViewById(R.id.noData).setVisibility(View.GONE);
                    }
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
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    /*
                        The course code TextView
                     */
                    TextView course = new TextView(context);
                    TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    courseParams.setMarginStart((int) (20 * pixelDensity));
                    courseParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    course.setLayoutParams(courseParams);
                    course.setText(c.getString(courseIndex));
                    course.setTextColor(getColor(R.color.colorPrimary));
                    course.setTextSize(20);
                    course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(course); //Adding course code to innerBlock

                    /*
                        The attendance percentage TextView
                     */
                    String percentage = c.getString(percentIndex);
                    String percentString = percentage + "%";

                    TextView percent = new TextView(context);
                    TableRow.LayoutParams percentParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    percentParams.setMarginEnd((int) (20 * pixelDensity));
                    percentParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    percent.setLayoutParams(percentParams);
                    percent.setText(percentString);
                    percent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    percent.setTextColor(getColor(R.color.colorPrimary));
                    percent.setTextSize(20);
                    percent.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(percent); //Adding percentage to innerBlock

                    LinearLayout secondInnerBlock = new LinearLayout(context);
                    secondInnerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    secondInnerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    /*
                        The course type code TextView
                     */
                    TextView type = new TextView(context);
                    TableRow.LayoutParams typeParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    typeParams.setMarginStart((int) (20 * pixelDensity));
                    typeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    type.setLayoutParams(typeParams);
                    type.setText(c.getString(typeIndex));
                    type.setTextColor(getColor(R.color.colorPrimary));
                    type.setTextSize(16);
                    type.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    secondInnerBlock.addView(type);   //Adding the course type to innerBlock

                    /*
                        The attended classes TextView
                     */
                    String attendedString = c.getString(attendedIndex) + " | " + c.getString(totalIndex);

                    TextView attended = new TextView(context);
                    TableRow.LayoutParams attendedParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    attendedParams.setMarginEnd((int) (20 * pixelDensity));
                    attendedParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    attended.setLayoutParams(attendedParams);
                    attended.setText(attendedString);
                    attended.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    attended.setTextColor(getColor(R.color.colorPrimary));
                    attended.setTextSize(16);
                    attended.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    secondInnerBlock.addView(attended);   //Adding attended classes to innerBlock

                    block.addView(innerBlock);   //Adding innerBlock to block
                    block.addView(secondInnerBlock);    //Adding secondInnerBlock to block

                    /*
                        Finally adding the block to the activity
                     */
                    if (Integer.parseInt(percentage) <= 75) {
                        final RelativeLayout container = new RelativeLayout(context);
                        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );
                        container.setLayoutParams(containerParams);

                        container.addView(block);

                        int marginStart = (int) (screenWidth - 30 * pixelDensity);

                        final ImageView notification = new ImageView(context);
                        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        notificationParams.setMarginStart(marginStart);
                        notificationParams.setMargins(0, (int) (5 * pixelDensity), 0, 0);
                        notification.setLayoutParams(notificationParams);
                        notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                        ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorRedTransparent)));
                        notification.setScaleX(0);
                        notification.setScaleY(0);

                        container.addView(notification);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendance.addView(container);
                                notification.animate().scaleX(1).scaleY(1);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendance.addView(block);
                            }
                        });
                    }

                    c.moveToNext();
                }

                c.close();
                myDatabase.close();
            }
        }).start();
    }
}