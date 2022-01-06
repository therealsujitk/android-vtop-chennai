package tk.therealsuji.vtopchennai.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.TimetableAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.widgets.InfoCard;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View homeFragment = inflater.inflate(R.layout.fragment_home, container, false);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        AppBarLayout appBarLayout = homeFragment.findViewById(R.id.app_bar);
        ViewPager2 timetable = homeFragment.findViewById(R.id.view_pager_timetable);

        appBarLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPadding(
                    windowInsets.getSystemWindowInsetLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    windowInsets.getSystemWindowInsetRight(),
                    0
            );

            return windowInsets;
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            LinearLayout header = homeFragment.findViewById(R.id.linear_layout_header);

            float alpha = 1 - ((float) (-1 * verticalOffset) / header.getHeight());
            header.setAlpha(alpha);
        });

        timetable.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPadding(
                    windowInsets.getSystemWindowInsetLeft(),
                    0,
                    windowInsets.getSystemWindowInsetRight(),
                    0
            );

            return windowInsets;
        });

        SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(this.requireContext());

        try {
            TextView greeting = homeFragment.findViewById(R.id.text_view_greeting);
            SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();

            Date now = hour24.parse(hour24.format(calendar.getTime()));
            assert now != null;

            if (now.before(hour24.parse("05:00"))) {
                greeting.setText(R.string.evening_greeting);
            } else if (now.before(hour24.parse("12:00"))) {
                greeting.setText(R.string.morning_greeting);
            } else if (now.before(hour24.parse("17:00"))) {
                greeting.setText(R.string.afternoon_greeting);
            } else {
                greeting.setText(R.string.evening_greeting);
            }
        } catch (Exception ignored) {
        }

        String name = sharedPreferences.getString("name", getString(R.string.name));
        ((TextView) homeFragment.findViewById(R.id.text_view_name)).setText(name);

        View spotlightButton = homeFragment.findViewById(R.id.image_button_spotlight);
        TooltipCompat.setTooltipText(spotlightButton, spotlightButton.getContentDescription());
        spotlightButton.setOnClickListener(view -> SettingsRepository.openRecyclerViewFragment(
                this.requireActivity(),
                R.string.spotlight,
                RecyclerViewFragment.TYPE_SPOTLIGHT
        ));

        InfoCard attendance = homeFragment.findViewById(R.id.info_card_attendance);
        InfoCard credits = homeFragment.findViewById(R.id.info_card_credits);
        InfoCard cgpa = homeFragment.findViewById(R.id.info_card_cgpa);

        attendance.setValue(sharedPreferences.getInt("overall_attendance", 0) + "%");
        credits.setValue(String.valueOf(sharedPreferences.getInt("total_credits", 0)));
        cgpa.setValue(new DecimalFormat("#.00").format(sharedPreferences.getFloat("cgpa", 0)));

        TabLayout days = homeFragment.findViewById(R.id.tab_layout_days);
        String[] dayStrings = {
                getString(R.string.sunday),
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday)
        };

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");
            timetable.setPageTransformer((page, position) -> page.setPadding(0, 0, 0, (int) (bottomNavigationHeight + 20 * pixelDensity)));
        });
        timetable.setAdapter(new TimetableAdapter());
        timetable.setCurrentItem(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);

        new TabLayoutMediator(days, timetable, (tab, position) -> {
            tab.setText(dayStrings[position].substring(0, 1));

            View day = tab.view;

            TooltipCompat.setTooltipText(day, dayStrings[position]);
            day.setContentDescription(dayStrings[position]);
        }).attach();

        for (int i = 0; i < days.getTabCount(); ++i) {
            View day = ((ViewGroup) days.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

            if (i == 0) {
                tabParams.setMarginStart((int) (20 * pixelDensity));
                tabParams.setMarginEnd((int) (5 * pixelDensity));
            } else if (i == days.getTabCount() - 1) {
                tabParams.setMarginStart((int) (5 * pixelDensity));
                tabParams.setMarginEnd((int) (20 * pixelDensity));
            } else {
                tabParams.setMarginStart((int) (5 * pixelDensity));
                tabParams.setMarginEnd((int) (5 * pixelDensity));
            }
        }

        return homeFragment;
    }
}