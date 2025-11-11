package tk.therealsuji.vtopchennai.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.GpaSubject;

public class GpaSubjectAdapter extends RecyclerView.Adapter<GpaSubjectAdapter.ViewHolder> {
    public interface OnRemoveSubjectListener {
        void onRemove(int position);
    }

    private final List<GpaSubject> subjects;
    private final OnRemoveSubjectListener onRemoveSubjectListener;
    private final DecimalFormat decimalFormat;

    public GpaSubjectAdapter(List<GpaSubject> subjects, OnRemoveSubjectListener onRemoveSubjectListener) {
        this.subjects = subjects;
        this.onRemoveSubjectListener = onRemoveSubjectListener;
        this.decimalFormat = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.getDefault()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_gpa_subject, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GpaSubject subject = this.subjects.get(position);
        holder.bind(subject, this.decimalFormat, this.onRemoveSubjectListener);
    }

    @Override
    public int getItemCount() {
        return this.subjects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectName;
        private final TextView gradeValue;
        private final TextView creditsValue;
        private final TextView pointsValue;
        private final ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.subjectName = itemView.findViewById(R.id.text_view_subject_name);
            this.gradeValue = itemView.findViewById(R.id.text_view_grade_value);
            this.creditsValue = itemView.findViewById(R.id.text_view_credits_value);
            this.pointsValue = itemView.findViewById(R.id.text_view_points_value);
            this.removeButton = itemView.findViewById(R.id.image_button_remove);
        }

        public void bind(GpaSubject subject, DecimalFormat decimalFormat, OnRemoveSubjectListener onRemoveSubjectListener) {
            String name = subject.getSubjectName();
            int credits = subject.getCredits();
            String creditsText = itemView.getResources().getQuantityString(R.plurals.gpa_credit_quantity, credits, credits);

            if (TextUtils.isEmpty(name)) {
                this.subjectName.setText(itemView.getContext().getString(
                        R.string.gpa_grade_label) + ": " + subject.getGrade() + " • " + creditsText);
            } else {
                this.subjectName.setText(name);
            }

            this.gradeValue.setText(subject.getGrade());
            this.creditsValue.setText(creditsText);
            this.pointsValue.setText(decimalFormat.format(subject.getGradePoints()));

            this.removeButton.setOnClickListener(view -> {
                if (onRemoveSubjectListener != null) {
                    int adapterPosition = getAdapterPosition();

                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onRemoveSubjectListener.onRemove(adapterPosition);
                    }
                }
            });
        }
    }
}

