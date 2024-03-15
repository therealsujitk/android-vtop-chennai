package tk.therealsuji.vtopchennai.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import tk.therealsuji.vtopchennai.R;

public class ReCaptchaDialogFragment extends DialogFragment {
    boolean isDismissed;
    OnCancelListener onCancelListener;
    WebView webView;

    public ReCaptchaDialogFragment() {
        // Required empty public constructor
    }

    // TODO: Remove this constructor, android fragments should never be created using a non-empty
    //       constructor as android can sometimes re-create them with the empty constructor.
    public ReCaptchaDialogFragment(@NonNull WebView webView, @NonNull OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        this.webView = webView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogFragment = inflater.inflate(R.layout.layout_dialog_captcha_grecaptcha, container, false);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        ViewGroup webViewParent = (ViewGroup) webView.getParent();
        if (webViewParent != null) {
            webViewParent.removeView(webView);
        }

        RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        webViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        webViewParams.setMargins(
                (int) (40 * pixelDensity),
                (int) (40 * pixelDensity),
                (int) (40 * pixelDensity),
                (int) (40 * pixelDensity)
        );

        webView.setLayoutParams(webViewParams);
        ((RelativeLayout) dialogFragment.findViewById(R.id.relative_layout_container)).addView(webView);

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
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
        this.isDismissed = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (!this.isDismissed) {
            this.onCancelListener.onCancel();
        }
    }

    public interface OnCancelListener {
        void onCancel();
    }
}
