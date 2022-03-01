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
import tk.therealsuji.vtopchennai.interfaces.CoursesDao;
import tk.therealsuji.vtopchennai.models.Course;

/**
 * ┬─── Courses Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.ViewPagerFragment}
 * ├─ {@link CoursesAdapter}        - ViewPager2 (Current File)
 * ╰→ {@link CoursesItemAdapter}    - RecyclerView
 */
public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {
    Context applicationContext;
    List<String> courseCodes;

    public CoursesAdapter(List<String> courseCodes) {
        this.courseCodes = courseCodes;
    }

    @NonNull
    @Override
    public CoursesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.applicationContext = context.getApplicationContext();

        RecyclerView courseView = new RecyclerView(context);
        ViewGroup.LayoutParams courseViewParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        courseView.setLayoutParams(courseViewParams);
        courseView.setLayoutManager(new LinearLayoutManager(context));
        courseView.setClipToPadding(false);

        return new ViewHolder(courseView);
    }

    @Override
    public void onBindViewHolder(@NonNull CoursesAdapter.ViewHolder holder, int position) {
        RecyclerView courseView = (RecyclerView) holder.itemView;

        AppDatabase appDatabase = AppDatabase.getInstance(this.applicationContext);
        CoursesDao coursesDao = appDatabase.coursesDao();

        coursesDao
                .getCourse(this.courseCodes.get(position))
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Course.AllData>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Course.AllData> course) {
                        courseView.setAdapter(new CoursesItemAdapter(course));
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        courseView.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.courseCodes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
