package tk.therealsuji.vtopchennai;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class DirectionsActivity extends AppCompatActivity {
    ScrollView locations;
    TextView[] locationCategories = new TextView[5];
    LinearLayout[] locationViews = new LinearLayout[5];
    float pixelDensity;
    int locationCategory;

    public void setCategory(View view) {
        int index = Integer.parseInt(view.getTag().toString());
        if (index == locationCategory) {
            return;
        }

        locationCategory = index;

        locations.scrollTo(0, 0);
        for (int i = 0; i < 5; ++i) {
            locationViews[i].setVisibility(View.GONE);
            locationCategories[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        locationViews[locationCategory].setAlpha(0);
        locationViews[locationCategory].setVisibility(View.VISIBLE);
        locationViews[locationCategory].animate().alpha(1);
        locationCategories[locationCategory].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int halfWidth = displayMetrics.widthPixels / 2;
        float location = 0;
        for (int i = 0; i < locationCategory; ++i) {
            location += 10 * pixelDensity + (float) locationCategories[i].getWidth();
        }
        location += 20 * pixelDensity + (float) locationCategories[locationCategory].getWidth() / 2;

        ((HorizontalScrollView) findViewById(R.id.location_categories)).smoothScrollTo((int) location - halfWidth, 0);
    }

    public void openLocation(View view) {
        String query = view.getTag().toString();
        query = query.replaceAll("#", "\"");
        String url = "https://www.google.com/maps/place/" + query;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        pixelDensity = context.getResources().getDisplayMetrics().density;
        locations = findViewById(R.id.locations);

        locationCategories[0] = findViewById(R.id.academics);
        locationCategories[1] = findViewById(R.id.hostels);
        locationCategories[2] = findViewById(R.id.food);
        locationCategories[3] = findViewById(R.id.atms);
        locationCategories[4] = findViewById(R.id.amenities);

        locationViews[0] = findViewById(R.id.academic_locations);
        locationViews[1] = findViewById(R.id.hostel_locations);
        locationViews[2] = findViewById(R.id.food_locations);
        locationViews[3] = findViewById(R.id.atm_locations);
        locationViews[4] = findViewById(R.id.amenity_locations);

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject academics = new JSONObject();
                try {
                    academics.put("Academic Block 1", "{\"description\": \"\", \"tag\": \"\"}");
                    academics.put("Academic Block 2", "{\"description\": \"\", \"tag\": \"\"}");
                    academics.put("Administrative Block", "{\"description\": \"\", \"tag\": \"\"}");
                    academics.put("Central Library", "{\"description\": \"\", \"tag\": \"\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject hostels = new JSONObject();
                try {
                    hostels.put("Boys Hostel A Block", "{\"description\": \"\", \"tag\": \"\"}");
                    hostels.put("Boys Hostel B Block", "{\"description\": \"\", \"tag\": \"\"}");
                    hostels.put("Boys Hostel C Block", "{\"description\": \"\", \"tag\": \"\"}");
                    hostels.put("Girls Hostel", "{\"description\": \"\", \"tag\": \"\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject food = new JSONObject();
                try {
                    food.put("Aavin Milk Parlor", "{\"description\": \"\", \"tag\": \"\"}");
                    food.put("Chai Galli", "{\"description\": \"\", \"tag\": \"\"}");
                    food.put("Domino's Pizza", "{\"description\": \"\", \"tag\": \"\"}");
                    food.put("Gazebo", "{\"description\": \"\", \"tag\": \"\"}");
                    food.put("Gym Khaana", "{\"description\": \"\", \"tag\": \"\"}");
                    food.put("Lassi House", "{\"description\": \"\", \"tag\": \"\"}");
                    food.put("Quality and Taste", "{\"description\": \"\", \"tag\": \"\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject atms = new JSONObject();
                try {
                    atms.put("KVB Bank ATM", "{\"description\": \"\", \"tag\": \"\"}");
                    atms.put("India Bank & ATM", "{\"description\": \"\", \"tag\": \"\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject amenities = new JSONObject();
                try {
                    amenities.put("Clock Tower", "{\"description\": \"\", \"tag\": \"\"}");
                    amenities.put("North Square", "{\"description\": \"\", \"tag\": \"\"}");
                    amenities.put("VIT Fun Park", "{\"description\": \"\", \"tag\": \"\"}");
                    amenities.put("VIT Garden", "{\"description\": \"\", \"tag\": \"\"}");
                    amenities.put("VIT Pond", "{\"description\": \"\", \"tag\": \"\"}");
                    amenities.put("V-Mart", "{\"description\": \"\", \"tag\": \"\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject[] locationData = {academics, hostels, food, atms, amenities};

                try {
                    boolean loadingVisible = true;
                    for (int i = 0; i < locationViews.length; ++i) {
                        Iterator<?> keys = locationData[i].keys();

                        while (keys.hasNext()) {
                            String title = (String) keys.next();

                            JSONObject data = new JSONObject(locationData[i].getString(title));
                            String description = data.getString("description");
                            String tag = data.getString("tag");

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
                            block.setOrientation(LinearLayout.HORIZONTAL);
                            block.setGravity(Gravity.CENTER_VERTICAL);
                            block.setAlpha(0);

                            /*
                                The inner block
                             */
                            LinearLayout innerBlock = new LinearLayout(context);
                            LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1
                            );
                            innerBlockParams.setMarginStart((int) (20 * pixelDensity));
                            innerBlockParams.setMarginEnd((int) (20 * pixelDensity));
                            innerBlock.setLayoutParams(innerBlockParams);
                            innerBlock.setOrientation(LinearLayout.VERTICAL);

                            /*
                                The title TextView
                             */
                            TextView titleView = new TextView(context);
                            TableRow.LayoutParams titleViewParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            titleViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (5 * pixelDensity));
                            titleView.setLayoutParams(titleViewParams);
                            titleView.setText(title);
                            titleView.setTextColor(getColor(R.color.colorPrimary));
                            titleView.setTextSize(20);
                            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);

                            innerBlock.addView(titleView);   //Adding title to the inner block

                            /*
                                The description TextView
                             */
                            TextView descriptionView = new TextView(context);
                            TableRow.LayoutParams descriptionViewParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            descriptionViewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (20 * pixelDensity));
                            descriptionView.setLayoutParams(descriptionViewParams);
                            descriptionView.setText(description);
                            descriptionView.setTextColor(getColor(R.color.colorPrimary));
                            descriptionView.setTextSize(16);
                            descriptionView.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));

                            innerBlock.addView(descriptionView);   //Adding description to the inner block

                            block.addView(innerBlock);

                            /*
                                The link button
                             */
                            final LinearLayout linkButton = new LinearLayout(context);
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
                            linkButton.setTag(tag);

                            StateListAnimator elevation = AnimatorInflater.loadStateListAnimator(context, R.animator.item_elevation);
                            linkButton.setStateListAnimator(elevation);

                            linkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openLocation(linkButton);
                                }
                            });

                            ImageView imageView = new ImageView(context);
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_location));

                            linkButton.addView(imageView);
                            block.addView(linkButton);

                            final int index = i;
                            if (loadingVisible) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.loading).setVisibility(View.GONE);
                                        locationViews[index].addView(block);
                                        block.animate().alpha(1);
                                    }
                                });
                                loadingVisible = false;
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        locationViews[index].addView(block);
                                        block.animate().alpha(1);
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}