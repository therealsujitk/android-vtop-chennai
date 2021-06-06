package tk.therealsuji.vtopchennai;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ReportBugActivity extends AppCompatActivity {
    LinearLayout logs;
    Dialog clear;
    Context context;

    boolean terminateThread;

    public void sendLog(String errorCode, String error) throws UnsupportedEncodingException {
//        String style = "<style>" +
//                "    pre {" +
//                "        background: #282b2e;" +
//                "        color: #a9b7c6;" +
//                "        padding: 15px;" +
//                "        border-radius: 7px;" +
//                "        overflow: auto;" +
//                "    }" +
//                "" +
//                "    code.comment {" +
//                "        color: #808080;" +
//                "    }" +
//                "" +
//                "    code.error {" +
//                "        color: #cc7832;" +
//                "    }" +
//                "" +
//                "    code.warning {" +
//                "        color: #ffc66d;" +
//                "    }" +
//                "    " +
//                "    code.debug {" +
//                "        color: #6897bb;" +
//                "    }" +
//                "    " +
//                "    code.tip {" +
//                "        color: #6a8759;" +
//                "    }" +
//                "</style>";
//
//        String html = style + "<pre>" + error + "</pre>";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:me@therealsuji.tk" +
                "?subject=VTOP Chennai - Bug Report (" + errorCode + ")" +
                "&body=" + URLEncoder.encode(error, String.valueOf(StandardCharsets.UTF_8)).replaceAll("\\+", " ").replaceAll("%5Cn", "\n")));
        startActivity(intent);
    }

    public void clearLogs(View view) {
        clear.dismiss();

        View loading = findViewById(R.id.loading);
        loading.setAlpha(1);

        new Thread(() -> {
            SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
            myDatabase.execSQL("DELETE FROM error_logs");
            myDatabase.close();
            runOnUiThread(() -> {
                logs.removeAllViews();
                findViewById(R.id.noLogs).setVisibility(View.VISIBLE);
                loading.animate().alpha(0);
            });
        }).start();
    }

    public void openClearLogs(MenuItem item) {
        if (clear != null) {
            clear.dismiss();
        }

        clear = new Dialog(this);
        clear.setContentView(R.layout.dialog_clear_logs);
        clear.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        clear.show();

        Window window = clear.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_bug);

        logs = findViewById(R.id.logs);
        context = this;

        new Thread(() -> {
            SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS error_logs (id INTEGER PRIMARY KEY, error_code VARCHAR, date VARCHAR, error VARCHAR)");
            Cursor c = myDatabase.rawQuery("SELECT * FROM error_logs ORDER BY id DESC", null);

            int errorCodeIndex = c.getColumnIndex("error_code");
            int dateIndex = c.getColumnIndex("date");
            int errorIndex = c.getColumnIndex("error");
            c.moveToFirst();

            CardGenerator myLog = new CardGenerator(context, CardGenerator.CARD_LOG);
            LinkButtonGenerator myLink = new LinkButtonGenerator(context);

            for (int i = 0; i < c.getCount(); ++i) {
                if (terminateThread) {
                    return;
                }

                if (i == 0) {
                    runOnUiThread(() -> findViewById(R.id.noLogs).setVisibility(View.GONE));
                }

                String errorCode = "Error " + c.getString(errorCodeIndex);
                String date = c.getString(dateIndex);
                String error = c.getString(errorIndex);

                final LinearLayout card = myLog.generateCard(errorCode, date);
                final LinearLayout linkView = myLink.generateButton(null, LinkButtonGenerator.LINK_REPORT);
                linkView.setTag(error);
                linkView.setOnClickListener(v -> {
                    try {
                        sendLog(errorCode, error);
                    } catch (UnsupportedEncodingException ignored) {
                    }
                });

                card.addView(linkView);
                card.setAlpha(0);
                card.animate().alpha(1);

                /*
                    Adding the card to the activity
                 */
                runOnUiThread(() -> logs.addView(card));

                c.moveToNext();
            }

            runOnUiThread(() -> findViewById(R.id.loading).animate().alpha(0));

            c.close();
            myDatabase.close();
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.report_bug_menu, menu);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}