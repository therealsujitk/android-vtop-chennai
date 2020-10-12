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

        LinearLayout staffInfo = findViewById(R.id.staffInfo);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        SQLiteDatabase myDatabase = getApplicationContext().openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor (id INT(3) PRIMARY KEY, column1 VARCHAR, column2 VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM proctor", null);

        int column1Index = c.getColumnIndex("column1");
        int column2Index = c.getColumnIndex("column2");
        c.moveToFirst();

        /*
            The outer block for proctor
         */
        LinearLayout block = new LinearLayout(this);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        blockParams.setMarginStart((int) (20 * pixelDensity));
        blockParams.setMarginEnd((int) (20 * pixelDensity));
        blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
        block.setLayoutParams(blockParams);
        block.setBackground(ContextCompat.getDrawable(this, R.drawable.plain_card));
        block.setOrientation(LinearLayout.VERTICAL);

        /*
            The proctor TextView
         */
        TextView proctor = new TextView(this);
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
        proctor.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

        block.addView(proctor);   //Adding proctor to block

        for (int i = 0; i < c.getCount(); ++i) {
            String key = c.getString(column1Index);
            String value = c.getString(column2Index);

            if (key.equals("") || value.equals("")) {
                c.moveToNext();
                continue;
            }

            LinearLayout innerBlock = new LinearLayout(this);
            innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            innerBlock.setOrientation(LinearLayout.HORIZONTAL);

            /*
                The key TextView
             */
            TextView keyView = new TextView(this);
            TableRow.LayoutParams keyViewParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            keyViewParams.setMarginStart((int) (20 * pixelDensity));
            keyViewParams.setMarginEnd((int) (20 * pixelDensity));
            if (i == c.getCount() - 1) {
                keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            } else {
                keyViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
            }
            keyView.setLayoutParams(keyViewParams);
            keyView.setText(key);
            keyView.setTextColor(getColor(R.color.colorPrimary));
            keyView.setTextSize(16);
            keyView.setTypeface(ResourcesCompat.getFont(this, R.font.rubik));

            innerBlock.addView(keyView);   //Adding key to innerBlock

            /*
                The value TextView
             */
            TextView valueView = new TextView(this);
            TableRow.LayoutParams valueViewParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            valueViewParams.setMarginStart((int) (20 * pixelDensity));
            valueViewParams.setMarginEnd((int) (20 * pixelDensity));
            if (i == c.getCount() - 1) {
                valueViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
            } else {
                valueViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
            }
            valueView.setLayoutParams(valueViewParams);
            valueView.setText(value);
            valueView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            valueView.setTextColor(getColor(R.color.colorPrimary));
            valueView.setTextSize(16);
            valueView.setTypeface(ResourcesCompat.getFont(this, R.font.rubik), Typeface.BOLD);

            innerBlock.addView(valueView);   //Adding key to innerBlock

            /*
                Adding innerBlock to block
             */
            block.addView(innerBlock);

            c.moveToNext();
        }

        staffInfo.addView(block);

        c.close();
        myDatabase.close();
    }
}