package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class StaffActivity extends AppCompatActivity {
    ScrollView staff;
    HorizontalScrollView staffContainer;
    TextView[] staffButtons = new TextView[3];
    LinearLayout[] staffViews = new LinearLayout[3];
    float pixelDensity;
    int staffID, halfWidth;

    boolean terminateThread;

    public void setStaff(View view) {
        int selectedStaffID = Integer.parseInt(view.getTag().toString());
        if (selectedStaffID == staffID) {
            return;
        }

        staffID = selectedStaffID;

        staff.scrollTo(0, 0);
        staff.removeAllViews();

        if (staffViews[staffID].getChildCount() > 0) {
            findViewById(R.id.noData).setVisibility(View.INVISIBLE);
            staff.setAlpha(0);
            staff.addView(staffViews[staffID]);
            staff.animate().alpha(1);
        } else {
            findViewById(R.id.noData).setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < 3; ++i) {
            staffButtons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        staffButtons[staffID].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float location = 0;
        for (int i = 0; i < staffID; ++i) {
            location += 10 * pixelDensity + (float) staffButtons[i].getWidth();
        }
        location += 20 * pixelDensity + (float) staffButtons[staffID].getWidth() / 2;

        staffContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        pixelDensity = context.getResources().getDisplayMetrics().density;
        staff = findViewById(R.id.staff);

        staffContainer = findViewById(R.id.staffContainer);

        staffButtons[0] = findViewById(R.id.proctor);
        staffButtons[1] = findViewById(R.id.dean);
        staffButtons[2] = findViewById(R.id.hod);

        staffContainer.animate().alpha(1);

        LayoutGenerator myLayout = new LayoutGenerator(this);

        for (int i = 0; i < 3; ++i) {
            staffViews[i] = myLayout.generateLayout();
        }

        staff.addView(staffViews[0]);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                /*
                    Proctor
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM proctor", null);

                CardGenerator myStaff = new CardGenerator(context, CardGenerator.CARD_STAFF);

                if (c.getCount() > 0) {
                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String key = c.getString(column1Index);
                        final String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        final LinearLayout card = myStaff.generateCard(key, value);

                        /*
                            Adding the card to the view
                         */
                        if (staffID == 0) {
                            card.setAlpha(0);
                            card.animate().alpha(1);

                            final LinearLayout proctorView = staffViews[0];
                            final int index = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    proctorView.addView(card);
                                    if (index <= 1) {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            staffViews[0].addView(card);
                        }
                    }
                }

                /*
                    Dean
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS dean (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM dean", null);

                if (c.getCount() > 0) {
                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String key = c.getString(column1Index);
                        final String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        final LinearLayout card = myStaff.generateCard(key, value);

                        /*
                            Adding the card to the view
                         */
                        if (staffID == 1) {
                            card.setAlpha(0);
                            card.animate().alpha(1);

                            final LinearLayout deanView = staffViews[1];
                            final int index = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deanView.addView(card);
                                    if (index <= 1) {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            staffViews[1].addView(card);
                        }
                    }
                }

                /*
                    HOD
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS hod (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM hod", null);

                if (c.getCount() > 0) {
                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String key = c.getString(column1Index);
                        final String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        final LinearLayout card = myStaff.generateCard(key, value);

                        /*
                            Adding the card to the view
                         */
                        if (staffID == 2) {
                            card.setAlpha(0);
                            card.animate().alpha(1);

                            final LinearLayout hodView = staffViews[2];
                            final int index = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hodView.addView(card);
                                    if (index <= 1) {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            staffViews[2].addView(card);
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}