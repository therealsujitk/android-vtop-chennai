package com.ashish.vtopchennai.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.activities.MainActivity;
import com.ashish.vtopchennai.adapters.AttachmentsItemAdapter;
import com.ashish.vtopchennai.helpers.SettingsRepository;
import com.ashish.vtopchennai.interfaces.MoodleApi;
import com.ashish.vtopchennai.models.Assignment;
import com.ashish.vtopchennai.models.Attachment;

public class AssignmentViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    int assignmentId, fileArea = 0;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    List<Attachment> attachments;
    MoodleApi moodleApi;
    String moodleToken;

    Button addSubmission;
    ExtendedFloatingActionButton submitAssignment;
    RecyclerView submissions;
    SwipeRefreshLayout swipeRefreshLayout;

    public AssignmentViewFragment() {
        // Required empty public constructor
    }

    private void getSubmissionStatus() {
        this.setLoading(true);

        this.moodleApi.getSubmissionStatus(this.moodleToken, this.assignmentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            JSONObject response = new JSONObject(responseBody.string());
                            throwErrorIfExists(response);

                            JSONObject lastAttempt = response.getJSONObject("lastattempt");
                            JSONObject submission;

                            if (lastAttempt.has("teamsubmission")) {
                                submission = lastAttempt.getJSONObject("teamsubmission");
                            } else if (lastAttempt.has("submission")) {
                                submission = lastAttempt.getJSONObject("submission");
                            } else {
                                throw new Exception("Failed to fetch submissions");
                            }

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

                                        Attachment attachment = new Attachment();
                                        attachment.name = file.getString("filename");
                                        attachment.mimetype = file.getString("mimetype");
                                        attachment.size = file.getLong("filesize");
                                        attachment.url = file.getString("fileurl");

                                        attachments.add(attachment);
                                    }

                                    if (lastAttempt.getBoolean("canedit")) {
                                        addSubmission.setAlpha(1);
                                        addSubmission.setEnabled(true);
                                    }

                                    break;
                                }
                            }

                            if (lastAttempt.getBoolean("cansubmit")) {
                                submitAssignment.setEnabled(true);
                                submitAssignment.setVisibility(View.VISIBLE);
                            } else {
                                submitAssignment.setVisibility(View.GONE);
                            }

                            displaySubmissions(null);
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

            for (Attachment attachment : this.attachments) {
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
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<File> files) {
                        addSubmissions(files, callback);
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

    private void displaySubmissions(List<Attachment> tentativeAttachments) {
        List<Attachment> attachments = this.attachments;

        if (tentativeAttachments != null) {
            attachments = new ArrayList<>(this.attachments);
            attachments.addAll(tentativeAttachments);
        }

        this.submissions.setAdapter(new AttachmentsItemAdapter(attachments));
    }

    private void addSubmissions(List<File> files, Function<Object, Object> callback) {
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            String responseString = responseBody.string();
                            if (responseString.startsWith("{")) {
                                JSONObject response = new JSONObject(responseString);
                                throwErrorIfExists(response);
                            }

                            JSONArray response = new JSONArray(responseString);

                            if (response.length() == 0) {
                                throw new Exception("Something went wrong.");
                            }

                            for (int i = 0; i < response.length(); ++i) {
                                throwErrorIfExists(response.getJSONObject(i));
                            }

                            fileArea = response.getJSONObject(0).getInt("itemid");

                            if (callback != null) {
                                callback.apply(null);
                            }

                            deleteTemporaryFiles(files);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            displaySubmissions(null);
                            setLoading(false);
                        } finally {
                            if (fileArea != 0) {
                                saveSubmissions();
                            }
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            String responseString = responseBody.string();
                            if (responseString.startsWith("{")) {
                                JSONObject response = new JSONObject(responseString);
                                throwErrorIfExists(response);
                            }

                            getSubmissionStatus();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            displaySubmissions(null);
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

    private void submitForGrading() {
        setLoading(true);

        this.moodleApi.submitAssignmentForGrading(this.moodleToken, this.assignmentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull ResponseBody responseBody) {
                        try {
                            JSONArray response = new JSONArray(responseBody.string());

                            if (response.length() != 0) {
                                JSONObject responseObject = response.getJSONObject(0);
                                throwErrorIfExists(responseObject);
                            }

                            getSubmissionStatus();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
        this.swipeRefreshLayout.setRefreshing(isLoading);

        if (isLoading) {
            this.addSubmission.setAlpha(0.5f);
            this.addSubmission.setEnabled(false);
            this.submitAssignment.setEnabled(false);
        }
    }

    private void throwErrorIfExists(JSONObject jsonObject) throws Exception {
        if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
        } else if (jsonObject.has("message")) {
            throw new Exception(jsonObject.getString("message"));
        }
    }

    @Override
    public void onRefresh() {
        this.getSubmissionStatus();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsViewFragment = inflater.inflate(R.layout.fragment_assignments_view, container, false);
        assignmentsViewFragment.getRootView().setBackgroundColor(requireContext().getColor(R.color.secondary_container_95));
        assignmentsViewFragment.getRootView().setOnTouchListener((view, motionEvent) -> true);

        LinearLayout header = assignmentsViewFragment.findViewById(R.id.linear_layout_header);
        NestedScrollView nestedScrollView = assignmentsViewFragment.findViewById(R.id.nested_scroll_view_assignments_view);
        this.submitAssignment = assignmentsViewFragment.findViewById(R.id.extended_floating_action_button);
        float pixelDensity = getResources().getDisplayMetrics().density;

        getParentFragmentManager().setFragmentResultListener("customInsets2", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int systemWindowInsetBottom = result.getInt("systemWindowInsetBottom");

            header.setPadding(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            nestedScrollView.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (systemWindowInsetBottom + 20 * pixelDensity)
            );

            ((ViewGroup.MarginLayoutParams) submitAssignment.getLayoutParams()).setMargins(
                    0,
                    0,
                    (int) (systemWindowInsetRight + 20 * pixelDensity),
                    (int) (systemWindowInsetBottom + 20 * pixelDensity)
            );
        });

        Assignment assignment = null;
        Bundle arguments = this.getArguments();
        this.moodleToken = Objects.requireNonNull(SettingsRepository
                .getEncryptedSharedPreferences(this.requireContext().getApplicationContext()))
                .getString("moodleToken", null);

        if (arguments != null) {
            assignment = arguments.getParcelable("assignment");
        }

        RecyclerView introAttachments = assignmentsViewFragment.findViewById(R.id.recycler_view_attachments);
        TextView allowLate = assignmentsViewFragment.findViewById(R.id.text_view_allow_late);
        TextView dueDate = assignmentsViewFragment.findViewById(R.id.text_view_due_date);
        TextView dueIn = assignmentsViewFragment.findViewById(R.id.text_view_due_in);
        TextView intro = assignmentsViewFragment.findViewById(R.id.text_view_intro);
        TextView title = assignmentsViewFragment.findViewById(R.id.text_view_title);
        View back = assignmentsViewFragment.findViewById(R.id.image_button_back);
        this.addSubmission = assignmentsViewFragment.findViewById(R.id.button_add_submission);
        this.submissions = assignmentsViewFragment.findViewById(R.id.recycler_view_submissions);
        this.swipeRefreshLayout = assignmentsViewFragment.findViewById(R.id.swipe_refresh_layout);

        this.swipeRefreshLayout.setColorSchemeColors(MaterialColors.getColor(this.swipeRefreshLayout, R.attr.colorSurface));
        this.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(MaterialColors.getColor(this.swipeRefreshLayout, R.attr.colorPrimary));
        this.swipeRefreshLayout.setOnRefreshListener(this);

        Rect compoundDrawableBounds = new Rect(0, 0, (int) (24 * pixelDensity), (int) (24 * pixelDensity));
        allowLate.getCompoundDrawablesRelative()[0].setBounds(compoundDrawableBounds);
        dueIn.getCompoundDrawablesRelative()[0].setBounds(compoundDrawableBounds);

        getParentFragmentManager().setFragmentResultListener("file", this, (requestKey, result) -> {
            List<File> files = new ArrayList<>();
            List<Attachment> tentativeAttachments = new ArrayList<>();
            List<String> filePaths = result.getStringArrayList("paths");

            for (String filePath : filePaths) {
                File file = new File(filePath);
                files.add(file);

                Attachment attachment = new Attachment();
                attachment.name = file.getName();
                attachment.size = file.length();

                tentativeAttachments.add(attachment);
            }

            displaySubmissions(tentativeAttachments);
            this.downloadPreviousAttempt(o -> {
                addSubmissions(files, null);
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
        submitAssignment.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.submit_assignment)
                .setMessage(Html.fromHtml(getString(R.string.submit_confirmation, this.attachments.size()), Html.FROM_HTML_MODE_LEGACY))
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton(R.string.submit, (dialogInterface, i) -> submitForGrading())
                .show());

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY + 12) {
                this.submitAssignment.shrink();
            } else if (scrollY == 0 || scrollY < oldScrollY - 12) {
                this.submitAssignment.extend();
            }
        });

        if (assignment != null) {
            title.setText(assignment.title);

            if (!assignment.intro.equals("")) {
                intro.setText(Html.fromHtml(assignment.intro, Html.FROM_HTML_MODE_LEGACY).toString().trim());
            } else {
                intro.setTypeface(intro.getTypeface(), Typeface.ITALIC);
            }

            if (assignment.dueDate != null) {
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
                long duration = assignment.dueDate - Calendar.getInstance().getTime().getTime();

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

                    if (assignment.cutoffDate > assignment.dueDate) {
                        long lateSubmission = assignment.cutoffDate - assignment.dueDate;

                        allowLate.setText(Html.fromHtml(
                                getString(R.string.late_submission, this.getTimeDifference(lateSubmission).trim()),
                                Html.FROM_HTML_MODE_LEGACY
                        ));
                    } else {
                        allowLate.setText(R.string.no_late_submission);
                    }
                }
            } else {
                dueDate.setText(R.string.no_due_date);
                dueIn.setText(R.string.assignment_no_due);
            }

            if (assignment.introAttachments != null) {
                introAttachments.setAdapter(new AttachmentsItemAdapter(assignment.introAttachments));
            }

            this.assignmentId = assignment.id;
            this.moodleApi = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .baseUrl(SettingsRepository.MOODLE_BASE_URL)
                    .build()
                    .create(MoodleApi.class);

            this.getSubmissionStatus();
        }

        return assignmentsViewFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bundle bottomNavigationVisibility = new Bundle();
        bottomNavigationVisibility.putBoolean("isVisible", true);
        getParentFragmentManager().setFragmentResult("bottomNavigationVisibility", bottomNavigationVisibility);
        compositeDisposable.dispose();
    }
}
