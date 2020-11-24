package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class PrivacyActivity extends AppCompatActivity {

    private void setNight() {
        getWindow().setBackgroundDrawableResource(R.color.colorDark);

        TextView privacyIntro = findViewById(R.id.privacyIntro);
        privacyIntro.setTextColor(getColor(R.color.colorLight));
        TextView privacyOwnership = findViewById(R.id.privacyOwnership);
        privacyOwnership.setTextColor(getColor(R.color.colorLight));
        TextView privacyOwnershipContent = findViewById(R.id.privacyOwnershipContent);
        privacyOwnershipContent.setTextColor(getColor(R.color.colorLight));
        TextView privacyData = findViewById(R.id.privacyData);
        privacyData.setTextColor(getColor(R.color.colorLight));
        TextView privacyDataContent = findViewById(R.id.privacyDataContent);
        privacyDataContent.setTextColor(getColor(R.color.colorLight));
        TextView privacyContact = findViewById(R.id.privacyContact);
        privacyContact.setTextColor(getColor(R.color.colorLight));
        TextView privacyContactContent = findViewById(R.id.privacyContactContent);
        privacyContactContent.setTextColor(getColor(R.color.colorLight));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        /*
            Set appearance
         */
        SharedPreferences sharedPreferences = this.getSharedPreferences("tk.therealsuji.vtopchennai", Context.MODE_PRIVATE);
        String appearance = sharedPreferences.getString("appearance", "system");

        if (appearance.equals("night")) {
            setNight();
        } else if (appearance.equals("system")) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setNight();
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    break;
            }
        }
    }
}