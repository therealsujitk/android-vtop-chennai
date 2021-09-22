package tk.therealsuji.vtopchennai;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tk.therealsuji.vtopchennai.fragments.CoursesFragment;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.PerformanceFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    Fragment homeFragment, performanceFragment, coursesFragment, profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.item_performance) {
                if (performanceFragment == null) {
                    performanceFragment = new PerformanceFragment();
                }

                selectedFragment = performanceFragment;
            } else if (item.getItemId() == R.id.item_courses) {
                if (coursesFragment == null) {
                    coursesFragment = new CoursesFragment();
                }

                selectedFragment = coursesFragment;
            } else if (item.getItemId() == R.id.item_profile) {
                if (profileFragment == null) {
                    profileFragment = new ProfileFragment();
                }

                selectedFragment = profileFragment;
            } else {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }

                selectedFragment = homeFragment;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_body, selectedFragment)
                    .commit();

            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.item_home);
    }
}