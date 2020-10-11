package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    public void openAbout(View view) {
        startActivity(new Intent(HomeActivity.this, AboutActivity.class));
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

        /*
            Add code to set upcoming/ongoing class here
         */

        final LinearLayout timetable = findViewById(R.id.timetable);
        timetable.setElevation(12f);
        timetable.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timetable.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        timetable.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout attendance = findViewById(R.id.attendance);
        attendance.setElevation(12f);
        attendance.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        attendance.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        attendance.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout messages = findViewById(R.id.messages);
        messages.setElevation(12f);
        messages.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        messages.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        messages.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout spotlight = findViewById(R.id.spotlight);
        spotlight.setElevation(12f);
        spotlight.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        spotlight.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        spotlight.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout staff = findViewById(R.id.staff);
        staff.setElevation(12f);
        staff.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        staff.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        staff.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout faculty = findViewById(R.id.faculty);
        faculty.setElevation(12f);
        faculty.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        faculty.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        faculty.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout share = findViewById(R.id.share);
        share.setElevation(12f);
        share.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        share.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        share.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout refresh = findViewById(R.id.refresh);
        refresh.setElevation(12f);
        refresh.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        refresh.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        refresh.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout about = findViewById(R.id.about);
        about.setElevation(12f);
        about.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        about.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        about.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout privacy = findViewById(R.id.privacy);
        privacy.setElevation(12f);
        privacy.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        privacy.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        privacy.setElevation(12f);
                        break;
                }
                return false;
            }
        });

        final LinearLayout signOut = findViewById(R.id.signOut);
        signOut.setElevation(12f);
        signOut.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signOut.setElevation(18f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        signOut.setElevation(12f);
                        break;
                }
                return false;
            }
        });
    }
}