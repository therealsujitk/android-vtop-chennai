package tk.therealsuji.vtopchennai.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tk.therealsuji.vtopchennai.models.Mark;
import tk.therealsuji.vtopchennai.widgets.MarksGroup;

public class MarksSectionAdapter extends RecyclerView.Adapter<MarksSectionAdapter.ViewHolder> {
    Map<Integer, List<Mark.AllData>> marks;
    List<Integer> sectionTypes;

    public MarksSectionAdapter(List<Mark.AllData> marks) {
        this.marks = new HashMap<>();
        this.sectionTypes = new ArrayList<>();

        for (int i = 0; i < marks.size(); ++i) {
            Mark.AllData mark = marks.get(i);
            int sectionType = MarksGroup.TYPE_THEORY;

            if (mark.courseType.equals("lab")) {
                sectionType = MarksGroup.TYPE_LAB;
            } else if (mark.courseType.equals("project")) {
                sectionType = MarksGroup.TYPE_PROJECT;
            }

            if (!this.marks.containsKey(sectionType)) {
                this.marks.put(sectionType, new ArrayList<>());
                this.sectionTypes.add(sectionType);
            }

            Objects.requireNonNull(this.marks.get(sectionType)).add(mark);
        }
    }

    @NonNull
    @Override
    public MarksSectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MarksGroup marksGroup = new MarksGroup(parent.getContext());
        return new ViewHolder(marksGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull MarksSectionAdapter.ViewHolder holder, int position) {
        Integer sectionType = this.sectionTypes.get(position);
        holder.setMarksSection(sectionType, this.marks.get(sectionType));
    }

    @Override
    public int getItemCount() {
        return this.sectionTypes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MarksGroup marksGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.marksGroup = (MarksGroup) itemView;
        }

        public void setMarksSection(int sectionType, List<Mark.AllData> marks) {
            this.marksGroup.setMarksSection(sectionType, marks);
        }
    }
}
