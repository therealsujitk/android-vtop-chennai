package tk.therealsuji.vtopchennai.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.LoginActivity;
import tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment;
import tk.therealsuji.vtopchennai.fragments.ViewPagerFragment;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class ProfileItem extends LinearLayout {
    private static final ItemData[] PERSONAL_PROFILE_ITEMS = {
            new ItemData(R.string.courses, R.drawable.ic_courses, context -> SettingsRepository.openViewPagerFragment(
                    (FragmentActivity) context,
                    R.string.courses,
                    ViewPagerFragment.TYPE_COURSES
            )),
            new ItemData(R.string.receipts, R.drawable.ic_receipts, context -> SettingsRepository.openRecyclerViewFragment(
                    (FragmentActivity) context,
                    R.string.receipts,
                    RecyclerViewFragment.TYPE_RECEIPTS
            )),
            new ItemData(R.string.staff, R.drawable.ic_staff, context -> SettingsRepository.openViewPagerFragment(
                    (FragmentActivity) context,
                    R.string.staff,
                    ViewPagerFragment.TYPE_STAFF
            )),
            new ItemData(R.string.switch_semester, R.drawable.ic_semester, context -> {

            })
    };

    private static final ItemData[] APPLICATION_PROFILE_ITEMS = {
            new ItemData(R.string.appearance, R.drawable.ic_appearance, context -> {
                String[] themes = {
                        context.getString(R.string.light),
                        context.getString(R.string.dark),
                        context.getString(R.string.system)
                };

                SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(context);

                int checkedItem = 2;
                String theme = sharedPreferences.getString("appearance", "system");

                if (theme.equals("light")) {
                    checkedItem = 0;
                } else if (theme.equals("dark")) {
                    checkedItem = 1;
                }

                new MaterialAlertDialogBuilder(context)
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                        .setSingleChoiceItems(themes, checkedItem, (dialogInterface, i) -> {
                            if (i == 0) {
                                sharedPreferences.edit().putString("appearance", "light").apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            } else if (i == 1) {
                                sharedPreferences.edit().putString("appearance", "dark").apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                sharedPreferences.edit().remove("appearance").apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            }

                            dialogInterface.dismiss();
                        })
                        .setTitle(R.string.appearance)
                        .show();
            }),
            new ItemData(R.string.faq, R.drawable.ic_faq, context -> SettingsRepository.openWebViewActivity(
                    context,
                    context.getString(R.string.faq),
                    SettingsRepository.APP_FAQ_URL
            )),
            new ItemData(R.string.notifications, R.drawable.ic_notifications, context -> {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
                intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

                context.startActivity(intent);
            }),
            new ItemData(R.string.privacy, R.drawable.ic_privacy, context -> SettingsRepository.openWebViewActivity(
                    context,
                    context.getString(R.string.privacy),
                    SettingsRepository.APP_PRIVACY_URL
            )),
            new ItemData(R.string.send_feedback, R.drawable.ic_feedback, context -> {
                View bottomSheetLayout = View.inflate(context, R.layout.layout_feedback_bottom_sheet, null);
                bottomSheetLayout.findViewById(R.id.contact_developer).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.DEVELOPER_BASE_URL));
                bottomSheetLayout.findViewById(R.id.open_issue).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_ISSUE_URL));
                bottomSheetLayout.findViewById(R.id.request_feature).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_FEATURE_URL));

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(bottomSheetLayout);
                bottomSheetDialog.show();
            }),
            new ItemData(R.string.share, R.drawable.ic_share, context -> {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject));
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, SettingsRepository.APP_BASE_URL));
                intent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(intent, context.getString(R.string.share_title));
                context.startActivity(shareIntent);
            }),
            new ItemData(R.string.sign_out, R.drawable.ic_sign_out, context -> new MaterialAlertDialogBuilder(context)
                    .setMessage(R.string.sign_out_text)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton(R.string.sign_out, (dialogInterface, i) -> {
                        SettingsRepository.getSharedPreferences(context).edit().remove("isSignedIn").apply();
                        context.startActivity(new Intent(context, LoginActivity.class));
                        ((Activity) context).finish();
                    })
                    .setTitle(R.string.sign_out)
                    .show())
    };

    public static final ItemData[][] PROFILE_ITEMS = {
            PERSONAL_PROFILE_ITEMS,
            APPLICATION_PROFILE_ITEMS
    };

    AppCompatTextView title;
    ImageView icon;

    public ProfileItem(Context context) {
        super(context);

        this.initialize();
    }

    public ProfileItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initialize();
    }

    private void initialize() {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
        ColorStateList colorSecondaryTintList = ColorStateList.valueOf(colorSecondary.data);

        TypedValue colorOnBackground = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorOnBackground, colorOnBackground, true);

        this.icon = new ImageView(this.getContext());
        LayoutParams iconParams = new LayoutParams(
                (int) (45 * pixelDensity),
                (int) (45 * pixelDensity)
        );
        this.icon.setLayoutParams(iconParams);
        this.icon.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.background_profile_item_icon));
        this.icon.setImageTintList(colorSecondaryTintList);
        this.icon.setPadding(
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity)
        );

        this.addView(this.icon);

        this.title = new AppCompatTextView(this.getContext());
        this.title.setPadding((int) (20 * pixelDensity), 0, 0, 0);
        this.title.setTextColor(colorOnBackground.data);
        this.title.setTextSize(18);

        this.addView(this.title);

        TypedValue selectableItemBackground = new TypedValue();
        this.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true);
        this.setBackgroundResource(selectableItemBackground.resourceId);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setClickable(true);
        this.setFocusable(true);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setPadding(
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity)
        );
    }

    public void initializeProfileItem(int profileGroupIndex, int profileItemIndex) {
        ItemData itemData = PROFILE_ITEMS[profileGroupIndex][profileItemIndex];

        this.title.setText(this.getContext().getString(itemData.titleId));
        this.icon.setImageDrawable(ContextCompat.getDrawable(this.getContext(), itemData.iconId));
        this.setOnClickListener(view -> itemData.onClickListener.onClick(this.getContext()));
    }

    public static class ItemData {
        public final int titleId, iconId;
        public final OnClickListener onClickListener;

        public ItemData(int titleId, int iconId, OnClickListener onClickListener) {
            this.titleId = titleId;
            this.iconId = iconId;
            this.onClickListener = onClickListener;
        }

        private interface OnClickListener {
            void onClick(Context context);
        }
    }


}
