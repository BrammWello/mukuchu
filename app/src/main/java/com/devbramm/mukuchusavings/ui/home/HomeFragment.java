package com.devbramm.mukuchusavings.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devbramm.mukuchusavings.AddTransactionActivity;
import com.devbramm.mukuchusavings.HomePageActivity;
import com.devbramm.mukuchusavings.R;
import com.devbramm.mukuchusavings.adapters.RecentTransactionsAdapter;
import com.devbramm.mukuchusavings.databinding.FragmentHomeBinding;
import com.devbramm.mukuchusavings.models.TransactionRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView tvUserName, tvDate, tvCurrentSavingsAmount, tvGoalPercentage, tvRemainingWeeks;
    private LineChart lineChart;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private Button toAddTransaction;
    private RecyclerView rvRecentTransactions;
    private RecentTransactionsAdapter adapter;
    private List<TransactionRecord> transactionList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lineChart = root.findViewById(R.id.lineChart);
        tvUserName = root.findViewById(R.id.tv_user_name_greeting);
        tvDate = root.findViewById(R.id.tv_date);
        tvCurrentSavingsAmount = root.findViewById(R.id.tv_current_savings_amount);
        toAddTransaction = root.findViewById(R.id.btn_to_add_transaction);
        tvGoalPercentage = root.findViewById(R.id.tv_goal_percentage);
        tvRemainingWeeks = root.findViewById(R.id.tv_remaining_weeks);
        rvRecentTransactions = root.findViewById(R.id.rv_recent_transactions);
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
            db = activity.getFirebaseFirestore();
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
            // TODO make user go to sign in screen
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

        //receycler view setup
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionList = new ArrayList<>();
        adapter = new RecentTransactionsAdapter(transactionList);
        rvRecentTransactions.setAdapter(adapter);

        db.collection("transactions")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("userTransactions")
                .orderBy("transactionDate", Query.Direction.DESCENDING) // Latest transactions first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transactionList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            TransactionRecord transaction = document.toObject(TransactionRecord.class);
                            if (transaction != null) {
                                transactionList.add(transaction);
                                Log.d("Firestore", "Transaction: " + transaction.getTransactionDate() +
                                        " - Ksh " + transaction.getTransactionAmount());
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("Firestore", "Transactions loaded successfully.");
                    } else {
                        Log.e("Firestore", "Error fetching transactions: ", task.getException());
                        Toast.makeText(getActivity(), "Failed to load transactions.", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void setupWeeklyChart() {
        Log.d("ChartDebug", "Fetching data from Firestore...");

        db.collection("transactions")
                .document(firebaseAuth.getCurrentUser().getUid()) // Get current user's document
                .collection("userTransactions") // Access their transactions
                .orderBy("transactionDate", Query.Direction.ASCENDING) // Order transactions by date
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Entry> entries = new ArrayList<>();
                            Map<Integer, Float> weeklySavings = new HashMap<>();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH);

                            Log.d("ChartDebug", "Transaction fetch successful. Processing data...");

                            float currentSavingsAmount = 0.0F;

                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Log.d("ChartDebug", "Processing transaction: " + document.getId());

                                    // Extract transaction amount
                                    Double amountDouble = document.getDouble("transactionAmount");
                                    if (amountDouble == null) {
                                        Log.e("ChartDebug", "Amount field missing in document: " + document.getId());
                                        continue;
                                    }
                                    float amount = amountDouble.floatValue();
                                    currentSavingsAmount = currentSavingsAmount + amount;
                                    Log.d("ChartDebug", "Amount: " + amount);

                                    // Extract and parse transactionDate (stored as string)
                                    String dateString = document.getString("transactionDate");
                                    if (dateString == null) {
                                        Log.e("ChartDebug", "Transaction Date field missing in document: " + document.getId());
                                        continue;
                                    }

                                    Date date;
                                    try {
                                        date = dateFormat.parse(dateString);
                                    } catch (Exception e) {
                                        Log.e("ChartDebug", "Error parsing date: " + dateString + " -> " + e.getMessage());
                                        continue;
                                    }
                                    Log.d("ChartDebug", "Parsed Date: " + date.toString());

                                    // Get week number from parsed date
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
                                    Log.d("ChartDebug", "Week of Year: " + weekOfYear);

                                    // Accumulate savings per week
                                    weeklySavings.put(weekOfYear,
                                            weeklySavings.getOrDefault(weekOfYear, 0f) + amount);
                                }
                            }

                            // Convert map to chart entries
                            for (Map.Entry<Integer, Float> entry : weeklySavings.entrySet()) {
                                entries.add(new Entry(entry.getKey(), entry.getValue()));
                                Log.d("ChartDebug", "Chart Entry -> Week: " + entry.getKey() + ", Savings: " + entry.getValue());
                            }

                            // Sort entries by week number
                            Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

                            // Check if entries are populated
                            if (entries.isEmpty()) {
                                Log.w("ChartDebug", "No data available for the chart.");
                                return;
                            }

                            // Set up the chart
                            LineDataSet dataSet = new LineDataSet(entries, "Weekly Savings");
                            dataSet.setColor(getResources().getColor(R.color.colorPrimary));
                            dataSet.setValueTextColor(getResources().getColor(R.color.black));
                            dataSet.setLineWidth(2f);
                            dataSet.setCircleRadius(4f);
                            dataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryDark));

                            LineData lineData = new LineData(dataSet);
                            lineChart.setData(lineData);
                            lineChart.invalidate(); // Refresh chart

                            Log.d("ChartDebug", "Chart successfully updated.");

                            // Customize X-Axis
                            XAxis xAxis = lineChart.getXAxis();
                            xAxis.setGranularity(1f);
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    return "Week " + ((int) value);
                                }
                            });

                            //Set up other textviews
                            tvCurrentSavingsAmount.setText(String.valueOf(currentSavingsAmount));
                            tvGoalPercentage.setText("You are " + Math.round((currentSavingsAmount/137800) * 100) + " percent to your goal");

                            Calendar calendar = Calendar.getInstance();
                            int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                            int totalWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR);

                            int remainingWeeks = totalWeeks - currentWeek;

                            tvRemainingWeeks.setText(remainingWeeks + " weeks remaining to save 😄");

                        } else {
                            Log.e("ChartDebug", "Failed to fetch transactions: " + task.getException());
                            Toast.makeText(getActivity(),
                                    "Failed to load data: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}