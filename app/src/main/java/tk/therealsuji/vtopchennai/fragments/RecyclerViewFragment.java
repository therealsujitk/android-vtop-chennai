package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.adapters.ReceiptsItemAdapter;
import tk.therealsuji.vtopchennai.adapters.SpotlightGroupAdapter;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.ReceiptsDao;
import tk.therealsuji.vtopchennai.interfaces.SpotlightDao;
import tk.therealsuji.vtopchennai.models.Receipt;
import tk.therealsuji.vtopchennai.models.Spotlight;

public class RecyclerViewFragment extends Fragment {
    public static final int TYPE_ATTENDANCE = 1;
    public static final int TYPE_RECEIPTS = 2;
    public static final int TYPE_SPOTLIGHT = 3;

    AppDatabase appDatabase;
    RecyclerView recyclerView;

    public RecyclerViewFragment() {
        // Required empty public constructor
    }

    private void attachAttendance() {

    }

    private void attachReceipts() {
        ReceiptsDao receiptsDao = this.appDatabase.receiptsDao();

        receiptsDao
                .getReceipts()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Receipt>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<Receipt> receipts) {
                        recyclerView.setAdapter(new ReceiptsItemAdapter(receipts));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });
    }

    private void attachSpotlight() {
        SpotlightDao spotlightDao = this.appDatabase.spotlightDao();
        spotlightDao.getSpotlight().subscribeOn(Schedulers.single()).subscribe(new SingleObserver<List<Spotlight>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onSuccess(@NonNull List<Spotlight> spotlight) {
                recyclerView.setAdapter(new SpotlightGroupAdapter(spotlight));
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View recyclerViewFragment = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        LinearLayout header = recyclerViewFragment.findViewById(R.id.linear_layout_header);
        header.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(0, windowInsets.getSystemWindowInsetTop(), 0, 0);
            view.setLayoutParams(layoutParams);

            return windowInsets.consumeSystemWindowInsets();
        });

        this.recyclerView = recyclerViewFragment.findViewById(R.id.recycler_view);
        this.appDatabase = AppDatabase.getInstance(this.requireActivity().getApplicationContext());

        int titleId = 0, contentType = 0;
        Bundle arguments = this.getArguments();

        if (arguments != null) {
            titleId = arguments.getInt("title_id", 0);
            contentType = arguments.getInt("content_type", 0);
        }

        recyclerViewFragment.findViewById(R.id.image_button_back).setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());
        ((TextView) recyclerViewFragment.findViewById(R.id.text_view_title)).setText(getString(titleId));

        switch (contentType) {
            case TYPE_ATTENDANCE:
                this.attachAttendance();
                break;
            case TYPE_RECEIPTS:
                this.attachReceipts();
                break;
            case TYPE_SPOTLIGHT:
                this.attachSpotlight();
                break;
        }

        return recyclerViewFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((MainActivity) this.requireActivity()).showBottomNavigationView();
    }
}