package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0f);

        final LinearLayout spotlight = findViewById(R.id.spotlight);
        spotlight.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        spotlight.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, view.getResources().getDisplayMetrics()));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        spotlight.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, view.getResources().getDisplayMetrics()));
                        break;
                }
                return false;
            }
        });

        final LinearLayout messages = findViewById(R.id.messages);
        messages.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        messages.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, view.getResources().getDisplayMetrics()));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        messages.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, view.getResources().getDisplayMetrics()));
                        break;
                }
                return false;
            }
        });

        final LinearLayout staff = findViewById(R.id.staff);
        staff.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        staff.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, view.getResources().getDisplayMetrics()));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        staff.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, view.getResources().getDisplayMetrics()));
                        break;
                }
                return false;
            }
        });

        final LinearLayout faculty = findViewById(R.id.faculty);
        faculty.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        faculty.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, view.getResources().getDisplayMetrics()));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        faculty.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, view.getResources().getDisplayMetrics()));
                        break;
                }
                return false;
            }
        });
    }
}