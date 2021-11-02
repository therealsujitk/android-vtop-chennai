package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.MarksDao;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.Mark;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.ViewHolder> {
    Context context;
    List<Course> courses;

    public MarksAdapter(List<Course> courses) {
        this.courses = courses;
    }

    @NonNull
    @Override
    public MarksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LinearLayout marks = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_marks, parent, false);

        return new ViewHolder(marks);
    }

    @Override
    public void onBindViewHolder(@NonNull MarksAdapter.ViewHolder holder, int position) {
        AppDatabase appDatabase = AppDatabase.getInstance(context.getApplicationContext());
        MarksDao marksDao = appDatabase.marksDao();
        Course course = courses.get(position);

        marksDao
                .getMarks(course.code)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Mark.AllData>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Mark.AllData> marks) {
                        holder.segregateMarks(marks);
                        holder.setAdapter();
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ChipGroup courseTypes;
        RecyclerView markGroups;
        Map<String, List<Mark.AllData>> marks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.courseTypes = itemView.findViewById(R.id.course_types);
            this.markGroups = itemView.findViewById(R.id.mark_groups);
            this.markGroups.addItemDecoration(new MaterialDividerItemDecoration(
                    itemView.getContext(),
                    MaterialDividerItemDecoration.VERTICAL
            ));

            this.markGroups.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int scrollPosition = Objects.requireNonNull(layoutManager).findFirstVisibleItemPosition();

                    if (scrollPosition < courseTypes.getChildCount()) {
                        ((Chip) courseTypes.getChildAt(scrollPosition)).setChecked(true);
                    }
                }
            });
        }

        private void addChips() {
            this.courseTypes.removeAllViews();

            List<String> courseTypes = new ArrayList<>(this.marks.keySet());

            for (int i = 0; i < courseTypes.size(); ++i) {
                String courseType = courseTypes.get(i);
                Chip chip = new Chip(this.markGroups.getContext());
                chip.setCheckable(true);

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
                chip.setOnClickListener(view -> this.markGroups.smoothScrollToPosition(scrollPosition));

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

        public void setAdapter() {
            this.markGroups.setAdapter(new MarksGroupAdapter(new ArrayList<>(this.marks.values())));
        }
    }
}
