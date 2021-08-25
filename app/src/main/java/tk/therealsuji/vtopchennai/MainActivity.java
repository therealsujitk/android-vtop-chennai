package tk.therealsuji.vtopchennai;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tk.therealsuji.vtopchennai.fragments.AcademicsFragment;
import tk.therealsuji.vtopchennai.fragments.CoursesFragment;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    Fragment homeFragment, academicsFragment, coursesFragment, profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        float pixelDensity = this.getResources().getDisplayMetrics().density;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(
                    (int) (20 * pixelDensity),
                    (int) (20 * pixelDensity),
                    (int) (20 * pixelDensity),
                    (int) (20 * pixelDensity + windowInsets.getSystemWindowInsetBottom())
            );
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.item_academics) {
                if (academicsFragment == null) {
                    academicsFragment = new AcademicsFragment();
                }

                selectedFragment = academicsFragment;
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