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
import tk.therealsuji.vtopchennai.interfaces.ExamsDao;
import tk.therealsuji.vtopchennai.models.Exam;

/**
 * ┬─── Staff Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.ViewPagerFragment}
 * ├─ {@link ExamsAdapter}      - ViewPager2 (Current File)
 * ╰→ {@link ExamsItemAdapter}  - RecyclerView
 */
public class ExamsAdapter extends RecyclerView.Adapter<ExamsAdapter.ViewHolder> {
    Context applicationContext;
    List<String> examTitles;

    public ExamsAdapter(List<String> examTitles) {
        this.examTitles = examTitles;
    }

    @NonNull
    @Override
    public ExamsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.applicationContext = context.getApplicationContext();

        RecyclerView examView = new RecyclerView(context);
        ViewGroup.LayoutParams examViewParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        examView.setLayoutParams(examViewParams);
        examView.setLayoutManager(new LinearLayoutManager(context));
        examView.setClipToPadding(false);

        return new ViewHolder(examView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamsAdapter.ViewHolder holder, int position) {
        RecyclerView examView = (RecyclerView) holder.itemView;

        AppDatabase appDatabase = AppDatabase.getInstance(this.applicationContext);
        ExamsDao examsDao = appDatabase.examsDao();

        examsDao
                .getExams(examTitles.get(position))
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Exam.AllData>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Exam.AllData> exams) {
                        examView.setAdapter(new ExamsItemAdapter(exams));
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        examView.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.examTitles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
