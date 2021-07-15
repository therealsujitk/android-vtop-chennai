package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

public class NotificationDotGenerator {
    public static int NOTIFICATION_DEFAULT = 0;
    public static int NOTIFICATION_URGENT = 1;
    Context context;
    float pixelDensity;

    public NotificationDotGenerator(Context context) {
        this.context = context;
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    public ImageView generateNotificationDot(int marginStart, int type) {
        ImageView notification = new ImageView(context);
        RelativeLayout.LayoutParams notificationParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        notificationParams.setMarginStart(marginStart);
        notification.setLayoutParams(notificationParams);
        notification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_notification_dot));
        StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
        notification.setStateListAnimator(elevation);
        if (type == NOTIFICATION_DEFAULT) {
            ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(context.getColor(R.color.colorPrimaryTransparent)));
        } else if (type == NOTIFICATION_URGENT) {
            ImageViewCompat.setImageTintList(notification, ColorStateList.valueOf(context.getColor(R.color.colorRedTransparent)));
        }
        notification.setScaleX(0);
        notification.setScaleY(0);

        return notification;
    }

    public RelativeLayout generateNotificationContainer(TextView button) {
        RelativeLayout container = new RelativeLayout(context);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) (25 * pixelDensity)
        );
        buttonParams.setMarginStart((int) (5 * pixelDensity));
        buttonParams.setMarginEnd((int) (5 * pixelDensity));
        buttonParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));

        button.setLayoutParams(buttonParams);
        container.addView(button);

        return container;
    }

    public RelativeLayout generateNotificationContainer(LinearLayout card) {
        RelativeLayout container = new RelativeLayout(context);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);

        RelativeLayout.LayoutParams cardParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMarginStart((int) (20 * pixelDensity));
        cardParams.setMarginEnd((int) (20 * pixelDensity));
        cardParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));

        card.setLayoutParams(cardParams);
        container.addView(card);

        return container;
    }
}
