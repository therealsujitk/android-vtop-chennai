package tk.therealsuji.vtopchennai.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.LoginActivity;
import tk.therealsuji.vtopchennai.adapters.AnnouncementItemAdapter;
import tk.therealsuji.vtopchennai.adapters.ProfileGroupAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class ProfileFragment extends Fragment {
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
                View bottomSheetLayout = View.inflate(context, R.layout.layout_bottom_sheet_feedback, null);
                bottomSheetLayout.findViewById(R.id.text_view_contact_developer).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.DEVELOPER_BASE_URL));
                bottomSheetLayout.findViewById(R.id.text_view_open_issue).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_ISSUE_URL));
                bottomSheetLayout.findViewById(R.id.text_view_request_feature).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_FEATURE_URL));

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

    public static final AnnouncementItemData[] ANNOUNCEMENT_ITEMS = {
            new AnnouncementItemData(
                    R.drawable.ic_whats_new,
                    "VIT Student is now Open Source!",
                    "Click to view the source code.",
                    context -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_BASE_URL)
            )
    };

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View profileFragment = inflater.inflate(R.layout.fragment_profile, container, false);

        View appBar = profileFragment.findViewById(R.id.app_bar);
        View profileView = profileFragment.findViewById(R.id.nested_scroll_view_profile);

        appBar.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPadding(
                    windowInsets.getSystemWindowInsetLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    windowInsets.getSystemWindowInsetRight(),
                    0
            );

            return windowInsets;
        });

        profileView.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPaddingRelative(
                    windowInsets.getSystemWindowInsetLeft(),
                    0,
                    windowInsets.getSystemWindowInsetRight(),
                    view.getPaddingBottom()
            );

            return windowInsets;
        });

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");
            float pixelDensity = getResources().getDisplayMetrics().density;

            profileView.setPaddingRelative(
                    profileView.getPaddingStart(),
                    0,
                    profileView.getPaddingEnd(),
                    (int) (bottomNavigationHeight + 20 * pixelDensity)
            );
        });

        RecyclerView announcements = profileFragment.findViewById(R.id.recycler_view_announcements);
        RecyclerView profileGroups = profileFragment.findViewById(R.id.recycler_view_profile_groups);

        announcements.setAdapter(new AnnouncementItemAdapter());
        profileGroups.setAdapter(new ProfileGroupAdapter());

        return profileFragment;
    }

    public interface OnClickListener {
        void onClick(Context context);
    }

    public static class ItemData {
        public final int titleId, iconId;
        public final OnClickListener onClickListener;

        public ItemData(@StringRes int titleId, @DrawableRes int iconId, @NonNull OnClickListener onClickListener) {
            this.titleId = titleId;
            this.iconId = iconId;
            this.onClickListener = onClickListener;
        }
    }

    public static class AnnouncementItemData {
        public final int iconId;
        public final String title, description;
        public final OnClickListener onClickListener;

        public AnnouncementItemData(@DrawableRes int iconId, String title, String description, OnClickListener onClickListener) {
            this.iconId = iconId;
            this.title = title;
            this.description = description;
            this.onClickListener = onClickListener;
        }
    }
}