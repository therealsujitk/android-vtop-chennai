package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Mark;

/**
 * ┬─── Marks Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.PerformanceFragment}
 * ├─ {@link MarksAdapter}          - ViewPager2 (Current File)
 * ├─ {@link MarksGroupAdapter}     - RecyclerView
 * ╰→ {@link MarksItemAdapter}      - RecyclerView
 */
public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.ViewHolder> {
    List<Observable<List<Mark.AllData>>> markObservables;

    public MarksAdapter(List<Observable<List<Mark.AllData>>> markObservables) {
        this.markObservables = markObservables;
    }

    @NonNull
    @Override
    public MarksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout marksView = (RelativeLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_marks, parent, false);

        return new ViewHolder(marksView);
    }

    @Override
    public void onBindViewHolder(@NonNull MarksAdapter.ViewHolder holder, int position) {
        this.markObservables.get(position)
                .subscribe(new Observer<List<Mark.AllData>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<Mark.AllData> marks) {
                        holder.segregateMarks(marks);
                        holder.displayMarks();
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        holder.displayError("Error: " + e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.markObservables.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ChipGroup courseTypes;
        RecyclerView markGroups;
        Map<String, List<Mark.AllData>> marks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.courseTypes = itemView.findViewById(R.id.chip_group_course_types);
            this.markGroups = itemView.findViewById(R.id.recycler_view_mark_groups);

            this.markGroups.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int scrollPosition = Objects.requireNonNull(layoutManager).findFirstVisibleItemPosition();

                    if (courseTypes.getChildAt(scrollPosition) != null) {
                        ((Chip) courseTypes.getChildAt(scrollPosition)).setChecked(true);
                    }
                }
            });
        }

        private void addChips() {
            List<String> courseTypes = new ArrayList<>();

            if (this.marks.containsKey("theory")) {
                courseTypes.add("theory");
            }

            if (this.marks.containsKey("lab")) {
                courseTypes.add("lab");
            }

            if (this.marks.containsKey("project")) {
                courseTypes.add("project");
            }

            this.courseTypes.removeAllViews();

            for (int i = 0; i < this.marks.size(); ++i) {
                String courseType = courseTypes.get(i);
                Chip chip = new Chip(this.markGroups.getContext());
                chip.setCheckable(true);
                chip.setElevation(15);

                if (courseType.equals("lab")) {
                    chip.setText(R.string.lab);
                    chip.setChipIconResource(R.drawable.ic_lab);
                } else if (courseType.equals("project")) {
                    chip.setText(R.string.project);
                    chip.setChipIconResource(R.drawable.ic_project);
                } else {
                    chip.setText(R.string.theory);
                    chip.setChipIconResource(R.drawable.ic_theory);
                }

                if (i == 0) {
                    chip.setChecked(true);
                }

                int scrollPosition = i;
                chip.setOnClickListener(view -> {
                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this.markGroups.getContext()) {
                        @Override
                        protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }
                    };

                    smoothScroller.setTargetPosition(scrollPosition);
                    Objects.requireNonNull(this.markGroups.getLayoutManager()).startSmoothScroll(smoothScroller);
                });

                this.courseTypes.addView(chip);
            }
        }

        public void segregateMarks(List<Mark.AllData> marks) {
            this.marks = new HashMap<>();

            for (int i = 0; i < marks.size(); ++i) {
                Mark.AllData mark = marks.get(i);
                String courseType = mark.courseType;

                if (!this.marks.containsKey(courseType)) {
                    this.marks.put(courseType, new ArrayList<>());
                }

                Objects.requireNonNull(this.marks.get(courseType)).add(mark);
            }

            this.addChips();
        }

        public void displayMarks() {
            List<List<Mark.AllData>> markGroups = new ArrayList<>();

            markGroups.add(this.marks.get("theory"));
            markGroups.add(this.marks.get("lab"));
            markGroups.add(this.marks.get("project"));

            markGroups.removeAll(Collections.singleton(null));

            this.markGroups.setAdapter(new MarksGroupAdapter(markGroups));
        }

        public void displayError(String errorMessage) {
            this.markGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, errorMessage));
        }
    }
}
