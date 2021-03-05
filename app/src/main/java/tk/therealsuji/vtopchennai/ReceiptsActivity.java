package tk.therealsuji.vtopchennai;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ReceiptsActivity extends AppCompatActivity {
    boolean terminateThread;

    private String convertToReadableAmount(String amount) {
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
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS receipts (id INT(3) PRIMARY KEY, receipt VARCHAR, date VARCHAR, amount VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT * FROM receipts", null);

                int receiptIndex = c.getColumnIndex("receipt");
                int dateIndex = c.getColumnIndex("date");
                int amountIndex = c.getColumnIndex("amount");
                c.moveToFirst();

                CardGenerator myReceipt = new CardGenerator(context, CardGenerator.CARD_RECEIPT);

                for (int i = 0; i < c.getCount(); ++i) {
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

                    String amount = "₹" + convertToReadableAmount(c.getString(amountIndex));
                    String receiptNo = c.getString(receiptIndex);
                    String date = c.getString(dateIndex);

                    final LinearLayout card = myReceipt.generateCard(amount, receiptNo, date);
                    card.setAlpha(0);
                    card.animate().alpha(1);

                    /*
                        Adding the block to the activity
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            receipts.addView(card);
                        }
                    });

                    c.moveToNext();
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
                sharedPreferences.edit().remove("newReceipts").apply();
            }
        }).start();

        /*
            Show the due payments dialog if there are any
         */
        SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("duePayments", false)) {
            Dialog duePayments = new Dialog(this);
            duePayments.setContentView(R.layout.dialog_due_payments);
            duePayments.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            duePayments.show();

            Window window = duePayments.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}