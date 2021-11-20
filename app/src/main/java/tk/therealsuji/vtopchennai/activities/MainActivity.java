package tk.therealsuji.vtopchennai.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

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

    public int getSystemNavigationPadding() {
        int systemNavigationHeight = this.getWindow().getDecorView().getRootWindowInsets().getSystemWindowInsetBottom();
        int extraPadding = (int) (20 * this.getResources().getDisplayMetrics().density);

        return systemNavigationHeight + extraPadding;
    }

    public int getBottomNavigationHeight() {
        return this.bottomNavigationView.getMeasuredHeight();
    }

    public void hideBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.animate().translationY(bottomNavigationView.getMeasuredHeight());

        int gestureLeft = 0;

        if (Build.VERSION.SDK_INT >= 29) {
            gestureLeft = this.getWindow().getDecorView().getRootWindowInsets().getSystemGestureInsets().left;
        }

        if (gestureLeft == 0) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    public void showBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.animate().translationY(0);

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
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