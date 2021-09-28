package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.ProfileGroupAdapter;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View profileFragment = inflater.inflate(R.layout.fragment_profile, container, false);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        profileFragment.findViewById(R.id.header).setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(
                    (int) (20 * pixelDensity),
                    (int) (20 * pixelDensity + windowInsets.getSystemWindowInsetTop()),
                    (int) (20 * pixelDensity),
                    (int) (20 * pixelDensity)
            );
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        RecyclerView profileGroups = profileFragment.findViewById(R.id.profile_groups);
        profileGroups.setAdapter(new ProfileGroupAdapter());

        return profileFragment;
    }
}