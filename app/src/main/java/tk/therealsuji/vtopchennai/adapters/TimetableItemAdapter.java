package tk.therealsuji.vtopchennai.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.models.Timetable;
import tk.therealsuji.vtopchennai.widgets.TimetableItem;

public class TimetableItemAdapter extends RecyclerView.Adapter<TimetableItemAdapter.ViewHolder> {
    Activity mainActivity;
    private List<Timetable> timetable;
    private final float pixelDensity;
    private int status;

    public TimetableItemAdapter(Context context) {
        this.mainActivity = (Activity) context;

        this.pixelDensity = context.getResources().getDisplayMetrics().density;
        this.status = TimetableItem.STATUS_FUTURE;
    }

    public void setTimetable(List<Timetable> timetable) {
        this.timetable = timetable;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @NonNull
    @Override
    public TimetableItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TimetableItem timetableItem = new TimetableItem(parent.getContext());
        timetableItem.setPadding(
                (int) (30 * pixelDensity),
                (int) (5 * pixelDensity),
                (int) (30 * pixelDensity),
                (int) (5 * pixelDensity)
        );
        timetableItem.setStatus(this.status);

        return new ViewHolder(timetableItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableItemAdapter.ViewHolder holder, int position) {
        holder.initializeTimetableItem(timetable.get(position));
    }

    @Override
    public int getItemCount() {
        if (timetable == null) {
            return 0;
        }

        return timetable.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TimetableItem timetableItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.timetableItem = (TimetableItem) itemView;
        }

        public void initializeTimetableItem(Timetable timetable) {
            timetableItem.setCourseType(timetable.courseType);
            timetableItem.setCourseCode(timetable.courseCode);
            timetableItem.setTimings(timetable.startTime, timetable.endTime);
        }
    }
}
