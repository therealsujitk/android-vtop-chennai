package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.Objects;

public class DownloadActivity extends AppCompatActivity {
    VTOP vtop;
    SharedPreferences sharedPreferences, encryptedSharedPreferences;

    public void signIn(View view) {
        String username = encryptedSharedPreferences.getString("username", null);
        String password = encryptedSharedPreferences.getString("password", null);

        /*
            If the credentials aren't encrypted
         */
        if (username == null) {
            /*
                Get the non-encrypted credentials
             */
            username = sharedPreferences.getString("username", null);
            password = sharedPreferences.getString("password", null);

            /*
                Encrypt them
             */
            encryptedSharedPreferences.edit().putString("username", username).apply();
            encryptedSharedPreferences.edit().putString("password", password).apply();

            /*
                Remove the non-encrypted credentials
             */
            sharedPreferences.edit().remove("username").apply();
            sharedPreferences.edit().remove("password").apply();
        }

        EditText captchaView = findViewById(R.id.captcha);
        String captcha = captchaView.getText().toString();
        vtop.signIn(username, password, captcha);

        LinearLayout captchaLayout = findViewById(R.id.captchaLayout);
        LinearLayout loadingLayout = findViewById(R.id.loadingLayout);
        captchaLayout.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        captchaView.setText("");
    }

    public void downloadTimetable(View view) {
        Spinner selectSemester = findViewById(R.id.selectSemester);
        String semester = selectSemester.getSelectedItem().toString();

        if (!sharedPreferences.getString("semester", "null").equals(semester.toLowerCase())) {
            sharedPreferences.edit().putBoolean("newTimetable", true).apply();
            sharedPreferences.edit().putBoolean("newFaculty", true).apply();
        }

        sharedPreferences.edit().putString("semester", semester.toLowerCase()).apply();

        vtop.selectTimetable();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

                try {
                    MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build();

                    encryptedSharedPreferences = EncryptedSharedPreferences.create(
                            context,
                            "credentials",
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        findViewById(R.id.loading).post(new Runnable() {
            @Override
            public void run() {
                vtop = new VTOP(context);
            }
        });

        /*
            Animating the buttons
         */
        final Button submit = findViewById(R.id.submit);
        submit.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        submit.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        submit.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        submit.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        submit.setAlpha(1f);
                        break;
                }
                return false;
            }
        });

        final Button select = findViewById(R.id.select);
        select.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        select.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        select.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        select.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        select.setAlpha(1f);
                        break;
                }
                return false;
            }
        });
    }
}