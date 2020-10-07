package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
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
    Boolean isOpened, isLoggedIn;
    LinearLayout captchaLayout, loadingLayout, semesterLayout;
    TextView loading;
    Spinner selectSemester;
    SharedPreferences sharedPreferences;
    int counter;

    @SuppressLint("SetJavaScriptEnabled")
    public void setVtop(final Context context, WebView webView, ImageView captcha, LinearLayout captchaLayout, LinearLayout loadingLayout, TextView loading, LinearLayout semesterLayout, Spinner selectSemester, SharedPreferences sharedPreferences) {
        this.context = context;
        this.webView = webView;
        this.captcha = captcha;
        this.captchaLayout = captchaLayout;
        this.loadingLayout = loadingLayout;
        this.semesterLayout = semesterLayout;
        this.loading = loading;
        this.selectSemester = selectSemester;
        this.sharedPreferences = sharedPreferences;
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (!isOpened) {
                    if (counter == 30) {
                        Toast.makeText(context, "Sorry, we had some trouble connecting to the server. Please try again later.", Toast.LENGTH_LONG).show();
                        //Go to previous avtivity
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
        hideLayouts();
        loading.setText("Loading");
        loadingLayout.setVisibility(View.VISIBLE);

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
                    src will look like "data:image/png:base64, blablabla...." (including the quotes)
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
                    hideLayouts();
                    loading.setText("Downloading Profile");
                    loadingLayout.setVisibility(View.VISIBLE);
                    downloadProfile();
                } else {
                    if (!value.equals("false") && !value.equals("null")) {
                        value = value.substring(1, value.length() - 1);
                        if (value.equals("Invalid User Id / Password") || value.equals("User Id Not available")) {
                            sharedPreferences.edit().putString("isLoggedIn", "false").apply();
                            //launch signin activity
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
                    obj will look like {"name":"JOHN DOE","register":"20XYZ1987"}
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
        Function to select the semester in order to save the timetable
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
                    obj will look like {"name":"JOHN DOE","register":"20XYZ1987"}
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
        loading.setText("Downloading Timetable");
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
                    downloadTimetable();
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
                //This is where you download the timetable
                "}" +
                "return 'false';" +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                value = value.substring(1, value.length() - 1);
                if (value.equals("false") || value.equals("null")) {
                    Toast.makeText(context, "Sorry, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    isOpened = false;
                    reloadPage();
                } else if (value.equals("loading")) {
                    downloadTimetable();
                } else {
                    // save time table
                }
            }
        });
    }

    /*
        Function to select the attendance to download
     */
    public void selectAttendance() {
        loading.setText("Downloading Attendance");
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
        Function to store the attendance in an SQLite database
     */
    public void downloadAttendance() {

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
