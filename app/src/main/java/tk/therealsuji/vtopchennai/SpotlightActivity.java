package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class SpotlightActivity extends AppCompatActivity {
    boolean night = true;

    private void setDay() {
        getWindow().setBackgroundDrawableResource(R.color.colorLight);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotlight);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        /*
            Set appearance
         */
        SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        String appearance = sharedPreferences.getString("appearance", "system");

        if (appearance.equals("day")) {
            setDay();
            night = false;
        } else if (appearance.equals("system")) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setDay();
                    night = false;
                    break;
            }
        }

        LinearLayout announcements = findViewById(R.id.announcements);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INT(3) PRIMARY KEY, category VARCHAR, announcement VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM spotlight", null);

        int categoryIndex = c.getColumnIndex("category");
        int announcementIndex = c.getColumnIndex("announcement");
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
            /*
                The outer block
             */
            LinearLayout block = new LinearLayout(this);
            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            blockParams.setMarginStart((int) (20 * pixelDensity));
            blockParams.setMarginEnd((int) (20 * pixelDensity));
            if (i == 0) {
                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
            } else if (i == c.getCount() - 1) {
                blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            } else {
                blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
            }
            block.setLayoutParams(blockParams);
            if (night) {
                block.setBackground(ContextCompat.getDrawable(this, R.drawable.plain_card_night));
            } else {
                block.setBackground(ContextCompat.getDrawable(this, R.drawable.plain_card));
            }
            block.setGravity(Gravity.CENTER_VERTICAL);
            block.setOrientation(LinearLayout.VERTICAL);

            /*
                The announcement TextView
             */
            TextView announcement = new TextView(this);
            TableRow.LayoutParams announcementParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            announcementParams.setMarginStart((int) (20 * pixelDensity));
            announcementParams.setMarginEnd((int) (20 * pixelDensity));
            announcementParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
            announcement.setLayoutParams(announcementParams);
            announcement.setText(c.getString(announcementIndex));
            announcement.setTextColor(getColor(R.color.colorPrimary));
            announcement.setTextSize(20);
            announcement.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

            block.addView(announcement); //Adding course code to block

            /*
                The category TextView
             */
            TextView category = new TextView(this);
            TableRow.LayoutParams categoryParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            categoryParams.setMarginStart((int) (20 * pixelDensity));
            categoryParams.setMarginEnd((int) (20 * pixelDensity));
            categoryParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            category.setLayoutParams(categoryParams);
            category.setText(c.getString(categoryIndex));
            category.setTextColor(getColor(R.color.colorPrimary));
            category.setTextSize(16);
            category.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            block.addView(category); //Adding course code to innerBlock

            /*
                Finally adding the block to the announcements layout
             */
            announcements.addView(block);
        }

        c.close();
        myDatabase.close();
    }
}