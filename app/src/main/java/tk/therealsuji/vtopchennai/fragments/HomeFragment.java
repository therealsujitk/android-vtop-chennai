package tk.therealsuji.vtopchennai.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.analytics.FirebaseAnalytics;

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
    public void onResume() {
        super.onResume();

        // Firebase Analytics Logging
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "HomeFragment");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Home");
        FirebaseAnalytics.getInstance(this.requireContext()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View homeFragment = inflater.inflate(R.layout.fragment_home, container, false);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        AppBarLayout appBarLayout = homeFragment.findViewById(R.id.app_bar);
        ViewPager2 timetable = homeFragment.findViewById(R.id.view_pager_timetable);

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");

            appBarLayout.setPadding(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            timetable.setPageTransformer((page, position) -> page.setPadding(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (bottomNavigationHeight + 20 * pixelDensity)
            ));

            // Only one listener can be added per requestKey, so we create a duplicate
            getParentFragmentManager().setFragmentResult("customInsets2", result);
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            LinearLayout header = homeFragment.findViewById(R.id.linear_layout_header);

            float alpha = 1 - ((float) (-1 * verticalOffset) / header.getHeight());
            header.setAlpha(alpha);
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

        BadgeDrawable spotlightBadge = BadgeDrawable.create(requireContext());
        spotlightBadge.setBadgeGravity(BadgeDrawable.TOP_END);
        spotlightBadge.setHorizontalOffset((int) (24 * pixelDensity));
        spotlightBadge.setVerticalOffset((int) (24 * pixelDensity));
        spotlightBadge.setVisible(false);

        spotlightButton.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @OptIn(markerClass = ExperimentalBadgeUtils.class)
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                BadgeUtils.attachBadgeDrawable(spotlightBadge, spotlightButton);
                spotlightButton.removeOnLayoutChangeListener(this);
            }
        });

        getParentFragmentManager().setFragmentResultListener("unreadCount", this, (requestKey, result) -> {
            int spotlightCount = result.getInt("spotlight");
            spotlightBadge.setNumber(spotlightCount);
            spotlightBadge.setVisible(spotlightCount != 0);
        });

        getParentFragmentManager().setFragmentResult("getUnreadCount", new Bundle());

        InfoCard attendance = homeFragment.findViewById(R.id.info_card_attendance);
        InfoCard credits = homeFragment.findViewById(R.id.info_card_credits);
        InfoCard cgpa = homeFragment.findViewById(R.id.info_card_cgpa);

        float totalCredits;

        try {
            // Support old integer based credits
            totalCredits = sharedPreferences.getInt("totalCredits", 0);
        } catch (Exception ignored) {
            totalCredits = sharedPreferences.getFloat("totalCredits", 0);
        }

        if (totalCredits == (int) totalCredits) {
            credits.setValue(String.valueOf((int) totalCredits));
        } else {
            credits.setValue(String.valueOf(totalCredits));
        }

        attendance.setValue(sharedPreferences.getInt("overallAttendance", 0) + "%");
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

        timetable.setAdapter(new TimetableAdapter());

        new TabLayoutMediator(days, timetable, (tab, position) -> {
            tab.setText(dayStrings[position].substring(0, 1));
            tab.view.setContentDescription(dayStrings[position]);
            TooltipCompat.setTooltipText(tab.view, dayStrings[position]);
        }).attach();

        // This is required to set the tooltip text again since it gets reset to the tab's text
        for (int i = 0; i < days.getTabCount(); ++i) {
            TabLayout.Tab tab = days.getTabAt(i);
            int position = i;

            if (tab == null) {
                continue;
            }

            tab.view.addOnLayoutChangeListener((view, i0, i1, i2, i3, i4, i5, i6, i7) -> {
                view.setContentDescription(dayStrings[position]);
                TooltipCompat.setTooltipText(tab.view, dayStrings[position]);
            });
        }

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

        timetable.setCurrentItem(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);

        return homeFragment;
    }
}
