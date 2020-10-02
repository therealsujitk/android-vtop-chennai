package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    public void openSpotlight(View view) {
        startActivity(new Intent(HomeActivity.this, SpotlightActivity.class));
    }

    public void openMessages(View view) {
        startActivity(new Intent(HomeActivity.this, MessagesActivity.class));
    }

    public void openStaff(View view) {
        startActivity(new Intent(HomeActivity.this, StaffActivity.class));
    }

    public void openFaculty(View view) {
        startActivity(new Intent(HomeActivity.this, FacultyActivity.class));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0f);

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
    }
}