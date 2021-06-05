package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
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
            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }

        /*
            Get the latest version code
         */
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL("https://vtopchennai.therealsuji.tk/about.json");
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
                int latest = about.getInt("version-code");
                sharedPreferences.edit().putInt("latest", latest).apply();
            } catch (Exception ignored) {
            }
        }).start();

        finish();
    }
}