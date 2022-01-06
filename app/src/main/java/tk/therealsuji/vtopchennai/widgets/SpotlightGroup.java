package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.SpotlightItemAdapter;
import tk.therealsuji.vtopchennai.models.Spotlight;

public class SpotlightGroup extends LinearLayout {
    AppCompatTextView category;
    RecyclerView announcements;

    public SpotlightGroup(Context context) {
        super(context);
        this.initialize();
    }

    void initialize() {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);

        this.category = new AppCompatTextView(this.getContext());
        this.category.setPadding(
                (int) (40 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (40 * pixelDensity),
                (int) (6 * pixelDensity)
        );
        this.category.setTextColor(colorSecondary.data);
        this.category.setTextSize(15);
        this.category.setTypeface(this.category.getTypeface(), Typeface.BOLD);

        this.addView(this.category);

        this.announcements = new RecyclerView(this.getContext());
        RecyclerView.LayoutParams announcementsParams = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        this.announcements.setLayoutParams(announcementsParams);
        this.announcements.setLayoutManager(new LinearLayoutManager(this.getContext()));

        this.addView(this.announcements);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setOrientation(VERTICAL);
        this.setPadding(
                0,
                0,
                0,
                (int) (5 * pixelDensity)
        );
    }

    public void setSpotlightSection(String category, List<Spotlight> announcements) {
        this.category.setText(category);
        this.announcements.setAdapter(new SpotlightItemAdapter(announcements));
    }
}
