package com.ashish.vtopchennai.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.ashish.vtopchennai.helpers.AppDatabase;
import com.ashish.vtopchennai.interfaces.TimetableDao;
import com.ashish.vtopchennai.models.Timetable;

/**
 * ┬─── Timetable Hierarchy
 * ├─ {@link com.ashish.vtopchennai.fragments.HomeFragment}
 * ├─ {@link TimetableAdapter}      - ViewPager2 (Current File)
 * ╰→ {@link TimetableItemAdapter}  - RecyclerView
 */
public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {
    Context applicationContext;

    @NonNull
    @Override
    public TimetableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.applicationContext = context.getApplicationContext();

        RecyclerView timetableView = new RecyclerView(context);
        ViewGroup.LayoutParams timetableParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        timetableView.setLayoutParams(timetableParams);
        timetableView.setLayoutManager(new LinearLayoutManager(context));
        timetableView.setClipToPadding(false);

        return new ViewHolder(timetableView);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableAdapter.ViewHolder holder, int position) {
        RecyclerView timetableView = (RecyclerView) holder.itemView;

        AppDatabase appDatabase = AppDatabase.getInstance(this.applicationContext);
        TimetableDao timetableDao = appDatabase.timetableDao();
        int day = holder.getAdapterPosition();

        timetableDao
                .get(day)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Timetable.AllData>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Timetable.AllData> timetable) {
                        if (timetable.size() == 0) {
                            timetableView.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_NO_TIMETABLE));
                            return;
                        }

                        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                        int status = TimetableItemAdapter.STATUS_FUTURE;

                        if (day < dayOfWeek) {
                            status = TimetableItemAdapter.STATUS_PAST;
                        } else if (day == dayOfWeek) {
                            status = TimetableItemAdapter.STATUS_PRESENT;
                        }

                        timetableView.setAdapter(new TimetableItemAdapter(timetable, status));
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        timetableView.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
