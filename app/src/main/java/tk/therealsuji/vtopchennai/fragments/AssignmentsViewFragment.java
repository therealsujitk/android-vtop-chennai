package tk.therealsuji.vtopchennai.fragments;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.color.MaterialColors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.adapters.AttachmentItemAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsViewFragment extends Fragment {
    ProgressBar loading;
    RecyclerView submissions;
    String moodleToken;
    TextView submissionsTitle;

    public AssignmentsViewFragment() {
        // Required empty public constructor
    }

    void getSubmissionStatus(int activityId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this.requireContext().getApplicationContext());
        String url = Uri.parse(SettingsRepository.MOODLE_WEBSERVICE_URL)
                .buildUpon()
                .appendQueryParameter("wstoken", this.moodleToken)
                .appendQueryParameter("moodlewsrestformat", "json")
                .appendQueryParameter("wsfunction", "mod_assign_get_submission_status")
                .appendQueryParameter("assignid", String.valueOf(activityId))
                .toString();

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.has("lastattempt")) {
                            return;
                        }

                        JSONObject submission = response.getJSONObject("lastattempt").getJSONObject("submission");
                        JSONArray plugins = submission.getJSONArray("plugins");
                        List<Assignment.Attachment> attachments = new ArrayList<>();

                        for (int i = 0; i < plugins.length(); ++i) {
                            JSONObject plugin = plugins.getJSONObject(i);
                            if (plugin.getString("type").equals("file")) {
                                JSONArray files = plugin.getJSONArray("fileareas");

                                if (files.length() > 0) {
                                    files = files.getJSONObject(0).getJSONArray("files");
                                }

                                for (int j = 0; j < files.length(); ++j) {
                                    JSONObject file = files.getJSONObject(j);

                                    Assignment.Attachment attachment = new Assignment.Attachment();
                                    attachment.name = file.getString("filename");
                                    attachment.mimetype = file.getString("mimetype");
                                    attachment.size = file.getInt("filesize");
                                    attachment.url = file.getString("fileurl");

                                    attachments.add(attachment);
                                }

                                break;
                            }
                        }

                        this.displaySubmissions(attachments);
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                }
        );

        requestQueue.add(stringRequest);
    }

    void displaySubmissions(List<Assignment.Attachment> attachments) {
        this.loading.setVisibility(View.GONE);
        this.submissionsTitle.setVisibility(View.VISIBLE);

        if (attachments.size() == 0) {
            float pixelDensity = this.requireContext().getResources().getDisplayMetrics().density;

            this.submissionsTitle.setText(R.string.no_submissions);
            this.submissionsTitle.setPadding(
                    0,
                    (int) (20 * pixelDensity),
                    0,
                    (int) (40 * pixelDensity)
            );
            return;
        }

        this.submissionsTitle.setText(R.string.your_submissions);
        this.submissionsTitle.setPadding(0, 0, 0, 0);
        this.submissions.setAdapter(new AttachmentItemAdapter(attachments));
    }

    String getTimeDifference(long duration) {
        StringBuilder timeRemaining = new StringBuilder();

        long days = TimeUnit.MILLISECONDS.toDays(duration);
        if (days >= 1) {
            timeRemaining.append(days).append((days == 1) ? " day " : " days ");
        }

        duration -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        if (hours >= 1) {
            timeRemaining.append(hours).append((hours == 1) ? " hour " : " hours ");
        }

        duration -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        if (minutes >= 1) {
            timeRemaining.append(minutes).append((hours == 1) ? " minute " : " minutes ");
        }

        return timeRemaining.toString().trim();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsViewFragment = inflater.inflate(R.layout.fragment_assignments_view, container, false);
        assignmentsViewFragment.getRootView().setBackgroundColor(requireContext().getColor(R.color.secondary_container_95));
        assignmentsViewFragment.getRootView().setOnTouchListener((view, motionEvent) -> true);

        LinearLayout header = assignmentsViewFragment.findViewById(R.id.linear_layout_header);
        header.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(0, windowInsets.getSystemWindowInsetTop(), 0, 0);
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        assignmentsViewFragment
                .findViewById(R.id.nested_scroll_view_assignments_view)
                .setPadding(0, 0, 0, ((MainActivity) this.requireActivity()).getSystemNavigationPadding());

        Assignment assignment = null;
        Bundle arguments = this.getArguments();
        this.moodleToken = SettingsRepository
                .getSharedPreferences(this.requireContext().getApplicationContext())
                .getString("moodleToken", "");

        if (arguments != null) {
            assignment = arguments.getParcelable("assignment");
        }

        ImageButton back = assignmentsViewFragment.findViewById(R.id.image_button_back);
        RecyclerView introAttachments = assignmentsViewFragment.findViewById(R.id.recycler_view_attachments);
        TextView allowLate = assignmentsViewFragment.findViewById(R.id.text_view_allow_late);
        TextView dueDate = assignmentsViewFragment.findViewById(R.id.text_view_due_date);
        TextView dueIn = assignmentsViewFragment.findViewById(R.id.text_view_due_in);
        TextView intro = assignmentsViewFragment.findViewById(R.id.text_view_intro);
        TextView title = assignmentsViewFragment.findViewById(R.id.text_view_title);
        this.loading = assignmentsViewFragment.findViewById(R.id.progress_bar_loading);
        this.submissions = assignmentsViewFragment.findViewById(R.id.recycler_view_submissions);
        this.submissionsTitle = assignmentsViewFragment.findViewById(R.id.text_view_submissions_title);

        back.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        if (assignment != null) {
            title.setText(assignment.title);

            if (assignment.intro.equals("")) {
                intro.setVisibility(View.GONE);
            } else {
                intro.setText(Html.fromHtml(assignment.intro, Html.FROM_HTML_MODE_LEGACY).toString().trim());
            }

            SimpleDateFormat date = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());

            try {
                String dueDateString = date.format(assignment.dueDate) + ", " + SettingsRepository.getSystemFormattedTime(
                        this.requireContext().getApplicationContext(),
                        time.format(assignment.dueDate)
                );
                dueDate.setText(dueDateString);
            } catch (Exception ignored) {
            }

            int dueInStringId = R.string.assignment_due;
            long duration = assignment.dueDate.getTime() - Calendar.getInstance().getTime().getTime();

            if (duration < 0) {
                duration *= -1;
                dueInStringId = R.string.assignment_overdue;

                int colorError = MaterialColors.getColor(dueIn, R.attr.colorError);
                TextViewCompat.setCompoundDrawableTintList(dueIn, ColorStateList.valueOf(colorError));
                dueIn.setTextColor(colorError);
            }

            dueIn.setText(Html.fromHtml(
                    getString(dueInStringId, this.getTimeDifference(duration)),
                    Html.FROM_HTML_MODE_LEGACY
            ));

            if (assignment.cutoffDate != null) {
                allowLate.setVisibility(View.VISIBLE);

                if (assignment.cutoffDate.after(assignment.dueDate)) {
                    long lateSubmission = assignment.cutoffDate.getTime() - assignment.dueDate.getTime();

                    allowLate.setText(Html.fromHtml(
                            getString(R.string.late_submission, this.getTimeDifference(lateSubmission).trim()),
                            Html.FROM_HTML_MODE_LEGACY
                    ));
                } else {
                    allowLate.setText(R.string.no_late_submission);
                }
            }

            if (assignment.introAttachments != null) {
                introAttachments.setAdapter(new AttachmentItemAdapter(assignment.introAttachments));
            }

            this.getSubmissionStatus(assignment.id);
        }

        return assignmentsViewFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((MainActivity) this.requireActivity()).showBottomNavigationView();
    }
}