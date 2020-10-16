package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id INT(3) PRIMARY KEY, faculty VARCHAR, time VARCHAR, message VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM messages", null);

        int facultyIndex = c.getColumnIndex("faculty");
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); ++i) {
            //Check stuff
            c.moveToNext();
        }

        c.close();
    }
}