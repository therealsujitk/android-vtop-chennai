package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tk.therealsuji.vtopchennai.fragments.CoursesFragment;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.PerformanceFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment homeFragment, performanceFragment, coursesFragment, profileFragment;

    public void hideBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.animate().translationY(bottomNavigationView.getMeasuredHeight());
    }

    public void showBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.animate().translationY(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        String appearance = sharedPreferences.getString("appearance", "system");
        int visibility = getWindow().getDecorView().getSystemUiVisibility();

        if (appearance.equals("light") || (appearance.equals("system") && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO)) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(visibility);

        this.bottomNavigationView = findViewById(R.id.bottom_navigation);

        this.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.item_performance) {
                if (this.performanceFragment == null) {
                    this.performanceFragment = new PerformanceFragment();
                }

                selectedFragment = this.performanceFragment;
            } else if (item.getItemId() == R.id.item_courses) {
                if (this.coursesFragment == null) {
                    this.coursesFragment = new CoursesFragment();
                }

                selectedFragment = this.coursesFragment;
            } else if (item.getItemId() == R.id.item_profile) {
                if (this.profileFragment == null) {
                    this.profileFragment = new ProfileFragment();
                }

                selectedFragment = this.profileFragment;
            } else {
                if (this.homeFragment == null) {
                    this.homeFragment = new HomeFragment();
                }

                selectedFragment = this.homeFragment;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_body, selectedFragment)
                    .commit();

            return true;
        });

        this.bottomNavigationView.setSelectedItemId(R.id.item_home);
    }
}