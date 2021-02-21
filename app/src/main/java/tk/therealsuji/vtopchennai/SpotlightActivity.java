package tk.therealsuji.vtopchennai;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Objects;

public class SpotlightActivity extends AppCompatActivity {
    ScrollView announcements;
    HorizontalScrollView categoriesContainer;
    ArrayList<TextView> categories = new ArrayList<>();
    ArrayList<LinearLayout> announcementViews = new ArrayList<>();
    String savedLink;
    float pixelDensity;
    int index, halfWidth;
    int STORAGE_PERMISSION_CODE = 1;

    public void setAnnouncements(View view) {
        int selectedIndex = Integer.parseInt(view.getTag().toString());
        if (selectedIndex == index) {
            return;
        } else {
            index = selectedIndex;
        }

        announcements.scrollTo(0, 0);
        announcements.removeAllViews();
        announcements.setAlpha(0);
        announcements.addView(announcementViews.get(index));
        announcements.animate().alpha(1);

        for (int i = 0; i < categories.size(); ++i) {
            categories.get(i).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        categories.get(index).setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float location = 0;
        for (int i = 0; i < index; ++i) {
            location += 10 * pixelDensity + (float) categories.get(i).getWidth();
        }
        location += 20 * pixelDensity + (float) categories.get(index).getWidth() / 2;
        categoriesContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    public void openLink(String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    public void downloadDocument(String link) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            savedLink = link;
            return;
        }

        final String downloadLink = "http://vtopcc.vit.ac.in/vtop/" + link + "?&x=";
        final boolean[] isOpened = {false};

        final WebView download = new WebView(this);
        download.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36");
        download.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (isOpened[0]) {
                    return;
                }

                isOpened[0] = true;
                download.loadUrl(downloadLink);
            }
        });
        download.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Toast.makeText(SpotlightActivity.this, "Downloading document", Toast.LENGTH_SHORT).show();

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                String fileName = contentDisposition.split("filename=")[1];
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "VTOP Spotlight/" + fileName);
                request.setMimeType(mimetype);
                request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url));
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);
            }
        });

        download.loadUrl("http://vtopcc.vit.ac.in/vtop");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadDocument(savedLink);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotlight);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        final LinearLayout categoryButtons = findViewById(R.id.categories);
        announcements = findViewById(R.id.announcements);
        pixelDensity = context.getResources().getDisplayMetrics().density;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        categoriesContainer = findViewById(R.id.categoriesContainer);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase myDatabase = context.openOrCreateDatabase("vtop", Context.MODE_PRIVATE, null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS spotlight (id INT(3) PRIMARY KEY, category VARCHAR, announcement VARCHAR, link VARCHAR)");
                Cursor c = myDatabase.rawQuery("SELECT DISTINCT category FROM spotlight", null);

                int categoryIndex = c.getColumnIndex("category");
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); ++i, c.moveToNext()) {
                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.noData).setVisibility(View.GONE);
                            }
                        });
                    }

                    String categoryTitle = c.getString(categoryIndex);

                    /*
                        Creating the announcements view
                     */
                    final LinearLayout announcementsView = new LinearLayout(context);
                    LinearLayout.LayoutParams announcementsViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    announcementsView.setLayoutParams(announcementsViewParams);
                    announcementsView.setPadding(0, (int) (65 * pixelDensity), 0, 0);
                    announcementsView.setOrientation(LinearLayout.VERTICAL);

                    announcementViews.add(announcementsView);    //Storing the view

                    /*
                        Creating the category button
                     */
                    final TextView category = new TextView(context);
                    LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) (25 * pixelDensity)
                    );
                    categoryParams.setMarginStart((int) (5 * pixelDensity));
                    categoryParams.setMarginEnd((int) (5 * pixelDensity));
                    categoryParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                    category.setLayoutParams(categoryParams);
                    category.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), 0);
                    if (i == 0) {
                        category.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary_selected));
                    } else {
                        category.setBackground(ContextCompat.getDrawable(context, R.drawable.button_secondary));
                    }
                    category.setTag(i);
                    category.setText(categoryTitle.toUpperCase());
                    category.setTextColor(getColor(R.color.colorPrimary));
                    category.setTextSize(12);
                    category.setGravity(Gravity.CENTER_VERTICAL);
                    category.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
                    category.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setAnnouncements(category);
                        }
                    });
                    category.setAlpha(0);
                    category.animate().alpha(1);

                    categories.add(category);    //Storing the button

                    /*
                        Finally adding the button to the HorizontalScrollView
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            categoryButtons.addView(category);
                        }
                    });

                    Cursor s = myDatabase.rawQuery("SELECT announcement, link FROM spotlight WHERE category = '" + categoryTitle + "'", null);

                    int announcementIndex = s.getColumnIndex("announcement");
                    int linkIndex = s.getColumnIndex("link");
                    s.moveToFirst();

                    for (int j = 0; j < s.getCount(); ++j, s.moveToNext()) {
                        /*
                            The outer block
                         */
                        final LinearLayout block = new LinearLayout(context);
                        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        blockParams.setMarginStart((int) (20 * pixelDensity));
                        blockParams.setMarginEnd((int) (20 * pixelDensity));
                        blockParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
                        block.setLayoutParams(blockParams);
                        block.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
                        block.setGravity(Gravity.CENTER_VERTICAL);
                        block.setOrientation(LinearLayout.HORIZONTAL);

                        /*
                            The announcement TextView
                         */
                        TextView announcement = new TextView(context);
                        TableRow.LayoutParams announcementParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1
                        );
                        announcementParams.setMarginStart((int) (20 * pixelDensity));
                        announcementParams.setMarginEnd((int) (20 * pixelDensity));
                        announcementParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                        announcement.setLayoutParams(announcementParams);
                        announcement.setText(s.getString(announcementIndex));
                        announcement.setTextColor(getColor(R.color.colorPrimary));
                        announcement.setTextSize(16);
                        announcement.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                        block.addView(announcement); //Adding course code to block

                        /*
                            Setting up the links
                         */
                        final String link = s.getString(linkIndex);
                        if (link.startsWith("http")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openLink(link);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_link));

                            linkButton.addView(imageView);
                            block.addView(linkButton);
                        } else if (!link.equals("null")) {
                            LinearLayout linkButton = new LinearLayout(context);
                            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                                    (int) (50 * pixelDensity),
                                    (int) (50 * pixelDensity)
                            );
                            linkParams.setMarginEnd((int) (20 * pixelDensity));
                            linkParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            linkButton.setLayoutParams(linkParams);
                            linkButton.setClickable(true);
                            linkButton.setFocusable(true);
                            linkButton.setGravity(Gravity.CENTER);
                            linkButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_link));

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    downloadDocument(link);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download));

                            linkButton.addView(imageView);
                            block.addView(linkButton);
                        }

                        /*
                            Finally adding the block to the announcements layout
                         */
                        announcementsView.addView(block);
                    }

                    if (i == index) {
                        announcementsView.setAlpha(0);
                        announcementsView.animate().alpha(1);
                    }

                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                announcements.addView(announcementsView);
                            }
                        });
                    }

                    s.close();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });

                c.close();
                myDatabase.close();

                SharedPreferences sharedPreferences = context.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("newSpotlight").apply();
            }
        }).start();
    }
}