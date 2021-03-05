package tk.therealsuji.vtopchennai;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences, encryptedSharedPreferences;
    Dialog download;
    VTOP vtop;

    public void signIn(View view) {
        hideKeyboard(this.getCurrentFocus());

        if (download != null) {
            download.dismiss();
            download = null;
        }

        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        encryptedSharedPreferences.edit().putString("username", username).apply();
        encryptedSharedPreferences.edit().putString("password", password).apply();

        download = new Dialog(this);
        download.setContentView(R.layout.dialog_download);
        download.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        download.setCanceledOnTouchOutside(false);

//        This part was commented because in some rare cases, the algorithm gets stuck
//        at loading and the only way to come out of it would be to close the application.
//        The statement below disables the back button when dialog_dialog is opened to
//        prevent it from closing during a download.

//        download.setCancelable(false);

        download.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                vtop.terminateDownload();
            }
        });

        download.show();

        Window window = download.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        vtop.start(download);

        /*
            Remove any non-encrypted credentials
         */
        sharedPreferences.edit().remove("username").apply();
        sharedPreferences.edit().remove("password").apply();
    }

    public void submitCaptcha(View view) {
        hideKeyboard(download.getCurrentFocus());
        vtop.hideLayouts();

        String username = encryptedSharedPreferences.getString("username", null);
        String password = encryptedSharedPreferences.getString("password", null);

        /*
            If the credentials aren't encrypted
         */
        if (username == null) {
            /*
                Get the non-encrypted credentials
             */
            username = sharedPreferences.getString("username", null);
            password = sharedPreferences.getString("password", null);

            /*
                Encrypt them
             */
            encryptedSharedPreferences.edit().putString("username", username).apply();
            encryptedSharedPreferences.edit().putString("password", password).apply();

            /*
                Remove the non-encrypted credentials
             */
            sharedPreferences.edit().remove("username").apply();
            sharedPreferences.edit().remove("password").apply();
        }

        EditText captchaView = download.findViewById(R.id.captcha);
        String captcha = captchaView.getText().toString();
        vtop.signIn(username, password, captcha);
    }

    public void selectSemester(View view) {
        vtop.hideLayouts();

        Spinner selectSemester = download.findViewById(R.id.selectSemester);
        String semester = selectSemester.getSelectedItem().toString().toLowerCase();

        if (!sharedPreferences.getString("semester", "null").equals(semester)) {
            sharedPreferences.edit().putBoolean("newTimetable", true).apply();
            sharedPreferences.edit().putBoolean("newFaculty", true).apply();
            sharedPreferences.edit().remove("newExams").apply();
            sharedPreferences.edit().remove("newMarks").apply();
            sharedPreferences.edit().remove("newGrades").apply();

            sharedPreferences.edit().putString("semester", semester).apply();
            vtop.getSemesterID(semester);
            return;
        }

        int lastDownload = vtop.getLastDownload();
        switch (lastDownload) {
            case 1:
                vtop.downloadTimetable();
                break;
            case 2:
                vtop.downloadFaculty();
                break;
            case 3:
                vtop.downloadProctor();
                break;
            case 4:
                vtop.downloadDeanHOD();
                break;
            case 5:
                vtop.downloadAttendance();
                break;
            case 6:
                vtop.downloadExams();
                break;
            case 7:
                vtop.downloadMarks();
                break;
            case 8:
                vtop.downloadGrades();
                break;
            case 9:
                vtop.downloadGradeHistory();
            case 10:
                vtop.downloadMessages();
                break;
            case 11:
                vtop.downloadProctorMessages();
                break;
            case 12:
                vtop.downloadSpotlight();
                break;
            case 13:
                vtop.downloadReceipts();
                break;
            default:
                vtop.getSemesterID(semester);
        }
    }

    public void cancelDownload(View view) {
        download.dismiss();
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openPrivacy(View view) {
        startActivity(new Intent(LoginActivity.this, PrivacyActivity.class));
    }

    public void openUpdate(View view) {
        String link = "http://vtopchennai.therealsuji.tk";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);

        try {
            MasterKey masterKey = new MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    "credentials",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
            The below code is just to addd some beautiful animation to the views
         */
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    username.animate().scaleX(1.07f).scaleY(1.07f).setDuration(200);
                } else {
                    username.animate().scaleX(1f).scaleY(1f).setDuration(200);
                }
            }
        });
        username.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    password.requestFocus();
                }

                return false;
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    password.animate().scaleX(1.07f).scaleY(1.07f).setDuration(200);
                } else {
                    password.animate().scaleX(1f).scaleY(1f).setDuration(200);
                }
            }
        });
        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    signIn(null);
                }

                return false;
            }
        });

        final Button signIn = findViewById(R.id.signIn);
        signIn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signIn.animate().scaleX(0.93f).scaleY(0.93f).setDuration(50);
                        signIn.setAlpha(0.85f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        signIn.animate().scaleX(1f).scaleY(1f).setDuration(50);
                        signIn.setAlpha(1f);
                        break;
                }
                return false;
            }
        });

        /*
            Locally check for a new version (The actually checking is done in the SplashScreenActivity)
         */
        int versionCode = BuildConfig.VERSION_CODE;
        int latestVersion = sharedPreferences.getInt("latest", versionCode);

        if (versionCode < latestVersion) {
            Dialog update = new Dialog(this);
            update.setContentView(R.layout.dialog_update);
            update.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            update.show();

            Window window = update.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        /*
            Initialising the VTOP WebView before hand to speed things up for the user
            because for some reason, initialising WebView's take a second
         */
        vtop = new VTOP(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (vtop != null) {
            vtop.terminateDownload();
        }
    }
}