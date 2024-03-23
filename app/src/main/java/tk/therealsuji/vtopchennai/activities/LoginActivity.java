package tk.therealsuji.vtopchennai.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import tk.therealsuji.vtopchennai.BuildConfig;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.fragments.dialogs.UpdateDialogFragment;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.helpers.VTOPHelper;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences encryptedSharedPreferences, sharedPreferences;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    VTOPHelper vtopHelper;

    public void signIn() {
        hideKeyboard(this.getCurrentFocus());

        EditText usernameView = findViewById(R.id.edit_text_username);
        EditText passwordView = findViewById(R.id.edit_text_password);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        encryptedSharedPreferences.edit().putString("username", username).apply();
        encryptedSharedPreferences.edit().putString("password", password).apply();

        this.vtopHelper.bind();
        this.vtopHelper.start();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.encryptedSharedPreferences = SettingsRepository.getEncryptedSharedPreferences(getApplicationContext());
        this.sharedPreferences = SettingsRepository.getSharedPreferences(getApplicationContext());

        findViewById(R.id.button_sign_in).setOnClickListener(view -> signIn());
        findViewById(R.id.button_privacy).setOnClickListener(view -> SettingsRepository.openWebViewActivity(
                this,
                getString(R.string.privacy),
                SettingsRepository.APP_PRIVACY_URL
        ));

        this.vtopHelper = new VTOPHelper(this, new VTOPHelper.Initiator() {
            @Override
            public void onLoading(boolean isLoading) {
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
            public void onForceSignOut() {
                // User is already signed out so do nothing
            }

            @Override
            public void onComplete() {
                startMainActivity();
            }
        });

        /*
            Check for updates
         */
        SettingsRepository.fetchAboutJson(true)
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull JSONObject about) {
                        try {
                            int versionCode = about.getInt("versionCode");
                            String versionName = about.getString("tagName");
                            String releaseNotes = about.getString("releaseNotes");

                            if (versionCode > BuildConfig.VERSION_CODE) {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                transaction.add(android.R.id.content, UpdateDialogFragment.newInstance(versionName, releaseNotes)).addToBackStack(null).commit();
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.vtopHelper.bind();

        if (!this.vtopHelper.isBound() && this.sharedPreferences.getBoolean("isSignedIn", false)) {
            this.startMainActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Firebase Analytics Logging
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "LoginActivity");
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.vtopHelper.unbind();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
