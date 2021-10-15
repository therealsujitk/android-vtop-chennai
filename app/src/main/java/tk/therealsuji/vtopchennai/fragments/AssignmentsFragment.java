package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import tk.therealsuji.vtopchennai.R;

public class AssignmentsFragment extends Fragment {

    public AssignmentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsFragment = inflater.inflate(R.layout.fragment_assignments, container, false);
        float pixelDensity = this.getResources().getDisplayMetrics().density;

        assignmentsFragment.findViewById(R.id.header).setOnApplyWindowInsetsListener((view, windowInsets) -> {
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

        return assignmentsFragment;
    }
}