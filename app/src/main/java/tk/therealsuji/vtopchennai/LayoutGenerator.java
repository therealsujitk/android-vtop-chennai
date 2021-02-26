package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.widget.LinearLayout;

public class LayoutGenerator {
    Context context;
    float pixelDensity;

    public LayoutGenerator(Context context) {
        this.context = context;
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    public LinearLayout generateLayout() {
        LinearLayout view = new LinearLayout(context);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        view.setLayoutParams(viewParams);
        view.setPadding(0, (int) (65 * pixelDensity), 0, (int) (15 * pixelDensity));
        view.setOrientation(LinearLayout.VERTICAL);

        return view;
    }
}
