package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MessagesActivity extends AppCompatActivity {
    boolean night = true;

    private void setDay() {
        getWindow().setBackgroundDrawableResource(R.color.colorLight);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
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

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id INT(3) PRIMARY KEY, faculty VARCHAR, time VARCHAR, message VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM messages", null);

        int facultyIndex = c.getColumnIndex("faculty");
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
            if (c.getString(facultyIndex).equals("null")) {
                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                findViewById(R.id.newData).setVisibility(View.VISIBLE);
            }
        }

        c.close();
    }
}