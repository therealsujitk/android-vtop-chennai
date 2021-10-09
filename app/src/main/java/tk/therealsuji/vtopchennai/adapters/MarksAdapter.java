package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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

        RecyclerView marksView = new RecyclerView(this.context);
        ViewGroup.LayoutParams timetableParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        marksView.setLayoutParams(timetableParams);
        marksView.setLayoutManager(new LinearLayoutManager(this.context));

        return new ViewHolder(marksView);
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
                        ((RecyclerView) holder.itemView).setAdapter(new MarksSectionAdapter(marks));
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
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
