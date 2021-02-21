package tk.therealsuji.vtopchennai;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.util.Base64;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

public class VTOP {
    Context context;

    WebView webView;
    Dialog downloadDialog;
    ImageView captcha;
    EditText captchaView;
    ProgressBar loading, progressBar;
    Spinner selectSemester;
    LinearLayout captchaLayout, downloadingLayout, semesterLayout;

    /*
        DARK is used to change the colours of the captcha image when in dark mode
     */
    private static final float[] DARK = {
            -0.853f, 0, 0, 0, 255, // R
            0, -0.853f, 0, 0, 255, // G
            0, 0, -0.853f, 0, 255, // B
            0, 0, 0, 1, 0  // A
    };

    SharedPreferences sharedPreferences;
    SQLiteDatabase myDatabase;
    TextView downloading, progressText;
    int counter, lastDownload;
    Boolean isOpened, isLoggedIn;

    String semesterID;

    @SuppressLint("SetJavaScriptEnabled")
    public VTOP(final Context context, final Dialog downloadDialog) {
        this.context = context;
        this.downloadDialog = downloadDialog;
        webView = new WebView(context);
        loading = downloadDialog.findViewById(R.id.loading);
        captcha = downloadDialog.findViewById(R.id.captchaCode);
        captchaLayout = downloadDialog.findViewById(R.id.captchaLayout);
        captchaView = downloadDialog.findViewById(R.id.captcha);
        downloadingLayout = downloadDialog.findViewById(R.id.downloadingLayout);
        semesterLayout = downloadDialog.findViewById(R.id.semesterLayout);
        downloading = downloadDialog.findViewById(R.id.downloading);
        progressBar = downloadDialog.findViewById(R.id.progressBar);
        progressText = downloadDialog.findViewById(R.id.progressText);
        selectSemester = downloadDialog.findViewById(R.id.selectSemester);
        sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36");
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (!isOpened) {
                    if (counter >= 60) {
                        Toast.makeText(context, "Sorry, we had some trouble connecting to the server. Please try again later.", Toast.LENGTH_LONG).show();
                        myDatabase.close();
                        downloadDialog.dismiss();
                        return;
                    }
                    isOpened = true;
                    openSignIn();
                    ++counter;
                }
            }
        });

        isOpened = false;
        isLoggedIn = false;
        counter = 0;
        lastDownload = 0;

        reloadPage();
    }

    /*
        Function to perform a smooth animation of expanding the layouts in the dialog
     */
    public static void expand(final View view) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.INVISIBLE);
        view.setAlpha(0);

        ValueAnimator anim = ValueAnimator.ofInt(0, targetHeight);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.height = (int) (targetHeight * animation.getAnimatedFraction());
                view.setLayoutParams(layoutParams);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                view.setVisibility(View.VISIBLE);
                view.animate().alpha(1);
            }
        });
        anim.start();
    }

    /*
        Function to perform a smooth animation of compressing the layouts in the dialog
     */
    public static void compress(final View view) {
        if (view.getVisibility() == View.GONE) {
            return;
        }

        view.animate().alpha(0);

        final int viewHeight = view.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(viewHeight, 0);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.height = (int) (viewHeight * (1 - animation.getAnimatedFraction()));
                view.setLayoutParams(layoutParams);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        anim.start();
    }

    /*
        Function to hide all layouts at once because i'm too lazy to keep typing these
     */
    public void hideLayouts() {
        loading.setVisibility(View.INVISIBLE);

        compress(downloadingLayout);
        compress(semesterLayout);
        compress(captchaLayout);
    }

    /*
        Function to update the progress of the download. If more data needs to be downloaded,
        the max value can simply be updated here (SHOULD ALSO BE UPDATED IN dialog_download.xml)
     */
    public void setProgress() {
        progressBar.setProgress(++lastDownload, true);
        String progress = lastDownload + " / 13";
        progressText.setText(progress);
    }

    /*
        Function to reload the page using javascript in case of an error.
        If something goes wrong, it'll log out and ask for the captcha again.
     */
    public void reloadPage() {
        if (loading.getVisibility() == View.INVISIBLE) {
            hideLayouts();
            loading.setVisibility(View.VISIBLE);
        }

        webView.clearCache(true);
        webView.clearHistory();
        CookieManager.getInstance().removeAllCookies(null);
        webView.loadUrl("http://vtopcc.vit.ac.in/vtop");
    }

    /*
        Function to open the sign in page
     */
    private void openSignIn() {
        webView.evaluateJavascript("(function() {" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url: 'vtopLogin'," +
                "   data: null," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.search('___INTERNAL___RESPONSE___') == -1 && response.includes('VTOP Login')) {" +
                "           $('#page_outline').html(response);" +
                "           successFlag = true;" +
                "       }" +
                "   }" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    getCaptcha();
                } else {
                    isOpened = false;
                    reloadPage();
                }
            }
        });
    }

    /*
        Function to get the captcha from the portal's sign in page and load it into the ImageView
     */
    private void getCaptcha() {
        webView.evaluateJavascript("(function() {" +
                "var images = document.getElementsByTagName('img');" +
                "for(var i = 0; i < images.length; ++i) {" +
                "   if(images[i].alt.toLowerCase().includes('captcha')) {" +
                "       return images[i].src;" +
                "   }" +
                "}" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String src) {
                /*
                    src will look like "data:image/png:base64, ContinuousGibberishText...." (including the quotes)
                 */
                if (src.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong while trying to fetch the captcha code. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else {
                    try {
                        src = src.substring(1, src.length() - 1).split(",")[1].trim();
                        byte[] decodedString = Base64.decode(src, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        String appearance = sharedPreferences.getString("appearance", "system");
                        captcha.setImageBitmap(decodedImage);

                        if (appearance.equals("dark") || (appearance.equals("system") && (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)) {
                            captcha.setColorFilter(new ColorMatrixColorFilter(DARK));
                        }

                        hideLayouts();
                        captchaView.setText("");
                        expand(captchaLayout);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error();
                    }
                }
            }
        });
    }

    /*
        Function to sign in to the portal
     */
    public void signIn(String username, String password, String captcha) {
        webView.evaluateJavascript("(function() {" +
                "var credentials = 'uname=" + username + "&passwd=' + encodeURIComponent('" + password + "') + '&captchaCheck=" + captcha + "';" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type : 'POST'," +
                "   url : 'doLogin'," +
                "   data : credentials," +
                "   async: false," +
                "   success : function(response) {" +
                "       if(response.search('___INTERNAL___RESPONSE___') == -1) {" +
                "           if(response.includes('authorizedIDX')) {" +
                "               $('#page_outline').html(response);" +
                "               successFlag = true;" +
                "           } else if(response.toLowerCase().includes('invalid captcha')) {" +
                "               successFlag = 'Invalid Captcha';" +
                "           } else if(response.toLowerCase().includes('invalid user id / password')) {" +
                "               successFlag = 'Invalid User Id / Password';" +
                "               } else if(response.toLowerCase().includes('user id not available')) {" +
                "               successFlag = 'User Id Not available';" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    isLoggedIn = true;
                    getSemesters();
                } else {
                    if (!value.equals("false") && !value.equals("null")) {
                        value = value.substring(1, value.length() - 1);
                        if (value.equals("Invalid User Id / Password") || value.equals("User Id Not available")) {
                            sharedPreferences.edit().putString("isLoggedIn", "false").apply();
                            myDatabase.close();
                            context.startActivity(new Intent(context, LoginActivity.class));
                            ((Activity) context).finish();
                        }
                        Toast.makeText(context, value, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    }
                    isOpened = false;
                    reloadPage();
                }
            }
        });
    }

    /*
        Function to get a list of the semesters. These semesters are obtained from the Timetable page
     */
    public void getSemesters() {
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = false;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'academics/common/StudentTimeTable'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('time table')) {" +
                "           var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "           var options = doc.getElementById('semesterSubId').getElementsByTagName('option');" +
                "           obj = {};" +
                "           for(var i = 0, j = 0; i < options.length; ++i, ++j) {" +
                "               if(options[i].innerText.toLowerCase().includes('choose') || options[i].innerText.toLowerCase().includes('select')) {" +
                "                   --j;" +
                "                   continue;" +
                "               }" +
                "               obj[j] = options[i].innerText;" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                /*
                    obj is in the form of a JSON string like {"0": "Semester 1", "1": "Semester 2", "2": "Semester 3",...}
                 */
                if (obj.equals("false") || obj.equals("null")) {
                    error();
                } else {
                    try {
                        JSONObject myObj = new JSONObject(obj);
                        List<String> options = new ArrayList<>();
                        for (int i = 0; i < myObj.length(); ++i) {
                            options.add(myObj.getString(Integer.toString(i)));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.style_spinner_selected, options);
                        adapter.setDropDownViewResource(R.layout.style_spinner);

                        selectSemester.setAdapter(adapter);
                        hideLayouts();
                        expand(semesterLayout);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error();
                    }
                }
            }
        });
    }

    /*
        Function to get the semester ID from the Timetable page and store it locally for future use
        This is required to download the timetable, faculty, attendance, exam schedule, marks & grades
     */
    public void getSemesterID(String semester) {
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var semID = '';" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'academics/common/StudentTimeTable'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('time table')) {" +
                "           var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "           var options = doc.getElementById('semesterSubId').getElementsByTagName('option');" +
                "           for(var i = 0; i < options.length; ++i) {" +
                "               if(options[i].innerText.toLowerCase().includes('" + semester + "')) {" +
                "                   semID = options[i].value;" +
                "               }" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return semID;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String semID) {
                if (semID.equals("null")) {
                    error();
                } else {
                    semesterID = semID.substring(1, semID.length() - 1);
                    downloadProfile();
                }
            }
        });
    }

    /*
        Function to save the name of the user and his/her ID (register number) in SharedPreferences
        TBD: Saving the users profile picture
     */
    public void downloadProfile() {
        downloading.setText(context.getString(R.string.downloading_profile));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'studentsRecord/StudentProfileAllView'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('personal information')) {" +
                "           var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "           var cells = doc.getElementsByTagName('td');" +
                "           var name = '', id = '', j = 0;" +
                "           for(var i = 0; i < cells.length && j < 2; ++i) {" +
                "               if(cells[i].innerText.toLowerCase().includes('name')) {" +
                "                   name = cells[++i].innerHTML;" +
                "                   ++j;" +
                "               }" +
                "               if(cells[i].innerText.toLowerCase().includes('register')) {" +
                "                   id = cells[++i].innerHTML;" +
                "                   ++j;" +
                "               }" +
                "           }" +
                "           obj['name'] = name;" +
                "           obj['id'] = id;" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                /*
                    obj is in the form of a JSON string like {"name": "JOHN DOE", "register": "20XYZ1987"}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else {
                    try {
                        JSONObject myObj = new JSONObject(obj);
                        sharedPreferences.edit().putString("name", myObj.getString("name")).apply();
                        sharedPreferences.edit().putString("id", myObj.getString("id")).apply();

                        lastDownload = 0;
                        setProgress();

                        downloadTimetable();
                    } catch (Exception e) {
                        e.printStackTrace();
                        error();
                    }
                }
            }
        });
    }

    /*
        Function to save the timetable in the SQLite database, and the credit score in SharedPreferences
     */
    public void downloadTimetable() {
        downloading.setText(context.getString(R.string.downloading_timetable));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var obj = {};" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type : 'POST'," +
                "   url : 'processViewTimeTable'," +
                "   data : data," +
                "   async: false," +
                "   success : function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       var spans = doc.getElementById('getStudentDetails').getElementsByTagName('span');" +
                "       var credits = '0';" +
                "       if(spans[0].innerText.toLowerCase().includes('no record(s) found')) {" +
                "          obj = 'unreleased';" +
                "          return;" +
                "       }" +
                "       for(var i = spans.length-1; i > 0; --i) {" +
                "          if(spans[i].innerText.toLowerCase().includes('credits')) {" +
                "              credits = spans[i+1].innerText;" +
                "              break;" +
                "          }" +
                "       }" +
                "       obj['credits'] = credits;" +
                "       var cells = doc.getElementById('timeTableStyle').getElementsByTagName('td');" +
                "       var category = '';" +
                "       var timings = '';" +
                "       var theory = {}, lab = {}, mon = {}, tue = {}, wed = {}, thu = {}, fri = {}, sat = {}, sun = {};" +
                "       var i = 0;" +
                "       for(var j = 0; j < cells.length; ++j) {" +
                "          if(cells[j].innerText.toLowerCase() == 'mon' || cells[j].innerText.toLowerCase() == 'tue' || cells[j].innerText.toLowerCase() == 'wed' || cells[j].innerText.toLowerCase() == 'thu' || cells[j].innerText.toLowerCase() == 'fri' || cells[j].innerText.toLowerCase() == 'sat' || cells[j].innerText.toLowerCase() == 'sun') {" +
                "              category = cells[j].innerText.toLowerCase();" +
                "              continue;" +
                "          }" +
                "          if(cells[j].innerText.toLowerCase() == 'theory' || cells[j].innerText.toLowerCase() == 'lab') {" +
                "              if(category == '' || category == 'theory' || category == 'lab') {" +
                "                  category = cells[j].innerText.toLowerCase();" +
                "              } else {" +
                "                  postfix = cells[j].innerText.toLowerCase();" +
                "              }" +
                "              i = 0;" +
                "              continue;" +
                "          }" +
                "          if(cells[j].innerText.toLowerCase() == 'start' || cells[j].innerText.toLowerCase() == 'end') {" +
                "              postfix = cells[j].innerText.toLowerCase();" +
                "              i = 0;" +
                "              continue;" +
                "          }" +
                "          subcat = i + postfix;" +
                "          if(category == 'theory') {" +
                "             theory[subcat] = cells[j].innerText.trim();" +
                "          } else if(category == 'lab') {" +
                "             lab[subcat] = cells[j].innerText.trim();" +
                "          } else if(category == 'mon') {" +
                "             if(cells[j].bgColor == '#CCFF33') {" +
                "                 mon[subcat] = cells[j].innerText.trim();" +
                "             }" +
                "          } else if(category == 'tue') {" +
                "             if(cells[j].bgColor == '#CCFF33') {" +
                "                 tue[subcat] = cells[j].innerText.trim();" +
                "             }" +
                "          } else if(category == 'wed') {" +
                "             if(cells[j].bgColor == '#CCFF33') {" +
                "                 wed[subcat] = cells[j].innerText.trim();" +
                "             }" +
                "          } else if(category == 'thu') {" +
                "             if(cells[j].bgColor == '#CCFF33') {" +
                "                 thu[subcat] = cells[j].innerText.trim();" +
                "             }" +
                "          } else if(category == 'fri') {" +
                "             if(cells[j].bgColor == '#CCFF33') {" +
                "                 fri[subcat] = cells[j].innerText.trim();" +
                "             }" +
                "          } else if(category == 'sat') {" +
                "            if(cells[j].bgColor == '#CCFF33') {" +
                "                sat[subcat] = cells[j].innerText.trim();" +
                "             }" +
                "          } else if(category == 'sun') {" +
                "             if(cells[j].bgColor == '#CCFF33') {" +
                "                sun[subcat] = cells[j].innerText.trim();" +
                "            }" +
                "         }" +
                "          ++i;" +
                "       }" +
                "       obj.theory = theory;" +
                "       obj.lab = lab;" +
                "       obj.mon = mon;" +
                "       obj.tue = tue;" +
                "       obj.wed = wed;" +
                "       obj.thu = thu;" +
                "       obj.fri = fri;" +
                "       obj.sat = sat;" +
                "       obj.sun = sun;" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"credits": "19", "lab": {"0start": "08:00", "0end": "08:50",...}, "mon": {"0theory": "MAT1001",...}, ...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("unreleased")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            myDatabase.execSQL("DROP TABLE IF EXISTS timetable_lab");
                            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INTEGER PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                            myDatabase.execSQL("DROP TABLE IF EXISTS timetable_theory");
                            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INTEGER PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                            sharedPreferences.edit().remove("credits").apply();

                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                            Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                            for (int j = 0; j < sharedPreferences.getInt("alarmCount", 0); ++j) {
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                                alarmManager.cancel(pendingIntent);
                            }

                            sharedPreferences.edit().remove("newTimetable").apply();
                            sharedPreferences.edit().remove("alarmCount").apply();

                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setProgress();
                                    downloadFaculty();
                                }
                            });
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);
                                String credits = "Credits: " + myObj.getString("credits");
                                sharedPreferences.edit().putString("credits", credits).apply();

                                myDatabase.execSQL("DROP TABLE IF EXISTS timetable_lab");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INTEGER PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                                myDatabase.execSQL("DROP TABLE IF EXISTS timetable_theory");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INTEGER PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                                JSONObject lab = new JSONObject(myObj.getString("lab"));
                                JSONObject theory = new JSONObject(myObj.getString("theory"));
                                JSONObject mon = new JSONObject(myObj.getString("mon"));
                                JSONObject tue = new JSONObject(myObj.getString("tue"));
                                JSONObject wed = new JSONObject(myObj.getString("wed"));
                                JSONObject thu = new JSONObject(myObj.getString("thu"));
                                JSONObject fri = new JSONObject(myObj.getString("fri"));
                                JSONObject sat = new JSONObject(myObj.getString("sat"));
                                JSONObject sun = new JSONObject(myObj.getString("sun"));

                                Calendar c = Calendar.getInstance();
                                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                                Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                Date today = dateFormat.parse(dateFormat.format(c.getTime()));
                                Date now = timeFormat.parse(timeFormat.format(c.getTime()));
                                int day = c.get(Calendar.DAY_OF_WEEK) - 1;

                                JSONObject[] days = {sun, mon, tue, wed, thu, fri, sat};

                                int alarmCount = 0;

                                /*
                                    This 12 hour check is because the genius developers at VIT decided it
                                    would be a great idea to use both 12 hour and 24 hour formats together
                                    because, who even cares...
                                 */
                                SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                                boolean checkHour12 = false, isHour12 = false;

                                for (int i = 0; i < lab.length() / 2 && i < theory.length() / 2; ++i) {
                                    String start_time_lab = lab.getString(i + "start");
                                    if (start_time_lab.toLowerCase().equals("lunch")) {
                                        checkHour12 = true;
                                        continue;
                                    }
                                    String end_time_lab = lab.getString(i + "end");
                                    String start_time_theory = theory.getString(i + "start");
                                    String end_time_theory = theory.getString(i + "end");

                                    if (checkHour12) {
                                        try {
                                            Date startTime = hour24.parse(start_time_lab);
                                            Date hourNoon = hour24.parse("12:00");

                                            if (startTime != null && startTime.before(hourNoon)) {
                                                isHour12 = true;
                                            }

                                            checkHour12 = false;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    String[] labPeriods = {"null", "null", "null", "null", "null", "null", "null"};
                                    String[] theoryPeriods = {"null", "null", "null", "null", "null", "null", "null"};

                                    for (int j = 0; j < 7; ++j) {
                                        /*
                                            Inserting Lab Periods
                                         */
                                        if (days[j].has(i + "lab")) {
                                            labPeriods[j] = days[j].getString(i + "lab");

                                            if (j == day) {
                                                Date current = timeFormat.parse(start_time_lab);
                                                assert current != null;
                                                if (current.after(now) || current.equals(now)) {
                                                    assert today != null;
                                                    c.setTime(today);
                                                } else {
                                                    assert today != null;
                                                    c.setTime(today);
                                                    c.add(Calendar.DATE, 7);
                                                }
                                            } else if (j > day) {
                                                assert today != null;
                                                c.setTime(today);
                                                c.add(Calendar.DATE, j - day);
                                            } else {
                                                assert today != null;
                                                c.setTime(today);
                                                c.add(Calendar.DATE, 7 - day + j);
                                            }

                                            Date date = df.parse(dateFormat.format(c.getTime()) + " " + start_time_lab);

                                            assert date != null;
                                            c.setTime(date);
                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, 0);
                                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

                                            c.add(Calendar.MINUTE, -30);
                                            pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, 0);
                                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                                        }

                                        /*
                                            Inserting Theory periods
                                         */
                                        if (days[j].has(i + "theory")) {
                                            theoryPeriods[j] = days[j].getString(i + "theory");

                                            if (j == day) {
                                                Date current = timeFormat.parse(start_time_theory);
                                                assert current != null;
                                                if (current.after(now) || current.equals(now)) {
                                                    assert today != null;
                                                    c.setTime(today);
                                                } else {
                                                    assert today != null;
                                                    c.setTime(today);
                                                    c.add(Calendar.DATE, 7);
                                                }
                                            } else if (j > day) {
                                                assert today != null;
                                                c.setTime(today);
                                                c.add(Calendar.DATE, j - day);
                                            } else {
                                                assert today != null;
                                                c.setTime(today);
                                                c.add(Calendar.DATE, 7 - day + j);
                                            }

                                            Date date = df.parse(dateFormat.format(c.getTime()) + " " + start_time_theory);

                                            assert date != null;
                                            c.setTime(date);
                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, 0);
                                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

                                            c.add(Calendar.MINUTE, -30);
                                            pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, 0);
                                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                                        }
                                    }

                                    if (isHour12) {
                                        try {
                                            Date startTimeLab = hour12.parse(start_time_lab + " PM");
                                            Date endTimeLab = hour12.parse(end_time_lab + " PM");
                                            Date startTimeTheory = hour12.parse(start_time_theory + " PM");
                                            Date endTimeTheory = hour12.parse(end_time_theory + " PM");

                                            if (startTimeLab != null && endTimeLab != null && startTimeTheory != null && endTimeTheory != null) {
                                                start_time_lab = hour24.format(startTimeLab);
                                                end_time_lab = hour24.format(endTimeLab);
                                                start_time_theory = hour24.format(startTimeTheory);
                                                end_time_theory = hour24.format(endTimeTheory);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    myDatabase.execSQL("INSERT INTO timetable_lab (start_time, end_time, sun, mon, tue, wed, thu, fri, sat) VALUES ('" + start_time_lab + "', '" + end_time_lab + "', '" + labPeriods[0] + "', '" + labPeriods[1] + "', '" + labPeriods[2] + "', '" + labPeriods[3] + "', '" + labPeriods[4] + "', '" + labPeriods[5] + "', '" + labPeriods[6] + "')");
                                    myDatabase.execSQL("INSERT INTO timetable_theory (start_time, end_time, sun, mon, tue, wed, thu, fri, sat) VALUES ('" + start_time_theory + "', '" + end_time_theory + "', '" + theoryPeriods[0] + "', '" + theoryPeriods[1] + "', '" + theoryPeriods[2] + "', '" + theoryPeriods[3] + "', '" + theoryPeriods[4] + "', '" + theoryPeriods[5] + "', '" + theoryPeriods[6] + "')");
                                }

                                for (int i = alarmCount; i < sharedPreferences.getInt("alarmCount", 0); ++i) {
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, notificationIntent, 0);
                                    alarmManager.cancel(pendingIntent);
                                }

                                sharedPreferences.edit().putInt("alarmCount", alarmCount).apply();

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadFaculty();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store the faculty info from the timetable page in the SQLite database.
     */
    public void downloadFaculty() {
        downloading.setText(context.getString(R.string.downloading_faculty));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var obj = {};" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type : 'POST'," +
                "   url : 'processViewTimeTable'," +
                "   data : data," +
                "   async: false," +
                "   success : function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       if (!doc.getElementById('studentDetailsList')) {" +
                "           obj = 'nothing';" +
                "           return;" +
                "       }" +
                "       var division = doc.getElementById('studentDetailsList'); " +
                "       var heads = division.getElementsByTagName('th');" +
                "       var courseIndex, facultyIndex, flag = 0;" +
                "       var columns = heads.length;" +
                "       for(var i = 0; i < columns; ++i) {" +
                "          var heading = heads[i].innerText.toLowerCase();" +
                "          if(heading == 'course') {" +
                "              courseIndex = i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "              ++flag;" +
                "          }" +
                "          if(heading.includes('faculty') && heading.includes('details')) {" +
                "              facultyIndex = i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "              ++flag;" +
                "          }" +
                "          if(flag >= 2) {" +
                "              break;" +
                "          }" +
                "       }" +
                "       var cells = division.getElementsByTagName('td');" +
                "       for(var i = 0; courseIndex < cells.length && facultyIndex < cells.length; ++i) {" +
                "           var temp = {};" +
                "           temp['course'] = cells[courseIndex].innerText.trim();" +
                "           temp['faculty'] = cells[facultyIndex].innerText.trim();" +
                "           obj[i] = temp;" +
                "           courseIndex += columns;" +
                "           facultyIndex += columns;" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"0": {"course": "MAT1001", "faculty": "JAMES VERTIGO"},...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("nothing")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS faculty");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS faculty (id INTEGER PRIMARY KEY, course VARCHAR, faculty VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadProctor();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().remove("newFaculty").apply();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS faculty");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS faculty (id INTEGER PRIMARY KEY, course VARCHAR, faculty VARCHAR)");

                                for (int i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String course = tempObj.getString("course");
                                    String faculty = tempObj.getString("faculty");

                                    myDatabase.execSQL("INSERT INTO faculty (course, faculty) VALUES('" + course + "', '" + faculty + "')");
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadProctor();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store the proctor info (1 / 2 - Staff info) in the SQLite database.
     */
    public void downloadProctor() {
        downloading.setText(context.getString(R.string.downloading_staff));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'proctor/viewProctorDetails'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       if(!doc.getElementById('showDetails').getElementsByTagName('td')) {" +
                "       obj = 'unavailable';" +
                "       return;" +
                "   }" +
                "   var cells = doc.getElementById('showDetails').getElementsByTagName('td');" +
                "   for(var i = 0; i < cells.length; ++i) {" +
                "       if(cells[i].innerHTML.includes('img')) {" +
                "           continue;" +
                "       }" +
                "       var key = cells[i].innerText.trim();" +
                "       var value = cells[++i].innerText.trim();" +
                "       var prefix = i;" +
                "       if (i < 10) {" +
                "           prefix = '0' + i;" +
                "       }" +
                "       obj[prefix + key] = value;" +
                "   }" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"Faculty Name": "Jack Ryan", "Email ID": "jack@cia.gov.us",...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("unavailable")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS proctor");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadDeanHOD();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS proctor");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                Iterator<?> keys = myObj.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    String value = myObj.getString(key);

                                    key = key.substring(2);

                                    myDatabase.execSQL("INSERT INTO proctor (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadDeanHOD();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store the HOD & Dean info (2 / 2 - Staff info) in the SQLite database.
     */
    public void downloadDeanHOD() {
        downloading.setText(context.getString(R.string.downloading_staff));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'hrms/viewHodDeanDetails'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       if(!doc.getElementsByTagName('table')[0]) {" +
                "           obj = 'unavailable';" +
                "           return;" +
                "       }" +
                "       var tables = doc.getElementsByTagName('table');" +
                "       var first = 'dean';" +
                "       if(doc.getElementsByTagName('h3')[0].innerText.toLowerCase() != 'dean') {" +
                "           first = 'hod';" +
                "       }" +
                "       var dean = {}, hod = {};" +
                "       var cells = tables[0].getElementsByTagName('td');" +
                "       for(var i = 0; i < cells.length; ++i) {" +
                "           if(first == 'dean') {" +
                "               if(cells[i].innerHTML.includes('img')) {" +
                "                   continue;" +
                "               }" +
                "               var key = cells[i].innerText.trim();" +
                "               var value = cells[++i].innerText.trim();" +
                "               var prefix = i;" +
                "               if (i < 10) {" +
                "                   prefix = '0' + i;" +
                "               }" +
                "               dean[prefix + key] = value;" +
                "               } else {" +
                "                   if(cells[i].innerHTML.includes('img')) {" +
                "                   continue;" +
                "               }" +
                "               var key = cells[i].innerText.trim();" +
                "               var value = cells[++i].innerText.trim();" +
                "               var prefix = i;" +
                "               if (i < 10) {" +
                "                   prefix = '0' + i;" +
                "               }" +
                "               hod[prefix + key] = value;" +
                "           }" +
                "       }" +
                "       var cells = tables[1].getElementsByTagName('td');" +   //Possible error: If only one table is present
                "       for(var i = 0; i < cells.length; ++i) {" +
                "           if(first == 'dean') {" +
                "               if(cells[i].innerHTML.includes('img')) {" +
                "                   continue;" +
                "               }" +
                "               var key = cells[i].innerText.trim();" +
                "               var value = cells[++i].innerText.trim();" +
                "               var prefix = i;" +
                "               if (i < 10) {" +
                "                   prefix = '0' + i;" +
                "               }" +
                "               hod[prefix + key] = value;" +
                "           } else {" +
                "               if(cells[i].innerHTML.includes('img')) {" +
                "                   continue;" +
                "           }" +
                "           var key = cells[i].innerText.trim();" +
                "           var value = cells[++i].innerText.trim();" +
                "           var prefix = i;" +
                "           if (i < 10) {" +
                "               prefix = '0' + i;" +
                "           }" +
                "           dean[prefix + key] = value;" +
                "       }" +
                "   }" +
                "   obj['dean'] = dean;" +
                "   obj['hod'] = hod;" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"dean": {"Faculty Name": "Jack Ryan", "Email ID": "jack@cia.gov.us",...}, "hod: {"Jimmy Fallon": "Jack Ryan", "Email ID": "jimmy@tonight.us",...}"}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("unavailable")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS dean");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS dean (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                myDatabase.execSQL("DROP TABLE IF EXISTS hod");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS hod (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadAttendance();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);
                                JSONObject dean = new JSONObject(myObj.getString("dean"));
                                JSONObject hod = new JSONObject(myObj.getString("hod"));

                                myDatabase.execSQL("DROP TABLE IF EXISTS dean");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS dean (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                myDatabase.execSQL("DROP TABLE IF EXISTS hod");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS hod (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                Iterator<?> keys = dean.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    String value = dean.getString(key);

                                    key = key.substring(2);

                                    myDatabase.execSQL("INSERT INTO dean (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                keys = hod.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    String value = hod.getString(key);

                                    key = key.substring(2);

                                    myDatabase.execSQL("INSERT INTO hod (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadAttendance();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store the attendance in the SQLite database.
     */
    public void downloadAttendance() {
        downloading.setText(context.getString(R.string.downloading_attendance));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var obj = {};" +
                "$.ajax({" +
                "   type : 'POST'," +
                "   url : 'processViewStudentAttendance'," +
                "   data : data," +
                "   async: false," +
                "   success : function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       var division = doc.getElementById('getStudentDetails');" +
                "       if(division.getElementsByTagName('td').length == 1) {" +
                "           obj = 'unavailable';" +
                "       } else {" +
                "           var heads = division.getElementsByTagName('th');" +
                "           var courseIndex, typeIndex, attendedIndex, totalIndex, percentIndex, flag = 0;" +
                "           var columns = heads.length;" +
                "           for(var i = 0; i < columns; ++i) {" +
                "               var heading = heads[i].innerText.toLowerCase();" +
                "               if(heading.includes('course') &&  heading.includes('code')) {" +
                "                   courseIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if(heading.includes('course') && heading.includes('type')) {" +
                "                   typeIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if(heading.includes('attended')) {" +
                "                   attendedIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if(heading.includes('total')) {" +
                "                   totalIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if(heading.includes('percentage')) {" +
                "                   percentIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if(flag >= 5) {" +
                "                   break;" +
                "               }" +
                "           }" +
                "           var cells = division.getElementsByTagName('td');" +
                "           for(var i = 0; courseIndex < cells.length && typeIndex < cells.length  && attendedIndex < cells.length && totalIndex < cells.length && percentIndex < cells.length; ++i) {" +
                "               var temp = {};" +
                "               temp['course'] = cells[courseIndex].innerText.trim();" +
                "               temp['type'] = cells[typeIndex].innerText.trim();" +
                "               temp['attended'] = cells[attendedIndex].innerText.trim();" +
                "               temp['total'] = cells[totalIndex].innerText.trim();" +
                "               temp['percent'] = cells[percentIndex].innerText.trim();" +
                "               obj[i] = temp;" +
                "               courseIndex += columns;" +
                "               attendedIndex += columns;" +
                "               totalIndex += columns;" +
                "               typeIndex += columns;" +
                "               percentIndex += columns;" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"0": {"course": "MAT1001", "type": "Embedded Theory",...},...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("unavailable")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS attendance");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS attendance (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, attended VARCHAR, total VARCHAR, percent VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadExams();
                                    }
                                });
                            } catch (Exception e) {
                                error();
                            }

                            sharedPreferences.edit().remove("failedAttendance").apply();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS attendance");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS attendance (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, attended VARCHAR, total VARCHAR, percent VARCHAR)");

                                sharedPreferences.edit().remove("failedAttendance").apply();

                                for (int i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String course = tempObj.getString("course");
                                    String type = tempObj.getString("type");
                                    String attended = tempObj.getString("attended");
                                    String total = tempObj.getString("total");
                                    String percent = tempObj.getString("percent");

                                    myDatabase.execSQL("INSERT INTO attendance (course, type, attended, total, percent) VALUES('" + course + "', '" + type + "', '" + attended + "', '" + total + "', '" + percent + "')");

                                    if (Integer.parseInt(percent) <= 75) {
                                        sharedPreferences.edit().putBoolean("failedAttendance", true).apply();
                                    }
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadExams();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store the exam schedule in the SQLite database.
     */
    public void downloadExams() {
        downloading.setText(context.getString(R.string.downloading_exams));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'examinations/doSearchExamScheduleForStudent'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('not') && response.toLowerCase().includes('found')) {" +
                "           obj = 'nothing';" +
                "       } else {" +
                "           var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "           var courseIndex, titleIndex, slotIndex, dateIndex, reportingIndex, timingIndex, venueIndex, locationIndex, seatIndex, flag = 0;" +
                "           var columns = doc.getElementsByTagName('tr')[0].getElementsByTagName('td');" +
                "           for (var i = 0; i < columns.length; ++i) {" +
                "               var heading = columns[i].innerText.toLowerCase();" +
                "               if (heading.includes('code')) {" +
                "                   courseIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('title')) {" +
                "                   titleIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('slot')) {" +
                "                   slotIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('date')) {" +
                "                   dateIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('reporting')) {" +
                "                   reportingIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('exam') && heading.includes('time')) {" +
                "                   timingIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('venu')) {" +
                "                   venueIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('location')) {" +
                "                   locationIndex = i;" +
                "                   ++flag;" +
                "               } else if (heading.includes('seat') && heading.includes('no.')) {" +
                "                   seatIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if (flag >= 9) {" +
                "                   break;" +
                "               }" +
                "           }" +
                "           var exam = '', cells = doc.getElementsByTagName('td'), remainder = 0, record = -1;" +
                "           for (var i = columns.length; i < cells.length; ++i) {" +
                "               if (flag >= 9) {" +
                "                   flag = 0;" +
                "                   ++record;" +
                "               }" +
                "               if (cells[i].colSpan > 1) {" +
                "                   exam = cells[i].innerText.trim();" +
                "                   ++remainder;" +
                "                   record = 0;" +
                "                   continue;" +
                "               }" +
                "               if (typeof obj[exam] == 'undefined') {" +
                "                   obj[exam] = {};" +
                "               }" +
                "               var index = (i - remainder) % columns.length;" +
                "               if (index == courseIndex) {" +
                "                   obj[exam]['course' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               } else if (index == titleIndex) {" +
                "                   obj[exam]['title' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               } else if (index == slotIndex) {" +
                "                   obj[exam]['slot' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               } else if (index == dateIndex) {" +
                "                   obj[exam]['date' + record] = cells[i].innerText.trim().toUpperCase();" +
                "                   ++flag;" +
                "               } else if (index == reportingIndex) {" +
                "                   obj[exam]['reporting' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               } else if (index == timingIndex) {" +
                "                   var timings = cells[i].innerText.split('-');" +
                "                   if (timings.length == 1) {" +
                "                       obj[exam]['start' + record] = '';" +
                "                       obj[exam]['end' + record] = '';" +
                "                   } else {" +
                "                       obj[exam]['start' + record] = timings[0].trim();" +
                "                       obj[exam]['end' + record] = timings[1].trim();" +
                "                   }" +
                "                   ++flag;" +
                "               } else if (index == venueIndex) {" +
                "                   obj[exam]['venue' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               } else if (index == locationIndex) {" +
                "                   obj[exam]['location' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               } else if (index == seatIndex) {" +
                "                   obj[exam]['seat' + record] = cells[i].innerText.trim();" +
                "                   ++flag;" +
                "               }" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"Mid Term": {"course": "MAT1001", "date": "04-Jan-1976",...},...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("nothing")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS exams");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INTEGER PRIMARY KEY, exam VARCHAR, course VARCHAR, title VARCHAR, slot VARCHAR, date VARCHAR, reporting VARCHAR, start_time VARCHAR, end_time VARCHAR, venue VARCHAR, location VARCHAR, seat VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadMarks();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().remove("newExams").apply();
                            sharedPreferences.edit().remove("examsCount").apply();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS exams");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INTEGER PRIMARY KEY, exam VARCHAR, course VARCHAR, title VARCHAR, slot VARCHAR, date VARCHAR, reporting VARCHAR, start_time VARCHAR, end_time VARCHAR, venue VARCHAR, location VARCHAR, seat VARCHAR)");

                                SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

                                Iterator<?> keys = myObj.keys();

                                while (keys.hasNext()) {
                                    String exam = (String) keys.next();
                                    JSONObject schedule = new JSONObject(myObj.getString(exam));

                                    for (int i = 0; i < schedule.length() / 9; ++i) {
                                        String course = schedule.getString("course" + i);
                                        String title = schedule.getString("title" + i);
                                        String slot = schedule.getString("slot" + i);
                                        String date = schedule.getString("date" + i);
                                        String reporting = schedule.getString("reporting" + i);
                                        String startTime = schedule.getString("start" + i);
                                        String endTime = schedule.getString("end" + i);
                                        String venue = schedule.getString("venue" + i);
                                        String location = schedule.getString("location" + i);
                                        String seat = schedule.getString("seat" + i);

                                        /*
                                            Converting to 24 hour format if necessary
                                         */
                                        try {
                                            Date reportingTime = hour12.parse(reporting);
                                            Date startTimeDate = hour12.parse(startTime);
                                            Date endTimeDate = hour12.parse(endTime);

                                            if (reportingTime != null) {
                                                reporting = hour24.format(reportingTime);
                                            }

                                            if (startTimeDate != null) {
                                                startTime = hour24.format(startTimeDate);
                                            }

                                            if (endTimeDate != null) {
                                                endTime = hour24.format(endTimeDate);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        myDatabase.execSQL("INSERT INTO exams (exam, course, title, slot, date, reporting, start_time, end_time, venue, location, seat) VALUES ('" + exam.toUpperCase() + "', '" + course + "', '" + title + "', '" + slot + "', '" + date + "', '" + reporting + "', '" + startTime + "', '" + endTime + "', '" + venue + "', '" + location + "', '" + seat + "')");
                                    }
                                }

                                int objLength = myObj.length();
                                if (objLength != sharedPreferences.getInt("examsCount", 0)) {
                                    sharedPreferences.edit().putBoolean("newExams", true).apply();
                                    sharedPreferences.edit().putInt("examsCount", objLength).apply();
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadMarks();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to download marks
     */
    public void downloadMarks() {
        downloading.setText(R.string.downloading_marks);
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'semesterSubId=' + '" + semesterID + "' + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'examinations/doStudentMarkView'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('no') && response.toLowerCase().includes('found')) {" +
                "           obj = 'nothing';" +
                "       } else {" +
                "           var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "           var rows = doc.getElementById('fixedTableContainer').getElementsByTagName('tr');" +
                "           var heads = rows[0].getElementsByTagName('td');" +
                "           var columns = heads.length;" +
                "           var courseIndex, typeIndex, titleIndex, maxIndex, percentIndex, statusIndex, scoredIndex, weightageIndex, averageIndex, postedIndex;" +
                "           var course = '', type = '', flag = 0, k = 0;" +
                "           for (var i = 0; i < columns; ++i) {" +
                "               var heading = heads[i].innerText.toLowerCase();" +
                "               if (heading.includes('code')) {" +
                "                   courseIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if (heading.includes('type')) {" +
                "                   typeIndex = i;" +
                "                   ++flag;" +
                "               }" +
                "               if (flag >= 2) {" +
                "                   break;" +
                "               }" +
                "           }" +
                "           flag = 0;" +
                "           for (var i = 1; i < rows.length; ++i) {" +
                "               if (rows[i].getElementsByTagName('table').length) {" +
                "                   var records = rows[i].getElementsByTagName('tr').length - 1;" +
                "                   var heads = rows[++i].getElementsByTagName('td');" +
                "                   if (!flag) {" +
                "                       for (var j = 0; j < heads.length; ++j) {" +
                "                           var heading = heads[j].innerText.toLowerCase();" +
                "                           if (heading.includes('title')) {" +
                "                               titleIndex = j;" +
                "                           }" +
                "                           if (heading.includes('max')) {" +
                "                               maxIndex = j;" +
                "                           }" +
                "                           if (heading.includes('%')) {" +
                "                               percentIndex = j;" +
                "                           }" +
                "                           if (heading.includes('status')) {" +
                "                               statusIndex = j;" +
                "                           }" +
                "                           if (heading.includes('scored')) {" +
                "                               scoredIndex = j;" +
                "                           }" +
                "                           if (heading.includes('weightage') && heading.includes('mark')) {" +
                "                               weightageIndex = j;" +
                "                           }" +
                "                           if (heading.includes('average')) {" +
                "                               averageIndex = j;" +
                "                           }" +
                "                           if (heading.includes('posted')) {" +
                "                               postedIndex = j;" +
                "                           }" +
                "                       }" +
                "                       ++flag;" +
                "                   }" +
                "                   for (var j = 0; j < records; ++j) {" +
                "                       var values = rows[++i].getElementsByTagName('td');" +
                "                       var temp = {};" +
                "                       temp['title'] = values[titleIndex].innerText.trim();" +
                "                       temp['max'] = values[maxIndex].innerText.trim();" +
                "                       temp['percent'] = values[percentIndex].innerText.trim();" +
                "                       temp['status'] = values[statusIndex].innerText.trim();" +
                "                       temp['scored'] = values[scoredIndex].innerText.trim();" +
                "                       temp['weightage'] = values[weightageIndex].innerText.trim();" +
                "                       temp['average'] = values[averageIndex].innerText.trim();" +
                "                       temp['posted'] = values[postedIndex].innerText.trim();" +
                "                       temp['course'] = course;" +
                "                       temp['type'] = type;" +
                "                       obj[k++] = temp;" +
                "                   }" +
                "               } else {" +
                "                   course = rows[i].getElementsByTagName('td')[courseIndex].innerText.trim();" +
                "                   type = rows[i].getElementsByTagName('td')[typeIndex].innerText.trim();" +
                "               }" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"0": {"course": "MAT1001", "score": "48",...},...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("nothing")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS marks");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadGrades();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().remove("newMarks").apply();
                            sharedPreferences.edit().remove("marksCount").apply();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");
                                myDatabase.execSQL("DROP TABLE IF EXISTS marks_new");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks_new (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");

                                for (int i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String course = tempObj.getString("course");
                                    String type = tempObj.getString("type");
                                    String title = tempObj.getString("title").toUpperCase();
                                    String score = tempObj.getString("scored") + " / " + tempObj.getString("max");
                                    String status = tempObj.getString("status");
                                    String weightage = tempObj.getString("weightage") + " / " + tempObj.getString("percent");
                                    String average = tempObj.getString("average");
                                    String posted = tempObj.getString("posted");

                                    myDatabase.execSQL("INSERT INTO marks_new (course, type, title, score, status, weightage, average, posted) VALUES('" + course + "', '" + type + "', '" + title + "', '" + score + "', '" + status + "', '" + weightage + "', '" + average + "', '" + posted + "')");
                                }

                                Cursor delete = myDatabase.rawQuery("SELECT id FROM marks WHERE (course, title, type) NOT IN (SELECT course, title, type FROM marks_new)", null);

                                int deleteIndex = delete.getColumnIndex("id");
                                delete.moveToFirst();

                                String newMarksString = sharedPreferences.getString("newMarks", "{}");
                                JSONObject newMarks = new JSONObject(newMarksString);

                                if (!newMarksString.equals("{}")) {
                                    for (int i = 0; i < delete.getCount(); ++i, delete.moveToNext()) {
                                        String id = delete.getString(deleteIndex);

                                        if (newMarks.has(id)) {
                                            newMarks.remove(id);
                                        }
                                    }
                                }

                                delete.close();

                                Iterator<?> keys = newMarks.keys();

                                JSONObject tempObj = new JSONObject(newMarks.toString());
                                while (keys.hasNext()) {
                                    String oldID = (String) keys.next();
                                    Cursor update = myDatabase.rawQuery("SELECT id FROM marks_new WHERE (course, title, type) IN (SELECT course, title, type FROM marks WHERE id = " + oldID + ")", null);
                                    update.moveToFirst();
                                    String newID = update.getString(update.getColumnIndex("id"));

                                    tempObj.remove(oldID);
                                    tempObj.put(newID, true);

                                    update.close();
                                }
                                newMarks = new JSONObject(tempObj.toString());

                                Cursor add = myDatabase.rawQuery("SELECT id FROM marks_new WHERE (course, title, type) NOT IN (SELECT course, title, type FROM marks)", null);

                                int addIndex = add.getColumnIndex("id");
                                add.moveToFirst();

                                for (int i = 0; i < add.getCount(); ++i, add.moveToNext()) {
                                    String id = add.getString(addIndex);
                                    newMarks.put(id, true);
                                }

                                add.close();

                                myDatabase.execSQL("DROP TABLE IF EXISTS marks");
                                myDatabase.execSQL("ALTER TABLE marks_new RENAME TO marks");

                                sharedPreferences.edit().putString("newMarks", newMarks.toString()).apply();

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadGrades();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to download grades
     */
    public void downloadGrades() {
        downloadGradeHistory();
    }

    /*
        Function to download grade history
     */
    public void downloadGradeHistory() {
        downloading.setText(context.getString(R.string.downloading_grade_history));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'examinations/examGradeView/StudentGradeHistory'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       var tables = doc.getElementsByTagName('table');" +
                "       for (var i = 0; i < tables.length; ++i) {" +
                "           var category = tables[i].getElementsByTagName('td')[0].innerText.trim().toLowerCase();" +
                "           if (category.includes('reg') && !category.includes('credits')) {" +
                "               continue;" +
                "           } else if (category.includes('effective')) {" +
                "               category = 'effective';" +
                "               var courseIndex, titleIndex, creditsIndex, gradeIndex, flag = 0;" +
                "               var columns = tables[i].getElementsByTagName('tr')[1].getElementsByTagName('td');" +
                "               for (var j = 0; j < columns.length; ++j) {" +
                "                   var heading = columns[j].innerText.trim().toLowerCase();" +
                "                   if (heading.includes('course') && heading.includes('codes')) {" +
                "                       courseIndex = j + columns.length + 1;" +
                "                       ++flag;" +
                "                   } else if (heading.includes('title')) {" +
                "                       titleIndex = j + columns.length + 1;" +
                "                       ++flag;" +
                "                   } else if (heading.includes('credits')) {" +
                "                       creditsIndex = j + columns.length + 1;" +
                "                       ++flag;" +
                "                   } else if (heading.includes('grades')) {" +
                "                       gradeIndex = j + columns.length + 1;" +
                "                       ++flag;" +
                "                   }" +
                "                   if (flag >= 4) {" +
                "                       break;" +
                "                   }" +
                "               }" +
                "               var temp = {};" +
                "               var cells = tables[i].getElementsByTagName('td');" +
                "               for (var j = 0; courseIndex < cells.length && titleIndex < cells.length && creditsIndex < cells.length && gradeIndex < cells.length; ++j) {" +
                "                   temp['course' + j] = cells[courseIndex].innerText.trim();" +
                "                   temp['title' + j] = cells[titleIndex].innerText.trim();" +
                "                   temp['credits' + j] = cells[creditsIndex].innerText.trim();" +
                "                   temp['grade' + j] = cells[gradeIndex].innerText.trim();" +
                "                   courseIndex += columns.length;" +
                "                   titleIndex += columns.length;" +
                "                   creditsIndex += columns.length;" +
                "                   gradeIndex += columns.length;" +
                "               }" +
                "               obj[category] = temp;" +
                "           } else if (category.includes('curriculum')) {" +
                "               category = 'curriculum';" +
                "               var typeIndex, requiredIndex, earnedIndex;" +
                "               var columns = tables[i].getElementsByTagName('tr')[1].getElementsByTagName('td');" +
                "               for (var j = 0; j < columns.length; ++j) {" +
                "                   var heading = columns[j].innerText.trim().toLowerCase();" +
                "                   if (heading.includes('type')) {" +
                "                       typeIndex = j + columns.length + 1;" +
                "                   } else if (heading.includes('required')) {" +
                "                       requiredIndex = j + columns.length + 1;" +
                "                   } else if (heading.includes('earned')) {" +
                "                       earnedIndex = j + columns.length + 1;" +
                "                   }" +
                "               }" +
                "               var temp = {};" +
                "               var cells = tables[i].getElementsByTagName('td');" +
                "               for (var j = 0; typeIndex < cells.length && requiredIndex < cells.length && earnedIndex < cells.length; ++j) {" +
                "                   temp['type' + j] = cells[typeIndex].innerText.trim();" +
                "                   temp['required' + j] = cells[requiredIndex].innerText.trim();" +
                "                   temp['earned' + j] = cells[earnedIndex].innerText.trim();" +
                "                   typeIndex += columns.length;" +
                "                   requiredIndex += columns.length;" +
                "                   earnedIndex += columns.length;" +
                "               }" +
                "               obj[category] = temp;" +
                "           } else if (category.includes('basket')) {" +
                "               category = 'basket';" +
                "               var titleIndex, requiredIndex, earnedIndex;" +
                "               var columns = tables[i].getElementsByTagName('tr')[1].getElementsByTagName('td');" +
                "               for (var j = 0; j < columns.length; ++j) {" +
                "                   var heading = columns[j].innerText.trim().toLowerCase();" +
                "                   if (heading.includes('title')) {" +
                "                       titleIndex = j + columns.length + 1;" +
                "                   } else if (heading.includes('required')) {" +
                "                       requiredIndex = j + columns.length + 1;" +
                "                   } else if (heading.includes('earned')) {" +
                "                       earnedIndex = j + columns.length + 1;" +
                "                   }" +
                "               }" +
                "               var temp = {};" +
                "               var cells = tables[i].getElementsByTagName('td');" +
                "               for (var j = 0; titleIndex < cells.length && requiredIndex < cells.length && earnedIndex < cells.length; ++j) {" +
                "                   temp['title' + j] = cells[titleIndex].innerText.trim();" +
                "                   temp['required' + j] = cells[requiredIndex].innerText.trim();" +
                "                   temp['earned' + j] = cells[earnedIndex].innerText.trim();" +
                "                   titleIndex += columns.length;" +
                "                   requiredIndex += columns.length;" +
                "                   earnedIndex += columns.length;" +
                "               }" +
                "               obj[category] = temp;" +
                "           } else {" +
                "               category = 'summary';" +
                "               var columns = tables[i].getElementsByTagName('tr')[0].getElementsByTagName('td');" +
                "               var cells = tables[i].getElementsByTagName('td');" +
                "               var temp = {};" +
                "               for (var j = 0; j < columns.length; ++j) {" +
                "                   var heading = columns[j].innerText.trim();" +
                "                   var prefix = j;" +
                "                   if (j < 10) {" +
                "                       prefix = '0' + j;" +
                "                   }" +
                "                   temp[prefix + heading] = cells[j + columns.length].innerText.trim();" +
                "               }" +
                "               obj[category] = temp;" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS grades_effective");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_effective (id INTEGER PRIMARY KEY, course VARCHAR, title VARCHAR, credits VARCHAR, grade VARCHAR)");

                                JSONObject effective = new JSONObject(myObj.getString("effective"));

                                for (int i = 0; i < effective.length() / 4; ++i) {
                                    String course = effective.getString("course" + i);
                                    String title = effective.getString("title" + i);
                                    String credits = effective.getString("credits" + i);
                                    String grade = effective.getString("grade" + i);

                                    myDatabase.execSQL("INSERT INTO grades_effective (course, title, credits, grade) VALUES('" + course + "', '" + title + "', '" + credits + "', '" + grade + "')");
                                }

                                myDatabase.execSQL("DROP TABLE IF EXISTS grades_curriculum");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_curriculum (id INTEGER PRIMARY KEY, type VARCHAR, credits VARCHAR)");

                                JSONObject curriculum = new JSONObject(myObj.getString("curriculum"));

                                for (int i = 0; i < curriculum.length() / 3; ++i) {
                                    String type = curriculum.getString("type" + i);
                                    String required = curriculum.getString("required" + i);
                                    String earned = curriculum.getString("earned" + i);

                                    if (required.equals("-")) {
                                        required = "0";
                                    }

                                    if (earned.equals("")) {
                                        earned = "0";
                                    }

                                    String credits = earned + " / " + required;

                                    myDatabase.execSQL("INSERT INTO grades_curriculum (type, credits) VALUES('" + type + "', '" + credits + "')");
                                }

                                myDatabase.execSQL("DROP TABLE IF EXISTS grades_basket");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_basket (id INTEGER PRIMARY KEY, title VARCHAR, credits VARCHAR)");

                                JSONObject basket = new JSONObject(myObj.getString("basket"));

                                for (int i = 0; i < basket.length() / 3; ++i) {
                                    String title = basket.getString("title" + i);
                                    String credits = basket.getString("earned" + i) + " / " + basket.getString("required" + i);

                                    myDatabase.execSQL("INSERT INTO grades_basket (title, credits) VALUES('" + title + "', '" + credits + "')");
                                }

                                myDatabase.execSQL("DROP TABLE IF EXISTS grades_summary");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades_summary (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                JSONObject summary = new JSONObject(myObj.getString("summary"));

                                Iterator<?> keys = summary.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    String value = summary.getString(key);

                                    key = key.substring(2);

                                    if (key.toLowerCase().contains("cgpa")) {
                                        key = "Overall CGPA";
                                    } else if (key.toLowerCase().contains("grades")) {
                                        key = "Number of " + key;
                                    }

                                    myDatabase.execSQL("INSERT INTO grades_summary (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadMessages();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store the class messages in the SQLite database.
     */
    public void downloadMessages() {
        downloading.setText(context.getString(R.string.downloading_messages));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'academics/common/StudentClassMessage'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('no messages')) {" +
                "           successFlag = true;" +
                "       } else {" +
                "           successFlag = 'new';" +
                "       }" +
                "   }" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                /*
                    obj is in the form of a JSON string that is yet to be created
                 */
                String temp = value.substring(1, value.length() - 1);
                if (value.equals("true")) {
                    /*
                        Dropping and recreating an empty table
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS messages");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY, faculty VARCHAR, time VARCHAR, message VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadProctorMessages();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                                isOpened = false;
                                reloadPage();
                            }

                            sharedPreferences.edit().remove("newMessages").apply();
                        }
                    }).start();
                } else if (temp.equals("new")) {
                    /*
                        Dropping, recreating and adding announcements
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS messages");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY, faculty VARCHAR, time VARCHAR, message VARCHAR)");

                                myDatabase.execSQL("INSERT INTO messages (faculty, time, message) VALUES('null', 'null', 'null')"); //To be changed with the actual announcements

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadProctorMessages();
                                    }
                                });

                                sharedPreferences.edit().putBoolean("newMessages", true).apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                } else {
                    error();
                }
            }
        });
    }

    /*
        Function to store the proctor messages in the SQLite database.
     */
    public void downloadProctorMessages() {
        downloading.setText(context.getString(R.string.downloading_messages));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'proctor/viewMessagesSendByProctor'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('no messages')) {" +
                "           successFlag = true;" +
                "       } else {" +
                "           successFlag = 'new';" +
                "       }" +
                "   }" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                /*
                    obj is in the form of a JSON string that is yet to be created
                 */
                String temp = value.substring(1, value.length() - 1);
                if (value.equals("true")) {
                    /*
                        Dropping and recreating an empty table
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS proctor_messages");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor_messages (id INTEGER PRIMARY KEY, time VARCHAR, message VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadSpotlight();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                                isOpened = false;
                                reloadPage();
                            }

                            sharedPreferences.edit().remove("newProctorMessages").apply();
                        }
                    }).start();
                } else if (temp.equals("new")) {
                    /*
                        Dropping, recreating and adding announcements
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS proctor_messages");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor_messages (id INTEGER PRIMARY KEY, time VARCHAR, message VARCHAR)");

                                myDatabase.execSQL("INSERT INTO proctor_messages (time, message) VALUES('null', 'null')"); //To be changed with the actual announcements

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadSpotlight();
                                    }
                                });

                                sharedPreferences.edit().putBoolean("newProctorMessages", true).apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                } else {
                    error();
                }
            }
        });
    }

    /*
        Function to store spotlight in the SQLite database.
     */
    public void downloadSpotlight() {
        downloading.setText(context.getString(R.string.downloading_spotlight));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'spotlight/spotlightViewOld'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       if(!doc.getElementsByClassName('box-info')) {" +
                "           obj = 'nothing';" +
                "       } else {" +
                "           var modals = doc.getElementsByClassName('modal-content');" +
                "           for(var i = 0; i < modals.length; ++i) {" +
                "               var category = modals[i].getElementsByTagName('h5')[0].innerText;" +
                "               if(category.toLowerCase().includes('finance')) {" +
                "                   category = 'Others';" +
                "               } else {" +
                "                   category = category.trim().replaceAll('\\t','').replaceAll('\\n','');" +
                "                   category = category.substring(0, category.length - 9).trim();" +
                "               }" +
                "               var announcements = modals[i].getElementsByTagName('li');" +
                "               if (announcements.length == 0) {" +
                "                   continue;" +
                "               }" +
                "               var temp = {};" +
                "               for(var j = 0; j < announcements.length; ++j) {" +
                "                   temp[j + 'announcement'] = announcements[j].innerText.trim().replaceAll('\\t','').replaceAll('\\n',' ');" +
                "                   if(!announcements[j].getElementsByTagName('a').length) {" +
                "                       temp[j + 'link'] = 'null';" +
                "                   } else {" +
                "                       temp[j + 'link'] = announcements[j].getElementsByTagName('a')[0].href;" +
                "                       if(temp[j + 'link'].includes('\\'')) {" +
                "                           temp[j + 'link'] = announcements[j].getElementsByTagName('a')[0].href.split('\\'')[1];" +
                "                       }" +
                "                   }" +
                "               }" +
                "               obj[category] = temp;" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"Academics": {"announcement": "In lieu of COVID-19, campus will remain shut for eternity.", "link": "null"},...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else if (temp.equals("nothing")) {
                    /*
                        Dropping and recreating an empty table
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS spotlight");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INTEGER PRIMARY KEY, category VARCHAR, announcement VARCHAR, link VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadReceipts();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().remove("newSpotlight").apply();
                        }
                    }).start();
                } else {
                    /*
                        Dropping, recreating and adding announcements
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INTEGER PRIMARY KEY, category VARCHAR, announcement VARCHAR, link VARCHAR)");
                                myDatabase.execSQL("DROP TABLE IF EXISTS spotlight_new");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight_new (id INTEGER PRIMARY KEY, category VARCHAR, announcement VARCHAR, link VARCHAR)");

                                Iterator<?> keys = myObj.keys();

                                while (keys.hasNext()) {
                                    String category = (String) keys.next();
                                    JSONObject tempObj = new JSONObject(myObj.getString(category));

                                    for (int i = 0; i < tempObj.length() / 2; ++i) {
                                        String announcement = tempObj.getString(i + "announcement");
                                        String link = tempObj.getString(i + "link");

                                        myDatabase.execSQL("INSERT INTO spotlight_new (category, announcement, link) VALUES('" + category + "', '" + announcement + "', '" + link + "')");
                                    }
                                }

                                Cursor newSpotlight = myDatabase.rawQuery("SELECT id FROM spotlight_new WHERE announcement NOT IN (SELECT announcement FROM spotlight)", null);

                                if (newSpotlight.getCount() > 0) {
                                    sharedPreferences.edit().putBoolean("newSpotlight", true).apply();
                                }

                                newSpotlight.close();

                                myDatabase.execSQL("DROP TABLE spotlight");
                                myDatabase.execSQL("ALTER TABLE spotlight_new RENAME TO spotlight");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProgress();
                                        downloadReceipts();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Function to store payment receipts
    */
    public void downloadReceipts() {
        downloading.setText(context.getString(R.string.downloading_receipts));
        if (downloadingLayout.getVisibility() == View.GONE) {
            hideLayouts();
            expand(downloadingLayout);
        }

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = {};" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'p2p/getReceiptsApplno'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       var receiptIndex, dateIndex, amountIndex, flag = 0;" +
                "       var columns = doc.getElementsByTagName('tr')[0].getElementsByTagName('td').length;" +
                "       var cells = doc.getElementsByTagName('td');" +
                "       for(var i = 0; i < columns; ++i) {" +
                "           var heading = cells[i].innerText.toLowerCase();" +
                "           if(heading.includes('receipt')) {" +
                "               receiptIndex = i + columns;" +
                "               ++flag;" +
                "           }" +
                "           if(heading.includes('date')) {" +
                "               dateIndex = i + columns;" +
                "               ++flag;" +
                "           }" +
                "           if(heading.includes('amount')) {" +
                "               amountIndex = i + columns;" +
                "               ++flag;" +
                "           }" +
                "           if(flag >= 3) {" +
                "               break;" +
                "           }" +
                "       }" +
                "       for(var i = 0; receiptIndex < cells.length && dateIndex < cells.length && amountIndex < cells.length; ++i) {" +
                "           var temp = {};" +
                "           temp['receipt'] = cells[receiptIndex].innerText.trim();" +
                "           temp['date'] = cells[dateIndex].innerText.trim();" +
                "           temp['amount'] = cells[amountIndex].innerText.trim();" +
                "           obj[i] = temp;" +
                "           receiptIndex += columns;" +
                "           dateIndex += columns;" +
                "           amountIndex += columns;" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                /*
                    obj is in the form of a JSON string like {"0": {"amount": "1000000", "date": "04-JAN-1976", "receipt": "17085"},...}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null") || temp.equals("")) {
                    error();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS receipts");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS receipts (id INTEGER PRIMARY KEY, receipt VARCHAR, date VARCHAR, amount VARCHAR)");

                                int i;
                                for (i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String receipt = tempObj.getString("receipt");
                                    String date = tempObj.getString("date").toUpperCase();
                                    String amount = tempObj.getString("amount");

                                    myDatabase.execSQL("INSERT INTO receipts (receipt, date, amount) VALUES('" + receipt + "', '" + date + "', '" + amount + "')");
                                }

                                if (i != sharedPreferences.getInt("receiptsCount", 0)) {
                                    sharedPreferences.edit().putBoolean("newReceipts", true).apply();
                                    sharedPreferences.edit().putInt("receiptsCount", i).apply();
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkDues();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    /*
        Check for payment dues
     */
    public void checkDues() {
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var duePayments;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'p2p/Payments'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "       if (doc.getElementsByTagName('font')[0]) {" +
                "           var text = doc.getElementsByTagName('font')[0].innerText.toLowerCase();" +
                "           if (text.includes('no') && text.includes('payment') && text.includes('dues')) {" +
                "               duePayments = false;" +
                "           } else {" +
                "               duePayments = true;" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return duePayments;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String duePayments) {
                if (duePayments.equals("true")) {
                    sharedPreferences.edit().putBoolean("duePayments", true).apply();
                } else {
                    sharedPreferences.edit().remove("duePayments").apply();
                }

                finishUp();
            }
        });
    }

    public void finishUp() {
        hideLayouts();
        loading.setVisibility(View.VISIBLE);
        sharedPreferences.edit().putBoolean("isSignedIn", true).apply();
        myDatabase.close();

        webView.clearCache(true);
        webView.clearHistory();
        CookieManager.getInstance().removeAllCookies(null);

        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat date = new SimpleDateFormat("MMM d", Locale.ENGLISH);
            SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            sharedPreferences.edit().putString("refreshedDate", date.format(c.getTime())).apply();
            sharedPreferences.edit().putString("refreshedTime", time.format(c.getTime())).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        downloadDialog.dismiss();
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    public void error() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                isOpened = false;
                reloadPage();
            }
        });
    }

    public int getLastDownload() {
        return lastDownload;
    }
}
