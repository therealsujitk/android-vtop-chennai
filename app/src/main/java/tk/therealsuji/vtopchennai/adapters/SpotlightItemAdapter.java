package tk.therealsuji.vtopchennai.adapters;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Spotlight;

/**
 * ┬─── Staff Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.ViewPagerFragment}
 * ├─ {@link SpotlightGroupAdapter}     - RecyclerView
 * ╰→ {@link SpotlightItemAdapter}      - RecyclerView (Current File)
 */
public class SpotlightItemAdapter extends RecyclerView.Adapter<SpotlightItemAdapter.ViewHolder> {
    private final List<Spotlight> spotlight;

    public SpotlightItemAdapter(List<Spotlight> spotlight) {
        this.spotlight = spotlight;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout spotlightItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_spotlight, parent, false);

        return new ViewHolder(spotlightItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setSpotlightItem(spotlight.get(position));

        if (position == 0 && this.getItemCount() == 1) {
            holder.setBackground(R.drawable.background_recycler_view_item_single);
        } else if (position == 0) {
            holder.setBackground(R.drawable.background_recycler_view_item_first);
        } else if (position == this.getItemCount() - 1) {
            holder.setBackground(R.drawable.background_recycler_view_item_last);
        }
    }

    @Override
    public int getItemCount() {
        return this.spotlight.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout spotlightItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.spotlightItem = (LinearLayout) itemView;
        }

        public void setSpotlightItem(Spotlight spotlightItem) {
            TextView announcement = this.spotlightItem.findViewById(R.id.text_view_announcement);
            announcement.setText(spotlightItem.announcement);

            if (spotlightItem.link == null) {
                this.setType(R.drawable.ic_announcement);

                this.spotlightItem.setClickable(false);
                this.spotlightItem.setFocusable(false);
            } else if (spotlightItem.link.toLowerCase().startsWith("http")) {
                this.setType(R.drawable.ic_link);

                this.spotlightItem.setOnClickListener(view -> SettingsRepository.openBrowser(
                        this.spotlightItem.getContext(),
                        spotlightItem.link
                ));
            } else {
                this.setType(R.drawable.ic_download);

                this.spotlightItem.setOnClickListener(view -> {
                    Context context = this.spotlightItem.getContext();

                    if (!SettingsRepository.hasFileWritePermission(context)) {
                        ((MainActivity) context).getRequestPermissionLauncher().launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        return;
                    }

                    String downloadLink = SettingsRepository.VTOP_BASE_URL + "/" + spotlightItem.link + "?&x=";
                    WebView downloadPage = new WebView(context);

                    // Clear cookies and cache to prevent a session timeout
                    CookieManager.getInstance().removeAllCookies(null);
                    downloadPage.clearCache(true);
                    downloadPage.clearHistory();

                    downloadPage.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36");
                    downloadPage.setWebViewClient(new WebViewClient() {
                        public void onPageFinished(WebView view, String url) {
                            view.loadUrl(downloadLink);
                            view.setWebViewClient(null);
                        }
                    });
                    downloadPage.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                        String fileName = contentDisposition.split("filename=")[1];
                        String cookie = CookieManager.getInstance().getCookie(url);
                        SettingsRepository.downloadFile(context, "VTOP Spotlight", fileName, mimetype, Uri.parse(url), cookie);
                    });
                    downloadPage.loadUrl(SettingsRepository.VTOP_BASE_URL);
                });
            }

            ImageView announcementType = this.spotlightItem.findViewById(R.id.image_view_announcement_type);
            if (!spotlightItem.isRead) {
                float pixelDensity = announcementType.getContext().getResources().getDisplayMetrics().density;
                BadgeDrawable announcementBadge = BadgeDrawable.create(announcementType.getContext());
                announcementBadge.setBadgeGravity(BadgeDrawable.TOP_END);
                announcementBadge.setHorizontalOffset((int) (10 * pixelDensity));
                announcementBadge.setVerticalOffset((int) (10 * pixelDensity));

                announcementType.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @OptIn(markerClass = ExperimentalBadgeUtils.class)
                    @Override
                    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                        BadgeUtils.attachBadgeDrawable(announcementBadge, announcementType);
                        announcementType.removeOnLayoutChangeListener(this);
                    }
                });
            } else {
                // Remove the BadgeDrawable if any (Required because RecyclerView recycles layouts)
                announcementType.getOverlay().clear();
            }
        }

        public void setBackground(@DrawableRes int backgroundDrawableId) {
            this.spotlightItem.setBackground(ContextCompat.getDrawable(this.spotlightItem.getContext(), backgroundDrawableId));
        }

        public void setType(@DrawableRes int iconDrawableId) {
            ImageView announcementType = this.spotlightItem.findViewById(R.id.image_view_announcement_type);
            announcementType.setImageDrawable(ContextCompat.getDrawable(this.spotlightItem.getContext(), iconDrawableId));
        }
    }
}
