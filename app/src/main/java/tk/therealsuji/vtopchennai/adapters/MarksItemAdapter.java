package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.models.Mark;
import tk.therealsuji.vtopchennai.widgets.MarksItem;

public class MarksItemAdapter extends RecyclerView.Adapter<MarksItemAdapter.ViewHolder> {
    float pixelDensity;

    List<Mark.AllData> marks;

    public MarksItemAdapter(List<Mark.AllData> marks) {
        this.marks = marks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
        MarksItem marksItem = new MarksItem(context);
        return new ViewHolder(marksItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.initializeMarksItem(marks.get(position));

        int left = (int) (30 * this.pixelDensity);
        int top = (int) (5 * this.pixelDensity);
        int right = (int) (30 * this.pixelDensity);
        int bottom = (int) (5 * this.pixelDensity);

        if (position == 0) {
            top = (int) (10 * this.pixelDensity);
        } else if (position == marks.size() - 1) {
            bottom = (int) (10 * this.pixelDensity);
        }

        holder.setMargin(left, top, right, bottom);
    }

    @Override
    public int getItemCount() {
        return marks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MarksItem marksItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.marksItem = (MarksItem) itemView;
        }

        public void initializeMarksItem(Mark.AllData markItem) {
            this.marksItem.initializeMarksItem(markItem);
        }

        public void setMargin(int left, int top, int right, int bottom) {
            this.marksItem.setMargin(left, top, right, bottom);
        }
    }
}
