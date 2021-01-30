package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
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

public class ReceiptsActivity extends AppCompatActivity {

    private String separateAmount(String amount) {
        StringBuilder separatedAmount = new StringBuilder(amount);
        int index = amount.length() - 3;

        while (index > 0) {
            separatedAmount.insert(index, ",");
            index -= 2;
        }

        return separatedAmount.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipts);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout receipts = findViewById(R.id.receipts);

        new Thread(new Runnable() {
            @Override
            public void run() {
                float pixelDensity = context.getResources().getDisplayMetrics().density;

                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS receipts (id INT(3) PRIMARY KEY, receipt VARCHAR, date VARCHAR, amount VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM receipts", null);

                int receiptIndex = c.getColumnIndex("receipt");
                int dateIndex = c.getColumnIndex("date");
                int amountIndex = c.getColumnIndex("amount");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i) {
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
                    if (i == 0) {
                        findViewById(R.id.noData).setVisibility(View.INVISIBLE);
                        blockParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    }
                    blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                    block.setLayoutParams(blockParams);
                    block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                    block.setGravity(Gravity.CENTER_VERTICAL);
                    block.setOrientation(LinearLayout.VERTICAL);
                    block.setAlpha(0);
                    block.animate().alpha(1);

                    /*
                        The amount TextView
                     */
                    String amountString = "₹" + separateAmount(c.getString(amountIndex)) + "/-";

                    TextView amount = new TextView(context);
                    TableRow.LayoutParams amountParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    amountParams.setMarginStart((int) (20 * pixelDensity));
                    amountParams.setMarginEnd((int) (20 * pixelDensity));
                    amountParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                    amount.setLayoutParams(amountParams);
                    amount.setText(amountString);
                    amount.setTextColor(getColor(R.color.colorPrimary));
                    amount.setTextSize(20);
                    amount.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                    block.addView(amount); //Adding amount to block

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
                        The receipt number TextView
                     */
                    TextView receipt = new TextView(context);
                    TableRow.LayoutParams receiptParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    receiptParams.setMarginStart((int) (20 * pixelDensity));
                    receiptParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    receipt.setLayoutParams(receiptParams);
                    receipt.setText(c.getString(receiptIndex));
                    receipt.setTextColor(getColor(R.color.colorPrimary));
                    receipt.setTextSize(16);
                    receipt.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(receipt); //Adding receipt to innerBlock

                    /*
                        The date TextView
                     */
                    TextView date = new TextView(context);
                    TableRow.LayoutParams dateParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    dateParams.setMarginEnd((int) (20 * pixelDensity));
                    dateParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                    date.setLayoutParams(dateParams);
                    date.setText(c.getString(dateIndex));
                    date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    date.setTextColor(getColor(R.color.colorPrimary));
                    date.setTextSize(16);
                    date.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                    innerBlock.addView(date);   //Adding date to innerBlock

                    block.addView(innerBlock);  //Adding innerBlock to block

                    /*
                        Finally adding the block to the activity
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            receipts.addView(block);
                        }
                    });

                    c.moveToNext();
                }

                c.close();
                myDatabase.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newReceipts").apply();
            }
        }).start();
    }
}