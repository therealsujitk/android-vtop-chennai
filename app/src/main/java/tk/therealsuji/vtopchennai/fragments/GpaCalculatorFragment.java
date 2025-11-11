package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.GpaSubjectAdapter;
import tk.therealsuji.vtopchennai.models.GpaSubject;

public class GpaCalculatorFragment extends Fragment {
    private static final String[] GRADES = {"S", "A", "B", "C", "D", "E", "F"};
    private static final String[] CREDITS = {"1", "2", "3", "4", "5", "6"};
    private static final Map<String, Integer> GRADE_POINTS = new HashMap<>();

    static {
        GRADE_POINTS.put("S", 10);
        GRADE_POINTS.put("A", 9);
        GRADE_POINTS.put("B", 8);
        GRADE_POINTS.put("C", 7);
        GRADE_POINTS.put("D", 6);
        GRADE_POINTS.put("E", 5);
        GRADE_POINTS.put("F", 0);
    }

    private final List<GpaSubject> subjects = new ArrayList<>();
    private final DecimalFormat gpaFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.getDefault()));
    private final DecimalFormat pointsFormat = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.getDefault()));

    private TextInputLayout gradeInputLayout;
    private TextInputLayout creditsInputLayout;
    private TextInputLayout subjectInputLayout;
    private TextInputEditText subjectEditText;
    private MaterialAutoCompleteTextView gradeDropdown;
    private MaterialAutoCompleteTextView creditsDropdown;
    private RecyclerView subjectsRecyclerView;
    private TextView emptyStateView;
    private TextView resultValueView;
    private TextView totalPointsView;
    private TextView totalCreditsView;
    private GpaSubjectAdapter subjectAdapter;

    public GpaCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bottomNavigationVisibility = new Bundle();
        bottomNavigationVisibility.putBoolean("isVisible", false);
        getParentFragmentManager().setFragmentResult("bottomNavigationVisibility", bottomNavigationVisibility);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gpa_calculator, container, false);
        view.getRootView().setBackgroundColor(requireContext().getColor(R.color.secondary_container_95));

        View header = view.findViewById(R.id.linear_layout_header);
        NestedScrollView nestedScrollView = view.findViewById(R.id.nested_scroll_view);

        subjectInputLayout = view.findViewById(R.id.text_input_subject);
        gradeInputLayout = view.findViewById(R.id.text_input_grade);
        creditsInputLayout = view.findViewById(R.id.text_input_credits);
        subjectEditText = view.findViewById(R.id.edit_text_subject);
        gradeDropdown = view.findViewById(R.id.dropdown_grade);
        creditsDropdown = view.findViewById(R.id.dropdown_credits);
        subjectsRecyclerView = view.findViewById(R.id.recycler_view_subjects);
        emptyStateView = view.findViewById(R.id.text_view_empty);
        resultValueView = view.findViewById(R.id.text_view_result_value);
        totalPointsView = view.findViewById(R.id.text_view_total_points_value);
        totalCreditsView = view.findViewById(R.id.text_view_total_credits_value);

        MaterialButton addSubjectButton = view.findViewById(R.id.button_add_subject);
        MaterialButton calculateButton = view.findViewById(R.id.button_calculate);

        view.findViewById(R.id.image_button_back).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, GRADES);
        ArrayAdapter<String> creditsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, CREDITS);

        gradeDropdown.setAdapter(gradeAdapter);
        creditsDropdown.setAdapter(creditsAdapter);

        gradeDropdown.setOnItemClickListener((adapterView, v, position, id) -> gradeInputLayout.setError(null));
        creditsDropdown.setOnItemClickListener((adapterView, v, position, id) -> creditsInputLayout.setError(null));
        gradeDropdown.setOnClickListener(v -> gradeDropdown.showDropDown());
        creditsDropdown.setOnClickListener(v -> creditsDropdown.showDropDown());
        gradeDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) gradeDropdown.showDropDown();
        });
        creditsDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) creditsDropdown.showDropDown();
        });

        subjectAdapter = new GpaSubjectAdapter(subjects, this::removeSubjectAt);

        subjectsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        subjectsRecyclerView.setAdapter(subjectAdapter);
        subjectsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        addSubjectButton.setOnClickListener(v -> addSubject());
        calculateButton.setOnClickListener(v -> calculateGpa());

        getParentFragmentManager().setFragmentResultListener("customInsets2", this, (requestKey, result) -> {
            int systemWindowInsetLeft = result.getInt("systemWindowInsetLeft");
            int systemWindowInsetTop = result.getInt("systemWindowInsetTop");
            int systemWindowInsetRight = result.getInt("systemWindowInsetRight");
            int systemWindowInsetBottom = result.getInt("systemWindowInsetBottom");
            float pixelDensity = getResources().getDisplayMetrics().density;

            header.setPaddingRelative(
                    systemWindowInsetLeft,
                    systemWindowInsetTop,
                    systemWindowInsetRight,
                    0
            );

            nestedScrollView.setPaddingRelative(
                    systemWindowInsetLeft,
                    0,
                    systemWindowInsetRight,
                    (int) (systemWindowInsetBottom + 20 * pixelDensity)
            );
        });

        updateSummary();
        updateEmptyState();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bundle bottomNavigationVisibility = new Bundle();
        bottomNavigationVisibility.putBoolean("isVisible", true);
        getParentFragmentManager().setFragmentResult("bottomNavigationVisibility", bottomNavigationVisibility);
    }

    private void addSubject() {
        gradeInputLayout.setError(null);
        creditsInputLayout.setError(null);

        String grade = gradeDropdown.getText() != null ? gradeDropdown.getText().toString().trim() : "";
        String creditsText = creditsDropdown.getText() != null ? creditsDropdown.getText().toString().trim() : "";
        String subjectName = subjectEditText.getText() != null ? subjectEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(grade)) {
            gradeInputLayout.setError(getString(R.string.gpa_error_select_grade));
            return;
        }

        if (TextUtils.isEmpty(creditsText)) {
            creditsInputLayout.setError(getString(R.string.gpa_error_select_credits));
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (NumberFormatException e) {
            creditsInputLayout.setError(getString(R.string.gpa_error_select_credits));
            return;
        }

        double gradePoints = getGradePoint(grade) * credits;
        subjects.add(new GpaSubject(subjectName, grade, credits, gradePoints));
        subjectAdapter.notifyItemInserted(subjects.size() - 1);
        subjectsRecyclerView.scrollToPosition(subjects.size() - 1);

        clearInputs();
        updateSummary();
        updateEmptyState();
    }

    private void removeSubjectAt(int position) {
        if (position < 0 || position >= subjects.size()) {
            return;
        }

        subjects.remove(position);
        subjectAdapter.notifyItemRemoved(position);
        updateSummary();
        updateEmptyState();
    }

    private void calculateGpa() {
        if (subjects.isEmpty()) {
            Toast.makeText(requireContext(), R.string.gpa_error_no_subjects, Toast.LENGTH_SHORT).show();
            return;
        }

        updateSummary();
        Toast.makeText(
                requireContext(),
                getString(R.string.gpa_result_label) + ": " + resultValueView.getText(),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateSummary() {
        double totalGradePoints = 0;
        int totalCredits = 0;

        for (GpaSubject subject : subjects) {
            totalGradePoints += subject.getGradePoints();
            totalCredits += subject.getCredits();
        }

        if (totalCredits == 0) {
            resultValueView.setText(gpaFormat.format(0));
        } else {
            resultValueView.setText(gpaFormat.format(totalGradePoints / totalCredits));
        }

        totalPointsView.setText(pointsFormat.format(totalGradePoints));
        totalCreditsView.setText(String.valueOf(totalCredits));
    }

    private void updateEmptyState() {
        if (subjects.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            subjectsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            subjectsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void clearInputs() {
        if (subjectEditText != null) {
            subjectEditText.setText(null);
        }

        subjectInputLayout.setError(null);
        gradeDropdown.setText("", false);
        creditsDropdown.setText("", false);
        gradeInputLayout.setError(null);
        creditsInputLayout.setError(null);
    }

    private int getGradePoint(String grade) {
        Integer point = GRADE_POINTS.get(grade);
        return point != null ? point : 0;
    }
}

