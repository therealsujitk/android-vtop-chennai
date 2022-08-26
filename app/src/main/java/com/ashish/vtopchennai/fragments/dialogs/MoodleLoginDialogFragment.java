package com.ashish.vtopchennai.fragments.dialogs;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.color.MaterialColors;

import org.json.JSONObject;

import java.util.Objects;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.helpers.SettingsRepository;
import com.ashish.vtopchennai.interfaces.MoodleApi;

public class MoodleLoginDialogFragment extends DialogFragment {
    RelativeLayout signIn;
    EditText username, password;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    Function<Object, Object> callback;
    MoodleApi moodleApi;

    public MoodleLoginDialogFragment(Function<Object, Object> callback) {
        this.callback = callback;
    }

    private void signIn() {
        this.setLoading(true);

        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        this.moodleApi.signIn(username, password)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull ResponseBody responseBody) {
                        try {
                            JSONObject response = new JSONObject(responseBody.string());

                            if (response.has("error")) {
                                throw new Exception(response.getString("error"));
                            }

                            SharedPreferences encryptedSharedPreferences = Objects.requireNonNull(SettingsRepository.getEncryptedSharedPreferences(requireContext().getApplicationContext()));
                            encryptedSharedPreferences.edit().putString("moodleToken", response.getString("token")).apply();
                            encryptedSharedPreferences.edit().putString("moodlePrivateToken", response.getString("privatetoken")).apply();
                            SettingsRepository.getSharedPreferences(requireContext().getApplicationContext()).edit().putBoolean("isMoodleSignedIn", true).apply();

                            dismiss();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            signIn.findViewById(R.id.text_view_sign_in).setVisibility(View.INVISIBLE);
            signIn.findViewById(R.id.progress_bar_loading).setVisibility(View.VISIBLE);
        } else {
            signIn.findViewById(R.id.text_view_sign_in).setVisibility(View.VISIBLE);
            signIn.findViewById(R.id.progress_bar_loading).setVisibility(View.INVISIBLE);
        }

        this.username.setEnabled(!isLoading);
        this.password.setEnabled(!isLoading);
        this.signIn.setEnabled(!isLoading);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogFragment = inflater.inflate(R.layout.activity_login, container, false);
        dialogFragment.getRootView().setBackgroundColor(MaterialColors.getColor(this.requireContext(), R.attr.colorPrimaryContainer, 0));
        ((TextView) dialogFragment.findViewById(R.id.text_view_description)).setText(R.string.moodle_login_description);

        this.username = dialogFragment.findViewById(R.id.edit_text_username);
        this.password = dialogFragment.findViewById(R.id.edit_text_password);
        this.signIn = dialogFragment.findViewById(R.id.button_sign_in);

        this.username.setInputType(InputType.TYPE_CLASS_TEXT);
        this.signIn.setOnClickListener(view -> this.signIn());
        dialogFragment.findViewById(R.id.button_privacy).setOnClickListener(view -> SettingsRepository.openWebViewActivity(
                requireContext(),
                getString(R.string.privacy),
                SettingsRepository.APP_PRIVACY_URL
        ));

        this.moodleApi = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(SettingsRepository.MOODLE_BASE_URL)
                .build()
                .create(MoodleApi.class);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (SettingsRepository.isMoodleSignedIn(requireContext())) {
            this.callback.apply(null);
        }

        compositeDisposable.dispose();
    }
}
