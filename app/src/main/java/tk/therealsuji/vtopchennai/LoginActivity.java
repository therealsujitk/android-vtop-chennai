package tk.therealsuji.vtopchennai;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.services.VTOP;

public class LoginActivity extends AppCompatActivity {
    AlertDialog vtopDialog;
    boolean isBound;
    Context context;
    Intent vtopIntent;
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
                    MaterialAlertDialogBuilder captchaDialogBuilder = new MaterialAlertDialogBuilder(context)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                            .setOnCancelListener(dialogInterface -> vtopService.endService(false))
                            .setTitle(R.string.solve_captcha);

                    if (captchaType == VTOP.CAPTCHA_DEFAULT) {
                        View captchaLayout = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_download_captcha, null);
                        ImageView captchaImage = captchaLayout.findViewById(R.id.captcha_image_view);
                        captchaImage.setImageBitmap(bitmap);

                        vtopDialog = captchaDialogBuilder
                                .setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                                    TextView captchaText = captchaLayout.findViewById(R.id.captcha_edit_text);
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
                            ViewGroup webViewParent = (ViewGroup) webView.getParent();
                            if (webViewParent != null) {
                                webViewParent.removeView(webView);
                            }

                            float pixelDensity = context.getResources().getDisplayMetrics().density;

                            RelativeLayout container = new RelativeLayout(context);
                            container.setPadding(
                                    (int) (30 * pixelDensity),
                                    (int) (10 * pixelDensity),
                                    (int) (30 * pixelDensity),
                                    0
                            );

                            ProgressBar loadingIndicator = new ProgressBar(context);
                            RelativeLayout.LayoutParams loadingIndicatorParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                            );
                            loadingIndicator.setLayoutParams(loadingIndicatorParams);
                            loadingIndicator.setPadding(0, (int) (10 * pixelDensity), 0, 0);

                            container.addView(loadingIndicator);
                            container.addView(webView);

                            vtopDialog = captchaDialogBuilder
                                    .setView(container)
                                    .show();
                        });
                    }
                }

                @Override
                public void onCaptchaComplete() {
                    if (vtopDialog != null) {
                        vtopDialog.dismiss();
                    }
                }

                @Override
                public void onRequestSemester(String[] semesters) {
                    vtopDialog = new MaterialAlertDialogBuilder(context)
                            .setItems(semesters, (dialogInterface, i) -> vtopService.setSemester(semesters[i]))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                            .setOnCancelListener(dialogInterface -> vtopService.endService(false))
                            .setTitle(R.string.select_semester)
                            .show();
                }

                @Override
                public void onServiceEnd() {
                    if (vtopDialog != null) {
                        vtopDialog.dismiss();
                        setLoading(false);
                    }
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

        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        encryptedSharedPreferences.edit().putString("username", username).apply();
        encryptedSharedPreferences.edit().putString("password", password).apply();

        bindService(this.vtopIntent, this.serviceConnection, 0);
        ContextCompat.startForegroundService(this, this.vtopIntent);
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
            findViewById(R.id.sign_in_text).setVisibility(View.INVISIBLE);
            findViewById(R.id.loading).setVisibility(View.VISIBLE);

            findViewById(R.id.sign_in_button).setEnabled(false);
            findViewById(R.id.username).setEnabled(false);
            findViewById(R.id.password).setEnabled(false);
        } else {
            findViewById(R.id.sign_in_text).setVisibility(View.VISIBLE);
            findViewById(R.id.loading).setVisibility(View.INVISIBLE);

            findViewById(R.id.sign_in_button).setEnabled(true);
            findViewById(R.id.username).setEnabled(true);
            findViewById(R.id.password).setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        int visibility = getWindow().getDecorView().getSystemUiVisibility();

        if (SettingsRepository.getTheme(this) == SettingsRepository.THEME_DAY) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(visibility);

        ConstraintLayout loginLayout = findViewById(R.id.layout_login);
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

        findViewById(R.id.sign_in_button).setOnClickListener(view -> signIn());
        findViewById(R.id.button_privacy).setOnClickListener(view -> SettingsRepository.openWebViewActivity(
                this,
                getString(R.string.privacy),
                SettingsRepository.APP_PRIVACY_URL
        ));

        /*
            Locally check for a new version (The actually checking is done in the SplashScreenActivity)
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

        this.vtopIntent = new Intent(this, VTOP.class);
        this.vtopIntent.putExtra("colorPrimary", MaterialColors.getColor(this, R.attr.colorPrimary, 0));
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        bindService(this.vtopIntent, this.serviceConnection, 0);

        if (!this.isBound && this.sharedPreferences.getBoolean("isSignedIn", false)) {
            this.startMainActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.vtopDialog != null) {
            this.vtopDialog.dismiss();
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