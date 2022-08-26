package com.ashish.vtopchennai.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

import com.ashish.vtopchennai.helpers.SettingsRepository;

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

        if (SettingsRepository.isSignedIn(this.getApplicationContext())) {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        } else {
            SettingsRepository.signOut(this.getApplicationContext());   // Delete old data
            startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
        }

        finish();
    }
}
