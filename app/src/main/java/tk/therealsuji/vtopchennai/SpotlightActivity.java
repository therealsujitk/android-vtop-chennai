package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class SpotlightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotlight);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INT(3) PRIMARY KEY, category VARCHAR, announcement VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM spotlight", null);

        int categoryIndex = c.getColumnIndex("category");
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
            if (c.getString(categoryIndex).equals("null")) {
                findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                findViewById(R.id.newData).setVisibility(View.VISIBLE);
            }
        }

        c.close();
    }
}