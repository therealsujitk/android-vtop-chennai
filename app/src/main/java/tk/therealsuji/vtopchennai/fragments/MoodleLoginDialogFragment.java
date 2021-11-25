package tk.therealsuji.vtopchennai.fragments;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.color.MaterialColors;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class MoodleLoginDialogFragment extends DialogFragment {
    Button signIn;
    EditText username, password;

    private void signIn() {
        this.setLoading(true);

        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        RequestQueue requestQueue = Volley.newRequestQueue(this.requireContext());
        String url = Uri.parse(SettingsRepository.MOODLE_LOGIN_URL)
                .buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .appendQueryParameter("service", "moodle_mobile_app")
                .toString();

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("error")) {
                            Toast.makeText(this.requireContext(), response.getString("error"), Toast.LENGTH_SHORT).show();
                        } else if (response.has("token")) {
                            SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(this.requireContext().getApplicationContext());
                            sharedPreferences.edit().putString("moodleToken", response.getString("token")).apply();
                            sharedPreferences.edit().putString("moodlePrivateToken", response.getString("privatetoken")).apply();

                            this.dismiss();
                        }
                    } catch (Exception ignored) {
                    } finally {
                        this.setLoading(false);
                    }
                },
                error -> this.setLoading(false)
        );

        requestQueue.add(stringRequest);
    }

    private void setLoading(boolean loading) {
        this.username.setEnabled(loading);
        this.password.setEnabled(loading);
        this.signIn.setEnabled(loading);
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

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
