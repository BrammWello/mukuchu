package com.devbramm.mukuchusavings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText edtTransactionDate, edtTransactionTime;
    private Calendar calendar;
    private MaterialAutoCompleteTextView autoCompleteTextView;
    private ImageView ivBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        edtTransactionDate = findViewById(R.id.et_transaction_date);
        edtTransactionTime = findViewById(R.id.et_transaction_time);
        ivBackBtn = findViewById(R.id.iv_back_btn);

        calendar = Calendar.getInstance();

        setupUI();
    }

    private void setupUI() {
        //Back btn
        ivBackBtn.setOnClickListener(view -> finish());

        //transaction type spinner
        //data for the dropdown
        String[] options = {"Deposit", "Withdrawal"};

        // Setting up the adapter for the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                options
        );
        autoCompleteTextView.setAdapter(adapter);

        // Trigger DatePicker when clicking the input field
        edtTransactionDate.setOnClickListener(v -> showDatePickerDialog());

        // Open TimePicker when the input field is clicked
        edtTransactionTime.setOnClickListener(v -> showTimePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Format the date and display it in the TextInputEditText
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
                    edtTransactionDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    // Format the time and display it in the TextInputEditText
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    edtTransactionTime.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // false for 12-hour format, true for 24-hour format
        );

        timePickerDialog.show();
    }
}