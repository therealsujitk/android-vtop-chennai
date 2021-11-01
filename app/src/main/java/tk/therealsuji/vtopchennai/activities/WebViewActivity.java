package tk.therealsuji.vtopchennai.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import tk.therealsuji.vtopchennai.R;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        String appearance = sharedPreferences.getString("appearance", "system");
        int visibility = getWindow().getDecorView().getSystemUiVisibility();

        if (appearance.equals("light") || (appearance.equals("system") && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO)) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(visibility);

        ConstraintLayout webViewLayout = findViewById(R.id.layout_web_view);
        webViewLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.setMargins(
                    windowInsets.getSystemWindowInsetLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    windowInsets.getSystemWindowInsetRight(),
                    windowInsets.getSystemWindowInsetBottom()
            );
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        findViewById(R.id.button_back).setOnClickListener(view -> finish());

        Bundle extras = getIntent().getExtras();
        String title = extras.getString("title");
        String url = getIntent().getExtras().getString("url");

        TextView titleView = findViewById(R.id.text_title);
        titleView.setText(title);

        WebView webView = findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }
}