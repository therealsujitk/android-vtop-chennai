package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

public class LinkButtonGenerator {
    static int LINK_CALL = 0;
    static int LINK_DIRECTION = 1;
    static int LINK_DOWNLOAD = 2;
    static int LINK_EMAIL = 3;
    static int LINK_LINK = 4;
    Context context;
    float pixelDensity;

    public LinkButtonGenerator(Context context) {
        this.context = context;
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    public LinearLayout generateButton(final String link, int linkType) {
        LinearLayout linkButton = new LinearLayout(context);
        LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                (int) (50 * pixelDensity),
                (int) (50 * pixelDensity)
        );
        linkParams.setMarginStart((int) (20 * pixelDensity));
        linkParams.setMarginEnd((int) (20 * pixelDensity));
        linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
        linkButton.setLayoutParams(linkParams);
        linkButton.setClickable(true);
        linkButton.setFocusable(true);
        linkButton.setGravity(Gravity.CENTER);
        linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

        StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
        linkButton.setStateListAnimator(elevation);

        ImageView imageView = new ImageView(context);

        if (linkType == LINK_CALL) {
            linkButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + link));
                context.startActivity(intent);
            });

            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_phone));
        } else if (linkType == LINK_DIRECTION) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_location));
        } else if (linkType == LINK_DOWNLOAD) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download));
        } else if (linkType == LINK_EMAIL) {
            linkButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + link));
                context.startActivity(intent);
            });

            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email));
        } else if (linkType == LINK_LINK) {
            linkButton.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(browserIntent);
            });

            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_link));
        }

        linkButton.addView(imageView);

        return linkButton;
    }
}
