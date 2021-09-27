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

import tk.therealsuji.vtopchennai.models.Marks;
import tk.therealsuji.vtopchennai.widgets.MarksSection;

public class MarksSectionAdapter extends RecyclerView.Adapter<MarksSectionAdapter.ViewHolder> {
    Map<Integer, List<Marks>> marks;
    List<Integer> sectionTypes;

    public MarksSectionAdapter(List<Marks> marks) {
        this.marks = new HashMap<>();
        this.sectionTypes = new ArrayList<>();

        for (int i = 0; i < marks.size(); ++i) {
            Marks mark = marks.get(i);
            int sectionType = MarksSection.TYPE_THEORY;

            if (mark.type.toLowerCase().contains("lab")) {
                sectionType = MarksSection.TYPE_LAB;
            } else if (mark.type.toLowerCase().contains("project")) {
                sectionType = MarksSection.TYPE_PROJECT;
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
        MarksSection marksSection = new MarksSection(parent.getContext());
        return new ViewHolder(marksSection);
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
        MarksSection marksSection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.marksSection = (MarksSection) itemView;
        }

        public void setMarksSection(int sectionType, List<Marks> marks) {
            this.marksSection.setMarksSection(sectionType, marks);
        }
    }
}
