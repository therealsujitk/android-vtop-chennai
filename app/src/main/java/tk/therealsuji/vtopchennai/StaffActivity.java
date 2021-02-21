package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class StaffActivity extends AppCompatActivity {
    ScrollView staff;
    HorizontalScrollView staffContainer;
    TextView[] staffButtons = new TextView[3];
    LinearLayout[] staffViews = new LinearLayout[3];
    float pixelDensity;
    int staffID, halfWidth;

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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        staffContainer = findViewById(R.id.staffContainer);

        staffButtons[0] = findViewById(R.id.proctor);
        staffButtons[1] = findViewById(R.id.dean);
        staffButtons[2] = findViewById(R.id.hod);

        staffContainer.animate().alpha(1);

        for (int i = 0; i < 3; ++i) {
            staffViews[i] = new LinearLayout(context);
            staffViews[i].setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            staffViews[i].setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
            staffViews[i].setOrientation(LinearLayout.VERTICAL);
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

                if (c.getCount() > 0) {
                    int column1Index = c.getColumnIndex("column1");
                    int column2Index = c.getColumnIndex("column2");
                    c.moveToFirst();

                    for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                        String key = c.getString(column1Index);
                        final String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        final LinearLayout outerBlock = new LinearLayout(context);
                        LinearLayout.LayoutParams outerBlockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        outerBlockParams.setMarginStart((int) (20 * pixelDensity));
                        outerBlockParams.setMarginEnd((int) (20 * pixelDensity));
                        outerBlockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        outerBlock.setLayoutParams(outerBlockParams);
                        outerBlock.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                        outerBlock.setOrientation(LinearLayout.HORIZONTAL);
                        outerBlock.setGravity(Gravity.CENTER_VERTICAL);

                        LinearLayout block = new LinearLayout(context);
                        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );
                        blockParams.setMarginStart((int) (20 * pixelDensity));
                        blockParams.setMarginEnd((int) (20 * pixelDensity));
                        block.setLayoutParams(blockParams);
                        block.setOrientation(LinearLayout.VERTICAL);

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setText(value);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(20);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(valueView);   //Adding key to block

                        /*
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(key);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        block.addView(keyView);   //Adding key to block

                        outerBlock.addView(block);

                        /*
                            Finally adding the block to the view
                         */
                        String lowerKey = key.toLowerCase();
                        if (lowerKey.contains("mobile") || lowerKey.contains("phone")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + value));
                                    startActivity(intent);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_phone));

                            linkButton.addView(imageView);
                            outerBlock.addView(linkButton);
                        } else if (lowerKey.contains("email")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                    intent.setData(Uri.parse("mailto:" + value));
                                    startActivity(intent);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email));

                            linkButton.addView(imageView);
                            outerBlock.addView(linkButton);
                        }

                        if (staffID == 0) {
                            outerBlock.setAlpha(0);
                            outerBlock.animate().alpha(1);

                            final LinearLayout proctorView = staffViews[0];
                            final int index = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    proctorView.addView(outerBlock);
                                    if (index <= 1) {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            staffViews[0].addView(outerBlock);
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
                        String key = c.getString(column1Index);
                        final String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        final LinearLayout outerBlock = new LinearLayout(context);
                        LinearLayout.LayoutParams outerBlockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        outerBlockParams.setMarginStart((int) (20 * pixelDensity));
                        outerBlockParams.setMarginEnd((int) (20 * pixelDensity));
                        outerBlockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        outerBlock.setLayoutParams(outerBlockParams);
                        outerBlock.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                        outerBlock.setOrientation(LinearLayout.HORIZONTAL);
                        outerBlock.setGravity(Gravity.CENTER_VERTICAL);

                        LinearLayout block = new LinearLayout(context);
                        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );
                        blockParams.setMarginStart((int) (20 * pixelDensity));
                        blockParams.setMarginEnd((int) (20 * pixelDensity));
                        block.setLayoutParams(blockParams);
                        block.setOrientation(LinearLayout.VERTICAL);

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setText(value);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(20);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(valueView);   //Adding key to block

                        /*
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(key);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        block.addView(keyView);   //Adding key to block

                        outerBlock.addView(block);

                        /*
                            Finally adding the block to the view
                         */
                        String lowerKey = key.toLowerCase();
                        if (lowerKey.contains("mobile") || lowerKey.contains("phone")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + value));
                                    startActivity(intent);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_phone));

                            linkButton.addView(imageView);
                            outerBlock.addView(linkButton);
                        } else if (lowerKey.contains("email")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                    intent.setData(Uri.parse("mailto:" + value));
                                    startActivity(intent);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email));

                            linkButton.addView(imageView);
                            outerBlock.addView(linkButton);
                        }

                        if (staffID == 1) {
                            outerBlock.setAlpha(0);
                            outerBlock.animate().alpha(1);

                            final LinearLayout deanView = staffViews[1];
                            final int index = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deanView.addView(outerBlock);
                                    if (index <= 1) {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            staffViews[1].addView(outerBlock);
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
                        String key = c.getString(column1Index);
                        final String value = c.getString(column2Index);

                        if (key.equals("") || value.equals("")) {
                            continue;
                        }

                        final LinearLayout outerBlock = new LinearLayout(context);
                        LinearLayout.LayoutParams outerBlockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        outerBlockParams.setMarginStart((int) (20 * pixelDensity));
                        outerBlockParams.setMarginEnd((int) (20 * pixelDensity));
                        outerBlockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        outerBlock.setLayoutParams(outerBlockParams);
                        outerBlock.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                        outerBlock.setOrientation(LinearLayout.HORIZONTAL);
                        outerBlock.setGravity(Gravity.CENTER_VERTICAL);

                        LinearLayout block = new LinearLayout(context);
                        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );
                        blockParams.setMarginStart((int) (20 * pixelDensity));
                        blockParams.setMarginEnd((int) (20 * pixelDensity));
                        block.setLayoutParams(blockParams);
                        block.setOrientation(LinearLayout.VERTICAL);

                        /*
                            The value TextView
                         */
                        TextView valueView = new TextView(context);
                        TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        valueViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                        valueView.setLayoutParams(valueViewParams);
                        valueView.setText(value);
                        valueView.setTextColor(getColor(R.color.colorPrimary));
                        valueView.setTextSize(20);
                        valueView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(valueView);   //Adding key to block

                        /*
                            The key TextView
                         */
                        TextView keyView = new TextView(context);
                        TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                        keyView.setLayoutParams(keyViewParams);
                        keyView.setText(key);
                        keyView.setTextColor(getColor(R.color.colorPrimary));
                        keyView.setTextSize(16);
                        keyView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                        block.addView(keyView);   //Adding key to block

                        outerBlock.addView(block);

                        /*
                            Finally adding the block to the view
                         */
                        String lowerKey = key.toLowerCase();
                        if (lowerKey.contains("mobile") || lowerKey.contains("phone")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + value));
                                    startActivity(intent);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_phone));

                            linkButton.addView(imageView);
                            outerBlock.addView(linkButton);
                        } else if (lowerKey.contains("email")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                    intent.setData(Uri.parse("mailto:" + value));
                                    startActivity(intent);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email));

                            linkButton.addView(imageView);
                            outerBlock.addView(linkButton);
                        }

                        if (staffID == 2) {
                            outerBlock.setAlpha(0);
                            outerBlock.animate().alpha(1);

                            final LinearLayout hodView = staffViews[2];
                            final int index = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hodView.addView(outerBlock);
                                    if (index <= 1) {
                                        findViewById(R.id.noData).setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            staffViews[2].addView(outerBlock);
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
}