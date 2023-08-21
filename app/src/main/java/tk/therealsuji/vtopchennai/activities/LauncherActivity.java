package tk.therealsuji.vtopchennai.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

/**
 * A trampoline activity to navigate the user to the right screen
 */
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

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
