package com.ashish.vtopchennai.fragments;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.ashish.vtopchennai.helpers.SettingsRepository.THEME_DAY;
import static com.ashish.vtopchennai.helpers.SettingsRepository.THEME_SYSTEM_DAY;
import static com.ashish.vtopchennai.helpers.SettingsRepository.getTheme;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.activities.LoginActivity;
import com.ashish.vtopchennai.adapters.AnnouncementItemAdapter;
import com.ashish.vtopchennai.adapters.ProfileGroupAdapter;
import com.ashish.vtopchennai.helpers.AppDatabase;
import com.ashish.vtopchennai.helpers.SettingsRepository;
import com.ashish.vtopchennai.interfaces.TimetableDao;
import com.ashish.vtopchennai.models.Timetable;

public class ProfileFragment extends Fragment {
    ActivityResultLauncher<Intent> forActivityResult;
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
    @SuppressLint("UseCompatLoadingForDrawables")
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
                    android.R.drawable.ic_lock_idle_alarm,
                    R.string.notification_interval,
                    context -> {

                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                        builder.setTitle("Set Notification Timing");
                        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.frame_notification_interval, (ViewGroup) getView(), false);
                        final EditText input = viewInflated.findViewById(R.id.input);
                        ImageView image=viewInflated.findViewById(R.id.image_info);
                        SharedPreferences sharedPreferences=SettingsRepository.getSharedPreferences(context);
                        if (getTheme(getContext())==THEME_DAY || getTheme(getContext())==THEME_SYSTEM_DAY) image.setImageDrawable(context.getDrawable(R.drawable.ic_info_dark));
                        else image.setImageDrawable(context.getDrawable(R.drawable.ic_info_light));

                        input.setText(String.valueOf(sharedPreferences.getInt("notification_interval",30)));
                        builder.setView(viewInflated);

                        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            if (Integer.parseInt(input.getText().toString())>=5 && Integer.parseInt(input.getText().toString())<=60){
                                sharedPreferences.edit().putInt("notification_interval",Integer.parseInt(input.getText().toString())).apply();
                                SettingsRepository.clearTimetableNotifications(context);
                                AppDatabase appDatabase = AppDatabase.getInstance(context);
                                TimetableDao timetableDao = appDatabase.timetableDao();
                                timetableDao.getTimetable().subscribeOn(Schedulers.single())
                                        .subscribe(new SingleObserver<List<Timetable>>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {
                                            }

                                            @Override
                                            public void onSuccess(@NonNull List<Timetable> timetable) {
                                                for (int i = 0; i < timetable.size(); ++i) {
                                                    try {
                                                        SettingsRepository.setTimetableNotifications(context, timetable.get(i));
                                                    } catch (Exception ignored) {
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                            }
                                        });
                            }
                            else{
                                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        });
                        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
                        builder.show();
                    },
                    null
            ),
            new ItemData(
                    R.drawable.ic_dnd,
                    R.string.dnd,
                    context -> {
                        String[] dnd_mode = {
                                context.getString(R.string.dnd_off),
                                context.getString(R.string.dnd_vibrate),
                                context.getString(R.string.dnd_silent)
                        };

                        SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(context);

                        int checkedItem = 0;
                        String dnd = sharedPreferences.getString("dnd", "off");

                        if (dnd.equals("vibrate")) {
                            checkedItem = 1;
                        }
                        else if (dnd.equals("silent")){
                            checkedItem = 2;
                        }
                        NotificationManager manager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
                        new MaterialAlertDialogBuilder(context)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                                .setSingleChoiceItems(dnd_mode, checkedItem, (dialogInterface, i) -> {
                                    if (i == 0) {
                                        sharedPreferences.edit().putString("dnd", "off").apply();
                                        refreshTimetable(context);
                                    } else if (i == 1) {
                                        sharedPreferences.edit().putString("dnd", "vibrate").apply();
                                        if (!manager.isNotificationPolicyAccessGranted()) {
                                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                            forActivityResult.launch(intent);
                                        }
                                        else{
                                            refreshTimetable(context);
                                        }
                                    } else if (i == 2) {
                                        sharedPreferences.edit().putString("dnd", "silent").apply();
                                        if (!manager.isNotificationPolicyAccessGranted()) {
                                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                            forActivityResult.launch(intent);
                                        }
                                        else{
                                            refreshTimetable(context);
                                        }
                                    }
                                    dialogInterface.dismiss();
                                })
                                .setTitle(R.string.dnd)
                                .show();
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
                        //Cast to BottomSheetDialog
                        ((BottomSheetDialog)bottomSheetDialog).setContentView(bottomSheetLayout);
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

        forActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(getContext());
                    NotificationManager manager=(NotificationManager)getContext().getSystemService(NOTIFICATION_SERVICE);
                    if (!manager.isNotificationPolicyAccessGranted()) {
                        sharedPreferences.edit().putString("dnd", "off").apply();
                    }
                    else {
                        refreshTimetable(getContext());
                    }
                });

        announcements.setAdapter(new AnnouncementItemAdapter(announcementItems));
        profileGroups.setAdapter(new ProfileGroupAdapter(this.profileGroups, this.profileItems));

        return profileFragment;
    }

    public void refreshTimetable(Context context){
        SettingsRepository.clearTimetableNotifications(context);

        AppDatabase appDatabase = AppDatabase.getInstance(context);
        TimetableDao timetableDao = appDatabase.timetableDao();

        timetableDao
                .getTimetable()
                .subscribeOn(Schedulers.single())
                .subscribe(new SingleObserver<List<Timetable>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<Timetable> timetable) {
                        for (int i = 0; i < timetable.size(); ++i) {
                            try {
                                SettingsRepository.setTimetableNotifications(context, timetable.get(i));
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });
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
