package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        The following functions are to open the activities in the "Appliaction" category
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
        int dayCode = cal.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

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
        } else if (dayCode == 7) {
            day = "sat";
        }

        SQLiteDatabase myDatabase = getApplicationContext().openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");

        Cursor theory = myDatabase.rawQuery("SELECT start_time, end_time FROM timetable_theory", null);
        Cursor lab = myDatabase.rawQuery("SELECT start_time, end_time FROM timetable_lab", null);

        int startTheory = theory.getColumnIndex("start_time");
        int endTheory = theory.getColumnIndex("end_time");

        int startLab = lab.getColumnIndex("start_time");
        int endLab = lab.getColumnIndex("end_time");

        theory.moveToFirst();
        lab.moveToFirst();

        LinearLayout upcoming = findViewById(R.id.upcoming);

        for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
            boolean flag = false;

            String startTimeTheory = theory.getString(startTheory);
            String endTimeTheory = theory.getString(endTheory);
            String startTimeLab = lab.getString(startLab);
            String endTimeLab = lab.getString(endLab);

            Date currentTime = null;
            try {
                currentTime = df.parse(df.format(cal.getTime()));
                if (currentTime.after(df.parse(startTimeTheory)) && currentTime.before(df.parse(endTimeTheory))) {
                    upcoming.removeAllViews();

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    TextView heading = new TextView(this);
                    TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    headingParams.setMarginStart((int) (20 * pixelDensity));
                    headingParams.setMarginEnd((int) (20 * pixelDensity));
                    headingParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    heading.setLayoutParams(headingParams);
                    heading.setText(getString(R.string.ongoing));
                    heading.setTextColor(getColor(R.color.colorPrimary));
                    heading.setTextSize(16);
                    heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    heading.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    upcoming.addView(innerBlock);
                    flag = true;
                }

                if (currentTime.after(df.parse(startTimeLab)) && currentTime.before(df.parse(endTimeLab))) {
                    if (!flag) {
                        upcoming.removeAllViews();
                    }

                    LinearLayout innerBlock = new LinearLayout(this);
                    LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    innerBlock.setLayoutParams(innerBlockParams);
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    TextView heading = new TextView(this);
                    TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    headingParams.setMarginStart((int) (20 * pixelDensity));
                    headingParams.setMarginEnd((int) (20 * pixelDensity));
                    headingParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    heading.setLayoutParams(headingParams);
                    heading.setText(getString(R.string.ongoing));
                    heading.setTextColor(getColor(R.color.colorPrimary));
                    heading.setTextSize(16);
                    heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    heading.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

                    upcoming.addView(innerBlock);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        theory.close();
        lab.close();
        myDatabase.close();
    }
}