package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ProctorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proctor);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        /*
            At the time of writing this code, there were no class messages in the vtop portal and so I had no idea how to extract the data
         */

        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS proctor_messages (id INT(3) PRIMARY KEY, time VARCHAR, message VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM proctor_messages", null);

                int timeIndex = c.getColumnIndex("time");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (c.getString(timeIndex).equals("null")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                                findViewById(R.id.newData).setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

                c.close();
                myDatabase.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newProctorMessages").apply();
            }
        }).start();

        TextView myLink = findViewById(R.id.newData);
        myLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}