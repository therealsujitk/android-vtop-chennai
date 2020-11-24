package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    public void signIn(View view) {
        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        sharedPreferences.edit().putString("username", username).apply();
        sharedPreferences.edit().putString("password", password).apply();

        startActivity(new Intent(LoginActivity.this, DownloadActivity.class));
    }

    public void openPrivacy(View view) {
        startActivity(new Intent(LoginActivity.this, PrivacyActivity.class));
    }

    private void setNight() {
        getWindow().setBackgroundDrawableResource(R.color.colorDark);

        findViewById(R.id.username).setBackground(ContextCompat.getDrawable(this, R.drawable.text_field_primary_night));
        findViewById(R.id.password).setBackground(ContextCompat.getDrawable(this, R.drawable.text_field_primary_night));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

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

        /*
            Set appearance
         */
        String appearance = sharedPreferences.getString("appearance", "system");

        if (appearance.equals("night")) {
            setNight();
        } else if (appearance.equals("system")) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setNight();
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    break;
            }
        }
    }
}