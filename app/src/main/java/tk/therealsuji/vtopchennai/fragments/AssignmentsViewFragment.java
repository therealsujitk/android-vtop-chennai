package tk.therealsuji.vtopchennai.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.adapters.AttachmentItemAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.MoodleApi;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsViewFragment extends Fragment {
    ProgressBar loading;
    RecyclerView submissions;

    int assignmentId, fileArea = 0;
    List<Assignment.Attachment> attachments;
    MoodleApi moodleApi;
    String moodleToken;

    public AssignmentsViewFragment() {
        // Required empty public constructor
    }

    private void getSubmissionStatus(int activityId) {
        this.setLoading(true);

        this.moodleApi.getSubmissionStatus(this.moodleToken, activityId)
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
                            attachments = new ArrayList<>();

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

                            displaySubmissions();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void downloadPreviousAttempt(Function<Object, Object> callback) {
        setLoading(true);

        if (this.fileArea != 0 || this.attachments.size() == 0) {
            callback.apply(null);
            return;
        }

        Observable.fromCallable(() -> {
            List<File> files = new ArrayList<>();

            for (Assignment.Attachment attachment : this.attachments) {
                Uri uri = Uri.parse(attachment.url)
                        .buildUpon()
                        .appendQueryParameter("token", this.moodleToken)
                        .build();
                URL url = new URL(uri.toString());
                File file = new File(requireContext().getCacheDir() + "/Moodle", attachment.name);
                file.deleteOnExit();

                if ((file.getParentFile() == null || !file.getParentFile().mkdir()) && !file.createNewFile() && !file.exists()) {
                    throw new Exception("Failed to download previous attempt.");
                }

                FileUtils.copyURLToFile(url, file);
                files.add(file);
            }

            return files;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<File>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull List<File> files) {
                        addSubmissions(files, false, callback);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void displaySubmissions() {
        this.submissions.setAdapter(new AttachmentItemAdapter(this.attachments));
    }

    private void addSubmissions(List<File> files, boolean saveSubmissions, Function<Object, Object> callback) {
        setLoading(true);

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        for (int i = 0; i < files.size(); ++i) {
            formBuilder.addFormDataPart(
                    "file" + (i + 1),
                    files.get(i).getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), files.get(i))
            );
        }

        this.moodleApi.addSubmissions(this.moodleToken, this.fileArea, formBuilder.build())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            String responseString = responseBody.string();
                            if (responseString.startsWith("{")) {
                                JSONObject response = new JSONObject(responseString);
                                if (response.has("error")) {
                                    throw new Exception(response.getString("error"));
                                }
                            }

                            JSONArray response = new JSONArray(responseString);

                            if (response.length() == 0) {
                                throw new Exception("Something went wrong.");
                            }

                            fileArea = response.getJSONObject(0).getInt("itemid");

                            if (saveSubmissions) {
                                saveSubmissions();
                            }

                            if (callback != null) {
                                callback.apply(null);
                            }

                            deleteTemporaryFiles(files);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void saveSubmissions() {
        this.moodleApi.saveSubmissions(this.moodleToken, this.assignmentId, this.fileArea)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            String responseString = responseBody.string();
                            if (responseString.startsWith("{")) {
                                JSONObject response = new JSONObject(responseString);

                                if (response.has("exception")) {
                                    throw new Exception(response.getString("message"));
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Exception: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void deleteTemporaryFiles(List<File> files) {
        boolean result = true;
        for (File file : files) {
            result = result && file.delete();
        }
    }

    private String getTimeDifference(long duration) {
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

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            this.loading.setVisibility(View.VISIBLE);
        } else {
            this.loading.setVisibility(View.GONE);
        }
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
        Button addSubmission = assignmentsViewFragment.findViewById(R.id.button_add_submission);
        TextView allowLate = assignmentsViewFragment.findViewById(R.id.text_view_allow_late);
        TextView dueDate = assignmentsViewFragment.findViewById(R.id.text_view_due_date);
        TextView dueIn = assignmentsViewFragment.findViewById(R.id.text_view_due_in);
        TextView intro = assignmentsViewFragment.findViewById(R.id.text_view_intro);
        TextView title = assignmentsViewFragment.findViewById(R.id.text_view_title);
        this.loading = assignmentsViewFragment.findViewById(R.id.progress_bar_loading);
        this.submissions = assignmentsViewFragment.findViewById(R.id.recycler_view_submissions);

        getParentFragmentManager().setFragmentResultListener("file", this, (requestKey, result) -> {
            List<File> files = new ArrayList<>();
            List<String> filePaths = result.getStringArrayList("paths");

            for (String filePath : filePaths) {
                files.add(new File(filePath));
            }

            this.downloadPreviousAttempt(o -> {
                addSubmissions(files, true, null);
                return null;
            });
        });
        back.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());
        addSubmission.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("*/*");
            ((MainActivity) requireActivity()).getRequestFileLauncher().launch(intent);
        });

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

            this.assignmentId = assignment.id;
            this.moodleApi = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .baseUrl(SettingsRepository.MOODLE_BASE_URL)
                    .client(SettingsRepository.getNaiveOkHttpClient())
                    .build()
                    .create(MoodleApi.class);

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