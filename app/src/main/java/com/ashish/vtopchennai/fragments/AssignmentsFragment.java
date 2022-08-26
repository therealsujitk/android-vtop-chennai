package com.ashish.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.color.MaterialColors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.adapters.AssignmentsGroupAdapter;
import com.ashish.vtopchennai.adapters.EmptyStateAdapter;
import com.ashish.vtopchennai.fragments.dialogs.MoodleLoginDialogFragment;
import com.ashish.vtopchennai.helpers.AppDatabase;
import com.ashish.vtopchennai.helpers.SettingsRepository;
import com.ashish.vtopchennai.interfaces.MoodleApi;
import com.ashish.vtopchennai.models.Assignment;
import com.ashish.vtopchennai.models.Attachment;

public class AssignmentsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    int moodleUserId;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MoodleApi moodleApi;
    String moodleToken;

    RecyclerView assignmentGroups;
    SwipeRefreshLayout swipeRefreshLayout;

    public AssignmentsFragment() {
        // Required empty public constructor
    }

    /**
     * Function to get the moodle user id
     */
    private void getUserId() {
        this.setLoading(true);
        this.moodleApi.getUserId(this.moodleToken)
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

                            moodleUserId = response.getInt("userid");
                            getCourses();
                        } catch (Exception e) {
                            if (e.getMessage() != null && e.getMessage().contains("token")) {
                                SettingsRepository.signOutMoodle(requireContext());
                                displaySignInPage();
                                Toast.makeText(getContext(), "You've been signed out of Moodle. Please sign in again.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }

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

    /**
     * Function to get a list of course ids
     */
    private void getCourses() {
        this.moodleApi.getCourses(this.moodleToken)
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

                            JSONArray courses = response.getJSONArray("courses");
                            List<Integer> courseIds = new ArrayList<>();

                            for (int i = 0; i < courses.length(); ++i) {
                                JSONObject course = courses.getJSONObject(i);
                                courseIds.add(course.getInt("id"));
                            }

                            getAssignments(courseIds);
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

    /**
     * Function to get all the assignments for a list of course ids
     *
     * @param courseIds The list of course ids
     */
    private void getAssignments(List<Integer> courseIds) {
        this.moodleApi.getAssignments(this.moodleToken, courseIds)
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

                            JSONArray coursesArray = response.getJSONArray("courses");
                            Map<Integer, Assignment> assignments = new HashMap<>();

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, -300);
                            Date dueLimit = calendar.getTime();

                            for (int i = 0, attachmentId = 0; i < coursesArray.length(); ++i) {
                                JSONObject courseObject = coursesArray.getJSONObject(i);
                                JSONArray assignmentsArray = courseObject.getJSONArray("assignments");

                                String course = courseObject.getString("fullname");
                                boolean hasAssignments = false;

                                course = course.replaceAll("\\(", " (");
                                course = course.replaceAll("\\)", ") ");
                                course = course.trim().replaceAll(" +", " ");

                                for (int j = 0; j < assignmentsArray.length(); ++j) {
                                    JSONObject assignmentObject = assignmentsArray.getJSONObject(j);

                                    Long cutoffDate = assignmentObject.getLong("cutoffdate") != 0
                                            ? assignmentObject.getLong("cutoffdate") * 1000
                                            : null;
                                    Long dueDate = assignmentObject.getLong("duedate") != 0
                                            ? assignmentObject.getLong("duedate") * 1000
                                            : null;

                                    if (dueDate == null && cutoffDate != null) {
                                        dueDate = cutoffDate;
                                    }

                                    if (dueDate != null && dueDate < dueLimit.getTime()) {
                                        continue;
                                    }

                                    int id = assignmentObject.getInt("id");
                                    int activityId = assignmentObject.getInt("cmid");
                                    String title = assignmentObject.getString("name");
                                    String intro = assignmentObject.getString("intro");

                                    Assignment assignment = new Assignment();
                                    assignment.id = id;
                                    assignment.course = course;
                                    assignment.title = title;
                                    assignment.intro = intro;
                                    assignment.dueDate = dueDate;
                                    assignment.cutoffDate = cutoffDate;

                                    JSONArray introAttachments = assignmentObject.getJSONArray("introattachments");
                                    assignment.introAttachments = (introAttachments.length() != 0) ? new ArrayList<>() : null;

                                    for (int k = 0; k < introAttachments.length(); ++k) {
                                        JSONObject attachmentObject = introAttachments.getJSONObject(k);

                                        Attachment attachment = new Attachment();
                                        attachment.id = ++attachmentId;
                                        attachment.assignmentId = assignment.id;
                                        attachment.name = attachmentObject.getString("filename");
                                        attachment.mimetype = attachmentObject.getString("mimetype");
                                        attachment.url = attachmentObject.getString("fileurl");
                                        attachment.size = attachmentObject.getLong("filesize");

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

    /**
     * Function to remove activities if they have been marked as completed
     *
     * @param courseIds The list of course ids
     */
    private void filterAssignmentsByCompletion(List<Integer> courseIds, Map<Integer, Assignment> assignmentsMap) {
        List<Assignment> assignments = new ArrayList<>();
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            JSONObject response = new JSONObject(responseBody.string());
                            throwErrorIfExists(response);

                            JSONArray statusArray = response.getJSONArray("statuses");

                            for (int j = 0; j < statusArray.length(); ++j) {
                                JSONObject statusObject = statusArray.getJSONObject(j);

                                int activityId = statusObject.getInt("cmid");
                                int state = statusObject.getInt("state");

                                if (state == 0 && assignmentsMap.containsKey(activityId)) {
                                    assignments.add(assignmentsMap.get(activityId));
                                }
                            }
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

                    @Override
                    public void onComplete() {
                        if (assignments.size() == 0) {
                            assignmentGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_NO_ASSIGNMENTS));
                        } else {
                            displayAssignments(assignments);
                        }

                        AppDatabase.getInstance(requireContext().getApplicationContext())
                                .assignmentsDao()
                                .insert(assignments)
                                .subscribeOn(Schedulers.io())
                                .subscribe();

                        setLoading(false);
                    }
                });
    }

    private void displayAssignments(List<Assignment> assignments) {
        try {
            this.assignmentGroups.setAdapter(new AssignmentsGroupAdapter(assignments));
        } catch (Exception e) {
            this.assignmentGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_ERROR, e.getLocalizedMessage()));
        }
    }

    private void setLoading(boolean isLoading) {
        this.swipeRefreshLayout.setRefreshing(isLoading);
    }

    private void throwErrorIfExists(JSONObject jsonObject) throws Exception {
        if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
        } else if (jsonObject.has("message")) {
            throw new Exception(jsonObject.getString("message"));
        }
    }

    private void displaySignInPage() {
        this.assignmentGroups.setAdapter(new EmptyStateAdapter(
                EmptyStateAdapter.TYPE_NOT_AUTHENTICATED,
                null,
                new EmptyStateAdapter.ButtonAttributes() {
                    @Override
                    public void onClick() {
                        MoodleLoginDialogFragment moodleLoginDialogFragment = new MoodleLoginDialogFragment(o -> {
                            displayAssignmentsPage();
                            return null;
                        });

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
        this.swipeRefreshLayout.setEnabled(false);
    }

    private void displayAssignmentsPage() {
        this.moodleToken = Objects.requireNonNull(SettingsRepository
                .getEncryptedSharedPreferences(this.requireContext().getApplicationContext()))
                .getString("moodleToken", null);

        if (this.assignmentGroups.getAdapter() != null
                && (this.assignmentGroups.getAdapter().getClass() != AssignmentsGroupAdapter.class
                || this.assignmentGroups.getAdapter().getItemCount() == 0)) {
            this.assignmentGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_FETCHING_ASSIGNMENTS));
        }

        this.assignmentGroups.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        this.swipeRefreshLayout.setEnabled(true);

        this.moodleApi = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(SettingsRepository.MOODLE_BASE_URL)
                .build()
                .create(MoodleApi.class);

        this.getUserId();
    }

    @Override
    public void onRefresh() {
        this.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsFragment = inflater.inflate(R.layout.fragment_assignments, container, false);

        this.assignmentGroups = assignmentsFragment.findViewById(R.id.recycler_view_assignment_groups);
        this.swipeRefreshLayout = assignmentsFragment.findViewById(R.id.swipe_refresh_layout);
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

        this.swipeRefreshLayout.setColorSchemeColors(MaterialColors.getColor(this.swipeRefreshLayout, R.attr.colorSurface));
        this.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(MaterialColors.getColor(this.swipeRefreshLayout, R.attr.colorPrimary));
        this.swipeRefreshLayout.setOnRefreshListener(this);

        if (SettingsRepository.isMoodleSignedIn(requireContext())) {
            AppDatabase.getInstance(requireContext().getApplicationContext())
                    .assignmentsDao()
                    .get()
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<List<Assignment>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(@NonNull List<Assignment> assignments) {
                            if (assignments.size() == 0) {
                                assignmentGroups.setAdapter(new EmptyStateAdapter(EmptyStateAdapter.TYPE_FETCHING_ASSIGNMENTS));
                            } else {
                                displayAssignments(assignments);
                            }

                            displayAssignmentsPage();
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                        }
                    });
        } else {
            this.displaySignInPage();
        }

        return assignmentsFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.dispose();
    }
}
