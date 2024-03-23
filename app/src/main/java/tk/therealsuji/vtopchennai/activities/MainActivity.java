package tk.therealsuji.vtopchennai.activities;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.BuildConfig;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.fragments.AssignmentsFragment;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.PerformanceFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;
import tk.therealsuji.vtopchennai.fragments.dialogs.UpdateDialogFragment;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.helpers.VTOPHelper;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    VTOPHelper vtopHelper;

    static final String HOME_FRAGMENT_TAG = "HOME_FRAGMENT_TAG";
    static final String PERFORMANCE_FRAGMENT_TAG = "PERFORMANCE_FRAGMENT_TAG";
    static final String ASSIGNMENTS_FRAGMENT_TAG = "ASSIGNMENTS_FRAGMENT_TAG";
    static final String PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT_TAG";

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    ActivityResultLauncher<Intent> requestFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        ArrayList<String> filePaths = new ArrayList<>();

                        if (result.getData().getClipData() != null) {
                            ClipData clipData = result.getData().getClipData();
                            for (int i = 0; i < clipData.getItemCount(); ++i) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                filePaths.add(this.copyFileToCache(uri));
                            }
                        } else if (result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            filePaths.add(this.copyFileToCache(uri));
                        } else {
                            return;
                        }

                        Bundle fileUri = new Bundle();
                        fileUri.putStringArrayList("paths", filePaths);
                        getSupportFragmentManager().setFragmentResult("file", fileUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    private String copyFileToCache(Uri uri) throws Exception {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        assert documentFile != null && documentFile.getName() != null;

        String fileName = documentFile.getName();
        File file = new File(getCacheDir() + "/Moodle", fileName);
        file.deleteOnExit();

        if ((file.getParentFile() == null || !file.getParentFile().mkdir()) && !file.createNewFile() && !file.exists()) {
            throw new Exception("Failed to copy one or more files.");
        }

        InputStream inputStream = getContentResolver().openInputStream(uri);
        assert inputStream != null;
        FileUtils.copyInputStreamToFile(inputStream, file);

        return file.getPath();
    }

    public ActivityResultLauncher<String> getRequestPermissionLauncher() {
        return this.requestPermissionLauncher;
    }

    public ActivityResultLauncher<Intent> getRequestFileLauncher() {
        return this.requestFileLauncher;
    }

    private void syncData() {
        vtopHelper.bind();
        vtopHelper.start();
    }

    private void hideBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.post(() -> this.bottomNavigationView.animate().translationY(bottomNavigationView.getMeasuredHeight()));

        if (Build.VERSION.SDK_INT >= 29) {
            this.getWindow().getDecorView().post(() -> {
                int gestureLeft = this.getWindow().getDecorView().getRootWindowInsets().getSystemGestureInsets().left;

                if (gestureLeft == 0) {
                    this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            });
        }
    }

    private void showBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.post(() -> this.bottomNavigationView.animate().translationY(0));

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    private void restartActivity() {
        Intent intent = new Intent(this, this.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void signOut() {
        SettingsRepository.signOut(this);
        this.startActivity(new Intent(this, LoginActivity.class));
        this.finish();
    }

    private void getUnreadCount() {
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplicationContext());
        Bundle unreadCount = new Bundle();

        Observable.concat(
                        Observable.fromSingle(appDatabase.spotlightDao().getUnreadCount()),
                        Observable.fromSingle(appDatabase.marksDao().getMarksUnreadCount())
                )
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Integer count) {
                        if (!unreadCount.containsKey("spotlight")) {
                            unreadCount.putInt("spotlight", count);
                        } else if (!unreadCount.containsKey("marks")) {
                            unreadCount.putInt("marks", count);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        int homeCount = unreadCount.getInt("spotlight");
                        int performanceCount = unreadCount.getInt("marks");

                        BadgeDrawable homeBadge = bottomNavigationView.getOrCreateBadge(R.id.item_home);
                        BadgeDrawable performanceBadge = bottomNavigationView.getOrCreateBadge(R.id.item_performance);

                        homeBadge.setNumber(homeCount);
                        homeBadge.setVisible(homeCount != 0);

                        performanceBadge.setNumber(performanceCount);
                        performanceBadge.setVisible(performanceCount != 0);

                        getSupportFragmentManager().setFragmentResult("unreadCount", unreadCount);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean amoledMode = SettingsRepository.getSharedPreferences(this).getBoolean("amoledMode", false);
        SettingsRepository.applyDynamicColors(this, amoledMode);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Firebase Analytics Logging
        Bundle bundle = new Bundle();
        bundle.putBoolean("recently_synced", !SettingsRepository.isRefreshRequired(this));
        FirebaseAnalytics.getInstance(this).logEvent("app_data", bundle);

        this.bottomNavigationView = findViewById(R.id.bottom_navigation);

        Bundle customInsets = new Bundle();
        customInsets.putInt("systemWindowInsetLeft", 0);
        customInsets.putInt("systemWindowInsetTop", 0);
        customInsets.putInt("systemWindowInsetRight", 0);
        customInsets.putInt("systemWindowInsetBottom", 0);
        customInsets.putInt("bottomNavigationHeight", 0);

        findViewById(R.id.frame_layout_fragment_container)
                .setOnApplyWindowInsetsListener((view, windowInsets) -> {
                    int systemWindowInsetLeft = windowInsets.getSystemWindowInsetLeft();
                    int systemWindowInsetTop = windowInsets.getSystemWindowInsetTop();
                    int systemWindowInsetRight = windowInsets.getSystemWindowInsetRight();
                    int systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom();

                    customInsets.putInt("systemWindowInsetLeft", systemWindowInsetLeft);
                    customInsets.putInt("systemWindowInsetTop", systemWindowInsetTop);
                    customInsets.putInt("systemWindowInsetRight", systemWindowInsetRight);
                    customInsets.putInt("systemWindowInsetBottom", systemWindowInsetBottom);

                    getSupportFragmentManager().setFragmentResult("customInsets", customInsets);

                    // Send the bottom navigation height to all fragments when ready
                    bottomNavigationView.post(() -> {
                        customInsets.putInt("bottomNavigationHeight", bottomNavigationView.getMeasuredHeight());
                        getSupportFragmentManager().setFragmentResult("customInsets", customInsets);
                    });

                    return windowInsets;
                });

        getSupportFragmentManager().setFragmentResultListener("bottomNavigationVisibility", this, (requestKey, result) -> {
            if (result.getBoolean("isVisible")) {
                this.showBottomNavigationView();
            } else {
                this.hideBottomNavigationView();
            }
        });

        Bundle syncDataState = new Bundle();
        syncDataState.putBoolean("isLoading", false);
        getSupportFragmentManager().setFragmentResultListener("syncData", this, (requestKey, result) -> this.syncData());
        getSupportFragmentManager().setFragmentResultListener("getUnreadCount", this, (requestKey, result) -> this.getUnreadCount());
        getSupportFragmentManager().setFragmentResultListener("applyDynamicColors", this, (requestKey, result) -> this.recreate());

        this.getUnreadCount();

        this.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            String selectedFragmentTag;

            if (item.getItemId() == R.id.item_performance) {
                selectedFragmentTag = PERFORMANCE_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new PerformanceFragment();
                }
            } else if (item.getItemId() == R.id.item_assignments) {
                selectedFragmentTag = ASSIGNMENTS_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new AssignmentsFragment();
                }
            } else if (item.getItemId() == R.id.item_profile) {
                getSupportFragmentManager().setFragmentResult("syncDataState", syncDataState);

                selectedFragmentTag = PROFILE_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new ProfileFragment();
                }
            } else {
                selectedFragmentTag = HOME_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new HomeFragment();
                }
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout_fragment_container, selectedFragment, selectedFragmentTag)
                    .commit();

            return true;
        });

        int selectedItem = R.id.item_home;
        Serializable launchFragment = this.getIntent().getSerializableExtra("launchFragment");
        String launchSubFragment = this.getIntent().getStringExtra("launchSubFragment");

        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("selectedItem");
        } else if (launchFragment != null) {
            // If the application is launched from notifications
            if (launchFragment.equals(ProfileFragment.class)) {
                selectedItem = R.id.item_profile;
            }

            if (launchSubFragment != null) {
                Bundle launchSubFragmentBundle = new Bundle();
                launchSubFragmentBundle.putString("subFragment", launchSubFragment);
                getSupportFragmentManager().setFragmentResult("launchSubFragment", launchSubFragmentBundle);
            }
        }

        this.bottomNavigationView.setSelectedItemId(selectedItem);
        this.vtopHelper = new VTOPHelper(this, new VTOPHelper.Initiator() {
            @Override
            public void onLoading(boolean isLoading) {
                syncDataState.putBoolean("isLoading", isLoading);
                getSupportFragmentManager().setFragmentResult("syncDataState", syncDataState);
            }

            @Override
            public void onForceSignOut() {
                signOut();
            }

            @Override
            public void onComplete() {
                restartActivity();
            }
        });

        /*
            Check for updates
         */
        Context context = this;
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

                                return;
                            }
                        } catch (Exception ignored) {
                        }

                        if (SettingsRepository.isRefreshRequired(context)) {
                            new MaterialAlertDialogBuilder(context)
                                    .setMessage(R.string.sync_message)
                                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                                    .setPositiveButton(R.string.sync, (dialogInterface, i) -> syncData())
                                    .setTitle(R.string.sync_title)
                                    .show();
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
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedItem", this.bottomNavigationView.getSelectedItemId());
    }

    @Override
    public void onConfigurationChanged(@androidx.annotation.NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (this.bottomNavigationView.getTranslationY() != 0) {
            this.hideBottomNavigationView();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.vtopHelper.bind();
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
