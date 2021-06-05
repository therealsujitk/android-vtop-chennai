package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class DirectionsActivity extends AppCompatActivity {
    ScrollView locations;
    TextView[] locationCategories = new TextView[6];
    LinearLayout[] locationViews = new LinearLayout[6];
    HorizontalScrollView locationCategoriesContainer;
    float pixelDensity;
    int locationCategory, halfWidth;

    boolean terminateThread;

    public void setCategory(View view) {
        int index = Integer.parseInt(view.getTag().toString());
        if (index == locationCategory) {
            return;
        }

        locationCategory = index;

        locations.scrollTo(0, 0);
        locations.removeAllViews();
        locations.setAlpha(0);
        locations.addView(locationViews[locationCategory]);
        locations.animate().alpha(1);

        for (int i = 0; i < 6; ++i) {
            locationCategories[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }
        locationCategories[locationCategory].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

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
        startActivity(new Intent(this, CampusMapActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Context context = this;
        pixelDensity = context.getResources().getDisplayMetrics().density;
        locations = findViewById(R.id.locations);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        halfWidth = displayMetrics.widthPixels / 2;

        locationCategoriesContainer = findViewById(R.id.location_categories);

        locationCategories[0] = findViewById(R.id.main_blocks);
        locationCategories[1] = findViewById(R.id.hostels);
        locationCategories[2] = findViewById(R.id.food);
        locationCategories[3] = findViewById(R.id.atms);
        locationCategories[4] = findViewById(R.id.amenities);
        locationCategories[5] = findViewById(R.id.sports);

        locationCategoriesContainer.animate().alpha(1);

        LayoutGenerator myLayout = new LayoutGenerator(this);

        for (int i = 0; i < 6; ++i) {
            locationViews[i] = myLayout.generateLayout();
        }

        locations.addView(locationViews[0]);

        new Thread(() -> {
            JSONObject main = new JSONObject();
            try {
                /*
                    TODO: Alpha block is redirected to an anonymous location. It has to be updated.
                 */
                main.put("Academic Block 1", "{\"description\": \"\", \"tag\": \"wWaWFDiuUnrSkv7Q8\"}");
                main.put("Academic Block 2", "{\"description\": \"\", \"tag\": \"2DcLPUj6JBU2SD698\"}");
                main.put("Administrative Block", "{\"description\": \"\", \"tag\": \"GNYq3VdZytBk42Jp7\"}");
                main.put("Alpha Block", "{\"description\": \"Health Centre\", \"tag\": \"hkRzrEu8fHjFKxMX6\"}");
                main.put("Central Library", "{\"description\": \"\", \"tag\": \"B61HccC3wuTcepRE6\"}");
            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject hostels = new JSONObject();
            try {
                hostels.put("Hostel A Block", "{\"description\": \"Hostel for Senior Boys\", \"tag\": \"sp6v7XomBw5sZ3cy8\"}");
                hostels.put("Hostel B Block", "{\"description\": \"Hostel for Girls & Freshmen Boys\", \"tag\": \"cJvcHPEBYoDymvP88\"}");
                hostels.put("Hostel C Block", "{\"description\": \"Hostel for Boys\", \"tag\": \"ZoqkXMuMh3GN3NVH9\"}");
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
                    TODO: India bank is redirected to an anonymous location. It has to be updated.
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
                CardGenerator myDirection = new CardGenerator(context, CardGenerator.CARD_DIRECTION);
                LinkButtonGenerator myLink = new LinkButtonGenerator(context);

                for (int i = 0; i < locationViews.length; ++i) {
                    if (terminateThread) {
                        return;
                    }

                    Iterator<?> keys = locationData[i].keys();

                    while (keys.hasNext()) {
                        if (terminateThread) {
                            return;
                        }

                        String title = (String) keys.next();

                        JSONObject data = new JSONObject(locationData[i].getString(title));
                        String description = data.getString("description");
                        String tag = data.getString("tag");

                        final LinearLayout card = myDirection.generateCard(title, description);
                        final LinearLayout linkView = myLink.generateButton(null, LinkButtonGenerator.LINK_DIRECTION);
                        linkView.setTag(tag);
                        linkView.setOnClickListener(v -> openLocation(linkView));

                        card.addView(linkView);

                        /*
                            Adding the card to the view
                         */
                        if (i == locationCategory) {
                            card.setAlpha(0);
                            card.animate().alpha(1);

                            runOnUiThread(() -> locationViews[locationCategory].addView(card));
                        } else {
                            locationViews[i].addView(card);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> findViewById(R.id.loading).animate().alpha(0));
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.directions_menu, menu);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        terminateThread = true;
    }
}