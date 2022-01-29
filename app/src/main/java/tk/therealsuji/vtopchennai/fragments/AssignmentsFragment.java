package tk.therealsuji.vtopchennai.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.AssignmentsGroupAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Assignment;

public class AssignmentsFragment extends Fragment {
    int moodleUserId;
    List<Assignment> assignments;
    String moodleToken;
    RecyclerView assignmentGroups;
    RequestQueue requestQueue;

    public AssignmentsFragment() {
        // Required empty public constructor
    }

    /**
     * Function to get the moodle user id
     */
    private void getUserId() {
        String url = Uri.parse(SettingsRepository.MOODLE_WEBSERVICE_URL)
                .buildUpon()
                .appendQueryParameter("wstoken", moodleToken)
                .appendQueryParameter("moodlewsrestformat", "json")
                .appendQueryParameter("wsfunction", "core_webservice_get_site_info")
                .toString();

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.has("userid")) {
                            return;
                        }

                        this.moodleUserId = response.getInt("userid");
                        this.getCourses();
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                }
        );

        this.requestQueue.add(stringRequest);
    }

    /**
     * Function to get a list of course ids
     */
    private void getCourses() {
        String url = Uri.parse(SettingsRepository.MOODLE_WEBSERVICE_URL)
                .buildUpon()
                .appendQueryParameter("wstoken", this.moodleToken)
                .appendQueryParameter("moodlewsrestformat", "json")
                .appendQueryParameter("wsfunction", "core_course_get_enrolled_courses_by_timeline_classification")
                .appendQueryParameter("classification", "inprogress")
                .toString();

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.has("courses")) {
                            return;
                        }

                        JSONArray courses = response.getJSONArray("courses");
                        List<Integer> courseIds = new ArrayList<>();

                        for (int i = 0; i < courses.length(); ++i) {
                            JSONObject course = courses.getJSONObject(i);
                            courseIds.add(course.getInt("id"));
                        }

                        this.getAssignments(courseIds);
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                }
        );

        this.requestQueue.add(stringRequest);
    }

    /**
     * Function to get all the assignments for a list of course ids
     *
     * @param courseIds The list of course ids
     */
    private void getAssignments(List<Integer> courseIds) {
        Uri.Builder builder = Uri.parse(SettingsRepository.MOODLE_WEBSERVICE_URL)
                .buildUpon()
                .appendQueryParameter("wstoken", this.moodleToken)
                .appendQueryParameter("moodlewsrestformat", "json")
                .appendQueryParameter("wsfunction", "mod_assign_get_assignments");

        for (int i = 0; i < courseIds.size(); ++i) {
            builder.appendQueryParameter("courseids[" + i + "]", String.valueOf(courseIds.get(i)));
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(), null,
                response -> {
                    try {
                        if (!response.has("courses")) {
                            return;
                        }

                        JSONArray coursesArray = response.getJSONArray("courses");

                        Map<Integer, Assignment> assignments = new HashMap<>();

                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -30);
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
                                courseIds.set(i, null);
                            }
                        }

                        this.filterAssignmentsByCompletion(courseIds, assignments);
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                }
        );

        this.requestQueue.add(stringRequest);
    }

    /**
     * Function to remove activities if they have been marked as completed
     *
     * @param courseIds The list of course ids
     */
    private void filterAssignmentsByCompletion(List<Integer> courseIds, Map<Integer, Assignment> assignments) {
        for (int i = 0; i < courseIds.size(); ++i) {
            if (courseIds.get(i) == null) {
                continue;
            }

            /*
                A new request queue is created for each request to avoid waiting for previous requests to complete
             */
            RequestQueue requestQueue = Volley.newRequestQueue(this.requireContext().getApplicationContext());
            String url = Uri.parse(SettingsRepository.MOODLE_WEBSERVICE_URL)
                    .buildUpon()
                    .appendQueryParameter("wstoken", this.moodleToken)
                    .appendQueryParameter("moodlewsrestformat", "json")
                    .appendQueryParameter("wsfunction", "core_completion_get_activities_completion_status")
                    .appendQueryParameter("courseid", courseIds.get(i).toString())
                    .appendQueryParameter("userid", String.valueOf(this.moodleUserId))
                    .toString();

            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            if (!response.has("statuses")) {
                                return;
                            }

                            JSONArray statusArray = response.getJSONArray("statuses");

                            for (int j = 0; j < statusArray.length(); ++j) {
                                JSONObject statusObject = statusArray.getJSONObject(j);

                                int activityId = statusObject.getInt("cmid");
                                int state = statusObject.getInt("state");

                                if (state == 0 && assignments.containsKey(activityId)) {
                                    this.assignments.add(assignments.get(activityId));
                                }
                            }

                            this.displayAssignments();
                        } catch (Exception ignored) {
                        }
                    },
                    error -> {
                    }
            );

            requestQueue.add(stringRequest);
        }
    }

    private void displayAssignments() {
        try {
            this.assignmentGroups.setAdapter(new AssignmentsGroupAdapter(this.assignments));
        } catch (Exception ignored) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsFragment = inflater.inflate(R.layout.fragment_assignments, container, false);

        this.assignmentGroups = assignmentsFragment.findViewById(R.id.recycler_view_assignment_groups);
        TextView header = assignmentsFragment.findViewById(R.id.text_view_title);
        LinearLayout signInContainer = assignmentsFragment.findViewById(R.id.linear_layout_container);

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");
            float pixelDensity = getResources().getDisplayMetrics().density;

            header.setPadding(
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

            signInContainer.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (bottomNavigationHeight + 20 * pixelDensity)
            );

            // Only one listener can be added per requestKey, so we create a duplicate
            getParentFragmentManager().setFragmentResult("customInsets2", result);
        });

        SettingsRepository.trustAllCertificates();
        SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(this.requireContext().getApplicationContext());
        this.moodleToken = sharedPreferences.getString("moodleToken", "");

        if (!this.moodleToken.equals("")) {
            this.requestQueue = Volley.newRequestQueue(this.requireContext().getApplicationContext());
            this.assignments = new ArrayList<>();
            this.getUserId();
        } else {
            signInContainer.setVisibility(View.VISIBLE);

            assignmentsFragment.findViewById(R.id.button_sign_in).setOnClickListener(view -> {
                MoodleLoginDialogFragment moodleLoginDialogFragment = new MoodleLoginDialogFragment();

                FragmentManager fragmentManager = this.requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, moodleLoginDialogFragment).addToBackStack(null).commit();
            });
        }

        return assignmentsFragment;
    }
}