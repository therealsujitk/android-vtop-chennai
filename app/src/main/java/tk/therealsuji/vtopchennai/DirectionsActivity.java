package tk.therealsuji.vtopchennai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class DirectionsActivity extends AppCompatActivity {
    ScrollView locations;
    TextView[] locationCategories = new TextView[5];
    LinearLayout[] locationViews = new LinearLayout[5];

    public void setCategory(View view) {
        locations.scrollTo(0, 0);

        int locationCategory = Integer.parseInt(view.getTag().toString());

        for (int i = 0; i < 5; ++i) {
            locationViews[i].setVisibility(View.GONE);
            locationCategories[i].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary));
        }

        locationViews[locationCategory].setVisibility(View.VISIBLE);
        locationCategories[locationCategory].setBackground(ContextCompat.getDrawable(this, R.drawable.button_secondary_selected));

        float pixelDensity = this.getResources().getDisplayMetrics().density;

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
    }
}