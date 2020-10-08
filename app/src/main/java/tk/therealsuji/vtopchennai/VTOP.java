package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

public class VTOP {
    Context context;
    WebView webView;
    ImageView captcha;
    EditText captchaView;
    Boolean isOpened, isLoggedIn;
    LinearLayout captchaLayout, loadingLayout, semesterLayout;
    TextView loading;
    Spinner selectSemester;
    SharedPreferences sharedPreferences;
    int counter;

    @SuppressLint("SetJavaScriptEnabled")
    public void setVtop(final Context context, WebView webView, ImageView captcha, LinearLayout captchaLayout, EditText captchaView, LinearLayout loadingLayout, TextView loading, LinearLayout semesterLayout, Spinner selectSemester, SharedPreferences sharedPreferences) {
        this.context = context;
        this.webView = webView;
        this.captcha = captcha;
        this.captchaLayout = captchaLayout;
        this.captchaView = captchaView;
        this.loadingLayout = loadingLayout;
        this.semesterLayout = semesterLayout;
        this.loading = loading;
        this.selectSemester = selectSemester;
        this.sharedPreferences = sharedPreferences;
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (!isOpened) {
                    if (counter == 60) {
                        Toast.makeText(context, "Sorry, we had some trouble connecting to the server. Please try again later.", Toast.LENGTH_LONG).show();
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
        counter = 1;
        this.webView.loadUrl("http://vtopcc.vit.ac.in:8080/vtop");
    }

    /*
        Function to open the sign in page
     */
    private void openSignIn() {
        webView.evaluateJavascript("(function() {" +
                "var successFlag = false;" +
                "$.ajax({" +
                "type: 'POST'," +
                "url: 'vtopLogin'," +
                "data: null," +
                "async: false," +
                "success: function(response) {" +
                "if(response.search('___INTERNAL___RESPONSE___') == -1 && response.includes('VTOP Login')) {" +
                "$('#page_outline').html(response);" +
                "successFlag = true;" +
                "}" +
                "}" +
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
        Function to reload the page using javascript in case of an error
     */
    public void reloadPage() {
        loading.setText(context.getString(R.string.loading));
        if (loadingLayout.getVisibility() == View.INVISIBLE) {
            hideLayouts();
            loadingLayout.setVisibility(View.VISIBLE);
        }

        webView.evaluateJavascript("(function() {" +
                "document.location.href = '/vtop';" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String src) {

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
                "if(images[i].alt == 'vtopCaptcha') {" +
                "return images[i].src;" +
                "}" +
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

                src = src.substring(24, src.length() - 1);  //It'll be better (and safer) to split the string using ' ' and take the second value
                byte[] decodedString = Base64.decode(src, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                captcha.setImageBitmap(decodedImage);

                hideLayouts();
                captchaView.setText("");
                captchaLayout.setVisibility(View.VISIBLE);
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
                "type : 'POST'," +
                "url : 'doLogin'," +
                "data : credentials," +
                "async: false," +
                "success : function(response) {" +
                "if(response.search('___INTERNAL___RESPONSE___') == -1) {" +
                "if(response.includes('authorizedIDX')) {" +
                "$('#page_outline').html(response);" +
                "successFlag = true;" +
                "} else if(response.includes('Invalid Captcha')) {" +
                "successFlag = 'Invalid Captcha';" +
                "} else if(response.includes('Invalid User Id / Password')) {" +
                "successFlag = 'Invalid User Id / Password';" +
                "} else if(response.includes('User Id Not available')) {" +
                "successFlag = 'User Id Not available';" +
                "}" +
                "}" +
                "}" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    isLoggedIn = true;
                    loading.setText(context.getString(R.string.downloading_profile));
                    downloadProfile();
                } else {
                    if (!value.equals("false") && !value.equals("null")) {
                        value = value.substring(1, value.length() - 1);
                        if (value.equals("Invalid User Id / Password") || value.equals("User Id Not available")) {
                            sharedPreferences.edit().putString("isLoggedIn", "false").apply();
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
        Function to save the name of the user and his/her ID (register number or faculty id, maybe in future) in SharedPreferences
        TBD: Saving the users profile picture
     */
    public void downloadProfile() {
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = false;" +
                "var name = '';" +
                "var id = '';" +
                "var j = 0;" +
                "$.ajax({" +
                "type: 'POST'," +
                "url : 'studentsRecord/StudentProfileAllView'," +
                "data : data," +
                "async: false," +
                "success: function(response) {" +
                "if(response.includes('Personal Information')) {" +
                "var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "var cells = doc.getElementsByTagName('td');" +
                "for(var i = 0; i < cells.length && j < 2; ++i) {" +
                "if(cells[i].innerHTML.includes('Name')) {" +
                "name = cells[++i].innerHTML;" +
                "++j;" +
                "}" +
                "if(cells[i].innerHTML.includes('Register')) {" +
                "id = cells[++i].innerHTML;" +
                "++j;" +
                "}" +
                "}" +
                "obj = {'name': name, 'id': id};" +
                "}" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                /*
                    obj is in the form of a JSON string like {"name":"JOHN DOE","register":"20XYZ1987"}
                 */
                if (obj.equals("false")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else {
                    try {
                        JSONObject myObj = new JSONObject(obj);
                        sharedPreferences.edit().putString("name", myObj.getString("name")).apply();
                        sharedPreferences.edit().putString("id", myObj.getString("id")).apply();
                        openTimetable();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Sorry, something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    /*
        Function to get the list of semesters in order to download the timetable. The same value will be used to download the attendance
     */
    public void openTimetable() {
        webView.evaluateJavascript("(function() {" +
                "var data = 'verifyMenu=true&winImage=' + $('#winImage').val() + '&authorizedID=' + $('#authorizedIDX').val() + '&nocache=@(new Date().getTime())';" +
                "var obj = false;" +
                "$.ajax({" +
                "type: 'POST'," +
                "url : 'academics/common/StudentTimeTable'," +
                "data : data," +
                "async: false," +
                "success: function(response) {" +
                "if(response.includes('Time Table')) {" +
                "$('#page-wrapper').html(response);" +
                "var options = document.getElementById('semesterSubId').getElementsByTagName('option');" +
                "obj = {};" +
                "for(var i = 0, j = 0; i < options.length; ++i, ++j) {" +
                "if(options[i].innerHTML.includes('Choose') || options[i].innerHTML.includes('Select')) {" +
                "--j;" +
                "continue;" +
                "}" +
                "obj[j] = options[i].innerHTML;" +
                "}" +
                "}" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                /*
                    obj is in the form of a JSON string like {"0": "Option 1", "1": "Option 2", "2": "Option 3",...}
                 */
                if (obj.equals("false") || obj.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else {
                    try {
                        JSONObject myObj = new JSONObject(obj);
                        List<String> options = new ArrayList<>();
                        for (int i = 0; i < myObj.length(); ++i) {
                            // All the string replacing has to be undone before submitting to get the timetable
                            String option = myObj.getString(Integer.toString(i)).replaceAll("&amp;", "&");

                            options.add(option);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options);
                        selectSemester.setAdapter(adapter);
                        hideLayouts();
                        semesterLayout.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                        isOpened = false;
                        reloadPage();
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
        loading.setText(context.getString(R.string.downloading_timetable));
        loadingLayout.setVisibility(View.VISIBLE);

        String semester = sharedPreferences.getString("semester", "null");

        webView.evaluateJavascript("(function() {" +
                "var select = document.getElementById('semesterSubId');" +
                "var options = select.getElementsByTagName('option');" +
                "for(var i = 0; i < options.length; ++i) {" +
                "if(options[i].innerHTML == '" + semester + "') {" +
                "document.getElementById('semesterSubId').selectedIndex = i;" +
                "document.getElementById('studentTimeTable').getElementsByTagName('button')[0].click();" +
                "return true;" +
                "}" +
                "}" +
                "return false;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloadTimetable();
                        }
                    }, 500);
                } else {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                }
            }
        });
    }

    /*
        Function to save the timetable in an SQLite database, and the credit score in SharedPreferences
     */
    public void downloadTimetable() {
        webView.evaluateJavascript("(function() {" +
                "var loading = document.getElementsByTagName('html')[0];" +
                "if(loading.innerHTML.includes('Please wait...')) {" +
                "return 'loading';" +
                "} else {" +
                "var obj = {};" +
                "var spans = document.getElementById('getStudentDetails').getElementsByTagName('span');" +
                "var credits = '0';" +
                "if(spans[0].innerHTML == 'No Record(s) Found') {" +
                "return 'unreleased';" +
                "}" +
                "for(var i = spans.length-1; i > 0; --i) {" +
                "if(spans[i].innerHTML.includes('Credits')) {" +
                "credits = spans[i+1].innerHTML;" +
                "break;" +
                "}" +
                "}" +
                "obj['credits'] = credits;" +
                "var cells = document.getElementById('timeTableStyle').getElementsByTagName('td');" +
                "var category = '';" +
                "var timings = '';" +
                "var theory = {}, lab = {}, mon = {}, tue = {}, wed = {}, thu = {}, fri = {}, sat = {}, sun = {};" +
                "var i = 0;" +
                "for(var j = 0; j < cells.length; ++j) {" +
                "if(cells[j].innerHTML.toLowerCase() == 'mon' || cells[j].innerHTML.toLowerCase() == 'tue' || cells[j].innerHTML.toLowerCase() == 'wed' || cells[j].innerHTML.toLowerCase() == 'thu' || cells[j].innerHTML.toLowerCase() == 'fri' || cells[j].innerHTML.toLowerCase() == 'sat' || cells[j].innerHTML.toLowerCase() == 'sun') {" +
                "category = cells[j].innerHTML.toLowerCase();" +
                "continue;" +
                "}" +
                "if(cells[j].innerHTML.toLowerCase() == 'theory' || cells[j].innerHTML.toLowerCase() == 'lab') {" +
                "if(category == '' || category == 'theory' || category == 'lab') {" +
                "category = cells[j].innerHTML.toLowerCase();" +
                "} else {" +
                "postfix = cells[j].innerHTML.toLowerCase();" +
                "}" +
                "i = 0;" +
                "continue;" +
                "}" +
                "if(cells[j].innerHTML.toLowerCase() == 'start' || cells[j].innerHTML.toLowerCase() == 'end') {" +
                "postfix = cells[j].innerHTML.toLowerCase();" +
                "i = 0;" +
                "continue;" +
                "}" +
                "subcat = i.toString() + postfix;" +
                "if(category == 'theory') {" +
                "theory[subcat] = cells[j].innerHTML;" +
                "} else if(category == 'lab') {" +
                "lab[subcat] = cells[j].innerHTML;" +
                "} else if(category == 'mon') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "mon[subcat] = cells[j].innerHTML;" +
                "}" +
                "} else if(category == 'tue') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "tue[subcat] = cells[j].innerHTML;" +
                "}" +
                "} else if(category == 'wed') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "wed[subcat] = cells[j].innerHTML;" +
                "}" +
                "} else if(category == 'thu') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "thu[subcat] = cells[j].innerHTML;" +
                "}" +
                "} else if(category == 'fri') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "fri[subcat] = cells[j].innerHTML;" +
                "}" +
                "} else if(category == 'sat') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "sat[subcat] = cells[j].innerHTML;" +
                "}" +
                "} else if(category == 'sun') {" +
                "if(cells[j].bgColor == '#CCFF33') {" +
                "sun[subcat] = cells[j].innerHTML;" +
                "}" +
                "}" +
                "++i;" +
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
                "}" +
                "return 'false';" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                String temp = obj.substring(1, obj.length() - 1);
                if (temp.equals("false") || obj.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else if (temp.equals("loading")) {
                    downloadTimetable();
                } else if (temp.equals("unreleased")) {
                    //erase existing timetable
                } else {
                    try {
                        JSONObject myObj = new JSONObject(obj);
                        String credits = "Credits: " + myObj.getString("credits");
                        sharedPreferences.edit().putString("credits", credits).apply();
                        //save timetable
                        downloadFaculty();
                    } catch (Exception e) {
                        Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                        isOpened = false;
                        reloadPage();
                    }
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
                "var division = document.getElementById('studentDetailsList'); " +
                "var heads = division.getElementsByTagName('th');" +
                "var courseIndex, facultyIndex, flag = 0;" +
                "var columns = heads.length;" +
                "for(var i = 0; i < columns; ++i) {" +
                "if(heads[i].innerText.toLowerCase() == 'course') {" +
                "courseIndex = i + 1;" +
                "++flag;" +
                "}" +
                "if(heads[i].innerText.toLowerCase() == 'faculty details') {" +
                "facultyIndex = i + 1;" +
                "++flag;" +
                "}" +
                "if(flag == 2) {" +
                "break;" +
                "}" +
                "}" +
                "var obj = {};" +
                "var cells = division.getElementsByTagName('td');" +
                "for(var i = 0; courseIndex < cells.length && facultyIndex < cells.length; ++i) {" +
                "var temp = {};" +
                "temp['0'] = cells[courseIndex].innerText;" +
                "temp['1'] = cells[facultyIndex].innerText;" +
                "obj[i.toString()] = temp;" +
                "courseIndex += columns;" +
                "facultyIndex += columns;" +
                "}" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else {
                    //save faculty info
                    downloadProctor();
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
                "type: 'POST'," +
                "url : 'proctor/viewProctorDetails'," +
                "data : data," +
                "async: false," +
                "success: function(response) {" +
                "var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "var cells = doc.getElementById('showDetails').getElementsByTagName('td');" +
                "for(var i = 0; i < cells.length; ++i) {" +
                "if(cells[i].innerHTML.includes('img')) {" +
                "continue;" +
                "}" +
                "var key = cells[i].innerText;" +
                "var value = cells[++i].innerText;" +
                "obj[key] = value;" +
                "}" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                if (obj.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else {
                    //save proctor info
                    downloadDeanHOD();
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
                "type: 'POST'," +
                "url : 'hrms/viewHodDeanDetails'," +
                "data : data," +
                "async: false," +
                "success: function(response) {" +
                "var doc = new DOMParser().parseFromString(response, 'text/html');" +
                "var tables = doc.getElementsByTagName('table');" +
                "var first = 'dean';" +
                "if(doc.getElementsByTagName('h3')[0].innerText.toLowerCase() != 'dean') {" +
                "first = 'hod';" +
                "}" +
                "var dean = {}, hod = {};" +
                "var cells = tables[0].getElementsByTagName('td');" +
                "for(var i = 0; i < cells.length; ++i) {" +
                "if(first == 'dean') {" +
                "if(cells[i].innerHTML.includes('img')) {" +
                "continue;" +
                "}" +
                "var key = cells[i].innerText;" +
                "var value = cells[++i].innerText;" +
                "dean[key] = value;" +
                "} else {" +
                "if(cells[i].innerHTML.includes('img')) {" +
                "continue;" +
                "}" +
                "var key = cells[i].innerText;" +
                "var value = cells[++i].innerText;" +
                "hod[key] = value;" +
                "}" +
                "}" +
                "var cells = tables[1].getElementsByTagName('td');" +
                "for(var i = 0; i < cells.length; ++i) {" +
                "if(first == 'dean') {" +
                "if(cells[i].innerHTML.includes('img')) {" +
                "continue;" +
                "}" +
                "var key = cells[i].innerText;" +
                "var value = cells[++i].innerText;" +
                "hod[key] = value;" +
                "} else {" +
                "if(cells[i].innerHTML.includes('img')) {" +
                "continue;" +
                "}" +
                "var key = cells[i].innerText;" +
                "var value = cells[++i].innerText;" +
                "dean[key] = value;" +
                "}" +
                "}" +
                "obj['dean'] = dean;" +
                "obj['hod'] = hod;" +
                "}" +
                "});" +
                "return obj;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String obj) {
                if (obj.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else {
                    //save HOD & Dean info
                    selectAttendance();
                }
            }
        });
    }

    /*
        Function to select the attendance to download
     */
    public void selectAttendance() {
        loading.setText(context.getString(R.string.downloading_attendance));
        String semester = sharedPreferences.getString("semester", "null");

        webView.evaluateJavascript("(function() {" +
                "var select = document.getElementById('semesterSubId');" +
                "var options = select.getElementsByTagName('option');" +
                "for(var i = 0; i < options.length; ++i) {" +
                "if(options[i].innerHTML == '" + semester + "') {" +
                "document.getElementById('semesterSubId').selectedIndex = i;" +
                "document.getElementById('studentAttendance').getElementsByTagName('button')[0].click();" +
                "return true;" +
                "}" +
                "}" +
                "return false;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    downloadAttendance();
                } else {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                }
            }
        });
    }

    /*
        Function to store the attendance in the SQLite database.
     */
    public void downloadAttendance() {
        webView.evaluateJavascript("(function() {" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    //save staff info
                    downloadMessages();
                } else {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
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
                "type: 'POST'," +
                "url : 'academics/common/StudentClassMessage'," +
                "data : data," +
                "async: false," +
                "success: function(response) {" +
                "if(!response.includes('No Messages Sent by Faculty')) {" +
                "successFlag = true;" +
                "}" +
                "}" +
                "});" +
                "return successFlag;" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value.equals("true")) {
                    //Save the messages
                    sharedPreferences.edit().putString("isLoggedIn", "true").apply();
                    context.startActivity(new Intent(context, HomeActivity.class));
                    ((Activity) context).finish();
                } else {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                }
            }
        });
    }

    /*
        Function to hide all layouts
     */
    public void hideLayouts() {
        loadingLayout.setVisibility(View.INVISIBLE);
        captchaLayout.setVisibility(View.INVISIBLE);
        semesterLayout.setVisibility(View.INVISIBLE);
    }
}
