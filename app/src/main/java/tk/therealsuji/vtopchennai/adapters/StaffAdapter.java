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
import tk.therealsuji.vtopchennai.interfaces.StaffDao;
import tk.therealsuji.vtopchennai.models.Staff;

/**
 * ┬─── Staff Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.ViewPagerFragment}
 * ├─ {@link StaffAdapter}      - ViewPager2 (Current File)
 * ╰→ {@link StaffItemAdapter}  - RecyclerView
 */
public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
    Context applicationContext;
    List<String> staffTypes;

    public StaffAdapter(List<String> staffTypes) {
        this.staffTypes = staffTypes;
    }

    @NonNull
    @Override
    public StaffAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.applicationContext = context.getApplicationContext();

        RecyclerView staffView = new RecyclerView(context);
        ViewGroup.LayoutParams staffViewParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        staffView.setLayoutParams(staffViewParams);
        staffView.setLayoutManager(new LinearLayoutManager(context));
        staffView.setClipToPadding(false);

        return new ViewHolder(staffView);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffAdapter.ViewHolder holder, int position) {
        RecyclerView staffView = (RecyclerView) holder.itemView;

        AppDatabase appDatabase = AppDatabase.getInstance(this.applicationContext);
        StaffDao staffDao = appDatabase.staffDao();

        staffDao
                .getStaff(staffTypes.get(position))
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Staff>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Staff> staff) {
                        staffView.setAdapter(new StaffItemAdapter(staff));
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        staffView.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, "Error: " + e.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.staffTypes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
