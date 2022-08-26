package com.ashish.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.adapters.EmptyStateAdapter;
import com.ashish.vtopchennai.adapters.MarksAdapter;
import com.ashish.vtopchennai.helpers.AppDatabase;
import com.ashish.vtopchennai.interfaces.MarksDao;
import com.ashish.vtopchennai.models.Course;
import com.ashish.vtopchennai.models.CumulativeMark;
import com.ashish.vtopchennai.models.Mark;
import com.ashish.vtopchennai.widgets.PerformanceCard;

public class PerformanceFragment extends Fragment {
    AppBarLayout appBarLayout;
    ViewPager2 marks;
    View performanceCards;

    public PerformanceFragment() {
        // Required empty public constructor
    }

    private void displayEmptyState(int type, String message) {
        this.marks.setAdapter(new EmptyStateAdapter(type, message));
        this.marks.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Disable app bar scrolling behaviour
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) this.appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();
        Objects.requireNonNull(behavior).setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@androidx.annotation.NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View performanceFragment = inflater.inflate(R.layout.fragment_performance, container, false);

        this.appBarLayout = performanceFragment.findViewById(R.id.app_bar);
        this.marks = performanceFragment.findViewById(R.id.view_pager_marks);
        this.performanceCards = performanceFragment.findViewById(R.id.horizontal_scroll_view_performance_cards);
        LinearLayout header = performanceFragment.findViewById(R.id.linear_layout_header);

        float pixelDensity = this.getResources().getDisplayMetrics().density;

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");

            this.appBarLayout.setPadding(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            this.marks.setPageTransformer((page, position) -> {
                int headerOffset = 0;
                if (this.performanceCards.getVisibility() != View.VISIBLE && page.findViewById(R.id.text_view_no_data) != null) {
                    headerOffset = header.getMeasuredHeight();
                }

                // Setting padding to the RecyclerView child inside the RelativeLayout
                ((ViewGroup) page).getChildAt(0).setPadding(
                        systemWindowInsetLeft,
                        0,
                        systemWindowInsetRight,
                        (int) (bottomNavigationHeight + 20 * pixelDensity + headerOffset)
                );
            });
        });

        this.appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            float alpha = 1 - ((float) (-1 * verticalOffset) / header.getHeight());
            header.setAlpha(alpha);
        });

        TabLayout courseTabs = performanceFragment.findViewById(R.id.tab_layout_courses);

        AppDatabase appDatabase = AppDatabase.getInstance(requireActivity().getApplicationContext());
        MarksDao marksDao = appDatabase.marksDao();

        marksDao.getCourses()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Course.AllData>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<Course.AllData> courses) {
                        if (courses.size() == 0) {
                            displayEmptyState(EmptyStateAdapter.TYPE_NO_PERFORMANCE, null);
                            return;
                        }

                        performanceCards.setVisibility(View.VISIBLE);
                        courseTabs.setVisibility(View.VISIBLE);

                        List<Observable<List<Mark.AllData>>> markObservables = new ArrayList<>();
                        for (Course.AllData course : courses) {
                            Observable<List<Mark.AllData>> observable = Observable.fromSingle(marksDao.getMarks(course.courseCode))
                                    .subscribeOn(Schedulers.single())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .replay()
                                    .autoConnect();

                            markObservables.add(observable);
                        }

                        marks.setAdapter(new MarksAdapter(markObservables));
                        new TabLayoutMediator(courseTabs, marks, (tab, position) -> {
                            Course.AllData course = courses.get(position);

                            tab.setText(course.courseCode);
                            TooltipCompat.setTooltipText(tab.view, course.courseTitle);
                            tab.view.setContentDescription(course.courseTitle);

                            if (courses.get(position).unreadMarkCount != 0) {
                                BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                                badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_END);
                                badgeDrawable.setNumber(courses.get(position).unreadMarkCount);
                                badgeDrawable.setHorizontalOffset((int) (-3 * pixelDensity));
                                badgeDrawable.setVerticalOffset((int) (-6 * pixelDensity));

                                if (courses.get(position).unreadMarkCount > 9) {
                                    badgeDrawable.setHorizontalOffset((int) (5 * pixelDensity));
                                }
                            }
                        }).attach();

                        for (int i = 0; i < courseTabs.getTabCount(); ++i) {
                            View day = ((ViewGroup) courseTabs.getChildAt(0)).getChildAt(i);
                            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

                            if (courseTabs.getTabCount() == 1) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (20 * pixelDensity));
                                break;
                            }

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

                        PerformanceCard overall = performanceFragment.findViewById(R.id.performance_card_overall);
                        PerformanceCard theory = performanceFragment.findViewById(R.id.performance_card_theory);
                        PerformanceCard project = performanceFragment.findViewById(R.id.performance_card_project);
                        PerformanceCard lab = performanceFragment.findViewById(R.id.performance_card_lab);

                        marks.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) {
                                super.onPageSelected(position);

                                final String courseCode = courses.get(position).courseCode;

                                overall.setIndeterminate(true);
                                theory.setIndeterminate(true);
                                project.setIndeterminate(true);
                                lab.setIndeterminate(true);

                                marksDao.getCumulativeMark(courseCode)
                                        .subscribeOn(Schedulers.single())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<CumulativeMark>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {
                                            }

                                            @Override
                                            public void onSuccess(@NonNull CumulativeMark cumulativeMark) {
                                                if (cumulativeMark.grandTotal != null) {
                                                    overall.show();
                                                    overall.setIndeterminate(false);
                                                    overall.setScore(cumulativeMark.grandTotal, cumulativeMark.grandMax);
                                                } else {
                                                    overall.hide();
                                                }

                                                if (cumulativeMark.theoryTotal != null) {
                                                    theory.show();
                                                    theory.setIndeterminate(false);
                                                    theory.setScore(cumulativeMark.theoryTotal, cumulativeMark.theoryMax);
                                                } else {
                                                    theory.hide();
                                                }

                                                if (cumulativeMark.projectTotal != null) {
                                                    project.show();
                                                    project.setIndeterminate(false);
                                                    project.setScore(cumulativeMark.projectTotal, cumulativeMark.projectMax);
                                                } else {
                                                    project.hide();
                                                }

                                                if (cumulativeMark.labTotal != null) {
                                                    lab.show();
                                                    lab.setIndeterminate(false);
                                                    lab.setScore(cumulativeMark.labTotal, cumulativeMark.labMax);
                                                } else {
                                                    lab.hide();
                                                }
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                                Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                if (courses.get(position).unreadMarkCount != 0) {
                                    markObservables.get(position)
                                            .subscribe(new Observer<List<Mark.AllData>>() {
                                                @Override
                                                public void onSubscribe(@NonNull Disposable d) {
                                                }

                                                @Override
                                                public void onNext(@NonNull List<Mark.AllData> marks) {
                                                }

                                                @Override
                                                public void onError(@NonNull Throwable e) {
                                                }

                                                @Override
                                                public void onComplete() {
                                                    marksDao.setMarksRead(courseCode)
                                                            .subscribeOn(Schedulers.single())
                                                            .subscribe();
                                                }
                                            });
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        displayEmptyState(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage());
                    }
                });

        return performanceFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getParentFragmentManager().setFragmentResult("getUnreadCount", new Bundle());
    }
}
