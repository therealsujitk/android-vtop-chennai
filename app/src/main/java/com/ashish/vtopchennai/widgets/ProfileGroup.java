package com.ashish.vtopchennai.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.vtopchennai.R;
import com.ashish.vtopchennai.adapters.ProfileItemAdapter;
import com.ashish.vtopchennai.fragments.ProfileFragment;

public class ProfileGroup extends LinearLayout {
    AppCompatTextView title;
    RecyclerView profileItemsView;

    public ProfileGroup(Context context) {
        super(context);

        this.initialize();
    }

    public ProfileGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initialize();
    }

    public void initialize() {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        TypedValue colorOnBackground = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorOnBackground, colorOnBackground, true);

        this.title = new AppCompatTextView(this.getContext());
        this.title.setPadding(
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (5 * pixelDensity)
        );
        this.title.setTextSize(15);
        this.title.setTextColor(colorOnBackground.data);
        this.title.setTypeface(this.title.getTypeface(), Typeface.BOLD);

        this.addView(this.title);

        this.profileItemsView = new RecyclerView(this.getContext());
        RecyclerView.LayoutParams profileItemsViewParams = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        this.profileItemsView.setLayoutParams(profileItemsViewParams);
        this.profileItemsView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        this.addView(this.profileItemsView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setOrientation(VERTICAL);
    }

    public void initializeProfileGroup(int profileGroup, ProfileFragment.ItemData[] profileItems) {
        String title = this.getContext().getString(profileGroup);
        this.title.setText(title);
        this.profileItemsView.setAdapter(new ProfileItemAdapter(profileItems));
    }
}
