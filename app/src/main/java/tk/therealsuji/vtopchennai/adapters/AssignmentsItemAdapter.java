package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsItemAdapter extends RecyclerView.Adapter<AssignmentsItemAdapter.ViewHolder> {
    List<Assignment> assignments;

    public AssignmentsItemAdapter(List<Assignment> assignments) {
        assignments.sort(Comparator.comparing(assignment -> assignment.dueDate));
        this.assignments = assignments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout assignmentItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_assignments, parent, false);

        return new ViewHolder(assignmentItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setAssignment(this.assignments.get(position));
    }

    @Override
    public int getItemCount() {
        return this.assignments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout assignmentsItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.assignmentsItem = (LinearLayout) itemView;
        }

        public void setAssignment(Assignment assignmentsItem) {
            TextView title = this.assignmentsItem.findViewById(R.id.text_view_title);
            TextView course = this.assignmentsItem.findViewById(R.id.text_view_course);
            ImageView pastDue = this.assignmentsItem.findViewById(R.id.image_view_past_due);

            title.setText(assignmentsItem.title);
            course.setText(assignmentsItem.course);

            if (assignmentsItem.dueDate.before(Calendar.getInstance().getTime())) {
                pastDue.setVisibility(View.VISIBLE);
            }
        }
    }
}
