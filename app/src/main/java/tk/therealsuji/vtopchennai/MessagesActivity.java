package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
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

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;
                final LinearLayout messages = findViewById(R.id.messages);

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id INT(3) PRIMARY KEY, course VARCHAR, type VARCHAR, message VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM messages", null);

                int courseIndex = c.getColumnIndex("course");
                int typeIndex = c.getColumnIndex("type");
                int messageIndex = c.getColumnIndex("message");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

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
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setOrientation(LinearLayout.VERTICAL);
                    block.setAlpha(0);
                    block.animate().alpha(1);

                    /*
                        The message TextView
                     */
                    TextView message = new TextView(context);
                    TableRow.LayoutParams messageParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    messageParams.setMarginStart((int) (20 * pixelDensity));
                    messageParams.setMarginEnd((int) (20 * pixelDensity));
                    messageParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    message.setLayoutParams(messageParams);
                    message.setText(c.getString(messageIndex));
                    message.setTextColor(getColor(R.color.colorPrimary));
                    message.setTextSize(16);
                    message.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(message); //Adding message to block

                    /*
                        The inner LinearLayout
                     */
                    LinearLayout innerBlock = new LinearLayout(context);
                    innerBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    innerBlock.setOrientation(LinearLayout.HORIZONTAL);

                    /*
                        The course TextView
                     */
                    TextView course = new TextView(context);
                    TableRow.LayoutParams courseParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    courseParams.setMarginStart((int) (20 * pixelDensity));
                    courseParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    course.setLayoutParams(courseParams);
                    course.setText(c.getString(courseIndex));
                    course.setTextColor(getColor(R.color.colorPrimary));
                    course.setTextSize(16);
                    course.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(course); //Adding course code to innerBlock

                    /*
                        The course type TextView
                     */
                    TextView type = new TextView(context);
                    TableRow.LayoutParams typeParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    typeParams.setMarginEnd((int) (20 * pixelDensity));
                    typeParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    type.setLayoutParams(typeParams);
                    type.setText(c.getString(typeIndex));
                    type.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    type.setTextColor(getColor(R.color.colorPrimary));
                    type.setTextSize(16);
                    type.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(type); //Adding type to innerBlock

                    block.addView(innerBlock);  //Adding innerBlock to block

                    /*
                        Finally adding the block to the view
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.addView(block);
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
}