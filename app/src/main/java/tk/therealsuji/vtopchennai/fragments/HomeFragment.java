package tk.therealsuji.vtopchennai.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.util.Calendar;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.TimetableAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.widgets.InfoCard;

public class HomeFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View homeFragment = inflater.inflate(R.layout.fragment_home, container, false);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        AppBarLayout appBarLayout = homeFragment.findViewById(R.id.app_bar);

        appBarLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(0, windowInsets.getSystemWindowInsetTop(), 0, 0);
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            LinearLayout header = homeFragment.findViewById(R.id.header);
            float alpha = 1 - ((float) (-1 * verticalOffset) / header.getHeight());

            header.setAlpha(alpha);
        });

        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

        String name = sharedPreferences.getString("name", getString(R.string.name));
        ((TextView) homeFragment.findViewById(R.id.text_name)).setText(name);

        View spotlightButton = homeFragment.findViewById(R.id.button_spotlight);
        TooltipCompat.setTooltipText(spotlightButton, spotlightButton.getContentDescription());
        spotlightButton.setOnClickListener(view -> SettingsRepository.openRecyclerViewFragment(
                this.requireActivity(),
                R.string.spotlight,
                RecyclerViewFragment.TYPE_SPOTLIGHT
        ));

        InfoCard attendance = homeFragment.findViewById(R.id.card_attendance);
        InfoCard credits = homeFragment.findViewById(R.id.card_credits);
        InfoCard cgpa = homeFragment.findViewById(R.id.card_cgpa);

        attendance.setValue(sharedPreferences.getInt("overall_attendance", 0) + "%");
        credits.setValue(String.valueOf(sharedPreferences.getInt("total_credits", 0)));
        cgpa.setValue(new DecimalFormat("#.00").format(sharedPreferences.getFloat("cgpa", 0)));

        TabLayout days = homeFragment.findViewById(R.id.days);
        String[] dayStrings = {
                getString(R.string.sunday),
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday)
        };
        String[] shortDayStrings = {
                getString(R.string.s),
                getString(R.string.m),
                getString(R.string.t),
                getString(R.string.w),
                getString(R.string.t),
                getString(R.string.f),
                getString(R.string.s)
        };

        ViewPager2 timetable = homeFragment.findViewById(R.id.timetable);

        timetable.setAdapter(new TimetableAdapter());
        timetable.setCurrentItem(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);

        new TabLayoutMediator(days, timetable, (tab, position) -> {
            tab.setText(shortDayStrings[position]);

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