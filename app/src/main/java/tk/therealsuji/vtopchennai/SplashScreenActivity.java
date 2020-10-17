package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        String isLoggedIn = sharedPreferences.getString("isLoggedIn", "false");

        assert isLoggedIn != null;
        if (isLoggedIn.equals("true")) {
            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }

        finish();

        /*
            Check for a new version
         */

        if (false) {
            NotificationHelper notificationHelper = new NotificationHelper(this);
            NotificationCompat.Builder n = notificationHelper.notifyApplication("New Release", "A new version of VTOP Chennai has been released");
            notificationHelper.getManager().notify(1, n.build());
        }
    }
}