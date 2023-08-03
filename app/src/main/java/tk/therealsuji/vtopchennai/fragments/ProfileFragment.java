package tk.therealsuji.vtopchennai.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.analytics.FirebaseAnalytics;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.LoginActivity;
import tk.therealsuji.vtopchennai.adapters.AnnouncementItemAdapter;
import tk.therealsuji.vtopchennai.adapters.ProfileGroupAdapter;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class ProfileFragment extends Fragment {
    /*
        User Related Profile Items
     */
    private final ItemData[] personalProfileItems = {
            new ItemData(
                    R.drawable.ic_courses,
                    R.string.courses,
                    context -> SettingsRepository.openViewPagerFragment(
                            (FragmentActivity) context,
                            R.string.courses,
                            ViewPagerFragment.TYPE_COURSES
                    ),
                    null
            ),
            new ItemData(
                    R.drawable.ic_exams,
                    R.string.exam_schedule,
                    context -> SettingsRepository.openViewPagerFragment(
                            (FragmentActivity) context,
                            R.string.exam_schedule,
                            ViewPagerFragment.TYPE_EXAMS
                    ),
                    null
            ),
            new ItemData(
                    R.drawable.ic_receipts,
                    R.string.receipts,
                    context -> SettingsRepository.openRecyclerViewFragment(
                            (FragmentActivity) context,
                            R.string.receipts,
                            RecyclerViewFragment.TYPE_RECEIPTS
                    ),
                    null
            ),
            new ItemData(
                    R.drawable.ic_staff,
                    R.string.staff,
                    context -> SettingsRepository.openViewPagerFragment(
                            (FragmentActivity) context,
                            R.string.staff,
                            ViewPagerFragment.TYPE_STAFF
                    ),
                    null
            ),
            new ItemData(
                    R.drawable.ic_sync,
                    R.string.sync_data,
                    context -> getParentFragmentManager().setFragmentResult("syncData", new Bundle()),
                    profileItem -> {
                        ProgressBar progressBar = new ProgressBar(profileItem.getContext());
                        RelativeLayout extraContainer = profileItem.findViewById(R.id.relative_layout_extra_container);
                        extraContainer.addView(progressBar);

                        getParentFragmentManager().setFragmentResultListener("syncDataState", this, (requestKey, result) -> {
                            if (result.getBoolean("isLoading")) {
                                profileItem.setEnabled(false);
                                extraContainer.setVisibility(View.VISIBLE);
                            } else {
                                profileItem.setEnabled(true);
                                extraContainer.setVisibility(View.GONE);
                            }
                        });
                    }
            )
    };

    /*
        Application Related Profile Items
     */
    private final ItemData[] applicationProfileItems = {
            new ItemData(
                    R.drawable.ic_appearance,
                    R.string.appearance,
                    context -> {
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

                        View appearanceView = getLayoutInflater().inflate(R.layout.layout_dialog_apperance, null);
                        MaterialSwitch amoledSwitch = appearanceView.findViewById(R.id.switch_amoled_mode);
                        amoledSwitch.setChecked(sharedPreferences.getBoolean("amoledMode", false));
                        amoledSwitch.setOnCheckedChangeListener((compoundButton, isAmoledModeEnabled) -> {
                            sharedPreferences.edit().putBoolean("amoledMode", isAmoledModeEnabled).apply();
                            Bundle applyDynamicColors = new Bundle();
                            applyDynamicColors.putBoolean("amoledMode", isAmoledModeEnabled);
                            getParentFragmentManager().setFragmentResult("applyDynamicColors", applyDynamicColors);
                        });

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
                                .setView(appearanceView)
                                .setTitle(R.string.appearance)
                                .show();
                    },
                    null
            ),
            new ItemData(
                    R.drawable.ic_notifications,
                    R.string.notifications,
                    context -> {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", context.getPackageName());
                        intent.putExtra("app_uid", context.getApplicationInfo().uid);
                        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

                        context.startActivity(intent);
                    },
                    null
            ),
            new ItemData(
                    R.drawable.ic_privacy,
                    R.string.privacy,
                    context -> SettingsRepository.openWebViewActivity(
                            context,
                            context.getString(R.string.privacy),
                            SettingsRepository.APP_PRIVACY_URL
                    ),
                    null
            ),
            new ItemData(
                    R.drawable.ic_feedback,
                    R.string.send_feedback,
                    context -> {
                        View bottomSheetLayout = View.inflate(context, R.layout.layout_bottom_sheet_feedback, null);
                        bottomSheetLayout.findViewById(R.id.text_view_contact_developer).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.DEVELOPER_BASE_URL));
                        bottomSheetLayout.findViewById(R.id.text_view_open_issue).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_ISSUE_URL));
                        bottomSheetLayout.findViewById(R.id.text_view_request_feature).setOnClickListener(view -> SettingsRepository.openBrowser(context, SettingsRepository.GITHUB_FEATURE_URL));

                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                        bottomSheetDialog.setContentView(bottomSheetLayout);
                        bottomSheetDialog.show();
                    },
                    null
            ),
            new ItemData(
                    R.drawable.ic_share,
                    R.string.share,
                    context -> {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, SettingsRepository.APP_BASE_URL));
                        intent.setType("text/plain");

                        Intent shareIntent = Intent.createChooser(intent, context.getString(R.string.share_title));
                        context.startActivity(shareIntent);
                    },
                    null
            ),
            new ItemData(
                    R.drawable.ic_sign_out,
                    R.string.sign_out,
                    context -> {
                        AlertDialog signOutDialog = new MaterialAlertDialogBuilder(context)
                                .setMessage(R.string.sign_out_text)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                                .setPositiveButton(R.string.sign_out, (dialogInterface, i) -> {
                                    SettingsRepository.signOut(context);
                                    context.startActivity(new Intent(context, LoginActivity.class));
                                    ((Activity) context).finish();
                                })
                                .setTitle(R.string.sign_out)
                                .create();

                        if (!SettingsRepository.isMoodleSignedIn(requireContext())) {
                            signOutDialog.show();
                            return;
                        }

                        AlertDialog moodleSignOutDialog = new MaterialAlertDialogBuilder(context)
                                .setMessage(R.string.moodle_sign_out_text)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                                .setPositiveButton(R.string.sign_out, (dialogInterface, i) -> {
                                    SettingsRepository.signOutMoodle(requireContext());
                                    Toast.makeText(context, "You've signed out of Moodle.", Toast.LENGTH_SHORT).show();
                                })
                                .setTitle(R.string.sign_out)
                                .create();

                        View bottomSheetLayout = View.inflate(context, R.layout.layout_bottom_sheet_sign_out, null);
                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                        bottomSheetDialog.setContentView(bottomSheetLayout);

                        bottomSheetLayout.findViewById(R.id.text_view_sign_out_moodle).setOnClickListener(view -> {
                            bottomSheetDialog.dismiss();
                            moodleSignOutDialog.show();
                        });
                        bottomSheetLayout.findViewById(R.id.text_view_sign_out_app).setOnClickListener(view -> {
                            bottomSheetDialog.dismiss();
                            signOutDialog.show();
                        });

                        bottomSheetDialog.show();
                    },
                    null)
    };

    private final ItemData[][] profileItems = {
            personalProfileItems,
            applicationProfileItems
    };

    private final int[] profileGroups = {
            R.string.personal,
            R.string.application
    };

    /*
        App announcements
     */
    private final ItemData[] announcementItems = {
            new ItemData(
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
    public void onResume() {
        super.onResume();

        // Firebase Analytics Logging
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ProfileFragment");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Profile");
        FirebaseAnalytics.getInstance(this.requireContext()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        getParentFragmentManager().setFragmentResultListener("launchSubFragment", this, (requestKey, result) -> {
            String subFragment = result.getString("subFragment");

            if (subFragment.equals("ExamSchedule")) {
                SettingsRepository.openViewPagerFragment(
                        requireActivity(),
                        R.string.exam_schedule,
                        ViewPagerFragment.TYPE_EXAMS
                );
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View profileFragment = inflater.inflate(R.layout.fragment_profile, container, false);

        View appBarLayout = profileFragment.findViewById(R.id.app_bar);
        View profileView = profileFragment.findViewById(R.id.nested_scroll_view_profile);

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");
            float pixelDensity = getResources().getDisplayMetrics().density;

            appBarLayout.setPadding(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            profileView.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (bottomNavigationHeight + 20 * pixelDensity)
            );

            // Only one listener can be added per requestKey, so we create a duplicate
            getParentFragmentManager().setFragmentResult("customInsets2", result);
        });

        RecyclerView announcements = profileFragment.findViewById(R.id.recycler_view_announcements);
        RecyclerView profileGroups = profileFragment.findViewById(R.id.recycler_view_profile_groups);

        announcements.setAdapter(new AnnouncementItemAdapter(announcementItems));
        profileGroups.setAdapter(new ProfileGroupAdapter(this.profileGroups, this.profileItems));

        return profileFragment;
    }

    public static class ItemData {
        public final int iconId, titleId;
        public final String title, description;
        public final OnClickListener onClickListener;
        public final OnInitListener onInitListener;

        public ItemData(@DrawableRes int iconId, @StringRes int titleId, OnClickListener onClickListener, OnInitListener onInitListener) {
            this.iconId = iconId;
            this.titleId = titleId;
            this.onClickListener = onClickListener;
            this.onInitListener = onInitListener;

            this.title = null;
            this.description = null;
        }

        public ItemData(@DrawableRes int iconId, String title, String description, OnClickListener onClickListener) {
            this.iconId = iconId;
            this.title = title;
            this.description = description;
            this.onClickListener = onClickListener;

            this.titleId = 0;
            this.onInitListener = null;
        }

        public interface OnClickListener {
            void onClick(Context context);
        }

        public interface OnInitListener {
            void onInit(View profileItem);
        }
    }
}
