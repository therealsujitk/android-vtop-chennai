package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Mark;

/**
 * ┬─── Marks Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.PerformanceFragment}
 * ├─ {@link MarksAdapter}          - ViewPager2
 * ├─ {@link MarksGroupAdapter}     - RecyclerView (Current File)
 * ╰→ {@link MarksItemAdapter}      - RecyclerView
 */
public class MarksGroupAdapter extends RecyclerView.Adapter<MarksGroupAdapter.ViewHolder> {
    List<List<Mark.AllData>> marks;

    public MarksGroupAdapter(List<List<Mark.AllData>> marks) {
        this.marks = marks;
    }

    @NonNull
    @Override
    public MarksGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        float pixelDensity = context.getResources().getDisplayMetrics().density;

        RecyclerView marksGroup = new RecyclerView(context);
        ViewGroup.LayoutParams marksGroupParams = new ViewGroup.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        marksGroup.setLayoutParams(marksGroupParams);
        marksGroup.setLayoutManager(new LinearLayoutManager(context));
        marksGroup.setNestedScrollingEnabled(false);
        marksGroup.setPadding(0, (int) (48 * pixelDensity), 0, 0);

        View courseTypes = ((View) parent.getParent()).findViewById(R.id.chip_group_course_types);
        courseTypes.post(() -> marksGroup.setPadding(0, courseTypes.getMeasuredHeight(), 0, 0));

        return new ViewHolder(marksGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull MarksGroupAdapter.ViewHolder holder, int position) {
        holder.setAdapter(new MarksItemAdapter(this.marks.get(position)));
    }

    @Override
    public int getItemCount() {
        return this.marks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView marksGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.marksGroup = (RecyclerView) itemView;
        }

        public void setAdapter(MarksItemAdapter marksItemAdapter) {
            this.marksGroup.setAdapter(marksItemAdapter);
        }
    }
}
