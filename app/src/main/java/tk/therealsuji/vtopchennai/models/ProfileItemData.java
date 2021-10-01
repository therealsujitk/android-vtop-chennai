package tk.therealsuji.vtopchennai.models;

import android.content.Context;
import android.content.Intent;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class ProfileItemData {
    public final int titleId, iconId;

    public ProfileItemData(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }

    public void onClick(Context context) {
        if (titleId == R.string.faq) {
            SettingsRepository.openWebViewActivity(
                    context,
                    context.getString(titleId),
                    SettingsRepository.APP_FAQ_URL
            );
        } else if (titleId == R.string.notifications) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

            context.startActivity(intent);
        } else if (titleId == R.string.privacy) {
            SettingsRepository.openWebViewActivity(
                    context,
                    context.getString(titleId),
                    SettingsRepository.APP_PRIVACY_URL
            );
        }
    }
}
