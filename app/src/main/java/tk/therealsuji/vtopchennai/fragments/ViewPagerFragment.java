package tk.therealsuji.vtopchennai.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.CoursesAdapter;
import tk.therealsuji.vtopchennai.adapters.EmptyStateAdapter;
import tk.therealsuji.vtopchennai.adapters.ExamsAdapter;
import tk.therealsuji.vtopchennai.adapters.StaffAdapter;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.CoursesDao;
import tk.therealsuji.vtopchennai.interfaces.ExamsDao;
import tk.therealsuji.vtopchennai.interfaces.StaffDao;

public class ViewPagerFragment extends Fragment {
    public static final int TYPE_COURSES = 1;
    public static final int TYPE_EXAMS = 2;
    public static final int TYPE_STAFF = 3;

    AppDatabase appDatabase;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    private void attachCourses() {
        CoursesDao coursesDao = this.appDatabase.coursesDao();
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        coursesDao
                .getCourseCodes()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<String> courseCodes) {
                        if (courseCodes.size() == 0) {
                            displayEmptyState(EmptyStateAdapter.TYPE_NO_DATA, null);
                            return;
                        }

                        viewPager.setAdapter(new CoursesAdapter(courseCodes));

                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                            tab.setText(courseCodes.get(position));
                            TooltipCompat.setTooltipText(tab.view, null);
                        }).attach();

                        // This is required to set the tooltip text again since it gets reset to the tab's text
                        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                            TabLayout.Tab tab = tabLayout.getTabAt(i);

                            if (tab == null) {
                                continue;
                            }

                            tab.view.addOnLayoutChangeListener((view, i0, i1, i2, i3, i4, i5, i6, i7) -> TooltipCompat.setTooltipText(tab.view, null));
                        }

                        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                            View day = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

                            if (tabLayout.getTabCount() == 1) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (20 * pixelDensity));
                                break;
                            }

                            if (i == 0) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            } else if (i == tabLayout.getTabCount() - 1) {
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
                        displayEmptyState(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage());
                    }
                });
    }

    private void attachExams() {
        ExamsDao examsDao = this.appDatabase.examsDao();
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        examsDao
                .getExamTitles()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<String> examTitles) {
                        if (examTitles.size() == 0) {
                            displayEmptyState(EmptyStateAdapter.TYPE_NO_DATA, null);
                            return;
                        }

                        viewPager.setAdapter(new ExamsAdapter(examTitles));

                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                            tab.setText(examTitles.get(position));
                            TooltipCompat.setTooltipText(tab.view, null);
                        }).attach();

                        // This is required to set the tooltip text again since it gets reset to the tab's text
                        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                            TabLayout.Tab tab = tabLayout.getTabAt(i);

                            if (tab == null) {
                                continue;
                            }

                            tab.view.addOnLayoutChangeListener((view, i0, i1, i2, i3, i4, i5, i6, i7) -> TooltipCompat.setTooltipText(tab.view, null));
                        }

                        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                            View day = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

                            if (tabLayout.getTabCount() == 1) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (20 * pixelDensity));
                                break;
                            }

                            if (i == 0) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            } else if (i == tabLayout.getTabCount() - 1) {
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
                        displayEmptyState(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage());
                    }
                });
    }

    private void attachStaff() {
        StaffDao staffDao = this.appDatabase.staffDao();
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        staffDao
                .getStaffTypes()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<String> staffTypes) {
                        if (staffTypes.size() == 0) {
                            displayEmptyState(EmptyStateAdapter.TYPE_NO_DATA, null);
                            return;
                        }

                        viewPager.setAdapter(new StaffAdapter(staffTypes));

                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                            tab.setText(staffTypes.get(position).toUpperCase());
                            TooltipCompat.setTooltipText(tab.view, null);
                        }).attach();

                        // This is required to set the tooltip text again since it gets reset to the tab's text
                        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                            TabLayout.Tab tab = tabLayout.getTabAt(i);

                            if (tab == null) {
                                continue;
                            }

                            tab.view.addOnLayoutChangeListener((view, i0, i1, i2, i3, i4, i5, i6, i7) -> TooltipCompat.setTooltipText(tab.view, null));
                        }

                        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                            View day = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

                            if (tabLayout.getTabCount() == 1) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (20 * pixelDensity));
                                break;
                            }

                            if (i == 0) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            } else if (i == tabLayout.getTabCount() - 1) {
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
                        displayEmptyState(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage());
                    }
                });
    }

    private void displayEmptyState(int type, String message) {
        this.tabLayout.setVisibility(View.GONE);
        this.viewPager.setAdapter(new EmptyStateAdapter(type, message));
        this.viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bottomNavigationVisibility = new Bundle();
        bottomNavigationVisibility.putBoolean("isVisible", false);
        getParentFragmentManager().setFragmentResult("bottomNavigationVisibility", bottomNavigationVisibility);
    }

    @Override
    public void onResume() {
        super.onResume();

        String screenName = "RecyclerView Fragment";
        Bundle arguments = this.getArguments();

        if (arguments != null) {
            int contentType = arguments.getInt("content_type", 0);
            switch (contentType) {
                case TYPE_COURSES:
                    screenName = "Courses";
                    break;
                case TYPE_EXAMS:
                    screenName = "Exams";
                    break;
                case TYPE_STAFF:
                    screenName = "Staff";
                    break;
            }
        }

        // Firebase Analytics Logging
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "RecyclerViewFragment");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        FirebaseAnalytics.getInstance(this.requireContext()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewPagerFragment = inflater.inflate(R.layout.fragment_view_pager, container, false);
        viewPagerFragment.getRootView().setBackgroundColor(requireContext().getColor(R.color.secondary_container_95));
        viewPagerFragment.getRootView().setOnTouchListener((view, motionEvent) -> true);

        View header = viewPagerFragment.findViewById(R.id.linear_layout_header);
        this.tabLayout = viewPagerFragment.findViewById(R.id.tab_layout);
        this.viewPager = viewPagerFragment.findViewById(R.id.view_pager);
        this.appDatabase = AppDatabase.getInstance(this.requireActivity().getApplicationContext());

        getParentFragmentManager().setFragmentResultListener("customInsets2", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int systemWindowInsetBottom = result.getInt("systemWindowInsetBottom");
            float pixelDensity = this.getResources().getDisplayMetrics().density;

            header.setPaddingRelative(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            this.viewPager.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    0
            );

            this.viewPager.setPageTransformer((page, position) -> page.setPadding(
                    0,
                    0,
                    0,
                    (int) (systemWindowInsetBottom + 20 * pixelDensity)
            ));
        });

        int titleId = 0, contentType = 0;
        Bundle arguments = this.getArguments();

        if (arguments != null) {
            titleId = arguments.getInt("title_id", 0);
            contentType = arguments.getInt("content_type", 0);
        }

        viewPagerFragment.findViewById(R.id.image_button_back).setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());
        ((TextView) viewPagerFragment.findViewById(R.id.text_view_title)).setText(getString(titleId));

        switch (contentType) {
            case TYPE_COURSES:
                this.attachCourses();
                break;
            case TYPE_EXAMS:
                this.attachExams();
                break;
            case TYPE_STAFF:
                this.attachStaff();
                break;
        }

        return viewPagerFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bundle bottomNavigationVisibility = new Bundle();
        bottomNavigationVisibility.putBoolean("isVisible", true);
        getParentFragmentManager().setFragmentResult("bottomNavigationVisibility", bottomNavigationVisibility);
    }
}
