package tk.therealsuji.vtopchennai.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.Arrays;
import java.util.Objects;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.services.VTOP;

public class VTOPHelper {
    boolean isBound;
    Context context;
    Initiator initiator;
    Intent vtopServiceIntent;
    SharedPreferences sharedPreferences;
    VTOP vtopService;

    Dialog captchaDialog, semesterDialog;
    ReCaptchaDialogFragment reCaptchaDialogFragment;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            VTOP.ServiceBinder serviceBinder = (VTOP.ServiceBinder) iBinder;

            vtopService = serviceBinder.getService();
            isBound = true;
            initiator.onLoading(true);

            serviceBinder.setCallback(new VTOP.Callback() {

                @Override
                public void onRequestCaptcha(int captchaType, Bitmap bitmap, WebView webView) {
                    if (captchaType == VTOP.CAPTCHA_DEFAULT) {
                        View captchaLayout = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_dialog_captcha_default, null);
                        ImageView captchaImage = captchaLayout.findViewById(R.id.image_view_captcha);
                        captchaImage.setImageBitmap(bitmap);

                        captchaDialog = new MaterialAlertDialogBuilder(context)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                                .setOnCancelListener(dialogInterface -> {
                                    try {
                                        vtopService.endService(false);
                                    } catch (Exception ignored) {
                                    }
                                })
                                .setTitle(R.string.solve_captcha)
                                .setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                                    TextView captchaText = captchaLayout.findViewById(R.id.edit_text_captcha);
                                    vtopService.signIn("captchaCheck=" + captchaText.getText());
                                })
                                .setView(captchaLayout)
                                .create();

                        Drawable background = captchaDialog.getWindow().getDecorView().getBackground();
                        if (background instanceof InsetDrawable) {
                            background = ((InsetDrawable) background).getDrawable();

                            if (background instanceof MaterialShapeDrawable && ((MaterialShapeDrawable) background).getFillColor() != null) {
                                // Getting the color and elevation of the dialog background
                                int backgroundColor = Objects.requireNonNull(((MaterialShapeDrawable) background).getFillColor()).getDefaultColor();
                                float backgroundElevation = ((MaterialShapeDrawable) background).getElevation();
                                float[] colorMatrix = {
                                        0, 0, 0, 0, 255,    // red
                                        0, 0, 0, 0, 255,    // green
                                        0, 0, 0, 0, 255,    // blue
                                        0, 0, 0, 1, 0,      // alpha
                                };

                                // Updating the color matrix based on the application theme
                                int appearance = SettingsRepository.getTheme(context);
                                if (appearance == SettingsRepository.THEME_NIGHT || appearance == SettingsRepository.THEME_SYSTEM_NIGHT) {
                                    colorMatrix[0] = (Color.red(backgroundColor) - 255f) / 255f;
                                    colorMatrix[6] = (Color.green(backgroundColor) - 255f) / 255f;
                                    colorMatrix[12] = (Color.blue(backgroundColor) - 255f) / 255f;
                                } else {
                                    colorMatrix[0] = Color.red(backgroundColor) / 255f;
                                    colorMatrix[6] = Color.green(backgroundColor) / 255f;
                                    colorMatrix[12] = Color.blue(backgroundColor) / 255f;

                                    colorMatrix[4] = 0;
                                    colorMatrix[9] = 0;
                                    colorMatrix[14] = 0;
                                }

                                // Setting the alpha value for the captcha overlay
                                int elevationOverlayColor = MaterialColors.getColor(context, R.attr.elevationOverlayColor, 0);
                                ColorDrawable overlay = new ColorDrawable(elevationOverlayColor);
                                overlay.setAlpha(new ElevationOverlayProvider(context).calculateOverlayAlpha(backgroundElevation));

                                // Updating the captcha image colors and adding the overlay
                                captchaImage.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                                captchaImage.setForeground(overlay);
                            }
                        }

                        captchaDialog.setCanceledOnTouchOutside(false);
                        captchaDialog.show();
                    } else {
                        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

                        float pixelDensity = displayMetrics.density;
                        float width = displayMetrics.widthPixels;
                        float scale = (width / pixelDensity - 80) / 300;

                        if (scale > 1) {
                            scale = 1;
                        }

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
                                "        clearInterval(captchaInterval);" +
                                "        captcha.children[0].style.display = 'none';" +
                                "        captcha.children[1].style.transform = 'scale(" + scale + ")';" +
                                "        captcha.children[1].style.transformOrigin = '0 0';" +
                                "    }" +
                                "}, 200);" +
                                "})();", value -> {
                            reCaptchaDialogFragment = new ReCaptchaDialogFragment(webView, () -> {
                                try {
                                    vtopService.endService(false);
                                } catch (Exception ignored) {
                                }
                            });

                            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
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
                            .setOnCancelListener(dialogInterface -> {
                                try {
                                    vtopService.endService(false);
                                } catch (Exception ignored) {
                                }
                            })
                            .setPositiveButton(R.string.select, (dialogInterface, i) -> {
                                vtopService.setSemester(semesters[checkedItem[0]]);
                                sharedPreferences.edit().putString("semester", semesters[checkedItem[0]]).apply();
                            })
                            .setTitle(R.string.select_semester)
                            .create();

                    semesterDialog.setCanceledOnTouchOutside(false);
                    semesterDialog.show();
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

                    initiator.onLoading(false);
                }

                @Override
                public void onComplete() {
                    initiator.onComplete();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            vtopService = null;
            initiator.onLoading(false);
        }
    };

    public VTOPHelper(Context context, Initiator initiator) {
        this.vtopServiceIntent = new Intent(context, VTOP.class);
        this.context = context;
        this.initiator = initiator;
        this.sharedPreferences = SettingsRepository.getSharedPreferences(context.getApplicationContext());
    }

    public void start() {
        ContextCompat.startForegroundService(this.context, this.vtopServiceIntent);
    }

    public void bind() {
        this.context.bindService(this.vtopServiceIntent, this.serviceConnection, 0);
    }

    public void unbind() {
        if (this.captchaDialog != null) {
            this.captchaDialog.cancel();
        }

        if (this.semesterDialog != null) {
            this.semesterDialog.cancel();
        }

        if (this.reCaptchaDialogFragment != null) {
            this.reCaptchaDialogFragment.dismiss();
        }

        if (this.isBound) {
            if (this.vtopService != null) {
                this.vtopService.clearCallback();
                this.vtopService.endService(false);
            }

            this.context.unbindService(serviceConnection);
            this.initiator.onLoading(false);
            this.isBound = false;
        }
    }

    public boolean isBound() {
        return this.isBound;
    }

    public interface Initiator {
        void onLoading(boolean isLoading);

        void onComplete();
    }

    public static class ReCaptchaDialogFragment extends DialogFragment {
        boolean isDismissed;
        OnCancelListener onCancelListener;
        WebView webView;

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
        public void dismiss() {
            super.dismiss();
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
}
