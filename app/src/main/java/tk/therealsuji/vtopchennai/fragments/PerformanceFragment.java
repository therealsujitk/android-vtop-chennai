package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.MarksAdapter;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.MarksDao;
import tk.therealsuji.vtopchennai.models.Course;

public class PerformanceFragment extends Fragment {

    public PerformanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View performanceFragment = inflater.inflate(R.layout.fragment_performance, container, false);

        AppBarLayout appBarLayout = performanceFragment.findViewById(R.id.app_bar);

        appBarLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(0, windowInsets.getSystemWindowInsetTop(), 0, 0);
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            LinearLayout header = performanceFragment.findViewById(R.id.header);
            float alpha = 1 - ((float) (-1 * verticalOffset) / header.getHeight());

            header.setAlpha(alpha);
        });

        float pixelDensity = this.getResources().getDisplayMetrics().density;

        TabLayout courseTabs = performanceFragment.findViewById(R.id.courses);
        ViewPager2 marks = performanceFragment.findViewById(R.id.marks);

        AppDatabase appDatabase = AppDatabase.getInstance(requireActivity().getApplicationContext());
        MarksDao marksDao = appDatabase.marksDao();

        marksDao
                .getCourses()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Course>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<Course> courses) {
                        marks.setAdapter(new MarksAdapter(courses));
                        new TabLayoutMediator(courseTabs, marks, (tab, position) -> {
                            Course course = courses.get(position);

                            tab.setText(course.code);
                            TooltipCompat.setTooltipText(tab.view, course.title);
                            tab.view.setContentDescription(course.title);
                        }).attach();

                        for (int i = 0; i < courses.size(); ++i) {
                            View day = ((ViewGroup) courseTabs.getChildAt(0)).getChildAt(i);
                            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

                            if (i == 0) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            } else if (i == courseTabs.getTabCount() - 1) {
                                tabParams.setMarginStart((int) (5 * pixelDensity));
                                tabParams.setMarginEnd((int) (20 * pixelDensity));
                            } else {
                                tabParams.setMarginStart((int) (5 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });

        return performanceFragment;
    }
}