package tk.therealsuji.vtopchennai;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class CampusMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final ImageView campusMap = findViewById(R.id.campus_map);

        campusMap.post(() -> {
            float originalHeight = campusMap.getHeight();
            float originalWidth = campusMap.getWidth();

            LinearLayout campusMapContainer = findViewById(R.id.campus_map_container);
            float newHeight = campusMapContainer.getHeight();
            int newWidth = (int) (originalWidth * (newHeight / originalHeight));

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    newWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            campusMap.setLayoutParams(imageParams);

            campusMap.animate().alpha(1);
        });
    }
}