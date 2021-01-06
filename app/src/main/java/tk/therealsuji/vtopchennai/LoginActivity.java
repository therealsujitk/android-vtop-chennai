package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences, encryptedSharedPreferences;

    public void signIn(View view) {
        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        encryptedSharedPreferences.edit().putString("username", username).apply();
        encryptedSharedPreferences.edit().putString("password", password).apply();

        startActivity(new Intent(LoginActivity.this, DownloadActivity.class));

        /*
            Remove any non-encrypted credentials
         */
        sharedPreferences.edit().remove("username").apply();
        sharedPreferences.edit().remove("password").apply();
    }

    public void openPrivacy(View view) {
        startActivity(new Intent(LoginActivity.this, PrivacyActivity.class));
    }

    public void openUpdate(View view) {
        WebView webView = new WebView(this);
        webView.loadUrl("http://vtopchennai.therealsuji.tk");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

        try {
            MasterKey masterKey = new MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    "CREDENTIALS",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            Check for a new version
         */
        int versionCode = BuildConfig.VERSION_CODE;
        int latestVersion = sharedPreferences.getInt("latest", versionCode);

        if (versionCode < latestVersion) {
            Dialog update = new Dialog(this);
            update.setContentView(R.layout.dialog_update);
            update.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            update.show();
        }
    }
}