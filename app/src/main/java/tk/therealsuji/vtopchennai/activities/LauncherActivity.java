package tk.therealsuji.vtopchennai.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

/**
 * A trampoline activity to navigate the user to the right screen
 */
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        int theme = SettingsRepository.getTheme(this.getApplicationContext());
        if (theme == SettingsRepository.THEME_DAY) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == SettingsRepository.THEME_NIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        if (this.getIntent().getExtras() != null) {
            intent.putExtras(this.getIntent().getExtras());
        }

        if (SettingsRepository.isSignedIn(this.getApplicationContext())) {
            intent.setClass(LauncherActivity.this, MainActivity.class);
        } else {
            SettingsRepository.signOut(this.getApplicationContext());   // Delete old data
            intent.setClass(LauncherActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
