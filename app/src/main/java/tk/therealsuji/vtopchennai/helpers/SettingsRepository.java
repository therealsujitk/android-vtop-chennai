package tk.therealsuji.vtopchennai.helpers;

import android.content.Context;
import android.content.Intent;

import tk.therealsuji.vtopchennai.WebViewActivity;

public class SettingsRepository {
    public static String APP_BASE_URL = "https://vtopchennai.therealsuji.tk";
    public static String APP_PRIVACY_URL = APP_BASE_URL + "/privacy-policy";
    public static String APP_FAQ_URL = APP_BASE_URL + "/frequently-asked-questions";

    public static void openWebViewActivity(Context context, String title, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }
}
