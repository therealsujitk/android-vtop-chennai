package tk.therealsuji.vtopchennai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        final SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(this.getApplicationContext());
        String theme = sharedPreferences.getString("appearance", "system");

        if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        super.onCreate(savedInstanceState);

        boolean isSignedIn = sharedPreferences.getBoolean("isSignedIn", false);

        if (isSignedIn) {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
        }

        /*
            Get the latest version code
         */
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(SettingsRepository.APP_ABOUT_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    sb.append(current);
                    data = reader.read();
                }

                String result = sb.toString();
                JSONObject about = new JSONObject(result);
                sharedPreferences.edit().putInt("latest", about.getInt("versionCode")).apply();
            } catch (Exception ignored) {
            }
        }).start();

        finish();
    }
}