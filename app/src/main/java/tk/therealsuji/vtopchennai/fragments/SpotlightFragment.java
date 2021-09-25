package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.MainActivity;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.SpotlightSectionAdapter;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.SpotlightDao;
import tk.therealsuji.vtopchennai.models.Spotlight;

public class SpotlightFragment extends Fragment {

    public SpotlightFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View spotlightFragment = inflater.inflate(R.layout.fragment_spotlight, container, false);

        LinearLayout header = spotlightFragment.findViewById(R.id.header);
        header.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(0, windowInsets.getSystemWindowInsetTop(), 0, 0);
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        spotlightFragment.findViewById(R.id.button_back).setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        RecyclerView spotlight = spotlightFragment.findViewById(R.id.spotlight);
        spotlight.setLayoutManager(new LinearLayoutManager(this.requireActivity()));
        SpotlightSectionAdapter spotlightSectionAdapter = new SpotlightSectionAdapter();

        spotlight.setAdapter(spotlightSectionAdapter);

        AppDatabase appDatabase = AppDatabase.getInstance(this.requireActivity().getApplicationContext());
        SpotlightDao spotlightDao = appDatabase.spotlightDao();

        spotlightDao.getSpotlight().subscribeOn(Schedulers.single()).subscribe(new SingleObserver<List<Spotlight>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onSuccess(@NonNull List<Spotlight> spotlight) {
                spotlightSectionAdapter.setSpotlight(spotlight);
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }
        });

        return spotlightFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((MainActivity) this.requireActivity()).showBottomNavigationView();
    }
}