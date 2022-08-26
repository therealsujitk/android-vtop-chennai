package com.ashish.vtopchennai.interfaces;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import com.ashish.vtopchennai.helpers.SettingsRepository;

public interface MoodleApi {
    @GET(SettingsRepository.MOODLE_LOGIN_PATH +
            "?service=moodle_mobile_app")
    Single<ResponseBody> signIn(@Query("username") String username,
                                @Query("password") String password);

    @GET(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=core_webservice_get_site_info" +
            "&moodlewsrestformat=json")
    Single<ResponseBody> getUserId(@Query("wstoken") String moodleToken);

    @GET(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=core_course_get_enrolled_courses_by_timeline_classification" +
            "&classification=inprogress" +
            "&moodlewsrestformat=json")
    Single<ResponseBody> getCourses(@Query("wstoken") String moodleToken);

    @GET(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=mod_assign_get_assignments" +
            "&moodlewsrestformat=json")
    Single<ResponseBody> getAssignments(@Query("wstoken") String moodleToken,
                                        @Query("courseids[]") List<Integer> courseIds);

    @GET(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=core_completion_get_activities_completion_status" +
            "&moodlewsrestformat=json")
    Single<ResponseBody> getAssignmentsCompletionStatus(@Query("wstoken") String moodleToken,
                                                        @Query("courseid") int courseId,
                                                        @Query("userid") int userId);

    @GET(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=mod_assign_get_submission_status" +
            "&moodlewsrestformat=json")
    Single<ResponseBody> getSubmissionStatus(@Query("wstoken") String moodleToken,
                                             @Query("assignid") int activityId);

    @POST(SettingsRepository.MOODLE_UPLOAD_PATH)
    Single<ResponseBody> addSubmissions(@Query("token") String moodleToken,
                                        @Query("itemid") int fileArea,
                                        @Body MultipartBody body);

    @POST(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=mod_assign_save_submission" +
            "&moodlewsrestformat=json")
    Single<ResponseBody> saveSubmissions(@Query("wstoken") String moodleToken,
                                         @Query("assignmentid") int assignmentId,
                                         @Query("plugindata[files_filemanager]") int fileArea);

    @POST(SettingsRepository.MOODLE_WEBSERVICE_PATH +
            "?wsfunction=mod_assign_submit_for_grading" +
            "&moodlewsrestformat=json" +
            "&acceptsubmissionstatement=1")
    Single<ResponseBody> submitAssignmentForGrading(@Query("wstoken") String moodleToken,
                                                    @Query("assignmentid") int assignmentId);
}
