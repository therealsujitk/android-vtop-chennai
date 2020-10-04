package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

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
        Toast.makeText(this, "This will be replaced with a sharing feature", Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
    }
}