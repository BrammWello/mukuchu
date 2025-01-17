package com.devbramm.mukuchusavings.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.devbramm.mukuchusavings.AddTransactionActivity;
import com.devbramm.mukuchusavings.HomePageActivity;
import com.devbramm.mukuchusavings.R;
import com.devbramm.mukuchusavings.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView tvUserName, tvDate;
    private LineChart lineChart;
    private FirebaseAuth firebaseAuth;
    private Button toAddTransaction;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lineChart = root.findViewById(R.id.lineChart);
        tvUserName = root.findViewById(R.id.tv_user_name_greeting);
        tvDate = root.findViewById(R.id.tv_date);
        toAddTransaction = root.findViewById(R.id.btn_to_add_transaction);
        setupUI();
        setupWeeklyChart();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupUI() {
        // Correct way: Access Firebase instance from the parent activity
        HomePageActivity activity = (HomePageActivity) getActivity();
        if (activity != null) {
            firebaseAuth = activity.getFirebaseAuth();  // Use the shared instance
        }

        // Check if Firebase Auth is properly initialized and user is signed in
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            String displayName = firebaseAuth.getCurrentUser().getDisplayName();

            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText("Hello, " + displayName + " 👋");
            } else {
                tvUserName.setText("Hello, User!");
            }
        } else {
            tvUserName.setText("Not Signed In");
        }

        // Display the current date in the desired format
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        tvDate.setText("Today is " + currentDate);

        //add transaction button
        toAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddTransactionActivity.class));
            }
        });
    }

    private void setupWeeklyChart() {
        ArrayList<Entry> entries = new ArrayList<>();

        // Populate data for weeks (X-axis from Week 1 to Week 52)
        for (int i = 1; i <= 52; i++) {
            float yValue = (float) (Math.random() * 500); // Random value for demonstration
            entries.add(new Entry(i, yValue));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Savings");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryDark));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart

        // Customize X-Axis for Week Labels
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // Display each week
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Week " + ((int) value);
            }
        });
    }
}