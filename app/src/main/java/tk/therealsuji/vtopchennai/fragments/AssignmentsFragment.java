package tk.therealsuji.vtopchennai.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;

public class AssignmentsFragment extends Fragment {

    public AssignmentsFragment() {
        // Required empty public constructor
    }

    private void getAssignments() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assignmentsFragment = inflater.inflate(R.layout.fragment_assignments, container, false);

        assignmentsFragment.findViewById(R.id.text_view_title).setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPadding(
                    0,
                    windowInsets.getSystemWindowInsetTop(),
                    0,
                    0
            );

            return windowInsets;
        });

        SharedPreferences sharedPreferences = SettingsRepository.getSharedPreferences(this.requireContext().getApplicationContext());
        boolean isSignedIn = !sharedPreferences.getString("moodleToken", "").equals("");

        if (isSignedIn) {
            this.getAssignments();
        } else {
            LinearLayout signInContainer = assignmentsFragment.findViewById(R.id.linear_layout_container);
            signInContainer.setVisibility(View.VISIBLE);

            ((ViewGroup.MarginLayoutParams) signInContainer.getLayoutParams()).setMargins(
                    0,
                    0,
                    0,
                    ((MainActivity) this.requireActivity()).getBottomNavigationPadding()
            );
        }

        return assignmentsFragment;
    }
}