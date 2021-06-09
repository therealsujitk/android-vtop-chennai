package tk.therealsuji.vtopchennai;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    int classesNotification, timetableNotification, messagesNotification, coursesNotification;
    int academicsNotification, examsNotification, marksNotification, gradesNotification, spotlightNotification;
    int campusNotification, proctorMessageNotification, receiptsNotification;
    SharedPreferences sharedPreferences, encryptedSharedPreferences;
    Dialog refresh, download, appearance, signOut;
    Context context;
    VTOP vtop;

    boolean terminateThread;

    /*
        The following functions are to open the activities in the "Classes" category
     */

    public static void expand(final View view) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        ValueAnimator anim = ValueAnimator.ofInt(0, targetHeight);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(300);
        anim.addUpdateListener(animation -> {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            layoutParams.height = (int) (targetHeight * animation.getAnimatedFraction());
            view.setLayoutParams(layoutParams);
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }
        });
        anim.start();
    }

    public void openAttendance(View view) {
        startActivity(new Intent(this, AttendanceActivity.class));
    }

    public static void compress(final View view) {
        final int viewHeight = view.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(viewHeight, 0);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(300);
        anim.addUpdateListener(animation -> {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            layoutParams.height = (int) (viewHeight * (1 - animation.getAnimatedFraction()));
            view.setLayoutParams(layoutParams);
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        anim.start();
    }

    public void openCourses(View view) {
        startActivity(new Intent(this, CoursesActivity.class));

        if (coursesNotification == -1) {
            return;
        }

        try {
            findViewById(coursesNotification).animate().scaleX(0).scaleY(0);

            if (sharedPreferences.getBoolean("failedAttendance", false)) {
                return;
            }

            if (!sharedPreferences.getBoolean("newTimetable", false) && !sharedPreferences.getBoolean("newMessages", false)) {
                findViewById(classesNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        The following functions are to open the activities in the "Academics" category
     */

    public void openExams(View view) {
        startActivity(new Intent(this, ExamsActivity.class));

        if (examsNotification == -1) {
            return;
        }

        try {
            findViewById(examsNotification).animate().scaleX(0).scaleY(0);

            if (sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newGrades", false) && sharedPreferences.getString("newSpotlight", "{}").equals("{}")) {
                findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openMarks(View view) {
        startActivity(new Intent(this, MarksActivity.class));

        if (marksNotification == -1) {
            return;
        }

        try {
            findViewById(marksNotification).animate().scaleX(0).scaleY(0);

            if (!sharedPreferences.getBoolean("newExams", false) && !sharedPreferences.getBoolean("newGrades", false) && sharedPreferences.getString("newSpotlight", "{}").equals("{}")) {
                findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openGrades(View view) {
        startActivity(new Intent(this, GradesActivity.class));

        if (gradesNotification == -1) {
            return;
        }

        try {
            findViewById(gradesNotification).animate().scaleX(0).scaleY(0);

            if (!sharedPreferences.getBoolean("newExams", false) && sharedPreferences.getString("newMarks", "{}").equals("{}") && sharedPreferences.getString("newSpotlight", "{}").equals("{}")) {
                findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openSpotlight(View view) {
        startActivity(new Intent(this, SpotlightActivity.class));

        if (spotlightNotification == -1) {
            return;
        }

        try {
            findViewById(spotlightNotification).animate().scaleX(0).scaleY(0);

            if (!sharedPreferences.getBoolean("newExams", false) && sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newGrades", false)) {
                findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        The following functions are to open the activities in the "Campus" category
     */

    public void openDirections(View view) {
        startActivity(new Intent(this, DirectionsActivity.class));
    }

    public void openStaff(View view) {
        startActivity(new Intent(this, StaffActivity.class));
    }

    public void openProctorMessages(View view) {
        startActivity(new Intent(this, ProctorMessagesActivity.class));

        if (proctorMessageNotification == -1) {
            return;
        }

        try {
            findViewById(proctorMessageNotification).animate().scaleX(0).scaleY(0);

            if (sharedPreferences.getBoolean("duePayments", false)) {
                return;
            }

            findViewById(campusNotification).animate().scaleX(0).scaleY(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openReceipts(View view) {
        startActivity(new Intent(this, ReceiptsActivity.class));

        if (receiptsNotification == -1) {
            return;
        }

        try {
            if (sharedPreferences.getBoolean("duePayments", false)) {
                return;
            }

            findViewById(receiptsNotification).animate().scaleX(0).scaleY(0);

            if (!sharedPreferences.getBoolean("newProctorMessages", false)) {
                findViewById(campusNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openReportBug(View view) {
        startActivity(new Intent(this, ReportBugActivity.class));
    }

    /*
        The following functions are to open the activities in the "Application" category
     */

    public void share(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Get the VTOP Chennai android app");
        intent.putExtra(Intent.EXTRA_TEXT, "https://vtopchennai.therealsuji.tk");
        intent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(intent, "Share \"VTOP Chennai\" via");
        startActivity(shareIntent);
    }

    public void openTimetable(View view) {
        startActivity(new Intent(this, TimetableActivity.class));

        if (timetableNotification == -1) {
            return;
        }

        try {
            findViewById(timetableNotification).animate().scaleX(0).scaleY(0);

            if (sharedPreferences.getBoolean("failedAttendance", false)) {
                return;
            }

            if (!sharedPreferences.getBoolean("newMessages", false) && !sharedPreferences.getBoolean("newCourses", false)) {
                findViewById(classesNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openMessages(View view) {
        startActivity(new Intent(this, MessagesActivity.class));

        if (messagesNotification == -1) {
            return;
        }

        try {
            findViewById(messagesNotification).animate().scaleX(0).scaleY(0);

            if (sharedPreferences.getBoolean("failedAttendance", false)) {
                return;
            }

            if (!sharedPreferences.getBoolean("newTimetable", false) && !sharedPreferences.getBoolean("newCourses", false)) {
                findViewById(classesNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectSemester(View view) {
        if (vtop.isInProgress) {
            return;
        }

        vtop.isInProgress = true;
        vtop.compress();

        Spinner selectSemester = download.findViewById(R.id.selectSemester);
        String semester = selectSemester.getSelectedItem().toString().toLowerCase();

        if (!sharedPreferences.getString("semester", "null").equals(semester)) {
            sharedPreferences.edit().putBoolean("newTimetable", true).apply();
            sharedPreferences.edit().putBoolean("newCourses", true).apply();

            sharedPreferences.edit().remove("newExams").apply();
            sharedPreferences.edit().remove("examsCount").apply();

            sharedPreferences.edit().remove("newMarks").apply();

            sharedPreferences.edit().remove("newGrades").apply();
            sharedPreferences.edit().remove("gradesCount").apply();

            sharedPreferences.edit().putString("semester", semester).apply();
            vtop.getSemesterID(semester);

            return;
        }

        switch (vtop.getLastDownload()) {
            case 1:
                vtop.downloadTimetable();
                break;
            case 2:
                vtop.downloadCourses();
                break;
            case 3:
                vtop.downloadProctor();
                break;
            case 4:
                vtop.downloadDeanHOD();
                break;
            case 5:
                vtop.downloadAttendance();
                break;
            case 6:
                vtop.downloadExams();
                break;
            case 7:
                vtop.downloadMarks();
                break;
            case 8:
                vtop.downloadGrades();
                break;
            case 9:
                vtop.downloadGradeHistory();
            case 10:
                vtop.downloadMessages();
                break;
            case 11:
                vtop.downloadProctorMessages();
                break;
            case 12:
                vtop.downloadSpotlight();
                break;
            case 13:
                vtop.downloadReceipts();
                break;
            default:
                vtop.getSemesterID(semester);
        }
    }

    public void cancelDownload(View view) {
        download.dismiss();
        download = null;
    }

    public void submitCaptcha(View view) {
        if (vtop.isVerifyingCaptcha) {
            return;
        }

        hideKeyboard();
        vtop.isVerifyingCaptcha = true;
        vtop.compress();

        EditText captchaView = download.findViewById(R.id.captcha);
        String captcha = captchaView.getText().toString();
        vtop.signIn("captchaCheck=" + captcha);
    }

    public void setAppearance(View view) {
        int selected = Integer.parseInt(view.getTag().toString());

        if (selected == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sharedPreferences.edit().putString("appearance", "light").apply();
            Toast.makeText(this, "\ud83e\udd28", Toast.LENGTH_LONG).show();
        } else if (selected == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPreferences.edit().putString("appearance", "dark").apply();
            Toast.makeText(this, "\ud83d\ude0e", Toast.LENGTH_LONG).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            sharedPreferences.edit().remove("appearance").apply();
            Toast.makeText(this, "\ud83d\ude32", Toast.LENGTH_LONG).show();
        }

        appearance.dismiss();
    }

    public void openNotifications(View view) {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

        startActivity(intent);
    }

    public void openPrivacy(View view) {
        startActivity(new Intent(HomeActivity.this, PrivacyActivity.class));
    }

    public void openAppearance(View view) {
        if (appearance != null) {
            return;
        }

        appearance = new Dialog(this);
        appearance.setContentView(R.layout.dialog_appearance);
        appearance.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String theme = sharedPreferences.getString("appearance", "system");
        if (theme.equals("light")) {
            RadioButton light = appearance.findViewById(R.id.light);
            light.setChecked(true);
        } else if (theme.equals("dark")) {
            RadioButton dark = appearance.findViewById(R.id.dark);
            dark.setChecked(true);
        } else {
            RadioButton system = appearance.findViewById(R.id.system);
            system.setChecked(true);
        }

        appearance.setOnDismissListener(dialog -> appearance = null);

        appearance.show();

        Window window = appearance.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public void openSignOut(View view) {
        if (signOut != null) {
            return;
        }

        signOut = new Dialog(this);
        signOut.setContentView(R.layout.dialog_signout);
        signOut.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        signOut.setOnDismissListener(dialog -> signOut = null);
        signOut.show();

        Window window = signOut.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public void cancelSignOut(View view) {
        signOut.dismiss();
        signOut = null;
    }

    public void openUpdate(View view) {
        String link = "http://vtopchennai.therealsuji.tk";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private void hideKeyboard() {
        if (download == null) {
            return;
        }

        View view = download.getCurrentFocus();

        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openDownload(View view) {
        if (download != null) {
            return;
        }

        download = new Dialog(this);
        download.setContentView(R.layout.dialog_download);
        download.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        download.setCanceledOnTouchOutside(false);
        download.setOnDismissListener(dialog -> {
            vtop.terminateDownload();
            download = null;
        });
        download.show();

        Window window = download.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        vtop.start(download);

        if (refresh != null) {
            refresh.dismiss();
        }
    }

    public void signOut(View view) {
        sharedPreferences.edit().remove("isSignedIn").apply();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();

        /*
            Clearing all Alarm manager tasks and deleting credentials
         */
        new Thread(() -> {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Intent notificationIntent = new Intent(context, NotificationReceiver.class);
            for (int j = 0; j < sharedPreferences.getInt("alarmCount", 0); ++j) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                alarmManager.cancel(pendingIntent);
            }

            /*
                Remove the encrypted credentials
             */
            encryptedSharedPreferences.edit().remove("username").apply();
            encryptedSharedPreferences.edit().remove("password").apply();

            /*
                Remove any non-encrypted credentials
             */
            sharedPreferences.edit().remove("username").apply();
            sharedPreferences.edit().remove("password").apply();

            /*
                Remove other data to prevent issues in the next sign in
             */
            sharedPreferences.edit().remove("semester").apply();
            sharedPreferences.edit().remove("newMarks").apply();
            sharedPreferences.edit().remove("filterByCourse").apply();
            sharedPreferences.edit().remove("newSpotlight").apply();
            sharedPreferences.edit().remove("examsCount").apply();
            sharedPreferences.edit().remove("gradesCount").apply();
            sharedPreferences.edit().remove("receiptsCount").apply();

            SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

            /*
                Dropping Marks
             */
            myDatabase.execSQL("DROP TABLE IF EXISTS marks");

            /*
                Dropping Spotlight
             */
            myDatabase.execSQL("DROP TABLE IF EXISTS spotlight");

            /*
                Dropping Messages
             */
            myDatabase.execSQL("DROP TABLE IF EXISTS messages");

            /*
                Dropping Proctor Messages
             */
            myDatabase.execSQL("DROP TABLE IF EXISTS proctor_messages");

            myDatabase.close();
        }).start();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

        try {
            MasterKey masterKey = new MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    "credentials",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        String name = sharedPreferences.getString("name", getString(R.string.name));
        String id = sharedPreferences.getString("id", getString(R.string.id));
        String credits = sharedPreferences.getString("credits", getString(R.string.credits) + ": 0");

        TextView nameView = findViewById(R.id.name);
        TextView idView = findViewById(R.id.id);
        TextView creditsView = findViewById(R.id.credits);

        nameView.setText(name);
        idView.setText(id);
        creditsView.setText(credits);

        /*
            This initialising is just to check if notification
            dots are in the layout in future
         */
        classesNotification = -1;
        timetableNotification = -1;
        messagesNotification = -1;
        coursesNotification = -1;

        academicsNotification = -1;
        examsNotification = -1;
        marksNotification = -1;
        gradesNotification = -1;
        spotlightNotification = -1;

        campusNotification = -1;
        proctorMessageNotification = -1;
        receiptsNotification = -1;

        final SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        final SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        context = this;
        final LinearLayout upcoming = findViewById(R.id.upcoming);
        final TextView noUpcoming = findViewById(R.id.no_upcoming);

        /*
            Initialising the VTOP WebView before hand to speed things up for the user
            because for some reason, initialising WebView's take a while
         */
        vtop = new VTOP(this);

        /*
            Deleting the old boolean value of newSpotlight
         */
        try {
            boolean newSpotlight = sharedPreferences.getBoolean("newSpotlight", false);
            if (newSpotlight) {
                sharedPreferences.edit().remove("newSpotlight").apply();
            }
        } catch (Exception ignored) {
        }

        new Thread(() -> {
            float pixelDensity = context.getResources().getDisplayMetrics().density;
            /*
                The outer block that holds the upcoming or ongoing & upcoming classes
             */
            final LinearLayout outerBlock = new LinearLayout(context);
            LinearLayout.LayoutParams outerBlockParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            outerBlock.setLayoutParams(outerBlockParams);
            outerBlock.setOrientation(LinearLayout.VERTICAL);
            outerBlock.setVisibility(View.GONE);

            Calendar cal = Calendar.getInstance();
            Calendar calFuture = Calendar.getInstance();
            calFuture.add(Calendar.MINUTE, 30);
            int dayCode = cal.get(Calendar.DAY_OF_WEEK);

            String day;

            if (dayCode == 1) {
                day = "sun";
            } else if (dayCode == 2) {
                day = "mon";
            } else if (dayCode == 3) {
                day = "tue";
            } else if (dayCode == 4) {
                day = "wed";
            } else if (dayCode == 5) {
                day = "thu";
            } else if (dayCode == 6) {
                day = "fri";
            } else {
                day = "sat";
            }

            SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_theory (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS timetable_lab (id INT(3) PRIMARY KEY, start_time VARCHAR, end_time VARCHAR, mon VARCHAR, tue VARCHAR, wed VARCHAR, thu VARCHAR, fri VARCHAR, sat VARCHAR, sun VARCHAR)");

            Cursor theory = myDatabase.rawQuery("SELECT start_time, end_time, " + day + " FROM timetable_theory", null);
            Cursor lab = myDatabase.rawQuery("SELECT start_time, end_time, " + day + " FROM timetable_lab", null);

            int startTheory = theory.getColumnIndex("start_time");
            int endTheory = theory.getColumnIndex("end_time");
            int dayTheory = theory.getColumnIndex(day);

            int startLab = lab.getColumnIndex("start_time");
            int endLab = lab.getColumnIndex("end_time");
            int dayLab = lab.getColumnIndex(day);

            theory.moveToFirst();
            lab.moveToFirst();

            boolean flag = false;

            CardGenerator myBlock = new CardGenerator(context, CardGenerator.CARD_HOME);

            for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
                if (terminateThread) {
                    return;
                }

                String startTimeTheory = theory.getString(startTheory);
                String endTimeTheory = theory.getString(endTheory);
                String startTimeLab = lab.getString(startLab);
                String endTimeLab = lab.getString(endLab);

                try {
                    Date currentTime = hour24.parse(hour24.format(cal.getTime()));
                    Date futureTime = hour24.parse(hour24.format(calFuture.getTime()));

                    if (currentTime != null && futureTime != null && !theory.getString(dayTheory).equals("null") && (futureTime.after(hour24.parse(startTimeTheory)) || futureTime.equals(hour24.parse(startTimeTheory))) && currentTime.before(hour24.parse(startTimeTheory))) {
                        String upcoming1 = getString(R.string.upcoming);
                        String course = theory.getString(dayTheory).split("-")[1].trim();

                        LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming1, course, true, true);

                        outerBlock.addView(headingBlock);  // Adding the headingBlock to the outer block

                        /*
                            Making a proper string of the timings
                         */
                        String timings = startTimeTheory + " - " + endTimeTheory;
                        if (!DateFormat.is24HourFormat(context)) {
                            try {
                                Date startTime = hour24.parse(startTimeTheory);
                                Date endTime = hour24.parse(endTimeTheory);
                                if (startTime != null && endTime != null) {
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        String type = getString(R.string.theory);

                        LinearLayout block = myBlock.generateInnerBlock(timings, type, true, false);

                        outerBlock.addView(block);  // Adding the block to the outer block

                        flag = true;    // Flag is set so terminate the loop when the time comes
                    }

                    if (currentTime != null && futureTime != null && !lab.getString(dayLab).equals("null") && (futureTime.after(hour24.parse(startTimeLab)) || futureTime.equals(hour24.parse(startTimeLab))) && currentTime.before(hour24.parse(startTimeLab))) {
                        String upcoming1 = getString(R.string.upcoming);
                        String course = lab.getString(dayLab).split("-")[1].trim();

                        LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming1, course, true, true);

                        outerBlock.addView(headingBlock);  // Adding the headingBlock to the outer block

                        /*
                            Making a proper string of the timings
                         */
                        String timings = startTimeLab + " - " + endTimeLab;
                        if (!DateFormat.is24HourFormat(context)) {
                            try {
                                Date startTime = hour24.parse(startTimeLab);
                                Date endTime = hour24.parse(endTimeLab);
                                if (startTime != null && endTime != null) {
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        String type = getString(R.string.lab);

                        LinearLayout block = myBlock.generateInnerBlock(timings, type, true, false);

                        outerBlock.addView(block);  // Adding the block to the outer block

                        flag = true;    // Flag is set so terminate the loop when the time comes
                    }

                    if (flag) {
                        break;  // If either Upcoming or Ongoing & Upcoming classes are available, the loop can terminate
                    }

                    if (currentTime != null && !theory.getString(dayTheory).equals("null") && (currentTime.after(hour24.parse(startTimeTheory)) || currentTime.equals(hour24.parse(startTimeTheory))) && currentTime.before(hour24.parse(endTimeTheory))) {
                        String upcoming1 = getString(R.string.ongoing);
                        String course = theory.getString(dayTheory).split("-")[1].trim();

                        LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming1, course, true, true);

                        outerBlock.addView(headingBlock);  // Adding the headingBlock to the outer block

                        /*
                            Making a proper string of the timings
                         */
                        String timings = startTimeTheory + " - " + endTimeTheory;
                        if (!DateFormat.is24HourFormat(context)) {
                            try {
                                Date startTime = hour24.parse(startTimeTheory);
                                Date endTime = hour24.parse(endTimeTheory);
                                if (startTime != null && endTime != null) {
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        String type = getString(R.string.theory);

                        LinearLayout block = myBlock.generateInnerBlock(timings, type, true, false);

                        outerBlock.addView(block);  // Adding the block to the outer block

                        flag = true;    // Flag is set so terminate the loop when the time comes
                    }

                    if (currentTime != null && !lab.getString(dayLab).equals("null") && (currentTime.after(hour24.parse(startTimeLab)) || currentTime.equals(hour24.parse(startTimeLab))) && currentTime.before(hour24.parse(endTimeLab))) {
                        String upcoming1 = getString(R.string.ongoing);
                        String course = lab.getString(dayLab).split("-")[1].trim();

                        LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming1, course, true, true);

                        outerBlock.addView(headingBlock);  // Adding the headingBlock to the outer block

                        /*
                            Making a proper string of the timings
                         */
                        String timings = startTimeLab + " - " + endTimeLab;
                        if (!DateFormat.is24HourFormat(context)) {
                            try {
                                Date startTime = hour24.parse(startTimeLab);
                                Date endTime = hour24.parse(endTimeLab);
                                if (startTime != null && endTime != null) {
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        String type = getString(R.string.lab);

                        LinearLayout block = myBlock.generateInnerBlock(timings, type, true, false);

                        outerBlock.addView(block);  // Adding the block to the outer block

                        flag = true;    // Flag is set so terminate the loop when the time comes
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (outerBlock.getChildCount() > 0) {
                runOnUiThread(() -> {
                    compress(noUpcoming);
                    upcoming.addView(outerBlock);
                    expand(outerBlock);
                });
            }

            theory.close();
            lab.close();
            myDatabase.close();

            /*
                From this point forward, it is to check for unread data
                to display default or urgent notification dots
             */
            NotificationDotGenerator myNotification = new NotificationDotGenerator(context);

            boolean classesFlag = false;

            if (sharedPreferences.getBoolean("newTimetable", false)) {
                classesFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (135 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                timetableNotification = View.generateViewId();
                notification.setId(timetableNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (sharedPreferences.getBoolean("newMessages", false)) {
                classesFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (425 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                messagesNotification = View.generateViewId();
                notification.setId(messagesNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (sharedPreferences.getBoolean("newCourses", false)) {
                classesFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                coursesNotification = View.generateViewId();
                notification.setId(coursesNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (sharedPreferences.getBoolean("failedAttendance", false)) {
                classesFlag = false;
                final ImageView notification = myNotification.generateNotificationDot((int) (280 * pixelDensity), NotificationDotGenerator.NOTIFICATION_URGENT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });

                /*
                    The classes category notification in red
                 */
                final ImageView notificationClasses = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_URGENT);
                notificationClasses.setPadding((int) (10 * pixelDensity), (int) (20 * pixelDensity), 0, 0);
                classesNotification = View.generateViewId();
                notificationClasses.setId(classesNotification);

                runOnUiThread(() -> {
                    ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationClasses);
                    ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationClasses.getLayoutParams();
                    notificationParams.leftToRight = R.id.classesHeading;
                    notificationParams.topToBottom = R.id.upcoming;
                    notificationParams.bottomToTop = R.id.classes;
                    notificationClasses.setLayoutParams(notificationParams);
                    notificationClasses.animate().scaleX(1).scaleY(1);
                });
            }

            /*
                The classes category notification (except if there's failed attendance)
             */
            if (classesFlag) {
                final ImageView notificationClasses = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notificationClasses.setPadding((int) (10 * pixelDensity), (int) (20 * pixelDensity), 0, 0);
                classesNotification = View.generateViewId();
                notificationClasses.setId(classesNotification);

                runOnUiThread(() -> {
                    ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationClasses);
                    ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationClasses.getLayoutParams();
                    notificationParams.leftToRight = R.id.classesHeading;
                    notificationParams.topToBottom = R.id.upcoming;
                    notificationParams.bottomToTop = R.id.classes;
                    notificationClasses.setLayoutParams(notificationParams);
                    notificationClasses.animate().scaleX(1).scaleY(1);
                });
            }


            boolean academicsFlag = false;

            if (sharedPreferences.getBoolean("newExams", false)) {
                academicsFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (135 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                examsNotification = View.generateViewId();
                notification.setId(examsNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (!sharedPreferences.getString("newMarks", "{}").equals("{}")) {
                academicsFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (280 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                marksNotification = View.generateViewId();
                notification.setId(marksNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (sharedPreferences.getBoolean("newGrades", false)) {
                academicsFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (425 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                gradesNotification = View.generateViewId();
                notification.setId(gradesNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (!sharedPreferences.getString("newSpotlight", "{}").equals("{}")) {
                academicsFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                spotlightNotification = View.generateViewId();
                notification.setId(spotlightNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            /*
                The academics category notification
             */
            if (academicsFlag) {
                final ImageView notificationAcademics = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notificationAcademics.setPadding((int) (10 * pixelDensity), (int) (10 * pixelDensity), 0, 0);
                academicsNotification = View.generateViewId();
                notificationAcademics.setId(academicsNotification);

                runOnUiThread(() -> {
                    ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationAcademics);
                    ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationAcademics.getLayoutParams();
                    notificationParams.leftToRight = R.id.academicsHeading;
                    notificationParams.topToBottom = R.id.classes;
                    notificationParams.bottomToTop = R.id.academics;
                    notificationAcademics.setLayoutParams(notificationParams);
                    notificationAcademics.animate().scaleX(1).scaleY(1);
                });
            }


            boolean campusFlag = false;

            if (sharedPreferences.getBoolean("newProctorMessages", false)) {
                campusFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (425 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                proctorMessageNotification = View.generateViewId();
                notification.setId(proctorMessageNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.campusLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            if (sharedPreferences.getBoolean("duePayments", false)) {
                campusFlag = false;
                final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_URGENT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                receiptsNotification = View.generateViewId();
                notification.setId(receiptsNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.campusLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });

                /*
                    The campus category notification in red
                 */
                final ImageView notificationCampus = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_URGENT);
                notificationCampus.setPadding((int) (10 * pixelDensity), (int) (10 * pixelDensity), 0, 0);
                campusNotification = View.generateViewId();
                notificationCampus.setId(campusNotification);

                runOnUiThread(() -> {
                    ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationCampus);
                    ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationCampus.getLayoutParams();
                    notificationParams.leftToRight = R.id.campusHeading;
                    notificationParams.topToBottom = R.id.academics;
                    notificationParams.bottomToTop = R.id.campus;
                    notificationCampus.setLayoutParams(notificationParams);
                    notificationCampus.animate().scaleX(1).scaleY(1);
                });
            } else if (sharedPreferences.getBoolean("newReceipts", false)) {
                campusFlag = true;
                final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                receiptsNotification = View.generateViewId();
                notification.setId(receiptsNotification);

                runOnUiThread(() -> {
                    ((RelativeLayout) findViewById(R.id.campusLayout)).addView(notification);
                    notification.animate().scaleX(1).scaleY(1);
                });
            }

            /*
                The campus category notification
             */
            if (campusFlag) {
                final ImageView notificationCampus = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_DEFAULT);
                notificationCampus.setPadding((int) (10 * pixelDensity), (int) (10 * pixelDensity), 0, 0);
                campusNotification = View.generateViewId();
                notificationCampus.setId(campusNotification);

                runOnUiThread(() -> {
                    ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationCampus);
                    ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationCampus.getLayoutParams();
                    notificationParams.leftToRight = R.id.campusHeading;
                    notificationParams.topToBottom = R.id.academics;
                    notificationParams.bottomToTop = R.id.campus;
                    notificationCampus.setLayoutParams(notificationParams);
                    notificationCampus.animate().scaleX(1).scaleY(1);
                });
            }

            /*
                Checking if the report bug button should be visible
             */
            if (ErrorHandler.isPreRelease) {
                LinearLayout reportBug = new LinearLayout(context);
                LinearLayout.LayoutParams reportBugParams = new LinearLayout.LayoutParams(
                        (int) (125 * pixelDensity),
                        (int) (125 * pixelDensity)
                );
                reportBugParams.setMarginStart((int) (10 * pixelDensity));
                reportBugParams.setMargins(0, (int) (10 * pixelDensity), 0, (int) (10 * pixelDensity));
                reportBugParams.setMarginEnd((int) (10 * pixelDensity));
                reportBug.setLayoutParams(reportBugParams);
                reportBug.setBackground(ContextCompat.getDrawable(context, R.drawable.button_card));
                reportBug.setClickable(true);
                reportBug.setFocusable(true);
                reportBug.setGravity(Gravity.CENTER_VERTICAL);
                reportBug.setOrientation(LinearLayout.VERTICAL);
                reportBug.setOnClickListener(this::openReportBug);

                StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                reportBug.setStateListAnimator(elevation);

                ImageView icon = new ImageView(context);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                icon.setLayoutParams(iconParams);
                icon.setContentDescription(getString(R.string.report_bug_description));
                icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_report_bug));
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(getColor(R.color.colorPrimary)));

                TextView title = new TextView(context);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                textParams.setMargins(0, (int) (2 * pixelDensity), 0, 0);
                title.setLayoutParams(textParams);
                title.setText(R.string.report_bug);
                title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                title.setTextColor(getColor(R.color.colorPrimary));
                title.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                reportBug.addView(icon);
                reportBug.addView(title);

                runOnUiThread(() -> {
                    LinearLayout appButtons = findViewById(R.id.appButtons);
                    appButtons.addView(reportBug, appButtons.getChildCount() - 1);
                });
            } else {
                myDatabase.execSQL("DROP TABLE IF EXISTS error_logs");
            }
        }).start();

        /*
            Displaying the last time data was refreshed
         */
        String refreshedDate = sharedPreferences.getString("refreshedDate", getString(R.string.refreshed_unavailable));
        String refreshedTime = sharedPreferences.getString("refreshedTime", getString(R.string.refreshed_unavailable));
        TextView refreshedView = findViewById(R.id.refreshed);
        if (refreshedDate.equals(getString(R.string.refreshed_unavailable))) {
            refreshedView.setText(getString(R.string.refreshed_unavailable));
        } else {
            String refreshed = getString(R.string.refreshed) + ": " + refreshedDate + ", ";
            if (!DateFormat.is24HourFormat(this)) {
                try {
                    Date time = hour24.parse(refreshedTime);
                    if (time != null) {
                        refreshedTime = hour12.format(time);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            refreshed += refreshedTime;
            refreshedView.setText(refreshed);
        }

        /*
            This little piece of code is some necessary steps to check if data has been refreshed recently
         */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.ENGLISH);
        Date today = null, lastRefreshed = null, futureRefresh = null;
        try {
            today = dateFormat.parse(dateFormat.format(c.getTime()));
            lastRefreshed = dateFormat.parse(refreshedDate);
            assert lastRefreshed != null;
            c.setTime(lastRefreshed);
            c.add(Calendar.DATE, 6);
            futureRefresh = dateFormat.parse(dateFormat.format(c.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*
            Locally check for a new version (The actually checking is done in the SplashScreenActivity)
         */
        int versionCode = BuildConfig.VERSION_CODE;
        int latestVersion = sharedPreferences.getInt("latest", versionCode);

        if (versionCode < latestVersion) {
            Dialog update = new Dialog(this);
            update.setContentView(R.layout.dialog_update);
            update.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            update.show();

            Window window = update.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else if (today != null && futureRefresh != null && (today.after(futureRefresh) || lastRefreshed.after(today))) {  // Next, check if data has been refreshed recently (1 week)
            refresh = new Dialog(this);
            refresh.setContentView(R.layout.dialog_refresh);
            refresh.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            refresh.show();

            Window window = refresh.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        TextView myLink = findViewById(R.id.builtBy);
        myLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (marksNotification != -1 && academicsNotification != -1 && !sharedPreferences.getString("newMarks", "{}").equals("{}")) {
            try {
                findViewById(marksNotification).animate().scaleX(1).scaleY(1);

                ImageView academicsNotificationView = findViewById(academicsNotification);
                if (academicsNotificationView.getScaleX() == 0) {
                    academicsNotificationView.animate().scaleX(1).scaleY(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (spotlightNotification != -1 && academicsNotification != -1 && !sharedPreferences.getString("newSpotlight", "{}").equals("{}")) {
            try {
                findViewById(spotlightNotification).animate().scaleX(1).scaleY(1);

                ImageView academicsNotificationView = findViewById(academicsNotification);
                if (academicsNotificationView.getScaleX() == 0) {
                    academicsNotificationView.animate().scaleX(1).scaleY(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;

        if (vtop != null) {
            vtop.terminateDownload();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final ScrollView mScrollView = findViewById(R.id.home_layout);
        final int[] position = savedInstanceState.getIntArray("SCROLL_POSITION");
        if (position != null) {
            mScrollView.post(() -> mScrollView.scrollTo(position[0], position[1]));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ScrollView mScrollView = findViewById(R.id.home_layout);
        outState.putIntArray("SCROLL_POSITION", new int[]{mScrollView.getScrollX(), mScrollView.getScrollY()});
    }
}
