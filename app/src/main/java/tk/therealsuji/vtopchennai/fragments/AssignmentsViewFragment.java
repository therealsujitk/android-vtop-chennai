package tk.therealsuji.vtopchennai.fragments;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.AttachmentItemAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.MoodleApi;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsViewFragment extends Fragment {
    ProgressBar loading;
    RecyclerView submissions;
    String moodleToken;

    public AssignmentsViewFragment() {
        // Required empty public constructor
    }

    void getSubmissionStatus(int activityId) {
        new Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(SettingsRepository.MOODLE_BASE_URL)
                .client(SettingsRepository.getNaiveOkHttpClient())
                .build()
                .create(MoodleApi.class)
                .getSubmissionStatus(this.moodleToken, activityId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            JSONObject response = new JSONObject(responseBody.string());

                            if (response.has("error")) {
                                throw new Exception(response.getString("error"));
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

                            displaySubmissions(attachments);
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    void displaySubmissions(List<Assignment.Attachment> attachments) {
        this.loading.setVisibility(View.GONE);
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
        NestedScrollView assignments = assignmentsViewFragment.findViewById(R.id.nested_scroll_view_assignments_view);
        ExtendedFloatingActionButton extendedFloatingActionButton = assignmentsViewFragment.findViewById(R.id.extended_floating_action_button);

        getParentFragmentManager().setFragmentResultListener("customInsets2", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int systemWindowInsetBottom = result.getInt("systemWindowInsetBottom");
            float pixelDensity = getResources().getDisplayMetrics().density;

            header.setPadding(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            assignments.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (systemWindowInsetBottom + 20 * pixelDensity)
            );

            ((ViewGroup.MarginLayoutParams) extendedFloatingActionButton.getLayoutParams()).setMargins(
                    0,
                    0,
                    (int) (systemWindowInsetRight + 20 * pixelDensity),
                    (int) (systemWindowInsetBottom + 20 * pixelDensity)
            );
        });

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

        back.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        if (assignment != null) {
            title.setText(assignment.title);

            if (!assignment.intro.equals("")) {
                intro.setText(Html.fromHtml(assignment.intro, Html.FROM_HTML_MODE_LEGACY).toString().trim());
            } else {
                intro.setTypeface(intro.getTypeface(), Typeface.ITALIC);
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

        Bundle bottomNavigationVisibility = new Bundle();
        bottomNavigationVisibility.putBoolean("isVisible", true);
        getParentFragmentManager().setFragmentResult("bottomNavigationVisibility", bottomNavigationVisibility);
    }
}