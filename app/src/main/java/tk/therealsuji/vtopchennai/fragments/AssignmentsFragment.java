package tk.therealsuji.vtopchennai.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.AssignmentsGroupAdapter;
import tk.therealsuji.vtopchennai.adapters.EmptyStateAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.MoodleApi;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsFragment extends Fragment {
    int moodleUserId;
    List<Assignment> assignments;
    MoodleApi moodleApi;
    String moodleToken;

    RecyclerView assignmentGroups;
    View imageButtonSync, progressBarLoading;

    public AssignmentsFragment() {
        // Required empty public constructor
    }

    /**
     * Function to get the moodle user id
     */
    private void getUserId() {
        this.setLoading(true);
        this.moodleApi.getUserId(this.moodleToken)
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

                            moodleUserId = response.getInt("userid");
                            getCourses();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setLoading(false);
                    }
                });
    }

    /**
     * Function to get a list of course ids
     */
    private void getCourses() {
        this.moodleApi.getCourses(this.moodleToken)
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

                            if (!response.has("courses")) {
                                throw new Exception(response.getString("error"));
                            }

                            JSONArray courses = response.getJSONArray("courses");
                            List<Integer> courseIds = new ArrayList<>();

                            for (int i = 0; i < courses.length(); ++i) {
                                JSONObject course = courses.getJSONObject(i);
                                courseIds.add(course.getInt("id"));
                            }

                            getAssignments(courseIds);
                        } catch (Exception ignored) {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setLoading(false);
                    }
                });
    }

    /**
     * Function to get all the assignments for a list of course ids
     *
     * @param courseIds The list of course ids
     */
    private void getAssignments(List<Integer> courseIds) {
        this.moodleApi.getAssignments(this.moodleToken, courseIds)
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

                            JSONArray coursesArray = response.getJSONArray("courses");
                            Map<Integer, Assignment> assignments = new HashMap<>();

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, -300);
                            Date dueLimit = calendar.getTime();

                            for (int i = 0; i < coursesArray.length(); ++i) {
                                JSONObject courseObject = coursesArray.getJSONObject(i);
                                JSONArray assignmentsArray = courseObject.getJSONArray("assignments");

                                String course = courseObject.getString("fullname");
                                boolean hasAssignments = false;

                                course = course.replaceAll("\\(", " (");
                                course = course.replaceAll("\\)", ") ");
                                course = course.trim().replaceAll(" +", " ");

                                for (int j = 0; j < assignmentsArray.length(); ++j) {
                                    JSONObject assignmentObject = assignmentsArray.getJSONObject(j);

                                    Date dueDate = new Date(assignmentObject.getLong("duedate") * 1000);
                                    Date cutoffDate = (assignmentObject.getLong("cutoffdate") != 0)
                                            ? new Date(assignmentObject.getLong("cutoffdate") * 1000)
                                            : null;

                                    if (dueDate.before(dueLimit)) {
                                        continue;
                                    }

                                    int id = assignmentObject.getInt("id");
                                    int activityId = assignmentObject.getInt("cmid");
                                    String title = assignmentObject.getString("name");
                                    String intro = assignmentObject.getString("intro");

                                    Assignment assignment = new Assignment();
                                    assignment.id = id;
                                    assignment.activityId = activityId;
                                    assignment.course = course;
                                    assignment.title = title;
                                    assignment.intro = intro;
                                    assignment.dueDate = dueDate;
                                    assignment.cutoffDate = cutoffDate;

                                    JSONArray introAttachments = assignmentObject.getJSONArray("introattachments");
                                    assignment.introAttachments = (introAttachments.length() != 0) ? new ArrayList<>() : null;

                                    for (int k = 0; k < introAttachments.length(); ++k) {
                                        JSONObject attachmentObject = introAttachments.getJSONObject(k);

                                        Assignment.Attachment attachment = new Assignment.Attachment();
                                        attachment.name = attachmentObject.getString("filename");
                                        attachment.mimetype = attachmentObject.getString("mimetype");
                                        attachment.url = attachmentObject.getString("fileurl");
                                        attachment.size = attachmentObject.getInt("filesize");

                                        assignment.introAttachments.add(attachment);
                                    }

                                    assignments.put(activityId, assignment);
                                    hasAssignments = true;
                                }

                                if (!hasAssignments) {
                                    int courseId = courseObject.getInt("id");
                                    courseIds.remove(Integer.valueOf(courseId));
                                }
                            }

                            filterAssignmentsByCompletion(courseIds, assignments);
                        } catch (Exception ignored) {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setLoading(false);
                    }
                });
    }

    /**
     * Function to remove activities if they have been marked as completed
     *
     * @param courseIds The list of course ids
     */
    private void filterAssignmentsByCompletion(List<Integer> courseIds, Map<Integer, Assignment> assignmentsMap) {
        List<Observable<ResponseBody>> singleSources = new ArrayList<>();

        for (Integer courseId : courseIds) {
            singleSources.add(Observable.fromSingle(
                    this.moodleApi.getAssignmentsCompletionStatus(
                            this.moodleToken,
                            courseId,
                            this.moodleUserId
                    )
            ));
        }

        Observable.merge(singleSources)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            JSONObject response = new JSONObject(responseBody.string());

                            if (response.has("error")) {
                                throw new Exception(response.getString("error"));
                            }

                            JSONArray statusArray = response.getJSONArray("statuses");

                            for (int j = 0; j < statusArray.length(); ++j) {
                                JSONObject statusObject = statusArray.getJSONObject(j);

                                int activityId = statusObject.getInt("cmid");
                                int state = statusObject.getInt("state");

                                if (state == 0 && assignmentsMap.containsKey(activityId)) {
                                    assignments.add(assignmentsMap.get(activityId));
                                }
                            }
                        } catch (Exception ignored) {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setLoading(false);
                    }

                    @Override
                    public void onComplete() {
                        if (assignments.size() == 0) {
                            assignmentGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_NO_ASSIGNMENTS));
                        } else {
                            displayAssignments();
                        }

                        setLoading(false);
                    }
                });
    }

    private void displayAssignments() {
        try {
            this.assignmentGroups.setAdapter(new AssignmentsGroupAdapter(this.assignments));
        } catch (Exception e) {
            this.assignmentGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, e.getLocalizedMessage()));
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            this.imageButtonSync.setVisibility(View.INVISIBLE);
            this.progressBarLoading.setVisibility(View.VISIBLE);
        } else {
            this.imageButtonSync.setVisibility(View.VISIBLE);
            this.progressBarLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsFragment = inflater.inflate(R.layout.fragment_assignments, container, false);

        this.assignmentGroups = assignmentsFragment.findViewById(R.id.recycler_view_assignment_groups);
        View appBarLayout = assignmentsFragment.findViewById(R.id.app_bar);

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");
            float pixelDensity = getResources().getDisplayMetrics().density;

            appBarLayout.setPadding(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            this.assignmentGroups.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (bottomNavigationHeight + 20 * pixelDensity)
            );

            // Only one listener can be added per requestKey, so we create a duplicate
            getParentFragmentManager().setFragmentResult("customInsets2", result);
        });

        SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(this.requireContext().getApplicationContext());
        this.moodleToken = sharedPreferences.getString("moodleToken", "");
        this.imageButtonSync = assignmentsFragment.findViewById(R.id.image_button_sync);
        this.progressBarLoading = assignmentsFragment.findViewById(R.id.progress_bar_loading);

        this.imageButtonSync.setOnClickListener(view -> this.getUserId());
        TooltipCompat.setTooltipText(this.imageButtonSync, this.imageButtonSync.getContentDescription());

        if (SettingsRepository.isMoodleSignedIn(requireContext())) {
            this.assignmentGroups.setAdapter(new EmptyStateAdapter(
                    EmptyStateAdapter.TYPE_FETCHING_DATA,
                    getString(R.string.fetch_assignments)
            ));
            this.moodleApi = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .baseUrl(SettingsRepository.MOODLE_BASE_URL)
                    .client(SettingsRepository.getNaiveOkHttpClient())
                    .build()
                    .create(MoodleApi.class);
            this.assignments = new ArrayList<>();
            this.getUserId();
        } else {
            this.assignmentGroups.setAdapter(new EmptyStateAdapter(
                    EmptyStateAdapter.TYPE_NOT_AUTHENTICATED,
                    null,
                    new EmptyStateAdapter.OnClickListener() {
                        @Override
                        public void onClick() {
                            MoodleLoginDialogFragment moodleLoginDialogFragment = new MoodleLoginDialogFragment();

                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.add(android.R.id.content, moodleLoginDialogFragment).addToBackStack(null).commit();
                        }

                        @Override
                        public int getButtonTextId() {
                            return R.string.moodle_sign_in;
                        }
                    }
            ));
            this.assignmentGroups.setOverScrollMode(View.OVER_SCROLL_NEVER);
            this.imageButtonSync.setVisibility(View.GONE);
            this.progressBarLoading.setVisibility(View.GONE);
        }

        return assignmentsFragment;
    }
}