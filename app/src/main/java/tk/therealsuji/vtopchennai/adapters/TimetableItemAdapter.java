package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.models.Timetable;
import tk.therealsuji.vtopchennai.widgets.TimetableItem;

public class TimetableItemAdapter extends RecyclerView.Adapter<TimetableItemAdapter.ViewHolder> {
    private final List<Timetable> timetable;
    float pixelDensity;

    public TimetableItemAdapter(Context context, List<Timetable> timetable) {
        pixelDensity = context.getResources().getDisplayMetrics().density;
        this.timetable = timetable;
    }

    @NonNull
    @Override
    public TimetableItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TimetableItem timetableItem = new TimetableItem(parent.getContext());
        return new ViewHolder(timetableItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableItemAdapter.ViewHolder holder, int position) {
        holder.initializeTimetableItem(timetable.get(position), pixelDensity);
    }

    @Override
    public int getItemCount() {
        return timetable.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TimetableItem timetableItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.timetableItem = (TimetableItem) itemView;
        }

        public void initializeTimetableItem(Timetable timetable, float pixelDensity) {
            timetableItem.setCourseType(timetable.courseType);
            timetableItem.setRawCourse(timetable.rawCourse);
            timetableItem.setTimings(timetable.startTime, timetable.endTime);
            timetableItem.setPadding(
                    (int) (30 * pixelDensity),
                    (int) (5 * pixelDensity),
                    (int) (30 * pixelDensity),
                    (int) (5 * pixelDensity)
            );
        }
    }
}
