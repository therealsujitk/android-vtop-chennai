package tk.therealsuji.vtopchennai.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.fragments.AssignmentsFragment;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.PerformanceFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment homeFragment, performanceFragment, assignmentsFragment, profileFragment;

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

        this.bottomNavigationView = findViewById(R.id.bottom_navigation);
        this.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.item_performance) {
                if (this.performanceFragment == null) {
                    this.performanceFragment = new PerformanceFragment();
                }

                selectedFragment = this.performanceFragment;
            } else if (item.getItemId() == R.id.item_assignments) {
                if (this.assignmentsFragment == null) {
                    this.assignmentsFragment = new AssignmentsFragment();
                }

                selectedFragment = this.assignmentsFragment;
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
                    .replace(R.id.frame_layout_fragment_container, selectedFragment)
                    .commit();

            return true;
        });

        this.bottomNavigationView.setSelectedItemId(R.id.item_home);
    }
}