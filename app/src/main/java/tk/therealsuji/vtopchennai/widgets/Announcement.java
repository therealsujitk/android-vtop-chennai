package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.R;

public class Announcement extends LinearLayout {
    static public int TYPE_ANNOUNCEMENT = 0;
    static public int TYPE_DOWNLOAD = 1;
    static public int TYPE_LINK = 2;

    private final Context context;
    private AppCompatTextView announcement;
    private ImageView announcementType;

    public Announcement(Context context) {
        super(context);

        this.context = context;
        this.initialize();
    }

    private void initialize() {
        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
        ColorStateList colorSecondaryTintList = ColorStateList.valueOf(colorSecondary.data);

        float pixelDensity = context.getResources().getDisplayMetrics().density;

        this.announcementType = new ImageView(this.context);
        LinearLayout.LayoutParams announcementTypeParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        announcementTypeParams.setMargins(
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity)
        );
        this.announcementType.setLayoutParams(announcementTypeParams);
        this.announcementType.setBackground(ContextCompat.getDrawable(this.context, R.drawable.background_announcement_type));
        this.announcementType.setImageTintList(colorSecondaryTintList);
        this.announcementType.setPadding(
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity)
        );

        this.addView(this.announcementType);

        this.announcement = new AppCompatTextView(this.context);
        LinearLayout.LayoutParams announcementParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        announcementParams.setMargins(
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity)
        );
        this.announcement.setLayoutParams(announcementParams);
        this.announcement.setTextColor(colorSecondary.data);
        this.announcement.setTextSize(16);

        this.addView(this.announcement);

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setBackground(ContextCompat.getDrawable(this.context, R.drawable.background_announcement_default));
        this.setClickable(true);
        this.setFocusable(true);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setOrientation(HORIZONTAL);
    }

    public void setAnnouncement(String announcement) {
        this.announcement.setText(announcement);
    }

    public void setAnnouncementType(int type) {
        if (type == TYPE_DOWNLOAD) {
            this.announcementType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download));
        } else if (type == TYPE_LINK) {
            this.announcementType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_link));
        } else {
            this.announcementType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_announcement));
        }
    }

    public void setMargins(int left, int top, int right, int bottom) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) this.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        this.setLayoutParams(params);
    }

    public void setFirst() {
        this.setBackground(ContextCompat.getDrawable(this.context, R.drawable.background_announcement_first));
    }

    public void setLast() {
        this.setBackground(ContextCompat.getDrawable(this.context, R.drawable.background_announcement_last));
    }

    public void setSingle() {
        this.setBackground(ContextCompat.getDrawable(this.context, R.drawable.background_announcement_single));
    }

    public void setLink(String link) {
        if (link == null) {
            this.setAnnouncementType(TYPE_ANNOUNCEMENT);
        } else if (link.toLowerCase().startsWith("http")) {
            this.setAnnouncementType(TYPE_LINK);
        } else {
            this.setAnnouncementType(TYPE_DOWNLOAD);
        }
    }
}
