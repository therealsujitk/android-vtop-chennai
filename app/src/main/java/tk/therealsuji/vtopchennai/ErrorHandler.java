package tk.therealsuji.vtopchennai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ErrorHandler {
    public static boolean isPreRelease = BuildConfig.BUILD_TYPE.toLowerCase().equals("debug") || BuildConfig.VERSION_NAME.toLowerCase().contains("beta");
    Context context;
    SQLiteDatabase myDatabase;
    String userAgent;
    StringBuilder errorLog;

    public ErrorHandler(Context context, String userAgent) {
        this.context = context;
        this.userAgent = userAgent;

        if (isPreRelease) {
            this.errorLog = new StringBuilder();
            this.myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        }
    }

    /*
        Append logs to errorLog
     */
    public StringBuilder append(String log) {
        errorLog.append(log);
        return errorLog;
    }

    /*
        Reset the logs
     */
    public void reset() {
        errorLog = new StringBuilder();
    }

    /*
        Display and store the error
     */
    public void error(final String errorCode) {
        if (isPreRelease) {
//          String errorComment = "<code class=\"comment\">-------------------- Error " + errorCode + " -------------------";
            String errorComment = "-------------------- Error " + errorCode + " -------------------";
            if (Integer.parseInt(errorCode) < 1000) errorComment = errorComment + "-";
//          errorComment = errorComment + "</code>\n";
            errorComment = errorComment + "\n";
            errorLog.insert(0, errorComment);

            append("\n").append(getAdditionalInfo());

            logError(errorCode, errorLog.toString());
        }

        ((Activity) context).runOnUiThread(() -> {
            Toast.makeText(context, "Error " + errorCode + ", please try again.", Toast.LENGTH_LONG).show();
            reset();
        });
    }

    /*
        Function to build stack trace
     */
    public void appendStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

//        append("<code class\"error\">");
        append(errors.toString().replaceAll("at ", "    at "));
//        append("</code>");
    }

    /*
        Function to add device info to the error log
     */
    private String getAdditionalInfo() {
        StringBuilder additionalInfo = new StringBuilder();

        String buildType = BuildConfig.BUILD_TYPE;
        String versionName = BuildConfig.VERSION_NAME;
        int versionCode = BuildConfig.VERSION_CODE;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat ts = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
        String timestamp = ts.format(c.getTime());

        String deviceModal = Build.MODEL;
        String androidVersion = Build.VERSION.RELEASE;
        int androidSDK = Build.VERSION.SDK_INT;

//        additionalInfo.append("<code class=\"comment\">----------------- Additional Info -----------------</code>\n");
//        additionalInfo.append("<code>");
        additionalInfo.append("----------------- Additional Info -----------------\n");
        additionalInfo.append("App Version         : ").append(versionName).append(" (").append(versionCode).append(") - ").append(buildType).append("\n");
        additionalInfo.append("Error Timestamp     : ").append(timestamp).append("\n");
        additionalInfo.append("Device Modal        : ").append(deviceModal).append("\n");
        additionalInfo.append("Android Version     : ").append(androidVersion).append(" (SDK ").append(androidSDK).append(")\n");
        if (userAgent != null) {
            additionalInfo.append("Default User Agent  : ").append(userAgent);
        }
//        additionalInfo.append("</code>");

        return additionalInfo.toString();
    }

    /*
        Function to store the error log
     */
    private void logError(final String errorCode, final String error) {
        new Thread(() -> {
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS error_logs (id INTEGER PRIMARY KEY, error_code VARCHAR, date VARCHAR, error VARCHAR)");

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            String date = df.format(c.getTime()).toUpperCase();

            myDatabase.execSQL("INSERT INTO error_logs (error_code, date, error) VALUES('" + errorCode + "', '" + date + "', '" + error.replaceAll("'", "''") + "')");

            NotificationHelper notificationHelper = new NotificationHelper(context);
            NotificationCompat.Builder n = notificationHelper.notifyError(context, "Error " + errorCode + " has been logged successfully.");
            notificationHelper.getManager().notify(3, n.build());
        }).start();
    }

    /*
        Function to send a log
     */
    public void sendLog(String errorCode, String error) {
//        String style = "<style>" +
//                "    pre {" +
//                "        background: #282b2e;" +
//                "        color: #a9b7c6;" +
//                "        padding: 15px;" +
//                "        border-radius: 7px;" +
//                "        overflow: auto;" +
//                "    }" +
//                "" +
//                "    code.comment {" +
//                "        color: #808080;" +
//                "    }" +
//                "" +
//                "    code.error {" +
//                "        color: #cc7832;" +
//                "    }" +
//                "" +
//                "    code.warning {" +
//                "        color: #ffc66d;" +
//                "    }" +
//                "    " +
//                "    code.debug {" +
//                "        color: #6897bb;" +
//                "    }" +
//                "    " +
//                "    code.tip {" +
//                "        color: #6a8759;" +
//                "    }" +
//                "</style>";
//
//        String html = style + "<pre>" + error + "</pre>";

        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:me@therealsuji.tk" +
                    "?subject=VTOP Chennai - Bug Report (" + errorCode + ")" +
                    "&body=" + URLEncoder.encode(error, String.valueOf(StandardCharsets.UTF_8)).replaceAll("\\+", " ").replaceAll("%5Cn", "\n")));
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    /*
        Function to clear all logs
     */
    public void clearLogs() {
        SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("DELETE FROM error_logs");
        myDatabase.close();
    }
}

/*
 *  Error Codes
 *
 *  Error 001   Failed to fetch encrypted credentials
 *
 *  Error 101   Failed to connect to the server
 *  Error 102   Failed to get captcha type (local / public)
 *  Error 103   Failed to get captcha image
 *  Error 104   Failed to display captcha image
 *
 *  Error 201   Unknown error during login (Possibly timeout)
 *
 *  Error 301   Failed to fetch semesters
 *  Error 302   Failed to display semesters
 *  Error 303   Failed to fetch semester ID
 *
 *  Error 401   Failed to download profile data
 *  Error 402   Error while storing profile data
 *
 *  Error 501   Failed to download timetable
 *  Error 502   Error while storing timetable
 *
 *  Error 601   Failed to download faculty info
 *  Error 602   Error while storing faculty info
 *
 *  Error 701   Failed to download proctor info
 *  Error 702   Error while storing proctor info
 *  Error 703   Failed to download hod & dean info
 *  Error 704   Error while storing hod & dean info
 *
 *  Error 801   Failed to download attendance
 *  Error 802   Error while storing attendance
 *
 *  Error 901   Failed to download the exam schedule
 *  Error 902   Failed to store the exam schedule
 *
 *  Error 1001  Failed to download marks
 *  Error 1002  Error while storing marks & updating unread marks
 *
 *  Error 1101  Failed to download grades
 *  Error 1102  Error while storing grades
 *  Error 1103  Failed to download grade history
 *  Error 1104  Error while storing grade history
 *
 *  Error 1201  Failed to download class messages
 *  Error 1202  Error while storing class messages
 *  Error 1203  Unknown error downloading / storing proctor messages
 *
 *  Error 1301  Failed to download spotlight
 *  Error 1302  Error while storing spotlight & updating unread announcements
 *
 *  Error 1401  Failed to download receipts
 *  Error 1402  Error while storing receipts
 *
 */
