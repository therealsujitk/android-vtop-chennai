package tk.therealsuji.vtopchennai.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.fragments.AssignmentsViewFragment;
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

            this.assignmentsItem.setOnClickListener(view -> {
                AssignmentsViewFragment assignmentsViewFragment = new AssignmentsViewFragment();
                FragmentActivity fragmentActivity = (FragmentActivity) this.assignmentsItem.getContext();

                Bundle bundle = new Bundle();
                bundle.putParcelable("assignment", assignmentsItem);

                assignmentsViewFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                        .add(R.id.frame_layout_fragment_container, assignmentsViewFragment)
                        .addToBackStack(null)
                        .commit();

                ((MainActivity) fragmentActivity).hideBottomNavigationView();
            });
        }
    }
}
