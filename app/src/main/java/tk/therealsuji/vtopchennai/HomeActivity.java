package tk.therealsuji.vtopchennai;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.View;
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
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    int classesNotification, timetableNotification, messagesNotification, facultyNotification;
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

            if (!sharedPreferences.getBoolean("newMessages", false) && !sharedPreferences.getBoolean("newFaculty", false)) {
                findViewById(classesNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openAttendance(View view) {
        startActivity(new Intent(this, AttendanceActivity.class));
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

            if (!sharedPreferences.getBoolean("newTimetable", false) && !sharedPreferences.getBoolean("newFaculty", false)) {
                findViewById(classesNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openFaculty(View view) {
        startActivity(new Intent(this, FacultyActivity.class));

        if (facultyNotification == -1) {
            return;
        }

        try {
            findViewById(facultyNotification).animate().scaleX(0).scaleY(0);

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

            if (sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newGrades", false) && !sharedPreferences.getBoolean("newSpotlight", false)) {
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

            if (!sharedPreferences.getBoolean("newExams", false) && !sharedPreferences.getBoolean("newGrades", false) && !sharedPreferences.getBoolean("newSpotlight", false)) {
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

            if (!sharedPreferences.getBoolean("newExams", false) && sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newSpotlight", false)) {
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

            if (!sharedPreferences.getBoolean("newReceipts", false)) {
                findViewById(campusNotification).animate().scaleX(0).scaleY(0);
            }
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
            findViewById(receiptsNotification).animate().scaleX(0).scaleY(0);

            if (sharedPreferences.getBoolean("duePayments", false)) {
                return;
            }

            if (!sharedPreferences.getBoolean("newProctorMessages", false)) {
                findViewById(campusNotification).animate().scaleX(0).scaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void openDownload(View view) {
        if (download != null) {
            download.dismiss();
        }

        download = new Dialog(this);
        download.setContentView(R.layout.dialog_download);
        download.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        download.setCanceledOnTouchOutside(false);

//        This part was commented because in some rare cases, the algorithm gets stuck
//        at loading and the only way to come out of it would be to close the application.
//        The statement below disables the back button when dialog_dialog is opened to
//        prevent it from closing during a download.

//        download.setCancelable(false);

        download.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                vtop.terminateDownload();
            }
        });

        download.show();

        vtop.start(download);

        if (refresh != null) {
            refresh.dismiss();
        }
    }

    public void submitCaptcha(View view) {
        hideKeyboard();
        vtop.hideLayouts();

        String username = encryptedSharedPreferences.getString("username", null);
        String password = encryptedSharedPreferences.getString("password", null);

        /*
            If the credentials aren't encrypted
         */
        if (username == null) {
            /*
                Get the non-encrypted credentials
             */
            username = sharedPreferences.getString("username", null);
            password = sharedPreferences.getString("password", null);

            /*
                Encrypt them
             */
            encryptedSharedPreferences.edit().putString("username", username).apply();
            encryptedSharedPreferences.edit().putString("password", password).apply();

            /*
                Remove the non-encrypted credentials
             */
            sharedPreferences.edit().remove("username").apply();
            sharedPreferences.edit().remove("password").apply();
        }

        EditText captchaView = download.findViewById(R.id.captcha);
        String captcha = captchaView.getText().toString();
        vtop.signIn(username, password, captcha);
    }

    public void selectSemester(View view) {
        vtop.hideLayouts();

        Spinner selectSemester = download.findViewById(R.id.selectSemester);
        String semester = selectSemester.getSelectedItem().toString().toLowerCase();

        if (!sharedPreferences.getString("semester", "null").equals(semester)) {
            sharedPreferences.edit().putBoolean("newTimetable", true).apply();
            sharedPreferences.edit().putBoolean("newFaculty", true).apply();
            sharedPreferences.edit().remove("newExams").apply();
            sharedPreferences.edit().remove("newMarks").apply();
            sharedPreferences.edit().remove("newGrades").apply();

            sharedPreferences.edit().putString("semester", semester).apply();
            vtop.getSemesterID(semester);
            return;
        }

        int lastDownload = vtop.getLastDownload();
        switch (lastDownload) {
            case 1:
                vtop.downloadTimetable();
                break;
            case 2:
                vtop.downloadFaculty();
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

    public void openAppearance(View view) {
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

        appearance.show();
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

    public void openSignOut(View view) {
        if (signOut != null) {
            signOut.dismiss();
        }

        signOut = new Dialog(this);
        signOut.setContentView(R.layout.dialog_signout);
        signOut.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        signOut.show();
    }

    public void signOut(View view) {
        sharedPreferences.edit().remove("isSignedIn").apply();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();

        /*
            Clearing all Alarm manager tasks and deleting credentials
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                for (int j = 0; j < sharedPreferences.getInt("alarmCount", 0); ++j) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, j, notificationIntent, 0);
                    alarmManager.cancel(pendingIntent);
                }

                /*
                    Remove any non-encrypted credentials
                 */
                sharedPreferences.edit().remove("username").apply();
                sharedPreferences.edit().remove("password").apply();

                /*
                    Remove the encrypted credentials
                 */
                encryptedSharedPreferences.edit().remove("username").apply();
                encryptedSharedPreferences.edit().remove("password").apply();

                /*
                    Remove other data to prevent issues in the next sign in
                 */
                sharedPreferences.edit().remove("semester").apply();
                sharedPreferences.edit().remove("newMarks").apply();
                sharedPreferences.edit().remove("examsCount").apply();
                sharedPreferences.edit().remove("receiptsCount").apply();

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                /*
                    Dropping Messages
                 */
                myDatabase.execSQL("DROP TABLE messages");

                /*
                    Dropping Marks
                 */
                myDatabase.execSQL("DROP TABLE marks");

                /*
                    Dropping Spotlight
                 */
                myDatabase.execSQL("DROP TABLE IF EXISTS spotlight");

                /*
                    Dropping Proctor Messages
                 */
                myDatabase.execSQL("DROP TABLE IF EXISTS proctor_messages");

                myDatabase.close();
            }
        }).start();
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

    public static void expand(final View view) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        ValueAnimator anim = ValueAnimator.ofInt(0, targetHeight);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                layoutParams.height = (int) (targetHeight * animation.getAnimatedFraction());
                view.setLayoutParams(layoutParams);
            }
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

    public static void compress(final View view) {
        final int viewHeight = view.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(viewHeight, 0);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(200);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
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
        String credits = sharedPreferences.getString("credits", getString(R.string.credits));

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
        facultyNotification = -1;

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

        new Thread(new Runnable() {
            @Override
            public void run() {
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

                        if (currentTime != null && futureTime != null && (futureTime.after(hour24.parse(startTimeTheory)) || futureTime.equals(hour24.parse(startTimeTheory))) && currentTime.before(hour24.parse(startTimeTheory)) && !theory.getString(dayTheory).equals("null")) {
                            String upcoming = getString(R.string.upcoming);
                            String course = theory.getString(dayTheory).split("-")[1].trim();

                            LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming, course, true, true);

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

                        if (currentTime != null && futureTime != null && (futureTime.after(hour24.parse(startTimeLab)) || futureTime.equals(hour24.parse(startTimeLab))) && currentTime.before(hour24.parse(startTimeLab)) && !lab.getString(dayLab).equals("null")) {
                            String upcoming = getString(R.string.upcoming);
                            String course = lab.getString(dayLab).split("-")[1].trim();

                            LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming, course, true, true);

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

                        if (currentTime != null && (currentTime.after(hour24.parse(startTimeTheory)) || currentTime.equals(hour24.parse(startTimeTheory))) && (currentTime.before(hour24.parse(endTimeTheory)) || currentTime.equals(hour24.parse(endTimeTheory))) && !theory.getString(dayTheory).equals("null")) {
                            String upcoming = getString(R.string.ongoing);
                            String course = theory.getString(dayTheory).split("-")[1].trim();

                            LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming, course, true, true);

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

                        if (currentTime != null && (currentTime.after(hour24.parse(startTimeLab)) || currentTime.equals(hour24.parse(startTimeLab))) && (currentTime.before(hour24.parse(endTimeLab)) || currentTime.equals(hour24.parse(endTimeLab))) && !lab.getString(dayLab).equals("null")) {
                            String upcoming = getString(R.string.ongoing);
                            String course = lab.getString(dayLab).split("-")[1].trim();

                            LinearLayout headingBlock = myBlock.generateInnerBlock(upcoming, course, true, true);

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            compress(noUpcoming);
                            upcoming.addView(outerBlock);
                            expand(outerBlock);
                        }
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (sharedPreferences.getBoolean("newMessages", false)) {
                    classesFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (425 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    messagesNotification = View.generateViewId();
                    notification.setId(messagesNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (sharedPreferences.getBoolean("newFaculty", false)) {
                    classesFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    facultyNotification = View.generateViewId();
                    notification.setId(facultyNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (sharedPreferences.getBoolean("failedAttendance", false)) {
                    classesFlag = false;
                    final ImageView notification = myNotification.generateNotificationDot((int) (280 * pixelDensity), NotificationDotGenerator.NOTIFICATION_URGENT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.classesLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });

                    /*
                        The classes category notification in red
                     */
                    final ImageView notificationClasses = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_URGENT);
                    notificationClasses.setPadding((int) (10 * pixelDensity), (int) (20 * pixelDensity), 0, 0);
                    classesNotification = View.generateViewId();
                    notificationClasses.setId(classesNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationClasses);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationClasses.getLayoutParams();
                            notificationParams.leftToRight = R.id.classesHeading;
                            notificationParams.topToBottom = R.id.upcoming;
                            notificationParams.bottomToTop = R.id.classes;
                            notificationClasses.setLayoutParams(notificationParams);
                            notificationClasses.animate().scaleX(1).scaleY(1);
                        }
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationClasses);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationClasses.getLayoutParams();
                            notificationParams.leftToRight = R.id.classesHeading;
                            notificationParams.topToBottom = R.id.upcoming;
                            notificationParams.bottomToTop = R.id.classes;
                            notificationClasses.setLayoutParams(notificationParams);
                            notificationClasses.animate().scaleX(1).scaleY(1);
                        }
                    });
                }


                boolean academicsFlag = false;

                if (sharedPreferences.getBoolean("newExams", false)) {
                    academicsFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (135 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    examsNotification = View.generateViewId();
                    notification.setId(examsNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (!sharedPreferences.getString("newMarks", "{}").equals("{}")) {
                    academicsFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (280 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    marksNotification = View.generateViewId();
                    notification.setId(marksNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (sharedPreferences.getBoolean("newGrades", false)) {
                    academicsFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (425 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    gradesNotification = View.generateViewId();
                    notification.setId(gradesNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (sharedPreferences.getBoolean("newSpotlight", false)) {
                    academicsFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    spotlightNotification = View.generateViewId();
                    notification.setId(spotlightNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.academicsLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationAcademics);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationAcademics.getLayoutParams();
                            notificationParams.leftToRight = R.id.academicsHeading;
                            notificationParams.topToBottom = R.id.classes;
                            notificationParams.bottomToTop = R.id.academics;
                            notificationAcademics.setLayoutParams(notificationParams);
                            notificationAcademics.animate().scaleX(1).scaleY(1);
                        }
                    });
                }


                boolean campusFlag = false;

                if (sharedPreferences.getBoolean("newProctorMessages", false)) {
                    campusFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (425 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    proctorMessageNotification = View.generateViewId();
                    notification.setId(proctorMessageNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.campusLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                if (sharedPreferences.getBoolean("duePayments", false)) {
                    campusFlag = false;
                    final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_URGENT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    receiptsNotification = View.generateViewId();
                    notification.setId(receiptsNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.campusLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });

                    /*
                        The campus category notification in red
                     */
                    final ImageView notificationCampus = myNotification.generateNotificationDot(0, NotificationDotGenerator.NOTIFICATION_URGENT);
                    notificationCampus.setPadding((int) (10 * pixelDensity), (int) (10 * pixelDensity), 0, 0);
                    campusNotification = View.generateViewId();
                    notificationCampus.setId(campusNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationCampus);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationCampus.getLayoutParams();
                            notificationParams.leftToRight = R.id.campusHeading;
                            notificationParams.topToBottom = R.id.academics;
                            notificationParams.bottomToTop = R.id.campus;
                            notificationCampus.setLayoutParams(notificationParams);
                            notificationCampus.animate().scaleX(1).scaleY(1);
                        }
                    });
                } else if (sharedPreferences.getBoolean("newReceipts", false)) {
                    campusFlag = true;
                    final ImageView notification = myNotification.generateNotificationDot((int) (570 * pixelDensity), NotificationDotGenerator.NOTIFICATION_DEFAULT);
                    notification.setPadding(0, (int) (10 * pixelDensity), 0, 0);
                    receiptsNotification = View.generateViewId();
                    notification.setId(receiptsNotification);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RelativeLayout) findViewById(R.id.campusLayout)).addView(notification);
                            notification.animate().scaleX(1).scaleY(1);
                        }
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationCampus);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationCampus.getLayoutParams();
                            notificationParams.leftToRight = R.id.campusHeading;
                            notificationParams.topToBottom = R.id.academics;
                            notificationParams.bottomToTop = R.id.campus;
                            notificationCampus.setLayoutParams(notificationParams);
                            notificationCampus.animate().scaleX(1).scaleY(1);
                        }
                    });
                }
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
        } else if (today != null && futureRefresh != null && (today.after(futureRefresh) || lastRefreshed.after(today))) {  // Next, check if data has been refreshed recently (1 week)
            refresh = new Dialog(this);
            refresh.setContentView(R.layout.dialog_refresh);
            refresh.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            refresh.show();
        }

        TextView myLink = findViewById(R.id.builtBy);
        myLink.setMovementMethod(LinkMovementMethod.getInstance());

        /*
            Initialising the VTOP WebView before hand to speed things up for the user
            because for some reason, initialising WebView's take a second
         */
        vtop = new VTOP(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (marksNotification == -1 || academicsNotification == -1) {
            return;
        }

        if (!sharedPreferences.getString("newMarks", "{}").equals("{}")) {
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
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ScrollView mScrollView = findViewById(R.id.home_layout);
        outState.putIntArray("SCROLL_POSITION", new int[]{mScrollView.getScrollX(), mScrollView.getScrollY()});
    }
}