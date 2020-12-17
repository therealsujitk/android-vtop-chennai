package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    Dialog appearance;

    /*
        The following functions are to open the activities in the "Classes" category
     */

    public void openTimetable(View view) {
        startActivity(new Intent(HomeActivity.this, TimetableActivity.class));
    }

    public void openAttendance(View view) {
        startActivity(new Intent(HomeActivity.this, AttendanceActivity.class));
    }

    public void openExams(View view) {
        startActivity(new Intent(HomeActivity.this, ExamsActivity.class));
    }

    public void openMessages(View view) {
        startActivity(new Intent(HomeActivity.this, MessagesActivity.class));
    }

    /*
        The following functions are to open the activities in the "Academics" category
     */

    public void openSpotlight(View view) {
        startActivity(new Intent(HomeActivity.this, SpotlightActivity.class));
    }

    public void openFaculty(View view) {
        startActivity(new Intent(HomeActivity.this, FacultyActivity.class));
    }

    public void openStaff(View view) {
        startActivity(new Intent(HomeActivity.this, StaffActivity.class));
    }

    public void openReceipts(View view) {
        startActivity(new Intent(HomeActivity.this, ReceiptsActivity.class));
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
        startActivity(new Intent(HomeActivity.this, DownloadActivity.class));
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

    public void signOut(View view) {
        sharedPreferences.edit().remove("isSignedIn").apply();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    private void openUpdate() {
        WebView webView = new WebView(this);
        webView.loadUrl("http://vtopchennai.therealsuji.tk");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_home);

        String name = sharedPreferences.getString("name", getString(R.string.name));
        String id = sharedPreferences.getString("id", getString(R.string.id));
        String credits = sharedPreferences.getString("credits", getString(R.string.credits));

        TextView nameView = findViewById(R.id.name);
        TextView idView = findViewById(R.id.id);
        TextView creditsView = findViewById(R.id.credits);

        nameView.setText(name);
        idView.setText(id);
        creditsView.setText(credits);

        final float pixelDensity = this.getResources().getDisplayMetrics().density;
        final SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        final SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        final Context context = this;
        final LinearLayout upcoming = findViewById(R.id.upcoming);

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            if (!flag) {
                                upcoming.removeAllViews();
                            }

                            /*
                                The upcoming text
                             */
                            final TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMarginEnd((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.upcoming));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(heading);  //Adding the upcoming text to the block
                                }
                            });

                            /*
                                The inner LinearLayout
                             */
                            final LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The upcoming class course code
                             */
                            String course = theory.getString(dayTheory).split("-")[1].trim() + " - Theory";

                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginStart((int) (20 * pixelDensity));
                            periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(16);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(period); //Adding the upcoming class to innerBlock

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
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginEnd((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(timing); //Adding timing to innerBlock

                            /*
                                Finally adding the innerBlock to the main block
                             */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(innerBlock);
                                }
                            });
                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }

                        if ((futureTime.after(hour24.parse(startTimeLab)) || futureTime.equals(hour24.parse(startTimeLab))) && currentTime.before(hour24.parse(startTimeLab)) && !lab.getString(dayLab).equals("null")) {
                            if (!flag) {
                                upcoming.removeAllViews();
                            }

                            /*
                                The upcoming text
                             */
                            final TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMarginEnd((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.upcoming));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(heading);  //Adding the upcoming text to the block
                                }
                            });

                            /*
                                The inner LinearLayout
                             */
                            final LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The upcoming class course code
                             */
                            String course = lab.getString(dayLab).split("-")[1].trim() + " - Lab";

                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginStart((int) (20 * pixelDensity));
                            periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(16);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(period); //Adding the upcoming class to innerBlock

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
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginEnd((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(timing); //Adding timing to innerBlock

                            /*
                                Finally adding the innerBlock to the main block
                             */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(innerBlock);
                                }
                            });
                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }

                        if (flag) {
                            break;  // If either Upcoming or Ongoing and Upcoming has been added, the loop can stop
                        }

                        if ((currentTime.after(hour24.parse(startTimeTheory)) || currentTime.equals(hour24.parse(startTimeTheory))) && (currentTime.before(hour24.parse(endTimeTheory)) || currentTime.equals(hour24.parse(endTimeTheory))) && !theory.getString(dayTheory).equals("null")) {
                            upcoming.removeAllViews();

                            /*
                                The ongoing text
                             */
                            final TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMarginEnd((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.ongoing));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(heading);  //Adding the ongoing text to the block
                                }
                            });

                            /*
                                The inner LinearLayout
                             */
                            final LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The ongoing class course code
                             */
                            String course = theory.getString(dayTheory).split("-")[1].trim() + " - Theory";
                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginStart((int) (20 * pixelDensity));
                            periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(16);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(period); //Adding the ongoing class to innerBlock

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
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginEnd((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            /*
                                Finally adding the innerBlock to the main block
                             */
                            innerBlock.addView(timing);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(innerBlock);
                                }
                            });
                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }

                        if ((currentTime.after(hour24.parse(startTimeLab)) || currentTime.equals(hour24.parse(startTimeLab))) && (currentTime.before(hour24.parse(endTimeLab)) || currentTime.equals(hour24.parse(endTimeLab))) && !lab.getString(dayLab).equals("null")) {
                            if (!flag) {
                                upcoming.removeAllViews();
                            }

                            /*
                                The ongoing text
                             */
                            final TextView heading = new TextView(context);
                            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            headingParams.setMarginStart((int) (20 * pixelDensity));
                            headingParams.setMarginEnd((int) (20 * pixelDensity));
                            headingParams.setMargins(0, 0, 0, (int) (5 * pixelDensity));
                            heading.setLayoutParams(headingParams);
                            heading.setText(getString(R.string.ongoing));
                            heading.setTextColor(getColor(R.color.colorPrimary));
                            heading.setTextSize(20);
                            heading.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(heading);  //Adding the ongoing text to the block
                                }
                            });

                            /*
                                The inner LinearLayout
                             */
                            final LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                            /*
                                The ongoing class course code
                             */
                            String course = lab.getString(dayLab).split("-")[1].trim() + " - Lab";
                            TextView period = new TextView(context);
                            TableRow.LayoutParams periodParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            periodParams.setMarginStart((int) (20 * pixelDensity));
                            periodParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            period.setLayoutParams(periodParams);
                            period.setText(course);
                            period.setTextColor(getColor(R.color.colorPrimary));
                            period.setTextSize(16);
                            period.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(period); //Adding the ongoing class to innerBlock

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
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            timingParams.setMarginEnd((int) (20 * pixelDensity));
                            timingParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            timing.setLayoutParams(timingParams);
                            timing.setText(timings);
                            timing.setTextColor(getColor(R.color.colorPrimary));
                            timing.setTextSize(16);
                            timing.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            timing.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            /*
                                Finally adding the innerBlock to the main block
                             */
                            innerBlock.addView(timing);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upcoming.addView(innerBlock);
                                }
                            });
                            flag = true;    //Flag is set so that the next code doesn't erase everything
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                theory.close();
                lab.close();
                myDatabase.close();
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

        /*
            Check for a new version
         */
        int versionCode = BuildConfig.VERSION_CODE;
        String latestVersion = sharedPreferences.getString("versionCode", Integer.toString(versionCode));

        if (versionCode < Integer.parseInt(latestVersion)) {
            LinearLayout container = findViewById(R.id.container);

            LinearLayout block = new LinearLayout(this);
            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            blockParams.setMarginStart((int) (20 * pixelDensity));
            blockParams.setMarginEnd((int) (20 * pixelDensity));
            blockParams.setMargins(0, 0, 0, (int) (10 * pixelDensity));
            block.setLayoutParams(blockParams);
            block.setBackground(ContextCompat.getDrawable(this, R.drawable.button_card));
            block.setOrientation(LinearLayout.HORIZONTAL);
            block.setGravity(Gravity.CENTER_VERTICAL);
            block.setFocusable(true);
            block.setClickable(true);
            block.setStateListAnimator(AnimatorInflater.loadStateListAnimator(this, R.animator.item_elevation));
            block.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUpdate();
                }
            });

            ImageView image = new ImageView(this);
            image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_download));
            TableRow.LayoutParams imageParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
            );
            imageParams.setMarginStart((int) (20 * pixelDensity));
            imageParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
            imageParams.height = (int) (30 * pixelDensity);
            image.setLayoutParams(imageParams);

            block.addView(image);

            TextView text = new TextView(this);
            TableRow.LayoutParams textParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            textParams.setMarginEnd((int) (20 * pixelDensity));
            textParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
            text.setLayoutParams(textParams);
            text.setText(getString(R.string.update));
            text.setTextColor(getColor(R.color.colorPrimary));
            text.setTextSize(18);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            block.addView(text);

            container.addView(block);
        }

        TextView myLink = findViewById(R.id.builtBy);
        myLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}