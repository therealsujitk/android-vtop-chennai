package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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
    }
}