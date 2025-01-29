package com.devbramm.mukuchusavings;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.devbramm.mukuchusavings.models.TransactionRecord;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText edtTransactionDate, edtTransactionTime;
    private TextInputLayout tilRecordDate, tilRecordTime, tilRecordAmount, tilRecordDescription;
    private Calendar calendar;
    private MaterialAutoCompleteTextView autoCompleteTextView;
    private ImageView ivBackBtn;
    private Button btnSave;
    private String recordType, recordDate, recordTime, recordAmount, recordDescription = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Dialog loadingDialog;

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
        btnSave = findViewById(R.id.btn_save);
        tilRecordDate = findViewById(R.id.til_transaction_date);
        tilRecordTime = findViewById(R.id.til_transaction_time);
        tilRecordAmount = findViewById(R.id.til_transaction_amount);
        tilRecordDescription = findViewById(R.id.til_transaction_description);

        calendar = Calendar.getInstance();

        mAuth = FirebaseAuth.getInstance();

        setupUI();
    }

    private void setupUI() {
        //Back btn
        ivBackBtn.setOnClickListener(view -> finish());

        //Back btn
        btnSave.setOnClickListener(view -> {
            saveRecord();
        });

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

    private void saveRecord() {
        recordType = autoCompleteTextView.getText().toString();
        recordAmount = tilRecordAmount.getEditText().getText().toString().trim();
        recordDescription = tilRecordDescription.getEditText().getText().toString().trim();

        if(Objects.equals(recordType, "") || Objects.equals(recordDate, "") || Objects.equals(recordTime, "") || Objects.equals(recordAmount, "") || Objects.equals(recordDescription, ""))
        {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else {
            showLoadingDialog(R.raw.money_loading);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            TransactionRecord transactionRecord = new TransactionRecord(currentUser.getDisplayName(), currentUser.getUid(), "", recordType, recordDate, recordTime, Float.parseFloat(recordAmount), recordDescription);
            db.collection("transactions")
                    .document(currentUser.getUid())  // User's document
                    .collection("userTransactions")  // Subcollection for transactions
                    .add(transactionRecord)  // Adds a new transaction (auto-generates a document ID)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            hideLoadingDialog();  // Hide the current loading dialog

                            // Show success animation
                            showLoadingDialog(R.raw.success);

                            // Delay for 2 seconds before navigating to HomeActivity
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideLoadingDialog(); // Hide the success animation dialog
                                    startActivity(new Intent(AddTransactionActivity.this, HomePageActivity.class));
                                    finish();  // Close the current activity
                                }
                            }, 2000); // 2 seconds delay before navigating
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddTransactionActivity.this, "Sorry. Record failed to save. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
                    recordDate = dateFormat.format(calendar.getTime());
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
                    recordTime = timeFormat.format(calendar.getTime());
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // false for 12-hour format, true for 24-hour format
        );

        timePickerDialog.show();
    }

    private void showLoadingDialog(int lottieAnimationResId) {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);

        // Set the Lottie animation dynamically
        LottieAnimationView lottieAnimationView = loadingDialog.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation(lottieAnimationResId);
        lottieAnimationView.playAnimation();
        lottieAnimationView.loop(true);

        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}