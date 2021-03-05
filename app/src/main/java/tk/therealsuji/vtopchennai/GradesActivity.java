package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class GradesActivity extends AppCompatActivity {

    public void openGradeHistory(MenuItem item) {
        startActivity(new Intent(this, GradeHistoryActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        /*
            At the time of writing this code, there were no class messages in the vtop portal and so I had no idea how to extract the data
         */

        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS grades (id INTEGER PRIMARY KEY, course VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM grades", null);

                int courseIndex = c.getColumnIndex("course");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (c.getString(courseIndex).equals("null")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                                findViewById(R.id.newData).setVisibility(View.VISIBLE);
                            }
                        });
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

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newGrades").apply();
            }
        }).start();

        TextView myLink = findViewById(R.id.newData);
        myLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grades_menu, menu);

        return true;
    }
}