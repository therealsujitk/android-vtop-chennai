package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Objects;

public class SpotlightActivity extends AppCompatActivity {
    ScrollView announcements;
    ArrayList<TextView> categories = new ArrayList<>();
    ArrayList<LinearLayout> announcementViews = new ArrayList<>();
    float pixelDensity;

    public void setAnnouncements(View view) {
        announcements.scrollTo(0, 0);
        announcements.removeAllViews();

        for (int i = 0; i < categories.size(); ++i) {
            categories.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        int index = Integer.parseInt(view.getTag().toString());
        categories.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));
        announcements.addView(announcementViews.get(index));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int halfWidth = displayMetrics.widthPixels / 2;
        float location = 0;
        for (int i = 0; i < index; ++i) {
            location += 10 * pixelDensity + (float) categories.get(i).getWidth();
        }
        location += 20 * pixelDensity + (float) categories.get(index).getWidth() / 2;
        ((HorizontalScrollView) findViewById(R.id.categoriesContainer)).smoothScrollTo((int) location - halfWidth, 0);
    }

    public void openLink(String link) {
        if (link.equals("NA")) {
            Dialog noLink = new Dialog(this);
            noLink.setContentView(R.layout.dialog_nolink);
            noLink.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            noLink.show();
        } else if (link.startsWith("http")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        } else {
            String downloadLink = "http://vtopcc.vit.ac.in/vtop/" + link + "?&x=";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink));
            startActivity(browserIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotlight);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout categoryButtons = findViewById(R.id.categories);
        announcements = findViewById(R.id.announcements);
        pixelDensity = context.getResources().getDisplayMetrics().density;

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INT(3) PRIMARY KEY, category VARCHAR, announcement VARCHAR, link VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT DISTINCT category FROM spotlight", null);

                int categoryIndex = c.getColumnIndex("category");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    String categoryTitle = c.getString(categoryIndex);

                    /*
                        Creating the announcements view
                     */
                    final LinearLayout announcementsView = new LinearLayout(context);
                    LinearLayout.LayoutParams announcementsViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    announcementsView.setLayoutParams(announcementsViewParams);
                    announcementsView.setPadding(0, (int) (65 * pixelDensity), 0, 0);
                    announcementsView.setOrientation(LinearLayout.VERTICAL);

                    announcementViews.add(announcementsView);    //Storing the view

                    /*
                        Creating the category button
                     */
                    final TextView category = new TextView(context);
                    LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) (25 * pixelDensity)
                    );
                    categoryParams.setMarginStart((int) (5 * pixelDensity));
                    categoryParams.setMarginEnd((int) (5 * pixelDensity));
                    categoryParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    category.setLayoutParams(categoryParams);
                    category.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), 0);
                    if (i == 0) {
                        category.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                        findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                    } else {
                        category.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
                    }
                    category.setTag(i);
                    category.setText(categoryTitle.toUpperCase());
                    category.setTextColor(getColor(R.color.colorPrimary));
                    category.setTextSize(12);
                    category.setGravity(Gravity.CENTER_VERTICAL);
                    category.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
                    category.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setAnnouncements(category);
                        }
                    });

                    categories.add(category);    //Storing the button

                    /*
                        Finally adding the button to the HorizontalScrollView
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            categoryButtons.addView(category);
                        }
                    });

                    Cursor s = myDatabase.rawQuery("SELECT announcement, link FROM spotlight WHERE category = '" + categoryTitle + "'", null);

                    int announcementIndex = s.getColumnIndex("announcement");
                    int linkIndex = s.getColumnIndex("link");
                    s.moveToFirst();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        /*
                            The outer block
                         */
                        final LinearLayout block = new LinearLayout(context);
                        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        blockParams.setMarginStart((int) (20 * pixelDensity));
                        blockParams.setMarginEnd((int) (20 * pixelDensity));
                        if (j == s.getCount() - 1) {
                            blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                        } else {
                            blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        }
                        block.setLayoutParams(blockParams);
                        block.setClickable(true);
                        block.isFocusable();
                        block.setBackground(ContextCompat.getDrawable(context, R.drawable.button_card));
                        block.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation));
                        block.setGravity(Gravity.CENTER_VERTICAL);
                        block.setOrientation(LinearLayout.VERTICAL);

                        /*
                            Setting the onClickListener
                         */
                        final String link = s.getString(linkIndex);
                        block.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openLink(link);
                            }
                        });

                        /*
                            The announcement TextView
                         */
                        TextView announcement = new TextView(context);
                        TableRow.LayoutParams announcementParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        );
                        announcementParams.setMarginStart((int) (20 * pixelDensity));
                        announcementParams.setMarginEnd((int) (20 * pixelDensity));
                        announcementParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                        announcement.setLayoutParams(announcementParams);
                        announcement.setText(s.getString(announcementIndex));
                        announcement.setTextColor(getColor(R.color.colorPrimary));
                        announcement.setTextSize(16);
                        announcement.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(announcement); //Adding course code to block

                        /*
                            Finally adding the block to the announcements layout
                         */
                        announcementsView.addView(block);
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                announcements.addView(announcementsView);
                            }
                        });
                    }

                    s.close();
                }

                c.close();
                myDatabase.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("newSpotlight", false).apply();
            }
        }).start();
    }
}