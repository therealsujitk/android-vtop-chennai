package tk.therealsuji.vtopchennai.adapters;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Exam;

/**
 * ┬─── Spotlight Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment}
 * ├─ {@link ExamsAdapter}      - ViewPager2
 * ╰→ {@link ExamsItemAdapter}  - RecyclerView (Current File)
 */
public class ExamsItemAdapter extends RecyclerView.Adapter<ExamsItemAdapter.ViewHolder> {
    List<Exam.AllData> exams = new ArrayList<>();

    public ExamsItemAdapter(List<Exam.AllData> exams) {
        Map<String, Exam.AllData> examsMap = new HashMap<>();

        for (int i = 0; i < exams.size(); ++i) {
            Exam.AllData exam = exams.get(i);

            if (!examsMap.containsKey(exam.courseCode)) {
                exam.slots = new ArrayList<>();
                examsMap.put(exam.courseCode, exam);
                this.exams.add(exam);
            }

            Objects.requireNonNull(examsMap.get(exam.courseCode)).slots.add(exam.slot);
        }
    }

    @NonNull
    @Override
    public ExamsItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout examItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_exams, parent, false);

        return new ViewHolder(examItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamsItemAdapter.ViewHolder holder, int position) {
        holder.setExamItem(exams.get(position));
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout examItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.examItem = (LinearLayout) itemView;
        }

        public void setExamItem(Exam.AllData examItem) {
            TextView courseTitle = this.examItem.findViewById(R.id.text_view_course_title);
            TextView courseCode = this.examItem.findViewById(R.id.text_view_course_code);
            TextView date = this.examItem.findViewById(R.id.text_view_date);
            TextView timings = this.examItem.findViewById(R.id.text_view_timings);
            TextView venue = this.examItem.findViewById(R.id.text_view_venue);
            ChipGroup slots = this.examItem.findViewById(R.id.chip_group_slots);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            courseTitle.setText(examItem.courseTitle);
            courseCode.setText(examItem.courseCode);

            if (examItem.startTime != null) {
                date.setText(Html.fromHtml(this.examItem.getContext().getString(R.string.date, dateFormat.format(examItem.startTime).toUpperCase(Locale.ENGLISH)), Html.FROM_HTML_MODE_LEGACY));
                date.setVisibility(View.VISIBLE);
            }

            if (examItem.startTime != null && examItem.endTime != null) {
                String startTime = timeFormat.format(examItem.startTime);
                String endTime = timeFormat.format(examItem.endTime);

                try {
                    startTime = SettingsRepository.getSystemFormattedTime(this.examItem.getContext(), startTime);
                    endTime = SettingsRepository.getSystemFormattedTime(this.examItem.getContext(), endTime);
                } catch (Exception ignored) {
                }

                timings.setText(Html.fromHtml(this.examItem.getContext().getString(R.string.timings, startTime, endTime), Html.FROM_HTML_MODE_LEGACY));
                timings.setVisibility(View.VISIBLE);

                if (Calendar.getInstance().getTime().after(new Date(examItem.endTime))) {
                    this.examItem.setAlpha(0.7f);
                }
            }

            if (examItem.venue != null && (examItem.seatLocation != null || examItem.seatNumber != null)) {
                String venueString = examItem.venue;

                if (examItem.seatLocation == null) {
                    venueString = venueString + " | " + examItem.seatNumber;
                } else if (examItem.seatNumber == null) {
                    venueString = venueString + " | " + examItem.seatLocation;
                } else {
                    venueString = venueString + " | " + examItem.seatLocation + " - " + examItem.seatNumber;
                }

                venue.setText(Html.fromHtml(this.examItem.getContext().getString(R.string.venue, venueString), Html.FROM_HTML_MODE_LEGACY));
                venue.setVisibility(View.VISIBLE);
            }

            slots.removeAllViews();

            for (int i = 0; i < examItem.slots.size(); ++i) {
                Chip slot = new Chip(this.examItem.getContext());
                slot.setChipIconResource(R.drawable.ic_theory);
                slot.setText(examItem.slots.get(i));

                slots.addView(slot);
            }
        }
    }
}
