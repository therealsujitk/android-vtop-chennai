package tk.therealsuji.vtopchennai.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.activities.WebViewActivity;
import tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment;

public class SettingsRepository {
    public static String APP_BASE_URL = "https://vtopchennai.therealsuji.tk";
    public static String APP_ABOUT_URL = APP_BASE_URL + "/about.json";
    public static String APP_PRIVACY_URL = APP_BASE_URL + "/privacy-policy";
    public static String APP_FAQ_URL = APP_BASE_URL + "/frequently-asked-questions";

    public static String DEVELOPER_BASE_URL = "https://therealsuji.tk";

    public static String GITHUB_BASE_URL = "https://github.com/therealsujitk/android-vtop-chennai";
    public static String GITHUB_FEATURE_URL = GITHUB_BASE_URL + "/issues";
    public static String GITHUB_ISSUE_URL = GITHUB_BASE_URL + "/issues";

    public static String VTOP_BASE_URL = "https://vtopcc.vit.ac.in/vtop";

    public static int THEME_DAY = 0;
    public static int THEME_NIGHT = 1;

    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        String appearance = sharedPreferences.getString("appearance", "system");
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (appearance.equals("dark")
                || (appearance.equals("system") && currentNightMode == Configuration.UI_MODE_NIGHT_YES)) {
            return THEME_NIGHT;
        }

        return THEME_DAY;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
    }

    public static SharedPreferences getEncryptedSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    "credentials",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static void openDownloadPage(Context context) {
        String link = APP_BASE_URL;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(browserIntent);
    }

    public static void openRecyclerViewFragment(FragmentActivity fragmentActivity, int titleId, int contentType) {
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();

        bundle.putInt("title_id", titleId);
        bundle.putInt("content_type", contentType);

        recyclerViewFragment.setArguments(bundle);

        fragmentActivity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(R.id.main_body, recyclerViewFragment)
                .addToBackStack(null)
                .commit();

        ((MainActivity) fragmentActivity).hideBottomNavigationView();
    }

    public static void openWebViewActivity(Context context, String title, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    public static void openBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
