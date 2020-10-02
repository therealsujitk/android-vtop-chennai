package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    public void signIn(View view) {
        Toast.makeText(this, "This should sign you in.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
    }

    public void openPrivacy(View view) {
        startActivity(new Intent(LoginActivity.this, PrivacyActivity.class));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username = findViewById(R.id.username);
        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    username.animate().scaleX(1.07f).scaleY(1.07f).setDuration(200);
                } else {
                    username.animate().scaleX(1f).scaleY(1f).setDuration(200);
                }
            }
        });

        final EditText password = findViewById(R.id.password);
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    password.animate().scaleX(1.07f).scaleY(1.07f).setDuration(200);
                } else {
                    password.animate().scaleX(1f).scaleY(1f).setDuration(200);
                }
            }
        });

        final Button signIn = findViewById(R.id.signIn);
        signIn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signIn.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        signIn.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        signIn.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        signIn.setAlpha(1f);
                        break;
                }
                return false;
            }
        });
    }
}