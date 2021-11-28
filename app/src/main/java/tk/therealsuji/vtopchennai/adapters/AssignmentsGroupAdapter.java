package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsGroupAdapter extends RecyclerView.Adapter<AssignmentsGroupAdapter.ViewHolder> {
    Map<Date, List<Assignment>> assignments;
    List<Date> dates;

    public AssignmentsGroupAdapter(List<Assignment> assignments) throws ParseException {
        this.assignments = new HashMap<>();
        this.dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (int i = 0; i < assignments.size(); ++i) {
            Assignment assignment = assignments.get(i);
            Date date = dateFormat.parse(dateFormat.format(assignment.dueDate));

            if (!this.assignments.containsKey(date)) {
                this.assignments.put(date, new ArrayList<>());
                this.dates.add(date);
            }

            Objects.requireNonNull(this.assignments.get(date)).add(assignment);
        }

        Collections.sort(dates);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout assignmentGroup = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_group_assignments, parent, false);

        return new ViewHolder(assignmentGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = dates.get(position);
        holder.setAssignment(date, Objects.requireNonNull(this.assignments.get(date)));

        if (position == this.getItemCount() - 1) {
            holder.setPaddingBottom(0);
        }
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout assignmentsGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.assignmentsGroup = (LinearLayout) itemView;
        }

        public void setAssignment(Date date, List<Assignment> assignmentItems) {
            TextView dateView = this.assignmentsGroup.findViewById(R.id.text_view_date);
            RecyclerView assignmentItemsView = this.assignmentsGroup.findViewById(R.id.recycler_view_assignment_items);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());

            dateView.setText(dateFormat.format(date));
            assignmentItemsView.setAdapter(new AssignmentsItemAdapter(assignmentItems));
        }

        public void setPaddingBottom(int paddingBottom) {
            this.assignmentsGroup.setPadding(0, 0, 0, paddingBottom);
        }
    }
}
