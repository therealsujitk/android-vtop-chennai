package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class ButtonGenerator {
    Context context;
    float pixelDensity;

    public ButtonGenerator(Context context) {
        this.context = context;
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    public TextView generateButton(String buttonText) {
        TextView button = new TextView(context);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) (25 * pixelDensity)
        );
        buttonParams.setMarginStart((int) (5 * pixelDensity));
        buttonParams.setMarginEnd((int) (5 * pixelDensity));
        buttonParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
        button.setLayoutParams(buttonParams);
        button.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), 0);
        button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
        button.setText(buttonText.toUpperCase());
        button.setTextColor(context.getColor(R.color.colorPrimary));
        button.setTextSize(12);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
        button.setAlpha(0);
        button.animate().alpha(1);

        return button;
    }
}
