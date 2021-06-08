package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class CoursesActivity extends AppCompatActivity {
    boolean terminateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout facultyInfo = findViewById(R.id.faculty);

        new Thread(() -> {
            SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS faculty (id INT(3) PRIMARY KEY, course VARCHAR, faculty VARCHAR)");
            Cursor c = myDatabase.rawQuery("SELECT * FROM faculty", null);

            int courseIndex = c.getColumnIndex("course");
            int facultyIndex = c.getColumnIndex("faculty");
            c.moveToFirst();

            CardGenerator myFaculty = new CardGenerator(context, CardGenerator.CARD_FACULTY);

            boolean isUsingNewLayout = false, checkIfIsUsingNewLayout = true;

            for (int i = 0; i < c.getCount(); ++i) {
                if (terminateThread) {
                    return;
                }

                if (i == 0) {
                    runOnUiThread(() -> findViewById(R.id.noData).setVisibility(View.GONE));
                }

                String[] rawString = c.getString(courseIndex).split("-");

                String faculty = c.getString(facultyIndex).split("-")[0];
                String course = rawString[0];
                String type = rawString[rawString.length - 1];

                if (checkIfIsUsingNewLayout) {
                    if (type.contains("(")) {
                        isUsingNewLayout = true;
                    }

                    checkIfIsUsingNewLayout = false;
                }

                if (isUsingNewLayout) {
                    type = type.substring(type.indexOf("(") + 1, type.indexOf(")")).trim();
                }

                final LinearLayout card = myFaculty.generateCard(faculty, course, type);
                card.setAlpha(0);
                card.animate().alpha(1);

                /*
                    Adding the block to the activity
                 */
                runOnUiThread(() -> facultyInfo.addView(card));

                c.moveToNext();
            }

            runOnUiThread(() -> findViewById(R.id.loading).animate().alpha(0));

            c.close();
            myDatabase.close();

            SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove("newFaculty").apply();
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}
