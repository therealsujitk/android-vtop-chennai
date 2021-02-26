package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
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
        notification.animate().scaleX(1).scaleY(1);

        return notification;
    }

    public RelativeLayout generateNotificationContainer() {
        RelativeLayout container = new RelativeLayout(context);
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);

        return container;
    }
}
