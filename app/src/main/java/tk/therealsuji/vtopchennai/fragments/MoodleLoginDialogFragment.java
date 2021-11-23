package tk.therealsuji.vtopchennai.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.color.MaterialColors;

import tk.therealsuji.vtopchennai.R;

public class MoodleLoginDialogFragment extends DialogFragment {
    EditText username, password;

    private void signIn(String username, String password) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogFragment = inflater.inflate(R.layout.activity_login, container, false);
        dialogFragment.getRootView().setBackgroundColor(MaterialColors.getColor(this.requireContext(), R.attr.colorPrimaryContainer, 0));
        ((TextView) dialogFragment.findViewById(R.id.text_view_description)).setText(R.string.moodle_login_description);

        this.username = dialogFragment.findViewById(R.id.edit_text_username);
        this.password = dialogFragment.findViewById(R.id.edit_text_password);

        this.username.setInputType(InputType.TYPE_CLASS_TEXT);

        dialogFragment.findViewById(R.id.button_sign_in).setOnClickListener(view -> {
            String username = this.username.getText().toString();
            String password = this.password.getText().toString();
            this.signIn(username, password);
        });

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
