package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    /*
        The following functions are to open the activities in the "Classes" category
     */

    public void openTimetable(View view) {
        startActivity(new Intent(HomeActivity.this, TimetableActivity.class));
    }

    public void openAttendance(View view) {
        startActivity(new Intent(HomeActivity.this, AttendanceActivity.class));
    }

    public void openMessages(View view) {
        startActivity(new Intent(HomeActivity.this, MessagesActivity.class));
    }

    /*
        The following functions are to open the activities in the "Academics" category
     */

    public void openSpotlight(View view) {
        startActivity(new Intent(HomeActivity.this, SpotlightActivity.class));
    }

    public void openStaff(View view) {
        startActivity(new Intent(HomeActivity.this, StaffActivity.class));
    }

    public void openFaculty(View view) {
        startActivity(new Intent(HomeActivity.this, FacultyActivity.class));
    }

    /*
        The following functions are to open the activities in the "Application" category
     */

    public void share(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Get the VTOP Chennai android app");
        intent.putExtra(Intent.EXTRA_TEXT, "https://vtopchennai.therealsuji.tk");
        intent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(intent, "Share \"VTOP Chennai\" via");
        startActivity(shareIntent);
    }

    public void openDownload(View view) {
        startActivity(new Intent(HomeActivity.this, DownloadActivity.class));
    }

    public void openTheme(View view) {

    }

    public void openNotifications(View view) {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

        startActivity(intent);
    }

    public void openPrivacy(View view) {
        startActivity(new Intent(HomeActivity.this, PrivacyActivity.class));
    }

    public void signOut(View view) {
        sharedPreferences.edit().putString("isLoggedIn", "false").apply();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

        String name = sharedPreferences.getString("name", getString(R.string.name));
        String id = sharedPreferences.getString("id", getString(R.string.id));
        String credits = sharedPreferences.getString("credits", getString(R.string.credits));

        TextView nameView = findViewById(R.id.name);
        TextView idView = findViewById(R.id.id);
        TextView creditsView = findViewById(R.id.credits);

        nameView.setText(name);
        idView.setText(id);
        creditsView.setText(credits);

        final float pixelDensity = this.getResources().getDisplayMetrics().density;

        Calendar cal = Calendar.getInstance();
        Calendar calFuture = Calendar.getInstance();
        calFuture.add(Calendar.MINUTE, 30);
        int dayCode = cal.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        String day;

        if (dayCode == 1) {
            day = "sun";
        } else if (dayCode == 2) {
            day = "mon";
        } else if (dayCode == 3) {
            day = "tue";
        } else if (dayCode == 4) {
            day = "wed";
        } else if (dayCode == 5) {
            day = "thu";
        } else if (dayCode == 6) {
            day = "fri";
        } else {
            day = "sat";
        }

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");

        Cursor theory = myDatabase.rawQuery("SELECT start_time, end_time, " + day + " FROM timetable_theory", null);
        Cursor lab = myDatabase.rawQuery("SELECT start_time, end_time, " + day + " FROM timetable_lab", null);

        int startTheory = theory.getColumnIndex("start_time");
        int endTheory = theory.getColumnIndex("end_time");
        int dayTheory = theory.getColumnIndex(day);

        int startLab = lab.getColumnIndex("start_time");
        int endLab = lab.getColumnIndex("end_time");
        int dayLab = lab.getColumnIndex(day);

        theory.moveToFirst();
        lab.moveToFirst();

        LinearLayout upcoming = findViewById(R.id.upcoming);

        boolean flag = false;

        for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
            String startTimeTheory = theory.getString(startTheory);
            String endTimeTheory = theory.getString(endTheory);
            String startTimeLab = lab.getString(startLab);
            String endTimeLab = lab.getString(endLab);

            try {
                Date currentTime = df.parse(df.format(cal.getTime()));
                Date futureTime = df.parse(df.format(calFuture.getTime()));

                assert currentTime != null;
                assert futureTime != null;

                if ((futureTime.after(df.parse(startTimeTheory)) || futureTime.equals(df.parse(startTimeTheory))) && currentTime.before(df.parse(startTimeTheory)) && !theory.getString(dayTheory).equals("null")) {
                    if (!flag) {
                        upcoming.removeAllViews();
                    }

                    TextView heading = new TextView(this);
                    TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    headingParams.setMarginStart((int) (20 * pixelDensity));
                    headingParams.setMarginEnd((int) (20 * pixelDensity));
                    headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                    heading.setLayoutParams(headingParams);
                    heading.setText(getString(R.string.upcoming));
                    heading.setTextColor(getColor(R.color.colorPrimary));
                    heading.setTextSize(20);
                    heading.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    upcoming.addView(heading);

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    String course = theory.getString(dayTheory).split("-")[1].trim() + " - Theory";

                    TextView period = new TextView(this);
                    TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    periodParams.setMarginStart((int) (20 * pixelDensity));
                    periodParams.setMarginEnd((int) (20 * pixelDensity));
                    periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    period.setLayoutParams(periodParams);
                    period.setText(course);
                    period.setTextColor(getColor(R.color.colorPrimary));
                    period.setTextSize(16);
                    period.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(period);

                    String timings = startTimeTheory + " - " + endTimeTheory;

                    TextView timing = new TextView(this);
                    TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    timingParams.setMarginStart((int) (20 * pixelDensity));
                    timingParams.setMarginEnd((int) (20 * pixelDensity));
                    timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    timing.setLayoutParams(timingParams);
                    timing.setText(timings);
                    timing.setTextColor(getColor(R.color.colorPrimary));
                    timing.setTextSize(16);
                    timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    timing.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(timing);

                    upcoming.addView(innerBlock);
                    flag = true;
                }

                if ((futureTime.after(df.parse(startTimeLab)) || futureTime.equals(df.parse(startTimeLab))) && currentTime.before(df.parse(startTimeLab)) && !lab.getString(dayLab).equals("null")) {
                    if (!flag) {
                        upcoming.removeAllViews();
                    }

                    TextView heading = new TextView(this);
                    TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    headingParams.setMarginStart((int) (20 * pixelDensity));
                    headingParams.setMarginEnd((int) (20 * pixelDensity));
                    headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                    heading.setLayoutParams(headingParams);
                    heading.setText(getString(R.string.upcoming));
                    heading.setTextColor(getColor(R.color.colorPrimary));
                    heading.setTextSize(20);
                    heading.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    upcoming.addView(heading);

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    String course = lab.getString(dayLab).split("-")[1].trim() + " - Lab";

                    TextView period = new TextView(this);
                    TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    periodParams.setMarginStart((int) (20 * pixelDensity));
                    periodParams.setMarginEnd((int) (20 * pixelDensity));
                    periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    period.setLayoutParams(periodParams);
                    period.setText(course);
                    period.setTextColor(getColor(R.color.colorPrimary));
                    period.setTextSize(16);
                    period.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(period);

                    String timings = startTimeLab + " - " + endTimeLab;

                    TextView timing = new TextView(this);
                    TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    timingParams.setMarginStart((int) (20 * pixelDensity));
                    timingParams.setMarginEnd((int) (20 * pixelDensity));
                    timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    timing.setLayoutParams(timingParams);
                    timing.setText(timings);
                    timing.setTextColor(getColor(R.color.colorPrimary));
                    timing.setTextSize(16);
                    timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    timing.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(timing);

                    upcoming.addView(innerBlock);
                    flag = true;
                }

                if (flag) {
                    break;
                }

                if ((currentTime.after(df.parse(startTimeTheory)) || currentTime.equals(df.parse(startTimeTheory))) && (currentTime.before(df.parse(endTimeTheory)) || currentTime.equals(df.parse(endTimeTheory))) && !theory.getString(dayTheory).equals("null")) {
                    upcoming.removeAllViews();

                    TextView heading = new TextView(this);
                    TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    headingParams.setMarginStart((int) (20 * pixelDensity));
                    headingParams.setMarginEnd((int) (20 * pixelDensity));
                    headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                    heading.setLayoutParams(headingParams);
                    heading.setText(getString(R.string.ongoing));
                    heading.setTextColor(getColor(R.color.colorPrimary));
                    heading.setTextSize(20);
                    heading.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    upcoming.addView(heading);

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    String course = theory.getString(dayTheory).split("-")[1].trim() + " - Theory";
                    TextView period = new TextView(this);
                    TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    periodParams.setMarginStart((int) (20 * pixelDensity));
                    periodParams.setMarginEnd((int) (20 * pixelDensity));
                    periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    period.setLayoutParams(periodParams);
                    period.setText(course);
                    period.setTextColor(getColor(R.color.colorPrimary));
                    period.setTextSize(16);
                    period.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(period);

                    String timings = startTimeTheory + " - " + endTimeTheory;

                    TextView timing = new TextView(this);
                    TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    timingParams.setMarginStart((int) (20 * pixelDensity));
                    timingParams.setMarginEnd((int) (20 * pixelDensity));
                    timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    timing.setLayoutParams(timingParams);
                    timing.setText(timings);
                    timing.setTextColor(getColor(R.color.colorPrimary));
                    timing.setTextSize(16);
                    timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    timing.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(timing);

                    upcoming.addView(innerBlock);
                    flag = true;
                }

                if ((currentTime.after(df.parse(startTimeLab)) || currentTime.equals(df.parse(startTimeLab))) && (currentTime.before(df.parse(endTimeLab)) || currentTime.equals(df.parse(endTimeLab))) && !lab.getString(dayLab).equals("null")) {
                    if (!flag) {
                        upcoming.removeAllViews();
                    }

                    TextView heading = new TextView(this);
                    TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    headingParams.setMarginStart((int) (20 * pixelDensity));
                    headingParams.setMarginEnd((int) (20 * pixelDensity));
                    headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                    heading.setLayoutParams(headingParams);
                    heading.setText(getString(R.string.ongoing));
                    heading.setTextColor(getColor(R.color.colorPrimary));
                    heading.setTextSize(20);
                    heading.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    upcoming.addView(heading);

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    String course = lab.getString(dayLab).split("-")[1].trim() + " - Lab";
                    TextView period = new TextView(this);
                    TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    periodParams.setMarginStart((int) (20 * pixelDensity));
                    periodParams.setMarginEnd((int) (20 * pixelDensity));
                    periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    period.setLayoutParams(periodParams);
                    period.setText(course);
                    period.setTextColor(getColor(R.color.colorPrimary));
                    period.setTextSize(16);
                    period.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(period);

                    String timings = startTimeLab + " - " + endTimeLab;

                    TextView timing = new TextView(this);
                    TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    timingParams.setMarginStart((int) (20 * pixelDensity));
                    timingParams.setMarginEnd((int) (20 * pixelDensity));
                    timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    timing.setLayoutParams(timingParams);
                    timing.setText(timings);
                    timing.setTextColor(getColor(R.color.colorPrimary));
                    timing.setTextSize(16);
                    timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    timing.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

                    innerBlock.addView(timing);

                    upcoming.addView(innerBlock);
                    flag = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        theory.close();
        lab.close();
        myDatabase.close();

        String refreshed = sharedPreferences.getString("refreshed", getString(R.string.refreshed));
        TextView refreshedView = findViewById(R.id.refreshed);
        refreshedView.setText(refreshed);
    }
}