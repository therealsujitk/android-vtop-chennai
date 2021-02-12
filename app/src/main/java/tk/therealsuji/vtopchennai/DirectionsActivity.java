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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class DirectionsActivity extends AppCompatActivity {
    ScrollView locations;
    TextView[] locationCategories = new TextView[6];
    LinearLayout[] locationViews = new LinearLayout[6];
    HorizontalScrollView locationCategoriesContainer;
    float pixelDensity;
    int locationCategory;

    public void setCategory(View view) {
        int index = Integer.parseInt(view.getTag().toString());
        if (index == locationCategory) {
            return;
        }

        locationCategory = index;

        locations.scrollTo(0, 0);
        for (int i = 0; i < 6; ++i) {
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

        locationCategoriesContainer.smoothScrollTo((int) location - halfWidth, 0);
    }

    public void openLocation(View view) {
        String query = view.getTag().toString();
        String url = "https://goo.gl/maps/" + query;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void openMap(MenuItem item) {
        Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        pixelDensity = context.getResources().getDisplayMetrics().density;
        locations = findViewById(R.id.locations);

        locationCategoriesContainer = findViewById(R.id.location_categories);

        locationCategories[0] = findViewById(R.id.main_blocks);
        locationCategories[1] = findViewById(R.id.hostels);
        locationCategories[2] = findViewById(R.id.food);
        locationCategories[3] = findViewById(R.id.atms);
        locationCategories[4] = findViewById(R.id.amenities);
        locationCategories[5] = findViewById(R.id.sports);

        locationViews[0] = findViewById(R.id.main_locations);
        locationViews[1] = findViewById(R.id.hostel_locations);
        locationViews[2] = findViewById(R.id.food_locations);
        locationViews[3] = findViewById(R.id.atm_locations);
        locationViews[4] = findViewById(R.id.amenity_locations);
        locationViews[5] = findViewById(R.id.sports_locations);

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject main = new JSONObject();
                try {
                    /*
                        Alpha block is redirected to an anonymous location. It has to be updated.
                     */
                    main.put("Academic Block 1", "{\"description\": \"\", \"tag\": \"wWaWFDiuUnrSkv7Q8\"}");
                    main.put("Academic Block 2", "{\"description\": \"\", \"tag\": \"2DcLPUj6JBU2SD698\"}");
                    main.put("Administrative Block", "{\"description\": \"\", \"tag\": \"GNYq3VdZytBk42Jp7\"}");
                    main.put("Alpha Block", "{\"description\": \"Health Centre\", \"tag\": \"c4eDWs1L2RJLpcoH8\"}");
                    main.put("Central Library", "{\"description\": \"\", \"tag\": \"B61HccC3wuTcepRE6\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject hostels = new JSONObject();
                try {
                    hostels.put("Hostel A Block", "{\"description\": \"Hostel for Sophomore & Junior Boys\", \"tag\": \"sp6v7XomBw5sZ3cy8\"}");
                    hostels.put("Hostel B Block", "{\"description\": \"Hostel for Senior Boys\", \"tag\": \"cJvcHPEBYoDymvP88\"}");
                    hostels.put("Hostel C Block", "{\"description\": \"Hostel for Girls & Freshmen Boys\", \"tag\": \"Mwk7WG6uRBZ6CNne7\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject food = new JSONObject();
                try {
                    food.put("Aavin Milk Parlor", "{\"description\": \"Dairy Store\", \"tag\": \"pWLcc1fBTfnBJDQk6\"}");
                    food.put("Chai Galli", "{\"description\": \"Café\", \"tag\": \"B3h9nQVADTti6jFZ7\"}");
                    food.put("Domino's Pizza", "{\"description\": \"Pizza Restaurant\", \"tag\": \"i8WRzayorFQkoDWMA\"}");
                    food.put("Food Park", "{\"description\": \"Cafeteria\", \"tag\": \"kyietg8dh5nuqeBB6\"}");
                    food.put("Gazebo", "{\"description\": \"Food Stall\", \"tag\": \"ARvc3dyCW6CoBaEr8\"}");
                    food.put("Georgia Coffee", "{\"description\": \"Food Stall\", \"tag\": \"MPhfv4hp3mHUXoAN9\"}");
                    food.put("Gym Khaana", "{\"description\": \"Food Court\", \"tag\": \"V6UpCxxr8cgiuwiR6\"}");
                    food.put("Lassi House", "{\"description\": \"Beverage Stall\", \"tag\": \"qBZqiFaKddKnxgAW6\"}");
                    food.put("Quality and Taste", "{\"description\": \"Fast Food Stall\", \"tag\": \"1gditDKn9CaCTma17\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject atms = new JSONObject();
                try {
                    /*
                        India bank is redirected to an anonymous location. It has to be updated.
                     */
                    atms.put("India Bank", "{\"description\": \"Bank & ATM\", \"tag\": \"JbFEbDmR69yxQyn47\"}");
                    atms.put("Karur Vysya Bank", "{\"description\": \"ATM\", \"tag\": \"zt1XxVx5mizdkEub8\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject amenities = new JSONObject();
                try {
                    amenities.put("Amphitheatre", "{\"description\": \"\", \"tag\": \"7uWQdMrS5MmFn8Pq8\"}");
                    amenities.put("Clock Tower", "{\"description\": \"\", \"tag\": \"5Hij99iPGzP3TXrL8\"}");
                    amenities.put("North Square", "{\"description\": \"Garden\", \"tag\": \"Dfohs4pj3Qg781oH6\"}");
                    amenities.put("VIT Fun Park", "{\"description\": \"\", \"tag\": \"EaauYydSgWsx9Sfg6\"}");
                    amenities.put("VIT Pond", "{\"description\": \"\", \"tag\": \"Q52wGN3tFStmSEae8\"}");
                    amenities.put("V-Mart", "{\"description\": \"Shopping Store\", \"tag\": \"jwru6FH6mmFGTiCq7\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject sports = new JSONObject();
                try {
                    sports.put("Athletic Track", "{\"description\": \"\", \"tag\": \"i85c3dzewqrdNkox6\"}");
                    sports.put("Ball Badminton Court", "{\"description\": \"\", \"tag\": \"mGddU7pvrBUJdsSd8\"}");
                    sports.put("Basketball Court 1", "{\"description\": \"\", \"tag\": \"dPVbXctTszwfUYFR9\"}");
                    sports.put("Basketball Court 2", "{\"description\": \"\", \"tag\": \"bbamrz4k7xbN4UbD9\"}");
                    sports.put("Cricket Ground 1", "{\"description\": \"\", \"tag\": \"2N7FzmGQrXwJfcnp8\"}");
                    sports.put("Cricket Ground 2", "{\"description\": \"\", \"tag\": \"AuH3J7eGcAQ6XmAd7\"}");
                    sports.put("Cricket Net Practice", "{\"description\": \"\", \"tag\": \"3aWrVX9poiugg7QD9\"}");
                    sports.put("Football Pitch", "{\"description\": \"\", \"tag\": \"KDjvALnJMNRvEzrk9\"}");
                    sports.put("Hockey Pitch", "{\"description\": \"\", \"tag\": \"oCuqAmMdxUPHAuf87\"}");
                    sports.put("Swimming Pool", "{\"description\": \"\", \"tag\": \"agZgCfRwJwjFpS7X6\"}");
                    sports.put("Tennis Courts", "{\"description\": \"\", \"tag\": \"vFdD9p7rEBGEUBG97\"}");
                    sports.put("Volley Ball Courts", "{\"description\": \"\", \"tag\": \"yZjhmPEqAx7BnJmeA\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject[] locationData = {main, hostels, food, atms, amenities, sports};

                try {
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
                            if (description.equals("")) {
                                titleViewParams.setMargins(0, (int) (20 * pixelDensity), 0, (int) (20 * pixelDensity));
                            } else {
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
                            }

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
                            if (i == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        locationCategoriesContainer.animate().alpha(1);
                                        locationViews[index].addView(block);
                                        block.animate().alpha(1);
                                    }
                                });
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loading).animate().alpha(0);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.directions_menu, menu);

        return true;
    }
}