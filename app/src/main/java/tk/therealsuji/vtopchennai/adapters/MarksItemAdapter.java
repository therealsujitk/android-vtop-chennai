package tk.therealsuji.vtopchennai.adapters;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.chip.Chip;

import java.text.DecimalFormat;
import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Mark;

/**
 * ┬─── Marks Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.PerformanceFragment}
 * ├─ {@link MarksAdapter}          - ViewPager2
 * ├─ {@link MarksGroupAdapter}     - RecyclerView
 * ╰→ {@link MarksItemAdapter}      - RecyclerView (Current File)
 */
public class MarksItemAdapter extends RecyclerView.Adapter<MarksItemAdapter.ViewHolder> {
    List<Mark.AllData> marks;

    public MarksItemAdapter(List<Mark.AllData> marks) {
        this.marks = marks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout marksItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_marks, parent, false);

        return new ViewHolder(marksItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setMarksItem(marks.get(position));
    }

    @Override
    public int getItemCount() {
        return marks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout marksItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.marksItem = (LinearLayout) itemView;
        }

        public void setMarksItem(Mark.AllData marksItem) {
            AppCompatTextView markTitle = this.marksItem.findViewById(R.id.text_view_title);
            AppCompatTextView averageText = this.marksItem.findViewById(R.id.text_view_average);
            AppCompatTextView statusText = this.marksItem.findViewById(R.id.text_view_status);
            AppCompatTextView scoreText = this.marksItem.findViewById(R.id.text_view_score);
            ProgressBar scoreProgress = this.marksItem.findViewById(R.id.progress_bar_score);
            AppCompatTextView markType = this.marksItem.findViewById(R.id.text_view_mark_type);
            Chip courseType = this.marksItem.findViewById(R.id.chip_course_type);

            markTitle.setText(marksItem.title);

            if (!marksItem.isRead) {
                BadgeDrawable markBadge = BadgeDrawable.create(markTitle.getContext());
                markBadge.setBadgeGravity(BadgeDrawable.TOP_START);

                markTitle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @OptIn(markerClass = ExperimentalBadgeUtils.class)
                    @Override
                    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                        markBadge.setHorizontalOffset(10 - ((View) markTitle.getParent()).getPaddingStart() / 2);
                        markBadge.setVerticalOffset(10 + markTitle.getMeasuredHeight() / 2);

                        BadgeUtils.attachBadgeDrawable(markBadge, markTitle);
                        markTitle.removeOnLayoutChangeListener(this);
                    }
                });
            } else {
                // Remove the BadgeDrawable if any (Required because RecyclerView recycles layouts)
                markTitle.getOverlay().clear();
            }

            if (marksItem.score != null && marksItem.maxScore != null && marksItem.weightage != null && marksItem.maxWeightage != null) {
                String markTypeScore = this.marksItem.getContext().getString(R.string.score);
                String score = new DecimalFormat("#.#").format(marksItem.score) + "/" + new DecimalFormat("#.#").format(marksItem.maxScore);
                String weightage = new DecimalFormat("#.#").format(marksItem.weightage) + "/" + new DecimalFormat("#.#").format(marksItem.maxWeightage);

                scoreText.setText(score);
                scoreProgress.setProgress(marksItem.score.intValue());
                scoreProgress.setMax(marksItem.maxScore.intValue());

                scoreText.setOnClickListener(view -> {
                    if (markType.getText().equals(markTypeScore)) {
                        scoreText.setText(weightage);
                        markType.setText(R.string.weightage);
                    } else {
                        scoreText.setText(score);
                        markType.setText(R.string.score);
                    }
                });
            }

            if (marksItem.average != null) {
                averageText.setText(Html.fromHtml(this.marksItem.getContext().getString(R.string.average, marksItem.average), Html.FROM_HTML_MODE_LEGACY));
                averageText.setVisibility(View.VISIBLE);
            } else {
                averageText.setVisibility(View.GONE);
            }

            if (marksItem.status != null) {
                statusText.setText(Html.fromHtml(this.marksItem.getContext().getString(R.string.status, marksItem.status), Html.FROM_HTML_MODE_LEGACY));
                statusText.setVisibility(View.VISIBLE);
            } else {
                statusText.setVisibility(View.GONE);
            }

            if (marksItem.courseType.equals("lab")) {
                courseType.setChipIconResource(R.drawable.ic_lab);
                courseType.setText(R.string.lab);
            } else if (marksItem.courseType.equals("project")) {
                courseType.setChipIconResource(R.drawable.ic_project);
                courseType.setText(R.string.project);
            } else {
                courseType.setChipIconResource(R.drawable.ic_theory);
                courseType.setText(R.string.theory);
            }
        }
    }
}
