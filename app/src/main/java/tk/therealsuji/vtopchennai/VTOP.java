package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    ImageView captcha;
    EditText captchaView;
    LinearLayout captchaLayout, loadingLayout, semesterLayout;
    Spinner selectSemester;
    TextView loading;

    Boolean isOpened, isLoggedIn;
    SharedPreferences sharedPreferences;
    int counter;

    SQLiteDatabase myDatabase;

    @SuppressLint("SetJavaScriptEnabled")
    public VTOP(final Context context) {
        /*
            Uncomment line 62 and Comment line 63 for debugging
         */

        this.context = context;
//        webView = ((Activity) context).findViewById(R.id.vtopPortal);
        webView = new WebView(context);
        captcha = ((Activity) context).findViewById(R.id.captchaCode);
        captchaLayout = ((Activity) context).findViewById(R.id.captchaLayout);
        captchaView = ((Activity) context).findViewById(R.id.captcha);
        loadingLayout = ((Activity) context).findViewById(R.id.loadingLayout);
        semesterLayout = ((Activity) context).findViewById(R.id.semesterLayout);
        loading = ((Activity) context).findViewById(R.id.loading);
        selectSemester = ((Activity) context).findViewById(R.id.selectSemester);
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
                        ((Activity) context).finish();
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

        reloadPage();
    }

    /*
        Function to reload the page using javascript in case of an error
     */
    public void reloadPage() {
        loading.setText(context.getString(R.string.loading));
        if (loadingLayout.getVisibility() == View.INVISIBLE) {
            hideLayouts();
            loadingLayout.setVisibility(View.VISIBLE);
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
                    return;
                }

                try {
                    src = src.substring(1, src.length() - 1).split(" ")[1];
                    byte[] decodedString = Base64.decode(src, Base64.DEFAULT);
                    Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    captcha.setImageBitmap(decodedImage);

                    hideLayouts();
                    captchaView.setText("");
                    captchaLayout.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    error();
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
                    downloadProfile();
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
        Function to save the name of the user and his/her ID (register number) in SharedPreferences
        TBD: Saving the users profile picture
     */
    public void downloadProfile() {
        loading.setText(context.getString(R.string.downloading_profile));

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = false;" +
                "var name = '';" +
                "var id = '';" +
                "var j = 0;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'studentsRecord/StudentProfileAllView'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('personal information')) {" +
                "           var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "           var cells = doc.getElementsByTagName('td');" +
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
                "           obj = {'name': name, 'id': id};" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                /*
                    obj is in the form of a JSON string like {"name":"JOHN DOE","register":"20XYZ1987"}
                 */
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("false") || obj.equals("null")) {
                    error();
                } else if (temp.equals("")) {
                    Toast.makeText(context, "Sorry, we had some trouble downloading your profile.", Toast.LENGTH_LONG).show();
                    ((Activity) context).finish();
                } else {
                    try {
                        JSONObject myObj = new JSONObject(obj);
                        sharedPreferences.edit().putString("name", myObj.getString("name")).apply();
                        sharedPreferences.edit().putString("id", myObj.getString("id")).apply();
                        openTimetable();
                    } catch (Exception e) {
                        e.printStackTrace();
                        error();
                    }
                }
            }
        });
    }

    /*
        Function to get the list of semesters in order to download the timetable. The same value will be used to download the attendance
     */
    public void openTimetable() {
        loading.setText(context.getString(R.string.loading));

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
                "           $('#page-wrapper').html(response);" +
                "           var options = document.getElementById('semesterSubId').getElementsByTagName('option');" +
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
                    obj is in the form of a JSON string like {"0": "Option 1", "1": "Option 2", "2": "Option 3",...}
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
                        semesterLayout.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error();
                    }
                }
            }
        });
    }

    /*
        Function to select the timetable to download
     */
    public void selectTimetable() {
        hideLayouts();
        loadingLayout.setVisibility(View.VISIBLE);

        String semester = sharedPreferences.getString("semester", "null");

        webView.evaluateJavascript("(function() {" +
                "var semID = '';" +
                "var options = document.getElementById('semesterSubId').getElementsByTagName('option');" +
                "for(var i = 0; i < options.length; ++i) {" +
                "   if(options[i].innerText.toLowerCase().includes('" + semester + "')) {" +
                "       semID = options[i].value;" +
                "   }" +
                "}" +
                "var data = 'semesterSubId=' + semID + '&authorizedID=' + $('#authorizedIDX').val();" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type : 'POST'," +
                "   url : 'processViewTimeTable'," +
                "   data : data," +
                "   async: false," +
                "   success : function(response) {" +
                "       $('#main-section').html(response);" +
                "       successFlag = true;" +
                "   }" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    downloadTimetable();
                } else {
                    error();
                }
            }
        });
    }

    /*
        Function to save the timetable in an SQLite database, and the credit score in SharedPreferences
     */
    public void downloadTimetable() {
        loading.setText(context.getString(R.string.downloading_timetable));

        webView.evaluateJavascript("(function() {" +
                "var obj = {};" +
                "var spans = document.getElementById('getStudentDetails').getElementsByTagName('span');" +
                "var credits = '0';" +
                "if(spans[0].innerText.toLowerCase().includes('no record(s) found')) {" +
                "   return 'unreleased';" +
                "}" +
                "for(var i = spans.length-1; i > 0; --i) {" +
                "   if(spans[i].innerText.toLowerCase().includes('credits')) {" +
                "       credits = spans[i+1].innerText;" +
                "       break;" +
                "   }" +
                "}" +
                "obj['credits'] = credits;" +
                "var cells = document.getElementById('timeTableStyle').getElementsByTagName('td');" +
                "var category = '';" +
                "var timings = '';" +
                "var theory = {}, lab = {}, mon = {}, tue = {}, wed = {}, thu = {}, fri = {}, sat = {}, sun = {};" +
                "var i = 0;" +
                "for(var j = 0; j < cells.length; ++j) {" +
                "   if(cells[j].innerText.toLowerCase() == 'mon' || cells[j].innerText.toLowerCase() == 'tue' || cells[j].innerText.toLowerCase() == 'wed' || cells[j].innerText.toLowerCase() == 'thu' || cells[j].innerText.toLowerCase() == 'fri' || cells[j].innerText.toLowerCase() == 'sat' || cells[j].innerText.toLowerCase() == 'sun') {" +
                "       category = cells[j].innerText.toLowerCase();" +
                "       continue;" +
                "   }" +
                "   if(cells[j].innerText.toLowerCase() == 'theory' || cells[j].innerText.toLowerCase() == 'lab') {" +
                "       if(category == '' || category == 'theory' || category == 'lab') {" +
                "           category = cells[j].innerText.toLowerCase();" +
                "       } else {" +
                "           postfix = cells[j].innerText.toLowerCase();" +
                "       }" +
                "       i = 0;" +
                "       continue;" +
                "   }" +
                "   if(cells[j].innerText.toLowerCase() == 'start' || cells[j].innerText.toLowerCase() == 'end') {" +
                "       postfix = cells[j].innerText.toLowerCase();" +
                "       i = 0;" +
                "       continue;" +
                "   }" +
                "   subcat = i + postfix;" +
                "   if(category == 'theory') {" +
                "      theory[subcat] = cells[j].innerText.trim();" +
                "   } else if(category == 'lab') {" +
                "      lab[subcat] = cells[j].innerText.trim();" +
                "   } else if(category == 'mon') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          mon[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   } else if(category == 'tue') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          tue[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   } else if(category == 'wed') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          wed[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   } else if(category == 'thu') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          thu[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   } else if(category == 'fri') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          fri[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   } else if(category == 'sat') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          sat[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   } else if(category == 'sun') {" +
                "      if(cells[j].bgColor == '#CCFF33') {" +
                "          sun[subcat] = cells[j].innerText.trim();" +
                "      }" +
                "   }" +
                "   ++i;" +
                "}" +
                "obj.theory = theory;" +
                "obj.lab = lab;" +
                "obj.mon = mon;" +
                "obj.tue = tue;" +
                "obj.wed = wed;" +
                "obj.thu = thu;" +
                "obj.fri = fri;" +
                "obj.sat = sat;" +
                "obj.sun = sun;" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("unreleased") || temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            myDatabase.execSQL("DROP TABLE IF EXISTS timetable_lab");
                            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INTEGER PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                            myDatabase.execSQL("DROP TABLE IF EXISTS timetable_theory");
                            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INTEGER PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, sun VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR)");

                            myDatabase.execSQL("DROP TABLE IF EXISTS faculty");
                            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS faculty (id INTEGER PRIMARY KEY, course VARCHAR, faculty VARCHAR)");

                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                            Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                            for (int j = 0; j < sharedPreferences.getInt("alarmCount", 0); ++j) {
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                                alarmManager.cancel(pendingIntent);
                            }

                            sharedPreferences.edit().putBoolean("newTimetable", false).apply();
                            sharedPreferences.edit().putInt("timetableCount", 0).apply();
                            sharedPreferences.edit().putInt("alarmCount", 0).apply();

                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadProctor();
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

                                for (int i = 0; i < lab.length() / 2 && i < theory.length() / 2; ++i) {
                                    String start_time_lab = lab.getString(i + "start");
                                    if (start_time_lab.toLowerCase().equals("lunch")) {
                                        continue;
                                    }
                                    String end_time_lab = lab.getString(i + "end");
                                    String start_time_theory = theory.getString(i + "start");
                                    String end_time_theory = theory.getString(i + "end");

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

                                    myDatabase.execSQL("INSERT INTO timetable_lab (start_time, end_time, sun, mon, tue, wed, thu, fri, sat) VALUES ('" + start_time_lab + "', '" + end_time_lab + "', '" + labPeriods[0] + "', '" + labPeriods[1] + "', '" + labPeriods[2] + "', '" + labPeriods[3] + "', '" + labPeriods[4] + "', '" + labPeriods[5] + "', '" + labPeriods[6] + "')");
                                    myDatabase.execSQL("INSERT INTO timetable_theory (start_time, end_time, sun, mon, tue, wed, thu, fri, sat) VALUES ('" + start_time_theory + "', '" + end_time_theory + "', '" + theoryPeriods[0] + "', '" + theoryPeriods[1] + "', '" + theoryPeriods[2] + "', '" + theoryPeriods[3] + "', '" + theoryPeriods[4] + "', '" + theoryPeriods[5] + "', '" + theoryPeriods[6] + "')");
                                }

                                for (int i = alarmCount; i < sharedPreferences.getInt("alarmCount", 0); ++i) {
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, notificationIntent, 0);
                                    alarmManager.cancel(pendingIntent);
                                }

                                if (alarmCount != sharedPreferences.getInt("timetableCount", 0)) {
                                    sharedPreferences.edit().putInt("timetableCount", alarmCount).apply();
                                }

                                sharedPreferences.edit().putInt("alarmCount", alarmCount).apply();
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
        Function to download faculty info
     */
    public void downloadFaculty() {
        loading.setText(context.getString(R.string.downloading_faculty));

        webView.evaluateJavascript("(function() {" +
                "var division = document.getElementById('studentDetailsList'); " +  //Possible error: If timetable is available but faculty info isn't
                "var heads = division.getElementsByTagName('th');" +
                "var courseIndex, facultyIndex, flag = 0;" +
                "var columns = heads.length;" +
                "for(var i = 0; i < columns; ++i) {" +
                "   var heading = heads[i].innerText.toLowerCase();" +
                "   if(heading == 'course') {" +
                "       courseIndex = i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "       ++flag;" +
                "   }" +
                "   if(heading.includes('faculty') && heading.includes('details')) {" +
                "       facultyIndex = i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "       ++flag;" +
                "   }" +
                "   if(flag >= 2) {" +
                "       break;" +
                "   }" +
                "}" +
                "var obj = {};" +
                "var cells = division.getElementsByTagName('td');" +
                "for(var i = 0; courseIndex < cells.length && facultyIndex < cells.length; ++i) {" +
                "   var temp = {};" +
                "   temp['course'] = cells[courseIndex].innerText.trim();" +
                "   temp['faculty'] = cells[facultyIndex].innerText.trim();" +
                "   obj[i] = temp;" +
                "   courseIndex += columns;" +
                "   facultyIndex += columns;" +
                "}" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS faculty");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS faculty (id INTEGER PRIMARY KEY, course VARCHAR, faculty VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadProctor();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().putInt("facultyCount", 0).apply();
                            sharedPreferences.edit().putBoolean("newFaculty", false).apply();
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

                                int i;
                                for (i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String course = tempObj.getString("course");
                                    String faculty = tempObj.getString("faculty");

                                    myDatabase.execSQL("INSERT INTO faculty (course, faculty) VALUES('" + course + "', '" + faculty + "')");
                                }

                                if (i != sharedPreferences.getInt("facultyCount", 0)) {
                                    sharedPreferences.edit().putInt("facultyCount", i).apply();
                                    sharedPreferences.edit().putBoolean("newFaculty", true).apply();
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
        Function to download proctor info (Staff info)
     */
    public void downloadProctor() {
        loading.setText(context.getString(R.string.downloading_staff));

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
                "       obj[key] = value;" +
                "   }" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("unavailable") || temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS proctor");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor (id INTEGER PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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

                                    myDatabase.execSQL("INSERT INTO proctor (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
        Function to download HOD & Dean info (Staff info)
     */
    public void downloadDeanHOD() {
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
                "               dean[key] = value;" +
                "               } else {" +
                "                   if(cells[i].innerHTML.includes('img')) {" +
                "                   continue;" +
                "               }" +
                "               var key = cells[i].innerText.trim();" +
                "               var value = cells[++i].innerText.trim();" +
                "               hod[key] = value;" +
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
                "               hod[key] = value;" +
                "           } else {" +
                "               if(cells[i].innerHTML.includes('img')) {" +
                "                   continue;" +
                "           }" +
                "           var key = cells[i].innerText.trim();" +
                "           var value = cells[++i].innerText.trim();" +
                "           dean[key] = value;" +
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
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("unavailable") || temp.equals("")) {
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
                                        openAttendance();
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

                                    myDatabase.execSQL("INSERT INTO dean (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                keys = hod.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    String value = hod.getString(key);

                                    myDatabase.execSQL("INSERT INTO hod (column1, column2) VALUES('" + key + "', '" + value + "')");
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        openAttendance();
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
        Function to open the attendance page
     */
    public void openAttendance() {
        loading.setText(context.getString(R.string.loading));

        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var successFlag = false;" +
                "$.ajax({" +
                "   type: 'POST'," +
                "   url : 'academics/common/StudentAttendance'," +
                "   data : data," +
                "   async: false," +
                "   success: function(response) {" +
                "       if(response.toLowerCase().includes('attendance')) {" +
                "           $('#page-wrapper').html(response);" +
                "           successFlag = true;" +
                "       }" +
                "   }" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    downloadAttendance();
                } else {
                    error();
                }
            }
        });
    }

    /*
        Function to select the attendance to download
     */
    public void downloadAttendance() {
        loading.setText(context.getString(R.string.downloading_attendance));

        String semester = sharedPreferences.getString("semester", "null");

        webView.evaluateJavascript("(function() {" +
                "var semID = '';" +
                "var options = document.getElementById('semesterSubId').getElementsByTagName('option');" +
                "for(var i = 0; i < options.length; ++i) {" +
                "   if(options[i].innerText.toLowerCase().includes('" + semester + "')) {" +
                "       semID = options[i].value;" +
                "   }" +
                "}" +
                "var data = 'semesterSubId=' + semID + '&authorizedID=' + $('#authorizedIDX').val();" +
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
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("unavailable") || temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS attendance");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS attendance (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, attended VARCHAR, total VARCHAR, percent VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadExams();
                                    }
                                });
                            } catch (Exception e) {
                                error();
                            }

                            sharedPreferences.edit().putBoolean("failedAttendance", false).apply();
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

                                sharedPreferences.edit().putBoolean("failedAttendance", false).apply();

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
        loading.setText(context.getString(R.string.downloading_exams));

        String semester = sharedPreferences.getString("semester", "null");

        webView.evaluateJavascript("(function() {" +
                "var semID = '';" +
                "var options = document.getElementById('semesterSubId').getElementsByTagName('option');" +
                "for(var i = 0; i < options.length; ++i) {" +
                "   if(options[i].innerText.toLowerCase().includes('" + semester + "')) {" +
                "       semID = options[i].value;" +
                "   }" +
                "}" +
                "var data = 'semesterSubId=' + semID + '&authorizedID=' + $('#authorizedIDX').val();" +
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
                "           var heads = doc.getElementsByTagName('tr')[0].getElementsByTagName('td');" +
                "           var courseIndex, dateIndex, timeIndex, flag = 0;" +
                "           var columns = heads.length;" +
                "           for(var i = 0; i < columns; ++i) {" +
                "               var heading = heads[i].innerText.toLowerCase();" +
                "               if(heading.includes('course') && heading.includes('code')) {" +
                "                   courseIndex = columns + i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "                   ++flag;" +
                "               }" +
                "               if(heading.includes('date')) {" +
                "                   dateIndex = columns + i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "                   ++flag;" +
                "               }" +
                "               if(heading.includes('exam') && heading.includes('time')) {" +
                "                   timeIndex = columns + i + 1;" + // +1 is a correction due to an extra 'td' element at the top
                "                   ++flag;" +
                "               }" +
                "               if(flag >= 3) {" +
                "                   break;" +
                "               }" +
                "           }" +
                "           var cells = doc.getElementsByTagName('td');" +
                "           for(var i = 0; courseIndex < cells.length && dateIndex < cells.length && timeIndex < cells.length; ++i) {" +
                "               var temp = {};" +
                "               temp['course'] = cells[courseIndex].innerText.trim();" +
                "               temp['date'] = cells[dateIndex].innerText.trim();" +
                "               temp['time'] = cells[timeIndex].innerText.trim();" +
                "               obj[i] = temp;" +
                "               courseIndex += columns;" +
                "               dateIndex += columns;" +
                "               timeIndex += columns;" +
                "           }" +
                "       }" +
                "   }" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String obj) {
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("nothing") || temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS exams");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INTEGER PRIMARY KEY, course VARCHAR, date VARCHAR, time VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadMarks();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().putBoolean("newExams", false).apply();
                            sharedPreferences.edit().putInt("examsCount", 0).apply();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS exams");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS exams (id INTEGER PRIMARY KEY, course VARCHAR, date VARCHAR, start_time VARCHAR, end_time VARCHAR)");

                                int i;
                                for (i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String course = tempObj.getString("course");
                                    String date = tempObj.getString("date").toUpperCase();
                                    String[] time = tempObj.getString("time").split("-");
                                    String startTime = time[0].trim();
                                    String endTime = time[1].trim();

                                    myDatabase.execSQL("INSERT INTO exams (course, date, start_time, end_time) VALUES('" + course + "', '" + date + "', '" + startTime + "', '" + endTime + "')");
                                }

                                if (i != sharedPreferences.getInt("examsCount", 0)) {
                                    sharedPreferences.edit().putBoolean("newExams", true).apply();
                                    sharedPreferences.edit().putInt("examsCount", i).apply();
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
        loading.setText(R.string.downloading_marks);

        String semester = sharedPreferences.getString("semester", "null");

        webView.evaluateJavascript("(function() {" +
                "var semID = '';" +
                "var options = document.getElementById('semesterSubId').getElementsByTagName('option');" +
                "for(var i = 0; i < options.length; ++i) {" +
                "   if(options[i].innerText.toLowerCase().includes('" + semester + "')) {" +
                "       semID = options[i].value;" +
                "   }" +
                "}" +
                "var data = 'semesterSubId=' + semID + '&authorizedID=' + $('#authorizedIDX').val();" +
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
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("nothing") || temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS marks");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadReceipts();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().putBoolean("newMarks", false).apply();
                            sharedPreferences.edit().putInt("marksCount", 0).apply();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject myObj = new JSONObject(obj);

                                myDatabase.execSQL("DROP TABLE IF EXISTS marks");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS marks (id INTEGER PRIMARY KEY, course VARCHAR, type VARCHAR, title VARCHAR, score VARCHAR, status VARCHAR, weightage VARCHAR, average VARCHAR, posted VARCHAR)");

                                int i;
                                for (i = 0; i < myObj.length(); ++i) {
                                    JSONObject tempObj = new JSONObject(myObj.getString(Integer.toString(i)));
                                    String course = tempObj.getString("course");
                                    String type = tempObj.getString("type");
                                    String title = tempObj.getString("title").toUpperCase();
                                    String score = tempObj.getString("scored") + " / " + tempObj.getString("max");
                                    String status = tempObj.getString("status");
                                    String weightage = tempObj.getString("weightage") + " / " + tempObj.getString("percent");
                                    String average = tempObj.getString("average");
                                    String posted = tempObj.getString("posted");

                                    myDatabase.execSQL("INSERT INTO marks (course, type, title, score, status, weightage, average, posted) VALUES('" + course + "', '" + type + "', '" + title + "', '" + score + "', '" + status + "', '" + weightage + "', '" + average + "', '" + posted + "')");
                                }

                                if (i != sharedPreferences.getInt("marksCount", 0)) {
                                    sharedPreferences.edit().putBoolean("newMarks", true).apply();
                                    sharedPreferences.edit().putInt("marksCount", i).apply();
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
        loading.setText(context.getString(R.string.downloading_receipts));

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
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myDatabase.execSQL("DROP TABLE IF EXISTS receipts");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS receipts (id INTEGER PRIMARY KEY, receipt VARCHAR, date VARCHAR, amount VARCHAR)");

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadMessages();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().putBoolean("newReceipts", false).apply();
                            sharedPreferences.edit().putInt("receiptsCount", 0).apply();
                        }
                    }).start();
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
                                        downloadMessages();
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
        Function to store the class messages in the SQLite database.
     */
    public void downloadMessages() {
        loading.setText(context.getString(R.string.downloading_messages));

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
                                        downloadSpotlight();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                                isOpened = false;
                                reloadPage();
                            }

                            sharedPreferences.edit().putBoolean("newMessages", false).apply();
                            sharedPreferences.edit().putInt("messagesCount", 0).apply();
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
                                        downloadSpotlight();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                                isOpened = false;
                                reloadPage();
                            }

                            sharedPreferences.edit().putBoolean("newProctorMessages", false).apply();
                            sharedPreferences.edit().putInt("proctorMessagesCount", 0).apply();
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
        loading.setText(context.getString(R.string.downloading_spotlight));

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
                "                   temp[j + 'announcement'] = announcements[j].innerText.trim().replaceAll('\\t','').replaceAll('\\n','');" +
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
                String temp = obj.substring(1, obj.length() - 1);
                if (obj.equals("null")) {
                    error();
                } else if (temp.equals("nothing") || temp.equals("")) {
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
                                        finishUp();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                error();
                            }

                            sharedPreferences.edit().putBoolean("newSpotlight", false).apply();
                            sharedPreferences.edit().putInt("spotlightCount", 0).apply();
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

                                myDatabase.execSQL("DROP TABLE IF EXISTS spotlight");
                                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INTEGER PRIMARY KEY, category VARCHAR, announcement VARCHAR, link VARCHAR)");

                                Iterator<?> keys = myObj.keys();

                                int count = 0;
                                while (keys.hasNext()) {
                                    String category = (String) keys.next();
                                    JSONObject tempObj = new JSONObject(myObj.getString(category));

                                    for (int i = 0; i < tempObj.length() / 2; ++i) {
                                        String announcement = tempObj.getString(i + "announcement");
                                        String link = tempObj.getString(i + "link");

                                        myDatabase.execSQL("INSERT INTO spotlight (category, announcement, link) VALUES('" + category + "', '" + announcement + "', '" + link + "')");
                                        ++count;
                                    }
                                }

                                if (count > sharedPreferences.getInt("spotlightCount", 0)) {
                                    sharedPreferences.edit().putBoolean("newSpotlight", true).apply();
                                    sharedPreferences.edit().putInt("spotlightCount", count).apply();
                                }

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finishUp();
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

    public void finishUp() {
        loading.setText(context.getString(R.string.loading));
        sharedPreferences.edit().putBoolean("isSignedIn", true).apply();
        myDatabase.close();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();

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
    }

    /*
        Function to hide all layouts
     */
    public void hideLayouts() {
        loadingLayout.setVisibility(View.INVISIBLE);
        captchaLayout.setVisibility(View.INVISIBLE);
        semesterLayout.setVisibility(View.INVISIBLE);
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
}
