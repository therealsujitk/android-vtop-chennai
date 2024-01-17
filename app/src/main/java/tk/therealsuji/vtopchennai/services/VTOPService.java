package tk.therealsuji.vtopchennai.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.helpers.NotificationHelper;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.AttendanceDao;
import tk.therealsuji.vtopchennai.interfaces.CoursesDao;
import tk.therealsuji.vtopchennai.interfaces.ExamsDao;
import tk.therealsuji.vtopchennai.interfaces.MarksDao;
import tk.therealsuji.vtopchennai.interfaces.ReceiptsDao;
import tk.therealsuji.vtopchennai.interfaces.StaffDao;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Attendance;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.CumulativeMark;
import tk.therealsuji.vtopchennai.models.Exam;
import tk.therealsuji.vtopchennai.models.Mark;
import tk.therealsuji.vtopchennai.models.Receipt;
import tk.therealsuji.vtopchennai.models.Slot;
import tk.therealsuji.vtopchennai.models.Spotlight;
import tk.therealsuji.vtopchennai.models.Staff;
import tk.therealsuji.vtopchennai.models.Timetable;

public class VTOPService extends Service {
    public static final int CAPTCHA_DEFAULT = 1;
    public static final int CAPTCHA_GRECATPCHA = 2;

    private static final String END_SERVICE_ACTION = "END_SERVICE_ACTION";

    AppDatabase appDatabase;
    Binder serviceBinder = new ServiceBinder();
    boolean isWebViewDestroyed;
    Callback callback;
    Integer counter, maxProgress, progress;
    NotificationCompat.Builder notification;
    NotificationManager notificationManager;
    PageState pageState;
    SharedPreferences sharedPreferences;
    WebView webView;

    Map<Integer, Course> theoryCourses, labCourses, projectCourses;
    Map<String, CumulativeMark> cumulativeMarks;
    Map<String, Slot> theorySlots, labSlots, projectSlots;
    Map<String, String> semesters;
    String username, password, semesterID;

    public void clearCallback() {
        this.callback = null;
    }

    @Override
    public void onCreate() {
        Intent endServiceIntent = new Intent(this, VTOPService.class);
        endServiceIntent.setAction(END_SERVICE_ACTION);
        PendingIntent endServicePendingIntent = PendingIntent.getService(
                this,
                0,
                endServiceIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT
        );

        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
        this.notificationManager = notificationHelper.getManager();

        this.notification = notificationHelper.notifySync(null, null);
        this.notification.addAction(R.drawable.ic_close, getString(R.string.cancel), endServicePendingIntent);
        this.notification.setOngoing(true);
        this.notification.setProgress(0, 0, true);

        this.createWebView();

        this.appDatabase = AppDatabase.getInstance(getApplicationContext());
        this.sharedPreferences = SettingsRepository.getSharedPreferences(getApplicationContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null && intent.getAction().equals(END_SERVICE_ACTION)) {
            this.endService(true);
            this.notificationManager.cancel(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD);  // In case the notification isn't removed for some reason
        } else {
            this.startForeground(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, this.notification.build());

            this.counter = 0;
            this.maxProgress = 12;

            SharedPreferences encryptedSharedPreferences = SettingsRepository.getEncryptedSharedPreferences(getApplicationContext());

            if (encryptedSharedPreferences == null) {
                error(102, "Failed to fetch credentials.");
            } else {
                this.username = encryptedSharedPreferences.getString("username", null);
                this.password = encryptedSharedPreferences.getString("password", null);
                this.reloadPage("/login", true);
            }
        }

        return START_NOT_STICKY;
    }

    /**
     * Function to create a fresh WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void createWebView() {
        this.webView = new WebView(getApplicationContext());
        this.webView.addJavascriptInterface(this, "Android");
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36");
        this.webView.setBackgroundColor(Color.TRANSPARENT);
        this.webView.setHorizontalScrollBarEnabled(false);
        this.webView.setVerticalScrollBarEnabled(false);
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                /*
                 *  JSON response format
                 *  {
                 *      "page_type": "LANDING"|"HOME"|"LOGIN"
                 *  }
                 */
                view.evaluateJavascript("(function() {" +
                        "const response = {" +
                        "   page_type: 'LANDING'" +
                        "};" +
                        "if ($('input[id=\"authorizedIDX\"]').length === 1) {" +
                        "   response.page_type = 'HOME';" +
                        "} if ($('form[id=\"vtopLoginForm\"]').length === 1) {" +
                        "   response.page_type = 'LOGIN';" +
                        "}" +
                        "return response;" +
                        "})();", responseString -> {
                    try {
                        JSONObject response = new JSONObject(responseString);
                        String pageType = response.getString("page_type");

                        switch (pageType) {
                            case "LANDING":
                                if (counter >= 10) {
                                    error(101, "Couldn't connect to the server.");
                                    endService(true);
                                    return;
                                }

                                openSignIn();
                                ++counter;

                                pageState = PageState.LANDING;
                                break;
                            case "LOGIN":
                                if (pageState == PageState.LOGIN) {
                                    break;
                                }

                                getCaptchaType();
                                pageState = PageState.LOGIN;
                                break;
                            case "HOME":
                                if (pageState == PageState.HOME) {
                                    break;
                                }

                                getSemesters();
                                pageState = PageState.HOME;
                                break;
                            default:
                                throw new Error("Unknown page exception.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     * Function to brutally destroy the WebView
     */
    private void destroyWebView() {
        this.webView.onPause();
        this.webView.removeAllViews();
        this.webView.destroyDrawingCache();
        this.webView.pauseTimers();

        ViewGroup webViewParent = (ViewGroup) webView.getParent();
        if (webViewParent != null) {
            webViewParent.removeView(webView);
        }

        this.webView.destroy();
        this.isWebViewDestroyed = true;
    }

    /**
     * Function to terminate the download
     *
     * @param force If force is true, it will end the service no matter what
     */
    public void endService(boolean force) {
        if (!force && this.progress != -1) {
            return;
        }

        this.destroyWebView();
        stopSelf();
        stopForeground(true);

        try {
            this.callback.onServiceEnd();
        } catch (Exception ignored) {
        }
    }

    /**
     * Function to reload the page after clearing all cache and history.
     */
    private void reloadPage(String path, boolean destroySession) {
        if (this.isWebViewDestroyed) {
            this.createWebView();
            this.isWebViewDestroyed = false;
        }

        this.pageState = null;
        this.progress = -1;

        this.notification.setContentTitle(getString(R.string.server_connect));
        this.notification.setContentText(null);
        this.notification.setProgress(0, 0, true);
        this.notificationManager.notify(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, this.notification.build());

        if (destroySession) {
            CookieManager.getInstance().removeAllCookies(null);
            this.webView.clearCache(true);
            this.webView.clearHistory();
        }

        this.webView.loadUrl(SettingsRepository.VTOP_BASE_URL + path);
    }

    /**
     * Function to update the download progress in the notification
     *
     * @param currentDownload The string ID for the current download
     */
    private void updateProgress(Integer currentDownload) {
        this.notification.setProgress(maxProgress, ++progress, false);
        this.notification.setContentText(progress + " / " + maxProgress);

        if (currentDownload != null) {
            this.notification.setContentTitle(getString(currentDownload));
        }

        this.notificationManager.notify(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, notification.build());
    }

    /**
     * Function to handle errors.
     */
    private void error(final int errorCode, final String errorMessage) {
        Toast.makeText(getApplicationContext(), "Error " + errorCode + ". " + errorMessage, Toast.LENGTH_SHORT).show();
        this.reloadPage("/login", true);

        // Firebase Crashlytics Logging
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.log("VTOP error " + errorCode + ". " + errorMessage);
    }

    /**
     * Function to open the login page.
     */
    private void openSignIn() {
        /*
         *  JSON response format
         *  {
         *      "success": true|false
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "const response = {" +
                "    success: false" +
                "};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url: '/vtop/prelogin/setup'," +
                "    data: $('#stdForm').serialize()," +
                "    async: false," +
                "    success: function(res) {" +
                "        response.success = true;" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                this.reloadPage("/login", false);
            } catch (Exception e) {
                error(103, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to get the type of captcha (Default Captcha / Google reCaptcha).
     */
    private void getCaptchaType() {
        /*
         *  JSON response format
         *
         *  {
         *      "captcha_type": "DEFAULT"|"GRECAPTCHA"
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "const response = {" +
                "    captcha_type: 'DEFAULT'" +
                "};" +
                "if ($('input[id=\"gResponse\"]').length === 1) {" +
                "   response.captcha_type = 'GRECAPTCHA';" +
                "}" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                this.notification.setContentTitle(getString(R.string.captcha_wait));
                this.notificationManager.notify(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, notification.build());

                if (response.getString("captcha_type").equals("DEFAULT")) {
                    getCaptcha();
                } else {
                    executeCaptcha();
                }
            } catch (Exception e) {
                error(104, e.getLocalizedMessage());
            }
        });
    }

    /**
     * For Default Captcha
     * Function to get the captcha from the portal's sign in page and load it into the ImageView.
     */
    private void getCaptcha() {
        /*
         *  JSON response format
         *
         *  {
         *      "captcha": "data:image/png:base64, ContinuousGibberishText...."
         *  }
         */
        this.webView.evaluateJavascript("(function() {" +
                "return {" +
                "   captcha: $('#captchaBlock img').get(0).src" +
                "};" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);

                String base64Captcha = response.getString("captcha").split(",")[1];
                byte[] decodedString = Base64.decode(base64Captcha, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                try {
                    this.callback.onRequestCaptcha(CAPTCHA_DEFAULT, decodedImage, null);
                } catch (Exception ignored) {
                    this.endService(true);
                }
            } catch (Exception e) {
                error(105, e.getLocalizedMessage());
            }
        });
    }

    /**
     * For Google reCaptcha
     * Function to override the default onSubmit function and execute the captcha.
     */
    private void executeCaptcha() {
        try {
            this.callback.onRequestCaptcha(CAPTCHA_GRECATPCHA, null, this.webView);
        } catch (Exception ignored) {
            this.endService(true);
            return;
        }

        /*
            Overriding the existing onSubmit function and attempting to render the reCaptcha
         */
        webView.evaluateJavascript("function callBuiltValidation(token) {" +
                "    Android.signIn(token);" +
                "}" +
                "(function() {" +
                "var executeInterval = setInterval(function() {" +
                "    try {" +   // typeof grecaptcha != 'undefined' always returns true for some reason
                "        grecaptcha.execute();" +
                "        clearInterval(executeInterval);" +
                "    } catch (err) {" +
                "    }" +
                "}, 500);" +
                "})();", value -> {
        });
    }

    /**
     * Function to sign in to the portal
     */
    @JavascriptInterface
    public void signIn(final String captcha) {
        this.notification.setContentTitle(getString(R.string.sign_in_attempt));
        this.notificationManager.notify(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, notification.build());

        new Handler(getApplicationContext().getMainLooper())
                .post(() -> {
                    try {
                        callback.onCaptchaComplete();
                    } catch (Exception ignored) {
                    }

                    ViewGroup webViewParent = (ViewGroup) webView.getParent();
                    if (webViewParent != null) {
                        webViewParent.removeView(webView);
                    }

                    /*
                     *  JSON response format
                     *  {
                     *      "authorized": true|false,
                     *      "error_message": null,
                     *      "error_code": 0
                     *  }
                     */
                    webView.evaluateJavascript("(function() {" +
                            "if (typeof captchaInterval != 'undefined') clearInterval(captchaInterval);" +
                            "if (typeof executeInterval != 'undefined') clearInterval(executeInterval);" +
                            "$('#vtopLoginForm [name=\"username\"]').val('" + username.replaceAll("'", "\\\\'") + "');" +
                            "$('#vtopLoginForm [name=\"password\"]').val('" + password.replaceAll("'", "\\\\'") + "');" +
                            "$('#vtopLoginForm [name=\"captchaStr\"]').val('" + captcha.replaceAll("'", "\\\\'") + "');" +
                            "$('#vtopLoginForm [name=\"gResponse\"]').val('" + captcha.replaceAll("'", "\\\\'") + "');" +
                            "var response = {" +
                            "    authorised: false," +
                            "    error_message: null," +
                            "    error_code: 0" +
                            "};" +
                            "$.ajax({" +
                            "    type : 'POST'," +
                            "    url : '/vtop/login'," +
                            "    data : $('#vtopLoginForm').serialize()," +
                            "    async: false," +
                            "    success : function(res) {" +
                            "        if(res.search('___INTERNAL___RESPONSE___') == -1) {" +
                            "            $('#page_outline').html(res);" +
                            "            if (res.includes('authorizedIDX')) {" +
                            "                response.authorised = true;" +
                            "                return;" +
                            "            }" +
                            "            var pageContent = res.toLowerCase();" +
                            "            var invalidCaptchaRegex = new RegExp(/invalid\\s*captcha/);" +
                            "            var invalidCredentialsRegex = new RegExp(/invalid\\s*(user\\s*name|login\\s*id|user\\s*id)\\s*\\/\\s*password/);" +
                            "            var accountLockedRegex = new RegExp(/account\\s*is\\s*locked/);" +
                            "            var maxFailAttemptsRegex = new RegExp(/maximum\\s*fail\\s*attempts\\s*reached/);" +
                            "            if (invalidCaptchaRegex.test(pageContent)) {" +
                            "                response.error_message = 'Invalid Captcha';" +
                            "                response.error_code = 1;" +
                            "            } else if(invalidCredentialsRegex.test(pageContent)) {" +
                            "                response.error_message = 'Invalid Username / Password';" +
                            "                response.error_code = 2;" +
                            "            } else if(accountLockedRegex.test(pageContent)) {" +
                            "                response.error_message = 'Your Account is Locked';" +
                            "                response.error_code = 3;" +
                            "            } else if(maxFailAttemptsRegex.test(pageContent)) {" +
                            "                response.error_message = 'Maximum login attempts reached, open VTOP in your browser to reset your password';" +
                            "                response.error_code = 4;" +
                            "            } else {" +
                            "                response.error_message = 'Unknown error';" +
                            "                response.error_code = 5;" +
                            "            }" +
                            "        }" +
                            "    }" +
                            "});" +
                            "return response;" +
                            "})();", responseString -> {
                        try {
                            JSONObject response = new JSONObject(responseString);
                            boolean isAuthorised = response.getBoolean("authorised");

                            if (isAuthorised) {
                                this.reloadPage("/content", false);
                            } else {
                                String errorMessage = response.getString("error_message");
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();

                                int errorCode = response.getInt("error_code");
                                if (errorCode == 1) {
                                    this.reloadPage("/login", false);
                                } else {
                                    if (errorCode == 2) {
                                        try {
                                            this.callback.onForceSignOut();
                                        } catch (Exception ignored) {}
                                    }

                                    this.endService(true);
                                }
                            }
                        } catch (Exception e) {
                            error(106, e.getLocalizedMessage());
                        }
                    });
                });
    }

    /**
     * Function to get a list of the semesters. These semesters are obtained from the Timetable page.
     */
    private void getSemesters() {
        /*
         *  JSON response format
         *
         *  {
         *      "semesters": [
         *          {
         *              "name": "Fall Semester 2020-21",
         *              "id": "CH2020211"
         *          },
         *          {
         *              "name": "Winter Semester 2020-21",
         *              "id": "CH2020215"
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'academics/common/StudentTimeTable'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        if(res.toLowerCase().includes('time table')) {" +
                "            var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "            var options = doc.getElementById('semesterSubId').getElementsByTagName('option');" +
                "            var semesters = [];" +
                "            for(var i = 0; i < options.length; ++i) {" +
                "                if(!options[i].value) {" +
                "                    continue;" +
                "                }" +
                "                var semester = {" +
                "                    name: options[i].innerText," +
                "                    id: options[i].value" +
                "                };" +
                "                semesters.push(semester);" +
                "            }" +
                "            response.semesters = semesters;" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray semesterArray = response.getJSONArray("semesters");
                this.semesters = new HashMap<>();

                for (int i = 0; i < semesterArray.length(); ++i) {
                    JSONObject semesterObject = semesterArray.getJSONObject(i);
                    this.semesters.put(semesterObject.getString("name"), semesterObject.getString("id"));
                }

                try {
                    this.notification.setContentTitle(getString(R.string.semester_wait));
                    this.notificationManager.notify(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, notification.build());

                    String[] semesters = this.semesters.keySet().toArray(new String[0]);
                    this.callback.onRequestSemester(semesters);
                } catch (Exception ignored) {
                    this.endService(true);
                }
            } catch (Exception e) {
                error(201, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to set the semester ID based on the semester selected.
     */
    public void setSemester(String semester) {
        this.semesterID = this.semesters.get(semester);
        getName();
    }

    /**
     * Function to save the name of the user in SharedPreferences.
     */
    private void getName() {
        updateProgress(R.string.downloading_profile);

        /*
         *  JSON response format
         *
         *  {
         *      "name": "JOHN DOE"
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'studentsRecord/StudentProfileAllView'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        if(res.toLowerCase().includes('personal information')) {" +
                "            var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "            var cells = doc.getElementsByTagName('td');" +
                "            for(var i = 0; i < cells.length; ++i) {" +
                "                var key = cells[i].innerText.toLowerCase();" +
                "                if(key.includes('student') && key.includes('name')) {" +
                "                    response.name = cells[++i].innerHTML;" +
                "                    break;" +
                "                }" +
                "            }" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                sharedPreferences.edit().putString("name", response.getString("name")).apply();

                this.getCreditsCGPA();
            } catch (Exception e) {
                error(301, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to sve the earned credits and CGPA in SharedPreferences.
     */
    private void getCreditsCGPA() {
        this.updateProgress(null);

        /*
         *  JSON response format
         *
         *  {
         *      "cgpa": 8.58
         *      "total_credits": 64
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'examinations/examGradeView/StudentGradeHistory'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var tables = doc.getElementsByTagName('table');" +
                "        for (var i = tables.length - 1; i >= 0 ; --i) {" +
                "            var headings = tables[i].getElementsByTagName('tr')[0].getElementsByTagName('td');" +
                "            if (headings[0].innerText.toLowerCase().includes('credits')) {" +
                "                var creditsIndex, cgpaIndex;" +
                "                for (var j = 0; j < headings.length; ++j) {" +
                "                    var heading = headings[j].innerText.toLowerCase();" +
                "                    if (heading.includes('earned')) {" +
                "                        creditsIndex = j + headings.length;" +
                "                    } else if (heading.includes('cgpa')) {" +
                "                        cgpaIndex = j + headings.length;" +
                "                    }" +
                "                }" +
                "                var cells = tables[i].getElementsByTagName('td');" +
                "                response.cgpa = parseFloat(cells[cgpaIndex].innerText) || 0;" +
                "                response.total_credits = parseFloat(cells[creditsIndex].innerText) || 0;" +
                "                break;" +
                "            }" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                this.sharedPreferences.edit().putFloat("cgpa", (float) response.getDouble("cgpa")).apply();
                this.sharedPreferences.edit().putFloat("totalCredits", (float) response.getDouble("total_credits")).apply();

                this.downloadCourses();
            } catch (Exception e) {
                error(302, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the course info from the timetable page.
     */
    private void downloadCourses() {
        this.updateProgress(R.string.downloading_courses);

        /*
         *  JSON response format
         *
         *  {
         *      "courses": [
         *          {
         *              "code": "CSE1001",
         *              "title": "Problem Solving and Programming",
         *              "type": "lab"|"project"|"theory",
         *              "credits": 3,
         *              "slots": [
         *                  "L45",
         *                  "L46"
         *              ],
         *              "venue": "AB2 - 015",
         *              "faculty": "JOHN DOE"
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = '_csrf=' + $('input[name=\"_csrf\"]').val() + '&semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var response = {" +
                "    courses: []" +
                "};" +
                "$.ajax({" +
                "    type : 'POST'," +
                "    url : 'processViewTimeTable'," +
                "    data : data," +
                "    async: false," +
                "    success : function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        if (!doc.getElementById('studentDetailsList')) {" +
                "            return;" +
                "        }" +
                "        var table = doc.getElementById('studentDetailsList').getElementsByTagName('table')[0];" +
                "        var headings = table.getElementsByTagName('th');" +
                "        var courseIndex, creditsIndex, slotVenueIndex, facultyIndex;" +
                "        for(var i = 0; i < headings.length; ++i) {" +
                "            var heading = headings[i].innerText.toLowerCase();" +
                "            if (heading == 'course') {" +
                "                courseIndex = i;" +
                "            } else if (heading == 'l t p j c') {" +
                "                creditsIndex = i;" +
                "            } else if (heading.includes('slot')) {" +
                "                slotVenueIndex = i;" +
                "            } else if (heading.includes('faculty')) {" +
                "                facultyIndex = i;" +
                "            }" +
                "        }" +
                "        var cells = table.getElementsByTagName('td');" +
                "        var headingOffset = headings[0].innerText.toLowerCase().includes('invoice') ? -1 : 0;" +
                "        var cellOffset = cells[0].innerText.toLowerCase().includes('invoice') ? 1 : 0;" +
                "        var offset = headingOffset + cellOffset;" +
                "        while (courseIndex < cells.length && creditsIndex < cells.length && slotVenueIndex < cells.length && facultyIndex < cells.length) {" +
                "            var course = {};" +
                "            var rawCourse = cells[courseIndex + offset].innerText.replace(/\\t/g,'').replace(/\\n/g,' ');" +
                "            var rawCourseType = rawCourse.split('(').slice(-1)[0].toLowerCase();" +
                "            var rawCredits = cells[creditsIndex + offset].innerText.replace(/\\t/g,'').replace(/\\n/g,' ').trim().split(' ');" +
                "            var rawSlotVenue = cells[slotVenueIndex + offset].innerText.replace(/\\t/g,'').replace(/\\n/g,'').split('-');" +
                "            var rawFaculty = cells[facultyIndex + offset].innerText.replace(/\\t/g,'').replace(/\\n/g,'').split('-');" +
                "            course.code = rawCourse.split('-')[0].trim();" +
                "            course.title = rawCourse.split('-').slice(1).join('-').split('(')[0].trim();" +
                "            course.type = (rawCourseType.includes('lab')) ? 'lab' : ((rawCourseType.includes('project')) ? 'project' : 'theory');" +
                "            course.credits = parseInt(rawCredits[rawCredits.length - 1]) || 0;" +
                "            course.slots = rawSlotVenue[0].trim().split('+');" +
                "            course.venue = rawSlotVenue.slice(1, rawSlotVenue.length).join(' - ').trim();" +
                "            course.faculty = rawFaculty[0].trim();" +
                "            response.courses.push(course);" +
                "            courseIndex += headings.length + headingOffset;" +
                "            creditsIndex += headings.length + headingOffset;" +
                "            slotVenueIndex += headings.length + headingOffset;" +
                "            facultyIndex += headings.length + headingOffset;" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray courseArray = response.getJSONArray("courses");

                List<Course> courses = new ArrayList<>();
                List<Slot> slots = new ArrayList<>();

                this.theorySlots = new HashMap<>();
                this.labSlots = new HashMap<>();
                this.projectSlots = new HashMap<>();

                this.theoryCourses = new HashMap<>();
                this.labCourses = new HashMap<>();
                this.projectCourses = new HashMap<>();

                for (int i = 0, slotId = 1; i < courseArray.length(); ++i) {
                    JSONObject courseObject = courseArray.getJSONObject(i);
                    Course course = new Course();

                    course.id = i + 1;
                    course.code = this.getStringValue(courseObject, "code");
                    course.title = this.getStringValue(courseObject, "title");
                    course.type = this.getStringValue(courseObject, "type");
                    course.credits = this.getIntegerValue(courseObject, "credits");
                    course.venue = this.getStringValue(courseObject, "venue");
                    course.faculty = this.getStringValue(courseObject, "faculty");

                    courses.add(course);

                    Map<String, Slot> slotReference;

                    if (course.type.equals("lab")) {
                        slotReference = this.labSlots;
                        this.labCourses.put(course.id, course);
                    } else if (course.type.equals("project")) {
                        slotReference = this.projectSlots;
                        this.projectCourses.put(course.id, course);
                    } else {
                        slotReference = this.theorySlots;
                        this.theoryCourses.put(course.id, course);
                    }

                    JSONArray slotsArray = courseObject.getJSONArray("slots");
                    for (int j = 0; j < slotsArray.length(); ++j, ++slotId) {
                        Slot slot = new Slot();

                        slot.id = slotId;
                        slot.slot = slotsArray.getString(j);
                        slot.courseId = course.id;

                        slots.add(slot);
                        slotReference.put(slot.slot, slot);
                    }
                }

                CoursesDao coursesDao = this.appDatabase.coursesDao();

                Observable<Object> deleteAllObservable = Observable.fromCompletable(coursesDao.deleteAll());
                Observable<Object> insertCoursesObservable = Observable.fromCompletable(coursesDao.insertCourses(courses));
                Observable<Object> insertSlotsObservable = Observable.fromCompletable(coursesDao.insertSlots(slots));

                Observable
                        .concat(
                                deleteAllObservable,
                                insertCoursesObservable,
                                insertSlotsObservable
                        )
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(402, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                downloadTimetable();
                            }
                        });
            } catch (Exception e) {
                error(401, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the timetable.
     */
    private void downloadTimetable() {
        updateProgress(R.string.downloading_timetable);

        /*
         *  JSON response format
         *
         *  {
         *      "lab": [
         *          {
         *              "start_time": "08:00",
         *              "end_time": "08:50",
         *              "sunday": null,
         *              "monday": null",
         *              "tuesday": null,
         *              "wednesday": null,
         *              "thursday": null,
         *              "friday": null,
         *              "saturday": null,
         *          },
         *          ...
         *      ],
         *      "theory": [
         *          {
         *              "start_time": "08:00",
         *              "end_time": "08:50",
         *              "sunday": null,
         *              "monday": "A1",
         *              "tuesday": "B1",
         *              "wednesday": null,
         *              "thursday": "D1",
         *              "friday": "E1",
         *              "saturday": "F1",
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = '_csrf=' + $('input[name=\"_csrf\"]').val() + '&semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var response = {" +
                "    lab: []," +
                "    theory: []" +
                "};" +
                "$.ajax({" +
                "    type : 'POST'," +
                "    url : 'processViewTimeTable'," +
                "    data : data," +
                "    async: false," +
                "    success : function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var spans = doc.getElementById('getStudentDetails').getElementsByTagName('span');" +
                "        if(spans[0].innerText.toLowerCase().includes('no record(s) found')) {" +
                "           return;" +
                "        }" +
                "        var cells = doc.getElementById('timeTableStyle').getElementsByTagName('td');" +
                "        var key, type;" +
                "        for (var i = 0, j = 0; i < cells.length; ++i) {" +
                "            var content = cells[i].innerText.toUpperCase();" +
                "            if (content.includes('THEORY')) {" +
                "                type = 'theory';" +
                "                j = 0;" +
                "                continue;" +
                "            } else if (content.includes('LAB')) {" +
                "                type = 'lab';" +
                "                j = 0;" +
                "                continue;" +
                "            } else if (content.includes('START')) {" +
                "                key = 'start';" +
                "                continue;" +
                "            } else if (content.includes('END')) {" +
                "                key = 'end';" +
                "                continue;" +
                "            } else if (content.includes('SUN')) {" +
                "                key = 'sunday';" +
                "                continue;" +
                "            } else if (content.includes('MON')) {" +
                "                key = 'monday';" +
                "                continue;" +
                "            } else if (content.includes('TUE')) {" +
                "                key = 'tuesday';" +
                "                continue;" +
                "            } else if (content.includes('WED')) {" +
                "                key = 'wednesday';" +
                "                continue;" +
                "            } else if (content.includes('THU')) {" +
                "                key = 'thursday';" +
                "                continue;" +
                "            } else if (content.includes('FRI')) {" +
                "                key = 'friday';" +
                "                continue;" +
                "            } else if (content.includes('SAT')) {" +
                "                key = 'saturday';" +
                "                continue;" +
                "            } else if (content.includes('LUNCH')) {" +
                "                continue;" +
                "            }" +
                "            if (key == 'start') {" +
                "                response[type].push({ start_time: content.trim() });" +
                "            } else if (key == 'end') {" +
                "                response[type][j++].end_time = content.trim();" +
                "            } else if (cells[i].bgColor == '#CCFF33') {" +
                "                response[type][j++][key] = content.split('-')[0].trim();" +
                "            } else {" +
                "                response[type][j++][key] = null;" +
                "            }" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray labArray = response.getJSONArray("lab");
                JSONArray theoryArray = response.getJSONArray("theory");

                SettingsRepository.clearNotificationPendingIntents(this.getApplicationContext());

                List<Timetable> timetable = new ArrayList<>();

                /*
                    Used for converting 12-hour to 24-hour if necessary
                 */
                SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

                for (int i = 0; i < labArray.length() && i < theoryArray.length(); ++i) {
                    JSONObject labObject = labArray.getJSONObject(i);
                    JSONObject theoryObject = theoryArray.getJSONObject(i);

                    Timetable lab = new Timetable();
                    Timetable theory = new Timetable();

                    lab.id = i * 2 + 1;
                    lab.startTime = this.getStringValue(labObject, "start_time");
                    lab.endTime = this.getStringValue(labObject, "end_time");
                    lab.sunday = this.getSlotId(this.getStringValue(labObject, "sunday"), Course.TYPE_LAB);
                    lab.monday = this.getSlotId(this.getStringValue(labObject, "monday"), Course.TYPE_LAB);
                    lab.tuesday = this.getSlotId(this.getStringValue(labObject, "tuesday"), Course.TYPE_LAB);
                    lab.wednesday = this.getSlotId(this.getStringValue(labObject, "wednesday"), Course.TYPE_LAB);
                    lab.thursday = this.getSlotId(this.getStringValue(labObject, "thursday"), Course.TYPE_LAB);
                    lab.friday = this.getSlotId(this.getStringValue(labObject, "friday"), Course.TYPE_LAB);
                    lab.saturday = this.getSlotId(this.getStringValue(labObject, "saturday"), Course.TYPE_LAB);

                    theory.id = i * 2 + 2;
                    theory.startTime = this.getStringValue(theoryObject, "start_time");
                    theory.endTime = this.getStringValue(theoryObject, "end_time");
                    theory.sunday = this.getSlotId(this.getStringValue(theoryObject, "sunday"), Course.TYPE_THEORY);
                    theory.monday = this.getSlotId(this.getStringValue(theoryObject, "monday"), Course.TYPE_THEORY);
                    theory.tuesday = this.getSlotId(this.getStringValue(theoryObject, "tuesday"), Course.TYPE_THEORY);
                    theory.wednesday = this.getSlotId(this.getStringValue(theoryObject, "wednesday"), Course.TYPE_THEORY);
                    theory.thursday = this.getSlotId(this.getStringValue(theoryObject, "thursday"), Course.TYPE_THEORY);
                    theory.friday = this.getSlotId(this.getStringValue(theoryObject, "friday"), Course.TYPE_THEORY);
                    theory.saturday = this.getSlotId(this.getStringValue(theoryObject, "saturday"), Course.TYPE_THEORY);

                    /*
                        Formatting time in 24-hour in-case it's given in 12-hour format because VIT
                        thought it would be a good idea to use both 12-hour and 24-hour formats

                        This conversion works under the assumption that there will not be any classes
                        after 20:00 and before 08:00. If the time is less than 08:00, the time is in
                        a 12-hour format and has to be converted
                     */
                    String[] timings = {lab.startTime, lab.endTime, theory.startTime, theory.endTime};
                    for (int j = 0; j < timings.length; ++j) {
                        try {
                            Date time = hour24.parse(timings[j]);
                            Date hourStart = hour24.parse("08:00");

                            if (time != null && time.before(hourStart)) {
                                time = hour12.parse(timings[j] + " PM");
                                if (time != null) {
                                    timings[j] = hour24.format(time);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    lab.startTime = timings[0];
                    lab.endTime = timings[1];
                    theory.startTime = timings[2];
                    theory.endTime = timings[3];

                    timetable.add(lab);
                    timetable.add(theory);

                    try {
                        SettingsRepository.setTimetableNotifications(this.getApplicationContext(), lab);
                        SettingsRepository.setTimetableNotifications(this.getApplicationContext(), theory);
                    } catch (Exception ignored) {
                    }
                }

                TimetableDao timetableDao = appDatabase.timetableDao();

                Observable<Object> deleteAllObservable = Observable.fromCompletable(timetableDao.deleteAll());
                Observable<Object> insertTimetableObservable = Observable.fromCompletable(timetableDao.insert(timetable));

                Observable
                        .concat(
                                deleteAllObservable,
                                insertTimetableObservable
                        )
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(502, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                downloadAttendance();
                            }
                        });
            } catch (Exception e) {
                error(501, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the attendance.
     */
    private void downloadAttendance() {
        updateProgress(R.string.downloading_attendance);

        /*
         *  JSON response format
         *
         *  {
         *      "attendance": [
         *          {
         *              "slot": "L45",
         *              "course_type": "Lab Only"
         *              "attended": 81,
         *              "total": 83,
         *              "percentage": 98
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = '_csrf=' + $('input[name=\"_csrf\"]').val() + '&semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var response = {" +
                "    attendance: []" +
                "};" +
                "$.ajax({" +
                "    type : 'POST'," +
                "    url : 'processViewStudentAttendance'," +
                "    data : data," +
                "    async: false," +
                "    success : function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var table = doc.getElementById('getStudentDetails');" +
                "        var headings = table.getElementsByTagName('th');" +
                "        var courseTypeIndex, slotIndex, attendedIndex, totalIndex, percentageIndex;" +
                "        for(var i = 0; i < headings.length; ++i) {" +
                "            var heading = headings[i].innerText.toLowerCase();" +
                "            if (heading.includes('course') && heading.includes('type')) {" +
                "                courseTypeIndex = i;" +
                "            } else if (heading.includes('slot')) {" +
                "                slotIndex = i;" +
                "            } else if (heading.includes('attended')) {" +
                "                attendedIndex = i;" +
                "            } else if (heading.includes('total')) {" +
                "                totalIndex = i;" +
                "            } else if (heading.includes('percentage')) {" +
                "                percentageIndex = i;" +
                "            }" +
                "        }" +
                "        var cells = table.getElementsByTagName('td');" +
                "        while (courseTypeIndex < cells.length && slotIndex < cells.length  && attendedIndex < cells.length && totalIndex < cells.length && percentageIndex < cells.length) {" +
                "            var attendanceObject = {};" +
                "            attendanceObject.course_type = cells[courseTypeIndex].innerText.trim();" +
                "            attendanceObject.slot = cells[slotIndex].innerText.trim().split('+')[0].trim();" +
                "            attendanceObject.attended = parseInt(cells[attendedIndex].innerText.trim()) || 0;" +
                "            attendanceObject.total = parseInt(cells[totalIndex].innerText.trim()) || 0;" +
                "            attendanceObject.percentage = parseInt(cells[percentageIndex].innerText.trim()) || 0;" +
                "            response.attendance.push(attendanceObject);" +
                "            courseTypeIndex += headings.length;" +
                "            slotIndex += headings.length;" +
                "            attendedIndex += headings.length;" +
                "            totalIndex += headings.length;" +
                "            percentageIndex += headings.length;" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray attendanceArray = response.getJSONArray("attendance");
                List<Attendance> attendance = new ArrayList<>();

                float overallAttendance = 0;
                int attendanceLength = 0;

                for (int i = 0; i < attendanceArray.length(); ++i) {
                    JSONObject attendanceObject = attendanceArray.getJSONObject(i);
                    Attendance attendanceItem = new Attendance();

                    int courseType = Course.TYPE_THEORY;

                    if (attendanceObject.getString("course_type").toLowerCase().contains("lab")) {
                        courseType = Course.TYPE_LAB;
                    }

                    attendanceItem.id = i + 1;
                    attendanceItem.courseId = this.getCourseId(attendanceObject.getString("slot"), courseType);
                    attendanceItem.attended = this.getIntegerValue(attendanceObject, "attended");
                    attendanceItem.total = this.getIntegerValue(attendanceObject, "total");

                    if (attendanceItem.attended != null && attendanceItem.total != null && attendanceItem.total != 0) {
                        attendanceItem.percentage = (int) Math.ceil((attendanceItem.attended * 100.0) / attendanceItem.total);
                        overallAttendance += attendanceItem.percentage;
                        ++attendanceLength;
                    }

                    attendance.add(attendanceItem);
                }

                if (attendanceLength != 0) {
                    overallAttendance /= attendanceLength;
                }

                sharedPreferences.edit().putInt("overallAttendance", (int) overallAttendance).apply();

                AttendanceDao attendanceDao = appDatabase.attendanceDao();
                Observable<Object> deleteObservable = Observable.fromCompletable(attendanceDao.delete());
                Observable<Object> insertObservable = Observable.fromCompletable(attendanceDao.insert(attendance));

                Observable
                        .concat(deleteObservable, insertObservable)
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull Object o) {

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(602, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                downloadMarks();
                            }
                        });
            } catch (Exception e) {
                error(601, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the marks.
     */
    private void downloadMarks() {
        updateProgress(R.string.downloading_marks);

        /*
         *  JSON response format
         *
         *  {
         *      "marks": [
         *          {
         *              "slot": "A1",
         *              "course_type": "Theory Only",
         *              "title": "CAT 1",
         *              "score": 26,
         *              "max_score": 30,
         *              "weightage": 13,
         *              "max_weightage": 15,
         *              "average": null
         *              "status": "Present"
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val()  + '&_csrf=' + $('input[name=\"_csrf\"]').val();" +
                "var response = {" +
                "    marks: []" +
                "};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'examinations/doStudentMarkView'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        if(res.toLowerCase().includes('no data found')) {" +
                "            return;" +
                "        }" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var table = doc.getElementById('fixedTableContainer');" +
                "        var rows = table.getElementsByTagName('tr');" +
                "        var headings = rows[0].getElementsByTagName('td');" +
                "        var courseTypeIndex, slotIndex;" +
                "        for (var i = 0; i < headings.length; ++i) {" +
                "            var heading = headings[i].innerText.toLowerCase();" +
                "            if (heading.includes('course') && heading.includes('type')) {" +
                "                courseTypeIndex = i;" +
                "            } else if (heading.includes('slot')) {" +
                "                slotIndex = i;" +
                "            }" +
                "        }" +
                "        for (var i = 1; i < rows.length; ++i) {" +
                "            var rawCourseType = rows[i].getElementsByTagName('td')[courseTypeIndex].innerText.trim().toLowerCase();" +
                "            var courseType = (rawCourseType.includes('lab')) ? 'lab' : ((rawCourseType.includes('project')) ? 'project' : 'theory');" +
                "            var slot = rows[i++].getElementsByTagName('td')[slotIndex].innerText.split('+')[0].trim();" +
                "            var innerTable = rows[i].getElementsByTagName('table')[0];" +
                "            var innerRows = innerTable.getElementsByTagName('tr');" +
                "            var innerHeadings = innerRows[0].getElementsByTagName('td');" +
                "            var titleIndex, scoreIndex, maxScoreIndex, weightageIndex, maxWeightageIndex, averageIndex, statusIndex;" +
                "            for (var j = 0; j < innerHeadings.length; ++j) {" +
                "                var innerHeading = innerHeadings[j].innerText.toLowerCase();" +
                "                if (innerHeading.includes('title')) {" +
                "                    titleIndex = j + innerHeadings.length;" +
                "                } else if (innerHeading.includes('max')) {" +
                "                    maxScoreIndex = j + innerHeadings.length;" +
                "                } else if (innerHeading.includes('%')) {" +
                "                    maxWeightageIndex = j + innerHeadings.length;" +
                "                } else if (innerHeading.includes('status')) {" +
                "                    statusIndex = j + innerHeadings.length;" +
                "                } else if (innerHeading.includes('scored')) {" +
                "                    scoreIndex = j + innerHeadings.length;" +
                "                } else if (innerHeading.includes('weightage') && innerHeading.includes('mark')) {" +
                "                    weightageIndex = j + innerHeadings.length;" +
                "                } else if (innerHeading.includes('average')) {" +
                "                    averageIndex = j + innerHeadings.length;" +
                "                }" +
                "            }" +
                "            var innerCells = innerTable.getElementsByTagName('td');" +
                "            while(titleIndex < innerCells.length && scoreIndex < innerCells.length && maxScoreIndex < innerCells.length && weightageIndex < innerCells.length && maxWeightageIndex < innerCells.length && averageIndex < innerCells.length && statusIndex < innerCells.length) {" +
                "                var mark = {};" +
                "                mark.slot = slot;" +
                "                mark.course_type = courseType;" +
                "                mark.title = innerCells[titleIndex].innerText.trim();" +
                "                mark.score = parseFloat(innerCells[scoreIndex].innerText) || 0;" +
                "                mark.max_score = parseFloat(innerCells[maxScoreIndex].innerText) || null;" +
                "                mark.weightage = parseFloat(innerCells[weightageIndex].innerText) || 0;" +
                "                mark.max_weightage = parseFloat(innerCells[maxWeightageIndex].innerText) || null;" +
                "                mark.average = parseFloat(innerCells[averageIndex].innerText) || null;" +
                "                mark.status = innerCells[statusIndex].innerText.trim();" +
                "                response.marks.push(mark);" +
                "                titleIndex += innerHeadings.length;" +
                "                scoreIndex += innerHeadings.length;" +
                "                maxScoreIndex += innerHeadings.length;" +
                "                weightageIndex += innerHeadings.length;" +
                "                maxWeightageIndex += innerHeadings.length;" +
                "                averageIndex += innerHeadings.length;" +
                "                statusIndex += innerHeadings.length;" +
                "            }" +
                "            i += innerRows.length;" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray marksArray = response.getJSONArray("marks");
                Map<Integer, Mark> marks = new HashMap<>();

                this.cumulativeMarks = new HashMap<>();

                for (int i = 0, j = 0; i < marksArray.length(); ++i) {
                    JSONObject markObject = marksArray.getJSONObject(i);
                    Mark mark = new Mark();

                    int courseType = Course.TYPE_THEORY;

                    if (markObject.getString("course_type").equals("lab")) {
                        courseType = Course.TYPE_LAB;
                    } else if (markObject.getString("course_type").equals("project")) {
                        courseType = Course.TYPE_PROJECT;
                    }

                    mark.id = i + 1;
                    mark.courseId = this.getCourseId(markObject.getString("slot"), courseType);
                    mark.title = this.getStringValue(markObject, "title");
                    mark.score = this.getDoubleValue(markObject, "score");
                    mark.maxScore = this.getDoubleValue(markObject, "max_score");
                    mark.weightage = this.getDoubleValue(markObject, "weightage");
                    mark.maxWeightage = this.getDoubleValue(markObject, "max_weightage");
                    mark.average = this.getDoubleValue(markObject, "average");
                    mark.status = this.getStringValue(markObject, "status");

                    String courseCode = this.getCourseCode(mark.courseId, courseType);
                    Integer courseCredits = this.getCourseCredits(mark.courseId, courseType);

                    if (!this.cumulativeMarks.containsKey(courseCode)) {
                        this.cumulativeMarks.put(courseCode, new CumulativeMark(++j));
                    }

                    Objects.requireNonNull(this.cumulativeMarks.get(courseCode)).courseCode = courseCode;
                    Objects.requireNonNull(this.cumulativeMarks.get(courseCode)).addWeightage(mark.weightage, mark.maxWeightage, courseType, courseCredits);

                    // Generating a unique hash signature to keep a track of read marks
                    mark.signature = (courseCode + markObject.getString("course_type") + mark.title + mark.score).hashCode();
                    marks.put(mark.signature, mark);
                }

                for (Map.Entry<String, CumulativeMark> cumulativeMark : this.cumulativeMarks.entrySet()) {
                    Double theoryTotal = cumulativeMark.getValue().theoryTotal;
                    Double labTotal = cumulativeMark.getValue().labTotal;
                    Double projectTotal = cumulativeMark.getValue().projectTotal;

                    Double theoryMax = cumulativeMark.getValue().theoryMax;
                    Double labMax = cumulativeMark.getValue().labMax;
                    Double projectMax = cumulativeMark.getValue().projectMax;

                    if (theoryTotal == null) {
                        theoryTotal = (double) 0;
                        theoryMax = (double) 0;
                    }

                    if (labTotal == null) {
                        labTotal = (double) 0;
                        labMax = (double) 0;
                    }

                    if (projectTotal == null) {
                        projectTotal = (double) 0;
                        projectMax = (double) 0;
                    }

                    int theoryCredits = cumulativeMark.getValue().theoryCredits;
                    int labCredits = cumulativeMark.getValue().labCredits;
                    int projectCredits = cumulativeMark.getValue().projectCredits;

                    double grandTotal = (theoryTotal * theoryCredits + labTotal * labCredits + projectTotal * projectCredits);
                    double grandMax = (theoryMax * theoryCredits + labMax * labCredits + projectMax * projectCredits);

                    grandTotal /= theoryCredits + labCredits + projectCredits;
                    grandMax /= theoryCredits + labCredits + projectCredits;

                    Objects.requireNonNull(this.cumulativeMarks.get(cumulativeMark.getKey())).grandTotal = grandTotal;
                    Objects.requireNonNull(this.cumulativeMarks.get(cumulativeMark.getKey())).grandMax = grandMax;
                }

                appDatabase.marksDao()
                        .insertMarks(marks)
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onComplete() {
                                downloadGrades();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(702, e.getLocalizedMessage());
                            }
                        });
            } catch (Exception e) {
                error(701, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the grades.
     */
    private void downloadGrades() {
        updateProgress(null);

        /*
         *  JSON response format
         *
         *  {
         *      "grades": [
         *          {
         *              "course_code": "CSE1001",
         *              "grade": "S"
         *          },
         *          ...
         *      ],
         *      "gpa": 8.58
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val();" +
                "var response = {" +
                "    grades: []," +
                "    gpa: null" +
                "};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'examinations/examGradeView/doStudentGradeView'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        if(res.toLowerCase().includes('no records')) {" +
                "            return;" +
                "        }" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var table = doc.getElementsByTagName('table')[0];" +
                "        var headings = table.getElementsByTagName('th');" +
                "        var courseCodeIndex, gradeIndex, creditsIndex, creditsSpan;" +
                "        for (var i = 0; i < headings.length; ++i) {" +
                "            var heading = headings[i].innerText.toLowerCase();" +
                "            if (heading.includes('code')) {" +
                "                courseCodeIndex = i;" +
                "            } else if (heading.includes('credits')) {" +
                "                creditsIndex = i;" +
                "                creditsSpan = headings[i].colSpan;" +
                "            } else if (heading.includes('grade')) {" +
                "                gradeIndex = i;" +
                "            }" +
                "        }" +
                "        if (courseCodeIndex > creditsIndex) {" +
                "            courseCodeIndex += creditsSpan - 1;" +
                "        }" +
                "        if (gradeIndex > creditsIndex) {" +
                "            gradeIndex += creditsSpan - 1;" +
                "        }" +
                "        var cells = table.getElementsByTagName('td');" +
                "        while(courseCodeIndex < cells.length && gradeIndex < cells.length) {" +
                "            var grade = {};" +
                "            grade.course_code = cells[courseCodeIndex].innerText.trim();" +
                "            grade.grade = cells[gradeIndex].innerText.trim();" +
                "            response.grades.push(grade);" +
                "            courseCodeIndex += headings.length - 1;" +
                "            gradeIndex += headings.length - 1;" +
                "        }" +
                "        response.gpa = cells[cells.length - 1].innerText.split(':')[1].trim();" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray gradesArray = response.getJSONArray("grades");

                for (int i = 0; i < gradesArray.length(); ++i) {
                    JSONObject gradesObject = gradesArray.getJSONObject(i);

                    String courseCode = this.getStringValue(gradesObject, "course_code");

                    if (this.cumulativeMarks.containsKey(courseCode)) {
                        Objects.requireNonNull(this.cumulativeMarks.get(courseCode)).grade = gradesObject.getString("grade");
                    }
                }

                this.sharedPreferences.edit().putString("gpa", response.getString("gpa")).apply();

                List<CumulativeMark> cumulativeMarks = new ArrayList<>(this.cumulativeMarks.values());
                MarksDao marksDao = this.appDatabase.marksDao();

                Observable<Object> deleteAllObservable = Observable.fromCompletable(marksDao.deleteCumulativeMarks());
                Observable<Object> insertCumulativeMarks = Observable.fromCompletable(marksDao.insertCumulativeMarks(cumulativeMarks));

                Observable
                        .concat(
                                deleteAllObservable,
                                insertCumulativeMarks
                        )
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(802, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                downloadExamSchedule();
                            }
                        });
            } catch (Exception e) {
                error(801, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the exam schedule.
     */
    private void downloadExamSchedule() {
        updateProgress(R.string.downloading_exam_schedule);

        /*
         *  JSON response format
         *
         *  {
         *      "FAT": [
         *          {
         *              "slot": "A1",
         *              "date": "01-JAN-2020",
         *              "start_time": "9:30 AM",
         *              "end_time": "12:30 PM",
         *              "venue": "DB-101",
         *              "seat_location": "R1C1",
         *              "seat_number": 1
         *          },
         *          ...
         *      ],
         *      ...
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val()  + '&_csrf=' + $('input[name=\"_csrf\"]').val();" +
                "var response = {};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url: 'examinations/doSearchExamScheduleForStudent'," +
                "    data: data," +
                "    async: false," +
                "    success: function(res) {" +
                "        if(res.toLowerCase().includes('not found')) {" +
                "            return;" +
                "        }" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var slotIndex, dateIndex, timingIndex, venueIndex, locationIndex, numberIndex;" +
                "        var columns = doc.getElementsByTagName('tr')[0].getElementsByTagName('td');" +
                "        for (var i = 0; i < columns.length; ++i) {" +
                "            var heading = columns[i].innerText.toLowerCase();" +
                "            if (heading.includes('slot')) {" +
                "                slotIndex = i;" +
                "            } else if (heading.includes('date')) {" +
                "                dateIndex = i;" +
                "            } else if (heading.includes('exam') && heading.includes('time')) {" +
                "                timingIndex = i;" +
                "            } else if (heading.includes('venue')) {" +
                "                venueIndex = i;" +
                "            } else if (heading.includes('location')) {" +
                "                locationIndex = i;" +
                "            } else if (heading.includes('seat') && heading.includes('no.')) {" +
                "                numberIndex = i;" +
                "            }" +
                "        }" +
                "        var examTitle = '', exam = {}, cells = doc.getElementsByTagName('td');" +
                "        for (var i = columns.length; i < cells.length; ++i) {" +
                "            if (cells[i].colSpan > 1) {" +
                "                examTitle = cells[i].innerText.trim();" +
                "                response[examTitle] = [];" +
                "                continue;" +
                "            }" +
                "            var index = (i - Object.keys(response).length) % columns.length;" +
                "            if (index == slotIndex) {" +
                "                exam.slot = cells[i].innerText.trim().split('+')[0];" +
                "            } else if (index == dateIndex) {" +
                "                var date = cells[i].innerText.trim().toUpperCase();" +
                "                exam.date = date == '' ? null : date;" +
                "            } else if (index == timingIndex) {" +
                "                var timings = cells[i].innerText.trim().split('-');" +
                "                if (timings.length == 2) {" +
                "                    exam.start_time = timings[0].trim();" +
                "                    exam.end_time = timings[1].trim();" +
                "                } else {" +
                "                    exam.start_time = null;" +
                "                    exam.end_time = null;" +
                "                }" +
                "            } else if (index == venueIndex) {" +
                "                var venue = cells[i].innerText.trim();" +
                "                exam.venue = venue.replace(/-/g,'') == '' ? null : venue;" +
                "            } else if (index == locationIndex) {" +
                "                var location = cells[i].innerText.trim();" +
                "                exam.seat_location = location.replace(/-/g,'') == '' ? null : location;" +
                "            } else if (index == numberIndex) {" +
                "                var number = cells[i].innerText.trim();" +
                "                exam.seat_number = number.replace(/-/g,'') == '' ? null : parseInt(number);" +
                "            }" +
                "            if (Object.keys(exam).length == 7) {" +
                "                response[examTitle].push(exam);" +
                "                exam = {};" +
                "            }" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                Iterator<String> keys = response.keys();
                List<Exam> exams = new ArrayList<>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH);

                int index = 1;

                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray examsArray = response.getJSONArray(key);

                    for (int i = 0; i < examsArray.length(); ++i) {
                        JSONObject examObject = examsArray.getJSONObject(i);
                        Exam exam = new Exam();
                        Pattern pattern = Pattern.compile("\\d");
                        Matcher matcher = pattern.matcher(key);

                        exam.id = ++index;
                        exam.courseId = getCourseId(examObject.getString("slot"), Course.TYPE_THEORY);
                        exam.title = key;

                        if (matcher.find()) {
                            exam.title = new StringBuilder(key).insert(matcher.start(), " ").toString().trim().replaceAll(" +", " ");
                        }

                        if (!examObject.isNull("date")) {
                            if (!examObject.isNull("start_time")) {
                                exam.startTime = Objects.requireNonNull(dateTimeFormat.parse(examObject.getString("date") + " " + examObject.getString("start_time"))).getTime();
                            } else {
                                exam.startTime = Objects.requireNonNull(dateFormat.parse(examObject.getString("date"))).getTime();
                            }

                            if (!examObject.isNull("end_time")) {
                                exam.endTime = Objects.requireNonNull(dateTimeFormat.parse(examObject.getString("date") + " " + examObject.getString("end_time"))).getTime();
                            }
                        }

                        exam.venue = getStringValue(examObject, "venue");
                        exam.seatLocation = getStringValue(examObject, "seat_location");
                        exam.seatNumber = getIntegerValue(examObject, "seat_number");

                        exams.add(exam);
                    }
                }

                SettingsRepository.setExamNotifications(this.getApplicationContext(), exams);

                ExamsDao examsDao = appDatabase.examsDao();

                Observable<Object> deleteAllObservable = Observable.fromCompletable(examsDao.deleteAll());
                Observable<Object> insertExamsObservable = Observable.fromCompletable(examsDao.insert(exams));

                Observable
                        .concat(
                                deleteAllObservable,
                                insertExamsObservable
                        )
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(902, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                downloadProctor();
                            }
                        });
            } catch (Exception e) {
                error(901, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the proctor info. (1 / 2 - Staff info)
     */
    private void downloadProctor() {
        updateProgress(R.string.downloading_staff);
        /*
         *  JSON response format
         *
         *  {
         *      "proctor" [
         *          {
         *              "key": "",
         *              "value": ""
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {" +
                "    proctor: []" +
                "};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'proctor/viewProctorDetails'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var cells = doc.getElementById('showDetails').getElementsByTagName('td');" +
                "        for(var i = 0; i < cells.length; ++i) {" +
                "            if(cells[i].innerHTML.includes('img')) {" +
                "                continue;" +
                "            }" +
                "            var record = {};" +
                "            record.key = cells[i].innerText.trim() || null;" +
                "            record.value = cells[++i].innerText.trim() || null;" +
                "            response.proctor.push(record);" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray proctorArray = response.getJSONArray("proctor");
                List<Staff> staff = new ArrayList<>();

                for (int i = 0; i < proctorArray.length(); ++i) {
                    JSONObject proctorObject = proctorArray.getJSONObject(i);
                    Staff staffItem = new Staff();

                    staffItem.id = i + 1;
                    staffItem.type = "proctor";
                    staffItem.key = this.getStringValue(proctorObject, "key");
                    staffItem.value = this.getStringValue(proctorObject, "value");

                    staff.add(staffItem);
                }

                StaffDao staffDao = appDatabase.staffDao();

                Observable<Object> deleteAllObservable = Observable.fromCompletable(staffDao.deleteAll());
                Observable<Object> insertStaffObservable = Observable.fromCompletable(staffDao.insert(staff));

                Observable
                        .concat(
                                deleteAllObservable,
                                insertStaffObservable
                        )
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(1002, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                downloadDeanHOD(staff.size());
                            }
                        });
            } catch (Exception e) {
                error(1001, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the HOD & Dean info. (2 / 2 - Staff info)
     */
    private void downloadDeanHOD(final int lastIndex) {
        updateProgress(null);
        /*
         *  JSON response format
         *
         *  {
         *      "dean": [
         *          {
         *              "key": "",
         *              "value": ""
         *          },
         *          ...
         *      ],
         *      "hod": [
         *          {
         *              "key": "",
         *              "value": ""
         *          },
         *          ...
         *      ],
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'hrms/viewHodDeanDetails'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var tables = doc.getElementsByTagName('table');" +
                "        var headings = doc.getElementsByTagName('h3');" +
                "        for (var i = 0; i < tables.length; ++i) {" +
                "            var heading = headings[i].innerText.toLowerCase().trim();" +
                "            var cells = tables[i].getElementsByTagName('td');" +
                "            response[heading] = [];" +
                "            for (var j = 0; j < cells.length; ++j) {" +
                "                if(cells[j].innerHTML.includes('img')) {" +
                "                    continue;" +
                "                }" +
                "                var record = {};" +
                "                record.key = cells[j].innerText.trim() || null;" +
                "                record.value = cells[++j].innerText.trim() || null;" +
                "                response[heading].push(record);" +
                "            }" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                Iterator<String> keys = response.keys();
                List<Staff> staff = new ArrayList<>();
                int index = lastIndex;

                while (keys.hasNext()) {
                    String staffType = keys.next();
                    JSONArray staffArray = response.getJSONArray(staffType);

                    if (staffType.contains("dean")) {
                        staffType = "dean";
                    } else if (staffType.contains("hod")) {
                        staffType = "hod";
                    } else {
                        staffType = staffType.toLowerCase();
                    }

                    for (int i = 0; i < staffArray.length(); ++i) {
                        JSONObject staffObject = staffArray.getJSONObject(i);
                        Staff staffItem = new Staff();

                        staffItem.id = ++index;
                        staffItem.type = staffType;
                        staffItem.key = this.getStringValue(staffObject, "key");
                        staffItem.value = this.getStringValue(staffObject, "value");

                        staff.add(staffItem);
                    }
                }

                StaffDao staffDao = appDatabase.staffDao();
                staffDao
                        .insert(staff)
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onComplete() {
                                downloadSpotlight();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(1004, e.getLocalizedMessage());
                            }
                        });
            } catch (Exception e) {
                error(1003, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the spotlight.
     */
    private void downloadSpotlight() {
        updateProgress(R.string.downloading_spotlight);

        /*
         *  JSON response format
         *
         *  {
         *      "spotlight": [
         *          {
         *              "announcement": "",
         *              "category": "",
         *              "link": null
         *          },
         *          ...
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = '_csrf=' + $('input[name=\"_csrf\"]').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&x=';" +
                "var response = {" +
                "    spotlight: []" +
                "};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'home'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        if(!doc.getElementsByClassName('box-info')) {" +
                "            return;" +
                "        }" +
                "        var sheets = doc.getElementsByClassName('offcanvas');" +
                "        for(var i = 0; i < sheets.length; ++i) {" +
                "            const header = sheets[i].getElementsByClassName('offcanvas-header')[0];" +
                "            const title = header.getElementsByTagName('span')[0];" +
                "            if (title === undefined) {" +
                "                continue;" +
                "            }" +
                "            const category = title.textContent;" +
                "            var announcements = sheets[i].getElementsByClassName('offcanvas-body')[0].getElementsByTagName('li');" +
                "            for(var j = 0; j < announcements.length; ++j) {" +
                "                var spotlightItem = {};" +
                "                spotlightItem.category = category;" +
                "                spotlightItem.announcement = announcements[j].textContent.replace(/\\t/g,'').replace(/\\n/g,' ').trim();" +
                "                if (announcements[j].getElementsByTagName('a').length == 0) {" +
                "                    spotlightItem.link = null;" +
                "                } else {" +
                "                    var link = announcements[j].getElementsByTagName('a')[0];" +
                "                    if(link.getAttribute('onclick')) {" +
                "                        spotlightItem.link = link.getAttribute('onclick').split('\\'')[1];" +
                "                    } else {" +
                "                        spotlightItem.link = link.href;" +
                "                    }" +
                "                }" +
                "                response.spotlight.push(spotlightItem);" +
                "            }" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray spotlightArray = response.getJSONArray("spotlight");
                Map<Integer, Spotlight> spotlight = new HashMap<>();

                for (int i = 0; i < spotlightArray.length(); ++i) {
                    JSONObject spotlightObject = spotlightArray.getJSONObject(i);
                    Spotlight spotlightItem = new Spotlight();

                    spotlightItem.id = i + 1;
                    spotlightItem.announcement = this.getStringValue(spotlightObject, "announcement");
                    spotlightItem.category = this.getStringValue(spotlightObject, "category");
                    spotlightItem.link = this.getStringValue(spotlightObject, "link");

                    // Generating a unique hash signature to keep a track of read announcements
                    spotlightItem.signature = (spotlightItem.announcement + spotlightItem.link).hashCode();
                    spotlight.put(spotlightItem.signature, spotlightItem);
                }

                appDatabase.spotlightDao()
                        .insert(spotlight)
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onComplete() {
                                downloadReceipts();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(1102, e.getLocalizedMessage());
                            }
                        });
            } catch (Exception e) {
                error(1101, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to download the payment receipts.
     */
    private void downloadReceipts() {
        updateProgress(R.string.downloading_receipts);

        /*
         *  JSON response format
         *
         *  {
         *      "receipts": [
         *          {
         *              "number": "10067",
         *              "amount": 97500,
         *              "date": "14-AUG-2020"
         *          }
         *      ]
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {" +
                "    receipts: []" +
                "};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'p2p/getReceiptsApplno'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        var doc = new DOMParser().parseFromString(res, 'text/html');" +
                "        var headings = doc.getElementsByTagName('tr')[0].getElementsByTagName('td');" +
                "        var cells = doc.getElementsByTagName('td');" +
                "        var receiptIndex, amountIndex, dateIndex;" +
                "        for(var i = 0; i < headings.length; ++i) {" +
                "            var heading = headings[i].innerText.toLowerCase();" +
                "            if(heading.includes('receipt')) {" +
                "                receiptIndex = i + headings.length;" +
                "            } else if (heading.includes('date')) {" +
                "                dateIndex = i + headings.length;" +
                "            } else if (heading.includes('amount')) {" +
                "                amountIndex = i + headings.length;" +
                "            }" +
                "        }" +
                "        while (receiptIndex < cells.length && amountIndex < cells.length && dateIndex < cells.length) {" +
                "            var receipt = {};" +
                "            receipt.number = parseInt(cells[receiptIndex].innerText.trim()) || null;" +
                "            receipt.amount = parseFloat(cells[amountIndex].innerText.trim()) || 0;" +
                "            receipt.date = cells[dateIndex].innerText.trim();" +
                "            response.receipts.push(receipt);" +
                "            receiptIndex += headings.length;" +
                "            amountIndex += headings.length;" +
                "            dateIndex += headings.length;" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                JSONArray receiptsArray = response.getJSONArray("receipts");
                List<Receipt> receipts = new ArrayList<>();

                for (int i = 0; i < receiptsArray.length(); ++i) {
                    JSONObject receiptsObject = receiptsArray.getJSONObject(i);
                    Receipt receipt = new Receipt();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

                    // If this is true, there's a web scrapping issue
                    if (receiptsObject.isNull("number")) {
                        continue;
                    }

                    String receiptDateString = this.getStringValue(receiptsObject, "date");
                    Date receiptDate = receiptDateString != null ? dateFormat.parse(receiptDateString) : null;

                    receipt.number = receiptsObject.getInt("number");
                    receipt.amount = this.getDoubleValue(receiptsObject, "amount");
                    receipt.date = receiptDate != null ? receiptDate.getTime() : 0;

                    receipts.add(receipt);
                }

                ReceiptsDao receiptsDao = appDatabase.receiptsDao();

                Observable<Object> deleteAllObservable = Observable.fromCompletable(receiptsDao.deleteAll());
                Observable<Object> insertReceiptsObservable = Observable.fromCompletable(receiptsDao.insert(receipts));

                Observable
                        .concat(
                                deleteAllObservable,
                                insertReceiptsObservable
                        )
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                error(1202, e.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                checkDues();
                            }
                        });
            } catch (Exception e) {
                error(1201, e.getLocalizedMessage());
            }
        });
    }

    /**
     * Function to check for any payment dues.
     */
    private void checkDues() {
        updateProgress(null);

        /*
         *  JSON response format
         *
         *  {
         *      "due_payments": true|false
         *  }
         */
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&_csrf=' + $('input[name=\"_csrf\"]').val() + '&nocache=@(new Date().getTime())';" +
                "var response = {};" +
                "$.ajax({" +
                "    type: 'POST'," +
                "    url : 'p2p/Payments'," +
                "    data : data," +
                "    async: false," +
                "    success: function(res) {" +
                "        if (res.toLowerCase().includes('no payment dues')) {" +
                "            response.due_payments = false;" +
                "        } else {" +
                "            response.due_payments = true;" +
                "        }" +
                "    }" +
                "});" +
                "return response;" +
                "})();", responseString -> {
            try {
                JSONObject response = new JSONObject(responseString);
                boolean duePayments = response.getBoolean("due_payments");

                if (duePayments) {
                    sharedPreferences.edit().putBoolean("duePayments", true).apply();
                } else {
                    sharedPreferences.edit().remove("duePayments").apply();
                }
            } catch (Exception e) {
                error(1203, e.getLocalizedMessage());
            }

            finishUp();
        });
    }

    /**
     * Function to make final changes before signing the user in.
     */
    private void finishUp() {
        this.notification.setContentTitle(getString(R.string.completing_sync));
        this.notification.setProgress(0, 0, true);
        this.notification.setContentText(null);

        this.notificationManager.notify(SettingsRepository.NOTIFICATION_ID_VTOP_DOWNLOAD, this.notification.build());

        this.sharedPreferences.edit().putBoolean("isVTOPSignedIn", true).apply();
        this.sharedPreferences.edit().putLong("lastRefreshed", Calendar.getInstance().getTimeInMillis()).apply();

        // Firebase Analytics Logging
        FirebaseAnalytics.getInstance(this).logEvent("data_sync", new Bundle());

        try {
            this.callback.onComplete();
        } catch (Exception ignored) {
        }

        this.endService(true);
    }

    /**
     * Function to get the slot ID using the slot.
     *
     * @param slot Ex: "A1", "L21", etc.
     * @return The slot ID
     */
    private Integer getSlotId(String slot, int courseType) {
        switch (courseType) {
            case Course.TYPE_LAB:
                if (!this.labSlots.containsKey(slot)) {
                    return null;
                }

                return Objects.requireNonNull(this.labSlots.get(slot)).id;
            case Course.TYPE_PROJECT:
                if (!this.projectSlots.containsKey(slot)) {
                    return null;
                }

                return Objects.requireNonNull(this.projectSlots.get(slot)).id;
            default:
                if (!this.theorySlots.containsKey(slot)) {
                    return null;
                }

                return Objects.requireNonNull(this.theorySlots.get(slot)).id;
        }
    }

    /**
     * Function to get the course ID using the slot.
     *
     * @param slot Ex: "A1", "L21", etc.
     * @return The course ID
     */
    private Integer getCourseId(String slot, int courseType) {
        switch (courseType) {
            case Course.TYPE_LAB:
                if (!this.labSlots.containsKey(slot)) {
                    return null;
                }

                return Objects.requireNonNull(this.labSlots.get(slot)).courseId;
            case Course.TYPE_PROJECT:
                if (!this.projectSlots.containsKey(slot)) {
                    return null;
                }

                return Objects.requireNonNull(this.projectSlots.get(slot)).courseId;
            default:
                if (!this.theorySlots.containsKey(slot)) {
                    return null;
                }

                return Objects.requireNonNull(this.theorySlots.get(slot)).courseId;
        }
    }

    /**
     * Function to get the course credits using the course ID.
     *
     * @param courseId The course ID as saved in the database
     * @return The number of credits for that course
     */
    private Integer getCourseCredits(Integer courseId, int courseType) {
        switch (courseType) {
            case Course.TYPE_LAB:
                if (!this.labCourses.containsKey(courseId)) {
                    return null;
                }

                return Objects.requireNonNull(this.labCourses.get(courseId)).credits;
            case Course.TYPE_PROJECT:
                if (!this.projectCourses.containsKey(courseId)) {
                    return null;
                }

                return Objects.requireNonNull(this.projectCourses.get(courseId)).credits;
            default:
                if (!this.theoryCourses.containsKey(courseId)) {
                    return null;
                }

                return Objects.requireNonNull(this.theoryCourses.get(courseId)).credits;
        }
    }

    /**
     * Function to get the course code using the course ID.
     *
     * @param courseId The course ID as saved in the database
     * @return The course code of that course
     */
    private String getCourseCode(Integer courseId, int courseType) {
        switch (courseType) {
            case Course.TYPE_LAB:
                if (!this.labCourses.containsKey(courseId)) {
                    return null;
                }

                return Objects.requireNonNull(this.labCourses.get(courseId)).code;
            case Course.TYPE_PROJECT:
                if (!this.projectCourses.containsKey(courseId)) {
                    return null;
                }

                return Objects.requireNonNull(this.projectCourses.get(courseId)).code;
            default:
                if (!this.theoryCourses.containsKey(courseId)) {
                    return null;
                }

                return Objects.requireNonNull(this.theoryCourses.get(courseId)).code;
        }
    }

    /**
     * Function to get the String value from a JSON object using a key.
     *
     * @param jsonObject The JSON object to be used
     * @param key        The key to be used to get the value
     * @return The value stored or null if the key wasn't present
     */
    private String getStringValue(JSONObject jsonObject, String key) throws JSONException {
        if (!jsonObject.has(key) || jsonObject.isNull(key)) {
            return null;
        }

        return jsonObject.getString(key);
    }

    /**
     * Function to get the Integer value from a JSON object using a key.
     *
     * @param jsonObject The JSON object to be used
     * @param key        The key to be used to get the value
     * @return The value stored or null if the key wasn't present
     */
    private Integer getIntegerValue(JSONObject jsonObject, String key) throws JSONException {
        if (!jsonObject.has(key) || jsonObject.isNull(key)) {
            return null;
        }

        return jsonObject.getInt(key);
    }

    /**
     * Function to get the Double value from a JSON object using a key.
     *
     * @param jsonObject The JSON object to be used
     * @param key        The key to be used to get the value
     * @return The value stored or null if the key wasn't present
     */
    private Double getDoubleValue(JSONObject jsonObject, String key) throws JSONException {
        if (!jsonObject.has(key) || jsonObject.isNull(key)) {
            return null;
        }

        return jsonObject.getDouble(key);
    }

    public interface Callback {
        void onRequestCaptcha(int captchaType, Bitmap bitmap, WebView webView);

        void onCaptchaComplete();

        void onRequestSemester(String[] semesters);

        void onServiceEnd();

        void onForceSignOut();

        void onComplete();
    }

    public class ServiceBinder extends Binder {
        public VTOPService getService() {
            return VTOPService.this;
        }

        public void setCallback(Callback mCallback) {
            callback = mCallback;
        }
    }

    private enum PageState {LANDING, LOGIN, HOME}
}

/*
 * Error codes
 *
 * Error 101    Errors connecting to the server
 * Error 102    Error fetching user credentials
 * Error 103    Error opening sign in page
 * Error 104    Error getting captcha type
 * Error 105    Error getting default captcha image
 * Error 106    Error while attempting to sign in
 *
 * Error 201    Error fetching list of semesters
 *
 * Error 301    Error fetching user's name
 * Error 302    Error fetching user's credits & GPA
 *
 * Error 401    Error downloading courses
 * Error 402    Error saving courses to the database
 *
 * Error 501    Error downloading timetable
 * Error 502    Error saving timetable to the database
 *
 * Error 601    Error downloading attendance
 * Error 602    Error saving attendance to the database
 *
 * Error 701    Error downloading marks
 * Error 702    Error saving marks to the database
 *
 * Error 801    Error downloading grades
 * Error 802    Error saving grades to the database
 *
 * Error 901    Error downloading exam schedule
 * Error 902    Error saving exam schedule to the database
 *
 * Error 1001   Error downloading proctor info
 * Error 1002   Error saving proctor info to the database
 * Error 1003   Error downloading dean & hod info
 * Error 1004   Error saving dean & hod info to the database
 *
 * Error 1101   Error downloading spotlight
 * Error 1102   Error saving spotlight to the database
 *
 * Error 1201   Error downloading receipts
 * Error 1202   Error saving receipts to the database
 * Error 1203   Error checking for due payments
 */
