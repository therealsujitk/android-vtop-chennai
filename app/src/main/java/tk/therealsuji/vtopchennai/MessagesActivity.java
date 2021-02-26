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

public class MessagesActivity extends AppCompatActivity {
    boolean terminateThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final LinearLayout messages = findViewById(R.id.messages);

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, message VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM messages", null);

                int courseIndex = c.getColumnIndex("course");
                int typeIndex = c.getColumnIndex("type");
                int messageIndex = c.getColumnIndex("message");
                c.moveToFirst();

                CardGenerator myMessage = new CardGenerator(context, CardGenerator.CARD_MESSAGE);

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (terminateThread) {
                        return;
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    String message = c.getString(messageIndex);
                    String course = c.getString(courseIndex);
                    String type = c.getString(typeIndex);

                    final LinearLayout card = myMessage.generateCard(message, course, type);
                    card.setAlpha(0);
                    card.animate().alpha(1);

                    /*
                        Adding the card to the view
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.addView(card);
                        }
                    });
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
                sharedPreferences.edit().remove("newMessages").apply();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}