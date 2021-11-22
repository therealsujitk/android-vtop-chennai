package tk.therealsuji.vtopchennai.adapters;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Spotlight;

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

                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ((MainActivity) context).getRequestPermissionLauncher().launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        return;
                    }

                    String downloadLink = SettingsRepository.VTOP_BASE_URL + "/" + spotlightItem.link + "?&x=";
                    WebView downloadPage = new WebView(context);

                    downloadPage.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36");
                    downloadPage.setWebViewClient(new WebViewClient() {
                        public void onPageFinished(WebView view, String url) {
                            downloadPage.loadUrl(downloadLink);
                            downloadPage.setWebViewClient(null);
                        }
                    });
                    downloadPage.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                        String fileName = contentDisposition.split("filename=")[1];
                        Toast.makeText(context, Html.fromHtml(context.getString(R.string.downloading_file, fileName), Html.FROM_HTML_MODE_LEGACY), Toast.LENGTH_SHORT).show();

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url));
                        request.allowScanningByMediaScanner();
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "VTOP Spotlight/" + fileName);
                        request.setMimeType(mimetype);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);
                    });
                    downloadPage.loadUrl(SettingsRepository.VTOP_BASE_URL);
                });
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
