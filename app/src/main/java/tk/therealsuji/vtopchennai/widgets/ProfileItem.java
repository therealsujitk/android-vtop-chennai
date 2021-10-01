package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.ProfileItemData;

public class ProfileItem extends LinearLayout {
    private static final ProfileItemData[] PERSONAL_PROFILE_ITEMS = {
            new ProfileItemData(R.string.messages, R.drawable.ic_messages),
            new ProfileItemData(R.string.receipts, R.drawable.ic_receipts),
            new ProfileItemData(R.string.switch_semester, R.drawable.ic_semester)
    };

    private static final ProfileItemData[] APPLICATION_PROFILE_ITEMS = {
            new ProfileItemData(R.string.appearance, R.drawable.ic_appearance),
            new ProfileItemData(R.string.faq, R.drawable.ic_faq),
            new ProfileItemData(R.string.notifications, R.drawable.ic_notifications),
            new ProfileItemData(R.string.privacy, R.drawable.ic_privacy),
            new ProfileItemData(R.string.send_feedback, R.drawable.ic_feedback),
            new ProfileItemData(R.string.sign_out, R.drawable.ic_sign_out)
    };

    public static final ProfileItemData[][] PROFILE_ITEMS = {
            PERSONAL_PROFILE_ITEMS,
            APPLICATION_PROFILE_ITEMS
    };

    AppCompatTextView title;
    ImageView icon;

    public ProfileItem(Context context) {
        super(context);

        this.initialize();
    }

    public ProfileItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initialize();
    }

    private void initialize() {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
        ColorStateList colorSecondaryTintList = ColorStateList.valueOf(colorSecondary.data);

        TypedValue colorOnBackground = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorOnBackground, colorOnBackground, true);

        this.icon = new ImageView(this.getContext());
        LayoutParams iconParams = new LayoutParams(
                (int) (45 * pixelDensity),
                (int) (45 * pixelDensity)
        );
        this.icon.setLayoutParams(iconParams);
        this.icon.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.background_profile_item_icon));
        this.icon.setImageTintList(colorSecondaryTintList);
        this.icon.setPadding(
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity)
        );

        this.addView(this.icon);

        this.title = new AppCompatTextView(this.getContext());
        this.title.setPadding((int) (20 * pixelDensity), 0, 0, 0);
        this.title.setTextColor(colorOnBackground.data);
        this.title.setTextSize(18);

        this.addView(this.title);

        TypedValue selectableItemBackground = new TypedValue();
        this.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true);
        this.setBackgroundResource(selectableItemBackground.resourceId);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setClickable(true);
        this.setFocusable(true);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setPadding(
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity)
        );
    }

    public void initializeProfileItem(int profileGroupIndex, int profileItemIndex) {
        ProfileItemData profileItemData = PROFILE_ITEMS[profileGroupIndex][profileItemIndex];

        this.title.setText(this.getContext().getString(profileItemData.titleId));
        this.icon.setImageDrawable(ContextCompat.getDrawable(this.getContext(), profileItemData.iconId));
        this.setOnClickListener(view -> profileItemData.onClick(this.getContext()));
    }
}
