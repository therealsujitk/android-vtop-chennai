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
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
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
    int classesNotification, timetableNotification, messagesNotification, facultyNotification;
    int academicsNotification, examsNotification, marksNotification, gradesNotification, spotlightNotification;
    int campusNotification, proctorMessageNotification, receiptsNotification;
    SharedPreferences sharedPreferences, encryptedSharedPreferences;
    Dialog download, appearance, signOut;
    Context context;
    VTOP vtop;

    /*
        The following functions are to open the activities in the "Classes" category
     */

    public void openTimetable(View view) {
        startActivity(new Intent(HomeActivity.this, TimetableActivity.class));

        if (timetableNotification == -1) {
            return;
        }

        findViewById(timetableNotification).animate().scaleX(0).scaleY(0);

        if (sharedPreferences.getBoolean("failedAttendance", false)) {
            return;
        }

        if (!sharedPreferences.getBoolean("newMessages", false) && !sharedPreferences.getBoolean("newFaculty", false)) {
            findViewById(classesNotification).animate().scaleX(0).scaleY(0);
        }
    }

    public void openAttendance(View view) {
        startActivity(new Intent(HomeActivity.this, AttendanceActivity.class));
    }

    public void openMessages(View view) {
        startActivity(new Intent(HomeActivity.this, MessagesActivity.class));

        if (messagesNotification == -1) {
            return;
        }

        findViewById(messagesNotification).animate().scaleX(0).scaleY(0);

        if (sharedPreferences.getBoolean("failedAttendance", false)) {
            return;
        }

        if (!sharedPreferences.getBoolean("newTimetable", false) && !sharedPreferences.getBoolean("newFaculty", false)) {
            findViewById(classesNotification).animate().scaleX(0).scaleY(0);
        }
    }

    public void openFaculty(View view) {
        startActivity(new Intent(HomeActivity.this, FacultyActivity.class));

        if (facultyNotification == -1) {
            return;
        }

        findViewById(facultyNotification).animate().scaleX(0).scaleY(0);

        if (sharedPreferences.getBoolean("failedAttendance", false)) {
            return;
        }

        if (!sharedPreferences.getBoolean("newTimetable", false) && !sharedPreferences.getBoolean("newMessages", false)) {
            findViewById(classesNotification).animate().scaleX(0).scaleY(0);
        }
    }

    /*
        The following functions are to open the activities in the "Academics" category
     */

    public void openExams(View view) {
        startActivity(new Intent(HomeActivity.this, ExamsActivity.class));

        if (examsNotification == -1) {
            return;
        }

        findViewById(examsNotification).animate().scaleX(0).scaleY(0);

        if (sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newGrades", false) && !sharedPreferences.getBoolean("newSpotlight", false)) {
            findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
        }
    }

    public void openMarks(View view) {
        startActivity(new Intent(HomeActivity.this, MarksActivity.class));

        if (marksNotification == -1) {
            return;
        }

        findViewById(marksNotification).animate().scaleX(0).scaleY(0);

        if (!sharedPreferences.getBoolean("newExams", false) && !sharedPreferences.getBoolean("newGrades", false) && !sharedPreferences.getBoolean("newSpotlight", false)) {
            findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
        }
    }

    public void openGrades(View view) {
        startActivity(new Intent(HomeActivity.this, GradesActivity.class));

        if (gradesNotification == -1) {
            return;
        }

        findViewById(gradesNotification).animate().scaleX(0).scaleY(0);

        if (!sharedPreferences.getBoolean("newExams", false) && sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newSpotlight", false)) {
            findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
        }
    }

    public void openSpotlight(View view) {
        startActivity(new Intent(HomeActivity.this, SpotlightActivity.class));

        if (spotlightNotification == -1) {
            return;
        }

        findViewById(spotlightNotification).animate().scaleX(0).scaleY(0);

        if (!sharedPreferences.getBoolean("newExams", false) && sharedPreferences.getString("newMarks", "{}").equals("{}") && !sharedPreferences.getBoolean("newGrades", false)) {
            findViewById(academicsNotification).animate().scaleX(0).scaleY(0);
        }
    }

    /*
        The following functions are to open the activities in the "Campus" category
     */

    public void openDirections(View view) {
        startActivity(new Intent(HomeActivity.this, DirectionsActivity.class));
    }

    public void openStaff(View view) {
        startActivity(new Intent(HomeActivity.this, StaffActivity.class));
    }

    public void openProctorMessages(View view) {
        startActivity(new Intent(HomeActivity.this, ProctorMessagesActivity.class));

        if (proctorMessageNotification == -1) {
            return;
        }

        findViewById(proctorMessageNotification).animate().scaleX(0).scaleY(0);

        if (sharedPreferences.getBoolean("duePayments", false)) {
            return;
        }

        if (!sharedPreferences.getBoolean("newReceipts", false)) {
            findViewById(campusNotification).animate().scaleX(0).scaleY(0);
        }
    }

    public void openReceipts(View view) {
        startActivity(new Intent(HomeActivity.this, ReceiptsActivity.class));

        if (receiptsNotification == -1) {
            return;
        }

        findViewById(receiptsNotification).animate().scaleX(0).scaleY(0);

        if (sharedPreferences.getBoolean("duePayments", false)) {
            return;
        }

        if (!sharedPreferences.getBoolean("newProctorMessages", false)) {
            findViewById(campusNotification).animate().scaleX(0).scaleY(0);
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

        download.show();

        vtop = new VTOP(this, download);
    }

    public void submitCaptcha(View view) {
        LinearLayout captchaLayout = download.findViewById(R.id.captchaLayout);
        ProgressBar loading = download.findViewById(R.id.loading);
        VTOP.compress(captchaLayout);
        loading.animate().alpha(1);

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
        LinearLayout semesterLayout = download.findViewById(R.id.semesterLayout);
        ProgressBar loading = download.findViewById(R.id.loading);
        VTOP.compress(semesterLayout);
        loading.animate().alpha(1);

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
                vtop.downloadMessages();
                break;
            case 10:
                vtop.downloadProctorMessages();
                break;
            case 11:
                vtop.downloadSpotlight();
                break;
            case 12:
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
                sharedPreferences.edit().remove("newTimetable").apply();
                sharedPreferences.edit().remove("failedAttendance").apply();
                sharedPreferences.edit().remove("newMessages").apply();
                sharedPreferences.edit().remove("newFaculty").apply();
                sharedPreferences.edit().remove("newExams").apply();
                sharedPreferences.edit().remove("examsCount").apply();
                sharedPreferences.edit().remove("receiptsCount").apply();

                // Something has to be done about the marks activity and the spotlight activity
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

        final float pixelDensity = this.getResources().getDisplayMetrics().density;
        final SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        final SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        context = this;
        final LinearLayout upcoming = findViewById(R.id.upcoming);
        final TextView noUpcoming = findViewById(R.id.no_upcoming);

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*
                    The outer OUTER block
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

                for (int i = 0; i < theory.getCount() && i < lab.getCount(); ++i, theory.moveToNext(), lab.moveToNext()) {
                    String startTimeTheory = theory.getString(startTheory);
                    String endTimeTheory = theory.getString(endTheory);
                    String startTimeLab = lab.getString(startLab);
                    String endTimeLab = lab.getString(endLab);

                    try {
                        Date currentTime = hour24.parse(hour24.format(cal.getTime()));
                        Date futureTime = hour24.parse(hour24.format(calFuture.getTime()));

                        assert currentTime != null;
                        assert futureTime != null;

                        if ((futureTime.after(hour24.parse(startTimeTheory)) || futureTime.equals(hour24.parse(startTimeTheory))) && currentTime.before(hour24.parse(startTimeTheory)) && !theory.getString(dayTheory).equals("null")) {
                            /*
                                The outer block
                             */
                            LinearLayout block = new LinearLayout(context);
                            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            block.setLayoutParams(blockParams);
                            block.setOrientation(LinearLayout.VERTICAL);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlockHeading = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockHeadingParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlockHeading.setLayoutParams(innerBlockHeadingParams);
                            innerBlockHeading.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The upcoming text
                             */
                            TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.upcoming));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(heading);

                            /*
                                The upcoming class course code
                             */
                            String course = theory.getString(dayTheory).split("-")[1].trim();

                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginEnd((int) (20 * pixelDensity));
                            periodParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(20);
                            period.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(period);

                            block.addView(innerBlockHeading);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                Making a proper string of the timings
                             */
                            String timings = startTimeTheory + " - " + endTimeTheory;
                            if (!DateFormat.is24HourFormat(context)) {
                                try {
                                    Date startTime = hour24.parse(startTimeTheory);
                                    Date endTime = hour24.parse(endTimeTheory);
                                    assert startTime != null;
                                    assert endTime != null;
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /*
                                The timing TextView
                             */
                            TextView timing = new TextView(context);
                            TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginStart((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(timing); //Adding timing to innerBlock

                            /*
                                The upcoming class course type
                             */
                            TextView courseType = new TextView(context);
                            TableRow.LayoutParams courseTypeParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            courseTypeParams.setMarginEnd((int) (20 * pixelDensity));
                            courseTypeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            courseType.setLayoutParams(courseTypeParams);
                            courseType.setText(getString(R.string.theory));
                            courseType.setTextColor(getColor(R.color.colorPrimary));
                            courseType.setTextSize(16);
                            courseType.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            courseType.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(courseType); //Adding the upcoming class type to innerBlock

                            block.addView(innerBlock);

                            outerBlock.addView(block);

                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }

                        if ((futureTime.after(hour24.parse(startTimeLab)) || futureTime.equals(hour24.parse(startTimeLab))) && currentTime.before(hour24.parse(startTimeLab)) && !lab.getString(dayLab).equals("null")) {
                            /*
                                The outer block
                             */
                            LinearLayout block = new LinearLayout(context);
                            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            block.setLayoutParams(blockParams);
                            block.setOrientation(LinearLayout.VERTICAL);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlockHeading = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockHeadingParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlockHeading.setLayoutParams(innerBlockHeadingParams);
                            innerBlockHeading.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The upcoming text
                             */
                            TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.upcoming));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(heading);

                            /*
                                The upcoming class course code
                             */
                            String course = lab.getString(dayLab).split("-")[1].trim();

                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginEnd((int) (20 * pixelDensity));
                            periodParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(20);
                            period.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(period); //Adding the upcoming class to innerBlock

                            block.addView(innerBlockHeading);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                Making a proper string of the timings
                             */
                            String timings = startTimeLab + " - " + endTimeLab;
                            if (!DateFormat.is24HourFormat(context)) {
                                try {
                                    Date startTime = hour24.parse(startTimeLab);
                                    Date endTime = hour24.parse(endTimeLab);
                                    assert startTime != null;
                                    assert endTime != null;
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /*
                                The timing TextView
                             */
                            TextView timing = new TextView(context);
                            TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginStart((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(timing); //Adding timing to innerBlock

                            /*
                                The upcoming class course code
                             */
                            TextView courseType = new TextView(context);
                            TableRow.LayoutParams courseTypeParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            courseTypeParams.setMarginEnd((int) (20 * pixelDensity));
                            courseTypeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            courseType.setLayoutParams(courseTypeParams);
                            courseType.setText(getString(R.string.lab));
                            courseType.setTextColor(getColor(R.color.colorPrimary));
                            courseType.setTextSize(16);
                            courseType.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            courseType.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(courseType); //Adding the upcoming class type to innerBlock

                            block.addView(innerBlock);

                            outerBlock.addView(block);

                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }

                        if (flag) {
                            break;  // If either Upcoming or Ongoing and Upcoming has been added, the loop can stop
                        }

                        if ((currentTime.after(hour24.parse(startTimeTheory)) || currentTime.equals(hour24.parse(startTimeTheory))) && (currentTime.before(hour24.parse(endTimeTheory)) || currentTime.equals(hour24.parse(endTimeTheory))) && !theory.getString(dayTheory).equals("null")) {
                            /*
                                The outer block
                             */
                            LinearLayout block = new LinearLayout(context);
                            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            block.setLayoutParams(blockParams);
                            block.setOrientation(LinearLayout.VERTICAL);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlockHeading = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockHeadingParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlockHeading.setLayoutParams(innerBlockHeadingParams);
                            innerBlockHeading.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The ongoing text
                             */
                            TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.ongoing));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(heading);

                            /*
                                The ongoing class course code
                             */
                            String course = theory.getString(dayTheory).split("-")[1].trim();
                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginEnd((int) (20 * pixelDensity));
                            periodParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(20);
                            period.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(period); //Adding the ongoing class to innerBlock

                            block.addView(innerBlockHeading);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                Making a proper string of the timings
                             */
                            String timings = startTimeTheory + " - " + endTimeTheory;
                            if (!DateFormat.is24HourFormat(context)) {
                                try {
                                    Date startTime = hour24.parse(startTimeTheory);
                                    Date endTime = hour24.parse(endTimeTheory);
                                    assert startTime != null;
                                    assert endTime != null;
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /*
                                The timing TextView
                             */
                            TextView timing = new TextView(context);
                            TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginStart((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            /*
                                Finally adding the innerBlock to the main block
                             */
                            innerBlock.addView(timing);

                            /*
                                The ongoing class course code
                             */
                            TextView courseType = new TextView(context);
                            TableRow.LayoutParams courseTypeParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            courseTypeParams.setMarginEnd((int) (20 * pixelDensity));
                            courseTypeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            courseType.setLayoutParams(courseTypeParams);
                            courseType.setText(getString(R.string.theory));
                            courseType.setTextColor(getColor(R.color.colorPrimary));
                            courseType.setTextSize(16);
                            courseType.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            courseType.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(courseType); //Adding the ongoing class type to innerBlock

                            block.addView(innerBlock);

                            outerBlock.addView(block);

                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }

                        if ((currentTime.after(hour24.parse(startTimeLab)) || currentTime.equals(hour24.parse(startTimeLab))) && (currentTime.before(hour24.parse(endTimeLab)) || currentTime.equals(hour24.parse(endTimeLab))) && !lab.getString(dayLab).equals("null")) {
                            /*
                                The outer block
                             */
                            LinearLayout block = new LinearLayout(context);
                            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            block.setLayoutParams(blockParams);
                            block.setOrientation(LinearLayout.VERTICAL);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlockHeading = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockHeadingParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlockHeading.setLayoutParams(innerBlockHeadingParams);
                            innerBlockHeading.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The ongoing text
                             */
                            TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.ongoing));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(heading);

                            /*
                                The ongoing class course code
                             */
                            String course = lab.getString(dayLab).split("-")[1].trim();
                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginEnd((int) (20 * pixelDensity));
                            periodParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(20);
                            period.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlockHeading.addView(period); //Adding the ongoing class to innerBlock

                            block.addView(innerBlockHeading);

                            /*
                                The inner LinearLayout
                             */
                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                Making a proper string of the timings
                             */
                            String timings = startTimeLab + " - " + endTimeLab;
                            if (!DateFormat.is24HourFormat(context)) {
                                try {
                                    Date startTime = hour24.parse(startTimeLab);
                                    Date endTime = hour24.parse(endTimeLab);
                                    assert startTime != null;
                                    assert endTime != null;
                                    timings = hour12.format(startTime) + " - " + hour12.format(endTime);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /*
                                The timing TextView
                             */
                            TextView timing = new TextView(context);
                            TableRow.LayoutParams timingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginStart((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            /*
                                Finally adding the innerBlock to the main block
                             */
                            innerBlock.addView(timing);

                            /*
                                The ongoing class course type
                             */
                            TextView courseType = new TextView(context);
                            TableRow.LayoutParams courseTypeParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            courseTypeParams.setMarginEnd((int) (20 * pixelDensity));
                            courseTypeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            courseType.setLayoutParams(courseTypeParams);
                            courseType.setText(getString(R.string.lab));
                            courseType.setTextColor(getColor(R.color.colorPrimary));
                            courseType.setTextSize(16);
                            courseType.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            courseType.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(courseType); //Adding the ongoing class type to innerBlock

                            block.addView(innerBlock);

                            outerBlock.addView(block);

                            flag = true;    //Flag is set so that the next code doesn't erase everything
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

                boolean classesFlag = false;

                if (sharedPreferences.getBoolean("newTimetable", false)) {
                    classesFlag = true;
                    final ImageView notification = new ImageView(context);
                    timetableNotification = View.generateViewId();
                    notification.setId(timetableNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (135 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    messagesNotification = View.generateViewId();
                    notification.setId(messagesNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (425 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    facultyNotification = View.generateViewId();
                    notification.setId(facultyNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (570 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (280 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorRedTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notificationClasses = new ImageView(context);
                    classesNotification = View.generateViewId();
                    notificationClasses.setId(classesNotification);
                    notificationClasses.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    ImageViewCompat.setImageTintList(notificationClasses, ColorStateList.valueOf(getColor(R.color.colorRedTransparent)));
                    notificationClasses.setScaleX(0);
                    notificationClasses.setScaleY(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationClasses);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationClasses.getLayoutParams();
                            notificationParams.leftToRight = R.id.classesHeading;
                            notificationParams.topToBottom = R.id.upcoming;
                            notificationParams.bottomToTop = R.id.classes;
                            notificationParams.setMarginStart((int) (10 * pixelDensity));
                            notificationParams.setMargins(0, (int) (20 * pixelDensity), 0, 0);
                            notificationClasses.setLayoutParams(notificationParams);
                            notificationClasses.animate().scaleX(1).scaleY(1);
                        }
                    });
                }

                /*
                    The classes category notification (except if there's failed attendance)
                 */
                if (classesFlag) {
                    final ImageView notification = new ImageView(context);
                    classesNotification = View.generateViewId();
                    notification.setId(classesNotification);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notification);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notification.getLayoutParams();
                            notificationParams.leftToRight = R.id.classesHeading;
                            notificationParams.topToBottom = R.id.upcoming;
                            notificationParams.bottomToTop = R.id.classes;
                            notificationParams.setMarginStart((int) (10 * pixelDensity));
                            notificationParams.setMargins(0, (int) (20 * pixelDensity), 0, 0);
                            notification.setLayoutParams(notificationParams);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }


                boolean academicsFlag = false;

                if (sharedPreferences.getBoolean("newExams", false)) {
                    academicsFlag = true;
                    final ImageView notification = new ImageView(context);
                    examsNotification = View.generateViewId();
                    notification.setId(examsNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (135 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    marksNotification = View.generateViewId();
                    notification.setId(marksNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (280 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    gradesNotification = View.generateViewId();
                    notification.setId(gradesNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (425 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    spotlightNotification = View.generateViewId();
                    notification.setId(spotlightNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (570 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    academicsNotification = View.generateViewId();
                    notification.setId(academicsNotification);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notification);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notification.getLayoutParams();
                            notificationParams.leftToRight = R.id.academicsHeading;
                            notificationParams.topToBottom = R.id.classes;
                            notificationParams.bottomToTop = R.id.academics;
                            notificationParams.setMarginStart((int) (10 * pixelDensity));
                            notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                            notification.setLayoutParams(notificationParams);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }


                boolean campusFlag = false;

                if (sharedPreferences.getBoolean("newProctorMessages", false)) {
                    campusFlag = true;
                    final ImageView notification = new ImageView(context);
                    proctorMessageNotification = View.generateViewId();
                    notification.setId(proctorMessageNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (425 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    receiptsNotification = View.generateViewId();
                    notification.setId(receiptsNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (570 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorRedTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notificationCampus = new ImageView(context);
                    campusNotification = View.generateViewId();
                    notificationCampus.setId(campusNotification);
                    notificationCampus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    ImageViewCompat.setImageTintList(notificationCampus, ColorStateList.valueOf(getColor(R.color.colorRedTransparent)));
                    notificationCampus.setScaleX(0);
                    notificationCampus.setScaleY(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notificationCampus);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notificationCampus.getLayoutParams();
                            notificationParams.leftToRight = R.id.campusHeading;
                            notificationParams.topToBottom = R.id.academics;
                            notificationParams.bottomToTop = R.id.campus;
                            notificationParams.setMarginStart((int) (10 * pixelDensity));
                            notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                            notificationCampus.setLayoutParams(notificationParams);
                            notificationCampus.animate().scaleX(1).scaleY(1);
                        }
                    });
                } else if (sharedPreferences.getBoolean("newReceipts", false)) {
                    campusFlag = true;
                    final ImageView notification = new ImageView(context);
                    receiptsNotification = View.generateViewId();
                    notification.setId(receiptsNotification);
                    LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    notificationParams.setMarginStart((int) (570 * pixelDensity));
                    notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                    notification.setLayoutParams(notificationParams);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                    notification.setStateListAnimator(elevation);
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

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
                    final ImageView notification = new ImageView(context);
                    campusNotification = View.generateViewId();
                    notification.setId(campusNotification);
                    notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
                    ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(getColor(R.color.colorPrimaryTransparent)));
                    notification.setScaleX(0);
                    notification.setScaleY(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ConstraintLayout) findViewById(R.id.home_constraint)).addView(notification);
                            ConstraintLayout.LayoutParams notificationParams = (ConstraintLayout.LayoutParams) notification.getLayoutParams();
                            notificationParams.leftToRight = R.id.campusHeading;
                            notificationParams.topToBottom = R.id.academics;
                            notificationParams.bottomToTop = R.id.campus;
                            notificationParams.setMarginStart((int) (10 * pixelDensity));
                            notificationParams.setMargins(0, (int) (10 * pixelDensity), 0, 0);
                            notification.setLayoutParams(notificationParams);
                            notification.animate().scaleX(1).scaleY(1);
                        }
                    });
                }
            }
        }).start();

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
                    assert time != null;
                    refreshedTime = hour12.format(time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            refreshed += refreshedTime;
            refreshedView.setText(refreshed);
        }

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
            Check for a new version
         */
        int versionCode = BuildConfig.VERSION_CODE;
        int latestVersion = sharedPreferences.getInt("latest", versionCode);

        if (versionCode < latestVersion) {
            Dialog update = new Dialog(this);
            update.setContentView(R.layout.dialog_update);
            update.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            update.show();
        } else if (today != null && futureRefresh != null && (today.after(futureRefresh) || lastRefreshed.after(today))) {  // Next, check if data has been refreshed recently (1 week)
            Dialog refresh = new Dialog(this);
            refresh.setContentView(R.layout.dialog_refresh);
            refresh.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            refresh.show();
        }

        TextView myLink = findViewById(R.id.builtBy);
        myLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (marksNotification == -1 || academicsNotification == -1) {
            return;
        }

        if (!sharedPreferences.getString("newMarks", "{}").equals("{}")) {
            findViewById(marksNotification).animate().scaleX(1).scaleY(1);

            ImageView academicsNotificationView = findViewById(academicsNotification);
            if (academicsNotificationView.getScaleX() == 0) {
                academicsNotificationView.animate().scaleX(1).scaleY(1);
            }
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