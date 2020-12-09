package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class StaffActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout staffInfo = findViewById(R.id.staffInfo);

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                boolean flag = false;

                /*
                    Proctor
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM proctor", null);

                if (c.getCount() > 0) {
                    findViewById(R.id.noData).setVisibility(View.INVISIBLE);

                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    /*
                        The outer block for proctor
                     */
                    final LinearLayout block = new LinearLayout(context);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setPadding(0, 0, 0, (int) (17 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The proctor TextView
                     */
                    TextView proctor = new TextView(context);
                    TableRow.LayoutParams proctorParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    proctorParams.setMarginStart((int) (20 * pixelDensity));
                    proctorParams.setMarginEnd((int) (20 * pixelDensity));
                    proctorParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    proctor.setLayoutParams(proctorParams);
                    proctor.setText(getString(R.string.proctor));
                    proctor.setTextColor(getColor(R.color.colorPrimary));
                    proctor.setTextSize(20);
                    proctor.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(proctor);   //Adding proctor to block

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        String key = c.getString(column1Index);
                        String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        /*
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMarginStart((int) (20 * pixelDensity));
                        keyViewParams.setMarginEnd((int) (20 * pixelDensity));
                        keyViewParams.setMargins(0, (int) (3 * pixelDensity), 0, (int) (2 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(key);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        block.addView(keyView);   //Adding key to block

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMarginStart((int) (20 * pixelDensity));
                        valueViewParams.setMarginEnd((int) (20 * pixelDensity));
                        valueViewParams.setMargins(0, (int) (2 * pixelDensity), 0, (int) (3 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setText(value);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(16);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(valueView);   //Adding key to block
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staffInfo.addView(block);
                        }
                    });
                    flag = true;
                }

                /*
                    Dean
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS dean (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM dean", null);

                if (c.getCount() > 0) {
                    findViewById(R.id.noData).setVisibility(View.INVISIBLE);

                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    /*
                        The outer block for dean
                     */
                    final LinearLayout block = new LinearLayout(context);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    if (!flag) {
                        blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    } else {
                        blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    }
                    block.setPadding(0, 0, 0, (int) (17 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The dean TextView
                     */
                    TextView dean = new TextView(context);
                    TableRow.LayoutParams deanParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    deanParams.setMarginStart((int) (20 * pixelDensity));
                    deanParams.setMarginEnd((int) (20 * pixelDensity));
                    deanParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    dean.setLayoutParams(deanParams);
                    dean.setText(getString(R.string.dean));
                    dean.setTextColor(getColor(R.color.colorPrimary));
                    dean.setTextSize(20);
                    dean.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(dean);   //Adding dean to block

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        String key = c.getString(column1Index);
                        String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        /*
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMarginStart((int) (20 * pixelDensity));
                        keyViewParams.setMarginEnd((int) (20 * pixelDensity));
                        keyViewParams.setMargins(0, (int) (3 * pixelDensity), 0, (int) (2 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(key);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        block.addView(keyView);   //Adding key to block

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMarginStart((int) (20 * pixelDensity));
                        valueViewParams.setMarginEnd((int) (20 * pixelDensity));
                        valueViewParams.setMargins(0, (int) (2 * pixelDensity), 0, (int) (3 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setText(value);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(16);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(valueView);   //Adding key to block
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staffInfo.addView(block);
                        }
                    });
                    flag = true;
                }

                /*
                    HOD
                 */
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS hod (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
                c = myDatabase.rawQuery("SELECT * FROM hod", null);

                if (c.getCount() > 0) {
                    findViewById(R.id.noData).setVisibility(View.INVISIBLE);

                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    /*
                        The outer block for hod
                     */
                    final LinearLayout block = new LinearLayout(context);
                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    blockParams.setMarginStart((int) (20 * pixelDensity));
                    blockParams.setMarginEnd((int) (20 * pixelDensity));
                    if (!flag) {
                        blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    } else {
                        blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    }
                    block.setPadding(0, 0, 0, (int) (17 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);

                    /*
                        The hod TextView
                     */
                    TextView hod = new TextView(context);
                    TableRow.LayoutParams hodParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    hodParams.setMarginStart((int) (20 * pixelDensity));
                    hodParams.setMarginEnd((int) (20 * pixelDensity));
                    hodParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    hod.setLayoutParams(hodParams);
                    hod.setText(getString(R.string.hod));
                    hod.setTextColor(getColor(R.color.colorPrimary));
                    hod.setTextSize(20);
                    hod.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(hod);   //Adding hod to block

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        String key = c.getString(column1Index);
                        String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        /*
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMarginStart((int) (20 * pixelDensity));
                        keyViewParams.setMarginEnd((int) (20 * pixelDensity));
                        keyViewParams.setMargins(0, (int) (3 * pixelDensity), 0, (int) (2 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(key);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        block.addView(keyView);   //Adding key to block

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMarginStart((int) (20 * pixelDensity));
                        valueViewParams.setMarginEnd((int) (20 * pixelDensity));
                        valueViewParams.setMargins(0, (int) (2 * pixelDensity), 0, (int) (3 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setText(value);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(16);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(valueView);   //Adding key to block
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staffInfo.addView(block);
                        }
                    });
                }

                c.close();
                myDatabase.close();
            }
        }).start();
    }
}