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

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Timetable;
import tk.therealsuji.vtopchennai.models.TimetableLab;
import tk.therealsuji.vtopchennai.models.TimetableTheory;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {
    Context context;

    List<TimetableLab> timetableLab;
    List<TimetableTheory> timetableTheory;

    public TimetableAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public TimetableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        RecyclerView timetable = new RecyclerView(context);
        ViewGroup.LayoutParams timetableParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        timetable.setLayoutParams(timetableParams);
        timetable.setLayoutManager(new LinearLayoutManager(context));

        return new ViewHolder(timetable);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableAdapter.ViewHolder holder, int position) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        TimetableDao timetableDao = appDatabase.timetableDao();

        Observable<List<TimetableLab>> timetableLabObservable = Observable.fromSingle(timetableDao.getLabTimetable(position));
        Observable<List<TimetableTheory>> timetableTheoryObservable = Observable.fromSingle(timetableDao.getTheoryTimetable(position));

        Observable.merge(timetableLabObservable, timetableTheoryObservable).subscribeOn(Schedulers.single()).subscribe(new Observer<List<?>>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<?> objects) {
                if (objects.get(0) instanceof TimetableLab) {
                    timetableLab = (List<TimetableLab>) objects;
                } else if (objects.get(0) instanceof TimetableTheory) {
                    timetableTheory = (List<TimetableTheory>) objects;
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
                ((Activity) context).runOnUiThread(() -> {
                    List<Timetable> timetable = Timetable.buildTimetable(timetableLab, timetableTheory, position);
                    ((RecyclerView) holder.itemView).setAdapter(new TimetableItemAdapter(context, timetable));
                });
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
