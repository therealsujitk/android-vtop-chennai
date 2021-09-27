package tk.therealsuji.vtopchennai.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.MarksDao;
import tk.therealsuji.vtopchennai.models.Marks;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.ViewHolder> {
    Context context;
    List<Marks> courses;

    public MarksAdapter(Context context, List<Marks> courses) {
        this.context = context;
        this.courses = courses;
    }

    @NonNull
    @Override
    public MarksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        RecyclerView marksView = new RecyclerView(context);
        ViewGroup.LayoutParams timetableParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        marksView.setLayoutParams(timetableParams);
        marksView.setLayoutManager(new LinearLayoutManager(context));

        return new ViewHolder(marksView);
    }

    @Override
    public void onBindViewHolder(@NonNull MarksAdapter.ViewHolder holder, int position) {
        AppDatabase appDatabase = AppDatabase.getInstance(context.getApplicationContext());
        MarksDao marksDao = appDatabase.marksDao();
        Marks course = courses.get(position);

        marksDao.getMarks(course.course).subscribeOn(Schedulers.single()).subscribe(new SingleObserver<List<Marks>>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Marks> marks) {
                ((Activity) context).runOnUiThread(() -> ((RecyclerView) holder.itemView).setAdapter(new MarksSectionAdapter(marks)));
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
