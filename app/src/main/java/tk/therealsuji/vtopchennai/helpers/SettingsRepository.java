package tk.therealsuji.vtopchennai.helpers;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import tk.therealsuji.vtopchennai.BuildConfig;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.WebViewActivity;
import tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment;
import tk.therealsuji.vtopchennai.fragments.ViewPagerFragment;
import tk.therealsuji.vtopchennai.models.Exam;
import tk.therealsuji.vtopchennai.models.Timetable;
import tk.therealsuji.vtopchennai.receivers.ExamNotificationReceiver;
import tk.therealsuji.vtopchennai.receivers.TimetableNotificationReceiver;

public class SettingsRepository {
    public static final String APP_BASE_URL = "https://vtopchennai.therealsuji.tk";
    public static final String APP_ABOUT_URL = APP_BASE_URL + "/about.json";
    public static final String APP_PRIVACY_URL = APP_BASE_URL + "/privacy-policy";

    public static final String DEVELOPER_BASE_URL = "https://therealsuji.tk";

    public static final String GITHUB_BASE_URL = "https://github.com/therealsujitk/android-vtop-chennai";
    public static final String GITHUB_FEATURE_URL = GITHUB_BASE_URL + "/issues";
    public static final String GITHUB_ISSUE_URL = GITHUB_BASE_URL + "/issues";

    public static final String MOODLE_BASE_URL = "https://lms.vit.ac.in";
    public static final String MOODLE_LOGIN_PATH = "/login/token.php";
    public static final String MOODLE_UPLOAD_PATH = "/webservice/upload.php";
    public static final String MOODLE_WEBSERVICE_PATH = "/webservice/rest/server.php";

    public static final String VTOP_BASE_URL = "https://vtopcc.vit.ac.in/vtop";

    public static final int THEME_DAY = 1;
    public static final int THEME_NIGHT = 2;
    public static final int THEME_SYSTEM_DAY = 3;
    public static final int THEME_SYSTEM_NIGHT = 4;

    public static final int NOTIFICATION_ID_EXAMS = 1;
    public static final int NOTIFICATION_ID_TIMETABLE = 2;
    public static final int NOTIFICATION_ID_VTOP_DOWNLOAD = 3;

    public static int getTheme(Context context) {
        String appearance = getSharedPreferences(context).getString("appearance", "system");

        if (appearance.equals("dark")) {
            return THEME_NIGHT;
        } else if (appearance.equals("light")) {
            return THEME_DAY;
        }

        return getSystemTheme(context);
    }

    public static int getSystemTheme(Context context) {
        int currentNightMode = context.getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            return THEME_SYSTEM_NIGHT;
        } else {
            return THEME_SYSTEM_DAY;
        }
    }

    public static void applyDynamicColors(Activity activity, boolean amoledMode) {
        DynamicColorsOptions.Builder dynamicColorsOptions = new DynamicColorsOptions.Builder();
        if (amoledMode) dynamicColorsOptions.setThemeOverlay(R.style.ThemeOverlay_VTOP_Amoled);
        DynamicColors.applyToActivityIfAvailable(activity, dynamicColorsOptions.build());
    }

    public static float getCGPA(Context context) {
        return getSharedPreferences(context).getFloat("cgpa", 0);
    }

    public static boolean isRefreshRequired(Context context) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -7);

        Date now = c.getTime();
        Date lastRefreshed = new Date(getSharedPreferences(context).getLong("lastRefreshed", 0));

        return !lastRefreshed.after(now);
    }

    public static boolean isSignedIn(Context context) {
        return getSharedPreferences(context).getBoolean("isVTOPSignedIn", false);
    }

    public static boolean isMoodleSignedIn(Context context) {
        return getSharedPreferences(context).getBoolean("isMoodleSignedIn", false);
    }

    public static void signOut(Context context) {
        AppDatabase.deleteDatabase(context);
        getSharedPreferences(context).edit().clear().apply();

        SharedPreferences encryptedSharedPreferences = getEncryptedSharedPreferences(context);

        if (encryptedSharedPreferences != null) {
            encryptedSharedPreferences.edit().clear().apply();
        }
    }

    public static void signOutMoodle(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().remove("isMoodleSignedIn").apply();
        sharedPreferences.edit().remove("moodleToken").apply();
        sharedPreferences.edit().remove("moodlePrivateToken").apply();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
    }

    public static SharedPreferences getEncryptedSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    "credentials",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasFileReadPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean hasFileWritePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        return hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static Observable<JSONObject> checkForUpdates() {
        return Observable.fromCallable(() -> {
                    try {
                        StringBuilder sb = new StringBuilder();
                        URL url = new URL(SettingsRepository.APP_ABOUT_URL + "?v=" + BuildConfig.VERSION_NAME);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        InputStream in = httpURLConnection.getInputStream();
                        InputStreamReader reader = new InputStreamReader(in);
                        int data = reader.read();

                        while (data != -1) {
                            char current = (char) data;
                            sb.append(current);
                            data = reader.read();
                        }

                        String result = sb.toString();
                        return new JSONObject(result);
                    } catch (Exception ignored) {
                        return new JSONObject();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void openDownloadPage(Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_BASE_URL));
        context.startActivity(browserIntent);
    }

    public static void openRecyclerViewFragment(FragmentActivity fragmentActivity, int titleId, int contentType) {
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();

        bundle.putInt("title_id", titleId);
        bundle.putInt("content_type", contentType);

        recyclerViewFragment.setArguments(bundle);

        fragmentActivity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(android.R.id.content, recyclerViewFragment)
                .addToBackStack(null)
                .commit();
    }

    public static void openViewPagerFragment(FragmentActivity fragmentActivity, int titleId, int contentType) {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        Bundle bundle = new Bundle();

        bundle.putInt("title_id", titleId);
        bundle.putInt("content_type", contentType);

        viewPagerFragment.setArguments(bundle);

        fragmentActivity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(android.R.id.content, viewPagerFragment)
                .addToBackStack(null)
                .commit();
    }

    public static void openWebViewActivity(Context context, String title, String url) {
        Intent intent = new Intent(context, WebViewActivity.class)
                .putExtra("url", url)
                .putExtra("title", title);
        context.startActivity(intent);
    }

    public static void openBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    public static void downloadFile(Context context, String filePath, String fileName, String mimetype, Uri uri, String cookie) {
        Toast.makeText(context, Html.fromHtml(context.getString(R.string.downloading_file, fileName), Html.FROM_HTML_MODE_LEGACY), Toast.LENGTH_SHORT).show();

        DownloadManager.Request request = new DownloadManager.Request(uri);
        String encodedFilePath = Uri.encode("VIT Student/" + filePath + "/" + fileName);
        request.addRequestHeader("cookie", cookie);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, encodedFilePath);
        request.setMimeType(mimetype);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(fileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    public static String getSystemFormattedTime(Context context, String time) throws ParseException {
        if (DateFormat.is24HourFormat(context)) {
            return time;
        } else {
            SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            return hour12.format(Objects.requireNonNull(hour24.parse(time)));
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void clearNotificationPendingIntents(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Intent notificationIntent = new Intent(context, TimetableNotificationReceiver.class);

        int alarmCount = sharedPreferences.getInt("alarmCount", 0);
        while (alarmCount >= 0) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, --alarmCount, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
        }

        sharedPreferences.edit().remove("alarmCount").apply();
    }

    public static void setTimetableNotifications(Context context, Timetable timetable) throws ParseException {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        Intent notificationIntent = new Intent(context, TimetableNotificationReceiver.class);
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        int alarmCount = sharedPreferences.getInt("alarmCount", 0);
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        Integer[] slots = {
                timetable.sunday,
                timetable.monday,
                timetable.tuesday,
                timetable.wednesday,
                timetable.thursday,
                timetable.friday,
                timetable.saturday
        };

        Date today = dateFormat.parse(dateFormat.format(calendar.getTime()));
        Date now = hour24.parse(hour24.format(calendar.getTime()));

        for (int i = 0; i < slots.length; ++i) {
            if (slots[i] == null) {
                continue;
            }

            assert today != null;
            Calendar alarm = Calendar.getInstance();
            alarm.setTime(today);

            if (i == day) {
                Date startTime = hour24.parse(timetable.startTime);
                assert startTime != null;

                if (startTime.before(now)) {
                    alarm.add(Calendar.DATE, 7);
                }
            } else if (i > day) {
                alarm.add(Calendar.DATE, i - day);
            } else {
                alarm.add(Calendar.DATE, 7 - day + i);
            }

            alarm.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetable.startTime.split(":")[0]));
            alarm.set(Calendar.MINUTE, Integer.parseInt(timetable.startTime.split(":")[1]));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

            alarm.add(Calendar.MINUTE, -30);
            pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        }

        sharedPreferences.edit().putInt("alarmCount", alarmCount).apply();
    }

    public static void setExamNotifications(Context context, List<Exam> exams) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        Intent notificationIntent = new Intent(context, ExamNotificationReceiver.class);
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        int alarmCount = sharedPreferences.getInt("alarmCount", 0);

        Date now = calendar.getTime();

        for (int i = 0; i < exams.size(); ++i) {
            Exam exam = exams.get(i);

            if (exam.startTime == null || now.after(new Date(exam.startTime))) {
                continue;
            }

            Calendar alarm = Calendar.getInstance();
            alarm.setTime(new Date(exam.startTime));
            alarm.add(Calendar.MINUTE, -30);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmCount++, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
        }

        sharedPreferences.edit().putInt("alarmCount", alarmCount).apply();
    }

    /**
     * WARNING: This function is to be used ONLY for the purpose of testing and not in production.
     * This function returns a naive OkHttpClient that accepts any and all certificates,
     * and hence can lead to attacks.
     */
    @Deprecated
    @SuppressLint({"BadHostnameVerifier", "CustomX509TrustManager", "TrustAllX509TrustManager"})
    public static OkHttpClient getNaiveOkHttpClient() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCertificates, new SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((s, sslSession) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
