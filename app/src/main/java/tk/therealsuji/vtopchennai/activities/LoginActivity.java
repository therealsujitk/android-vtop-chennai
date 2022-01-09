package tk.therealsuji.vtopchennai.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;

import tk.therealsuji.vtopchennai.BuildConfig;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.fragments.ReCaptchaDialogFragment;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.services.VTOP;

public class LoginActivity extends AppCompatActivity {
    boolean isBound;
    Context context;
    Dialog captchaDialog, semesterDialog;
    Intent vtopServiceIntent;
    ReCaptchaDialogFragment reCaptchaDialogFragment;
    SharedPreferences encryptedSharedPreferences, sharedPreferences;
    VTOP vtopService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            VTOP.ServiceBinder serviceBinder = (VTOP.ServiceBinder) iBinder;

            vtopService = serviceBinder.getService();
            isBound = true;
            setLoading(true);

            serviceBinder.setCallback(new VTOP.Callback() {

                @Override
                public void onRequestCaptcha(int captchaType, Bitmap bitmap, WebView webView) {
                    if (captchaType == VTOP.CAPTCHA_DEFAULT) {
                        View captchaLayout = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_dialog_captcha_default, null);
                        ImageView captchaImage = captchaLayout.findViewById(R.id.image_view_captcha);
                        captchaImage.setImageBitmap(bitmap);

                        captchaDialog = new MaterialAlertDialogBuilder(context)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                                .setOnCancelListener(dialogInterface -> vtopService.endService(false))
                                .setTitle(R.string.solve_captcha)
                                .setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                                    TextView captchaText = captchaLayout.findViewById(R.id.edit_text_captcha);
                                    vtopService.signIn("captchaCheck=" + captchaText.getText());
                                })
                                .setView(captchaLayout)
                                .show();
                    } else {
                        webView.evaluateJavascript("(function() {" +
                                "var body = document.getElementsByTagName('body')[0];" +
                                "body.style.backgroundColor = 'transparent';" +
                                "var children = body.children;" +
                                "for (var i = 0; i < children.length - 1; ++i) {" +
                                "    children[i].style.display = 'none';" +
                                "}" +
                                "var captchaInterval = setInterval(function() {" +
                                "    var children = document.getElementsByTagName('body')[0].children;" +
                                "    var captcha = children[children.length - 1];" +
                                "    if (captcha.children[0] != undefined && captcha.children[1] != undefined) {" +
                                "        captcha.children[0].style.display = 'none';" +
                                "        captcha.children[1].style.transform = 'scale(" + 1 + ")';" +
                                "        captcha.children[1].style.transformOrigin = '0 0';" +
                                "        clearInterval(captchaInterval);" +
                                "    }" +
                                "}, 500);" +
                                "})();", value -> {
                            reCaptchaDialogFragment = new ReCaptchaDialogFragment(webView);

                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.add(android.R.id.content, reCaptchaDialogFragment).addToBackStack(null).commit();
                        });
                    }
                }

                @Override
                public void onCaptchaComplete() {
                    if (captchaDialog != null) {
                        captchaDialog.dismiss();
                    }

                    if (reCaptchaDialogFragment != null) {
                        reCaptchaDialogFragment.dismiss();
                    }
                }

                @Override
                public void onRequestSemester(String[] semesters) {
                    String semester = sharedPreferences.getString("semester", null);
                    final int[] checkedItem = {Arrays.asList(semesters).indexOf(semester)};
                    if (checkedItem[0] == -1) checkedItem[0] = 0;

                    semesterDialog = new MaterialAlertDialogBuilder(context)
                            .setSingleChoiceItems(semesters, checkedItem[0], (dialogInterface, i) -> checkedItem[0] = i)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                            .setOnCancelListener(dialogInterface -> vtopService.endService(false))
                            .setPositiveButton(R.string.select, (dialogInterface, i) -> {
                                vtopService.setSemester(semesters[checkedItem[0]]);
                                sharedPreferences.edit().putString("semester", semesters[checkedItem[0]]).apply();
                            })
                            .setTitle(R.string.select_semester)
                            .show();
                }

                @Override
                public void onServiceEnd() {
                    if (captchaDialog != null) {
                        captchaDialog.dismiss();
                    }

                    if (reCaptchaDialogFragment != null) {
                        reCaptchaDialogFragment.dismiss();
                    }

                    if (semesterDialog != null) {
                        semesterDialog.dismiss();
                    }

                    setLoading(false);
                }

                @Override
                public void onComplete() {
                    startMainActivity();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            setLoading(false);
        }
    };

    public void signIn() {
        hideKeyboard(this.getCurrentFocus());

        EditText usernameView = findViewById(R.id.edit_text_username);
        EditText passwordView = findViewById(R.id.edit_text_password);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        encryptedSharedPreferences.edit().putString("username", username).apply();
        encryptedSharedPreferences.edit().putString("password", password).apply();

        bindService(this.vtopServiceIntent, this.serviceConnection, 0);
        ContextCompat.startForegroundService(this, this.vtopServiceIntent);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            findViewById(R.id.text_view_sign_in).setVisibility(View.INVISIBLE);
            findViewById(R.id.progress_bar_loading).setVisibility(View.VISIBLE);

            findViewById(R.id.button_sign_in).setEnabled(false);
            findViewById(R.id.edit_text_username).setEnabled(false);
            findViewById(R.id.edit_text_password).setEnabled(false);
        } else {
            findViewById(R.id.text_view_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.progress_bar_loading).setVisibility(View.INVISIBLE);

            findViewById(R.id.button_sign_in).setEnabled(true);
            findViewById(R.id.edit_text_username).setEnabled(true);
            findViewById(R.id.edit_text_password).setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ConstraintLayout loginLayout = findViewById(R.id.constraint_layout_login);
        loginLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.setMargins(
                    windowInsets.getSystemWindowInsetLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    windowInsets.getSystemWindowInsetRight(),
                    windowInsets.getSystemWindowInsetBottom()
            );
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        this.context = this;
        this.encryptedSharedPreferences = SettingsRepository.getEncryptedSharedPreferences(this);
        this.sharedPreferences = SettingsRepository.getSharedPreferences(this);

        findViewById(R.id.button_sign_in).setOnClickListener(view -> signIn());
        findViewById(R.id.button_privacy).setOnClickListener(view -> SettingsRepository.openWebViewActivity(
                this,
                getString(R.string.privacy),
                SettingsRepository.APP_PRIVACY_URL
        ));

        /*
            Locally check for a new version (The actually checking is done in the LauncherActivity)
         */
        int versionCode = BuildConfig.VERSION_CODE;
        int latestVersion = this.sharedPreferences.getInt("latest", versionCode);

        if (versionCode < latestVersion) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.update_message)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton(R.string.update, (dialogInterface, i) -> SettingsRepository.openDownloadPage(this))
                    .setTitle(R.string.update_title)
                    .show();
        }

        this.vtopServiceIntent = new Intent(this, VTOP.class);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        bindService(this.vtopServiceIntent, this.serviceConnection, 0);

        if (!this.isBound && this.sharedPreferences.getBoolean("isSignedIn", false)) {
            this.startMainActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.semesterDialog != null) {
            this.semesterDialog.cancel();
        }

        if (reCaptchaDialogFragment != null) {
            reCaptchaDialogFragment.dismiss();
        }

        if (semesterDialog != null) {
            semesterDialog.dismiss();
        }

        if (this.isBound) {
            if (this.vtopService != null) {
                this.vtopService.clearCallback();
                this.vtopService.endService(true);
            }

            unbindService(serviceConnection);
            this.setLoading(false);
            this.isBound = false;
        }
    }
}