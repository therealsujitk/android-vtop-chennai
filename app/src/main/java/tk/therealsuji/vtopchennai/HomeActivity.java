package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0f);

        final Button upcoming = findViewById(R.id.upcoming);
        upcoming.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        upcoming.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        upcoming.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                        upcoming.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        upcoming.setAlpha(1f);
                        break;
                }
                return false;
            }
        });

        final Button messages = findViewById(R.id.messages);
        messages.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        messages.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        messages.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                        messages.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        messages.setAlpha(1f);
                        break;
                }
                return false;
            }
        });

        final Button staff = findViewById(R.id.staff);
        staff.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        staff.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        staff.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                        staff.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        staff.setAlpha(1f);
                        break;
                }
                return false;
            }
        });

        final Button faculty = findViewById(R.id.faculty);
        faculty.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        faculty.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        faculty.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                        faculty.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        faculty.setAlpha(1f);
                        break;
                }
                return false;
            }
        });
    }
}