package com.devbramm.mukuchusavings;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Dialog loadingDialog;
    private MaterialButton signInBtn;
    private SpannableString spannableString;
    private TextView textView;
    private TextInputLayout tilEmail, tilPass;
    private boolean isSigningIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        signInBtn = findViewById(R.id.btn_sign_in);
        tilEmail = findViewById(R.id.til_email);
        tilPass = findViewById(R.id.til_pass);
        textView = findViewById(R.id.tv_sign_in);

        spannableString = new SpannableString("Don't have an account? Sign Up");

        setupUI();
    }

    private void setupUI() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check Email validation
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(tilEmail.getEditText().getText().toString()).matches()) {
                    tilEmail.setError(null);
                    tilEmail.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    tilEmail.setEndIconDrawable(R.drawable.round_check_24);
                } else {
                    tilEmail.setEndIconMode(TextInputLayout.END_ICON_NONE);
                }

                // Check Password validation
                if (tilPass.getEditText().getText().toString().matches("^(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$")) {
                    tilPass.setError(null);
                    tilPass.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    tilPass.setEndIconDrawable(R.drawable.round_check_24);
                } else {
                    tilPass.setEndIconMode(TextInputLayout.END_ICON_NONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Attach TextWatcher to all fields
        tilEmail.getEditText().addTextChangedListener(textWatcher);
        tilPass.getEditText().addTextChangedListener(textWatcher);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = tilEmail.getEditText().getText().toString().trim();
                String password = tilPass.getEditText().getText().toString().trim();

                boolean isValid = true;

                // Validate email
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.setError("Kindly provide a valid email address");
                    isValid = false;
                }

                // Validate password (at least 8 characters, 1 number, 1 special character)
                if (!password.matches("^(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$")) {
                    tilPass.setError("Password must have at least 8 characters, 1 number, and 1 special character");
                    isValid = false;
                }

                // Proceed if all inputs are valid
                if (isValid && !isSigningIn) {
                    isSigningIn = true;
                    showLoadingDialog(R.raw.money_loading);
                    signInUser();
                }
            }
        });

        // Make "Sign Up" clickable
        ClickableSpan signUpClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navigate to the Sign In page
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // Set clickable text color
                ds.setUnderlineText(false); // Remove underline if desired
            }
        };

        // Apply the ClickableSpan to "Sign In" (position 16 to 23)
        spannableString.setSpan(signUpClick, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to the TextView
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance()); // Enable clickable behavior

        textView.setOnClickListener(view -> {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            hideLoadingDialog();
            finish();
        });
    }

    private void signInUser() {
        String email = tilEmail.getEditText().getText().toString().trim();
        String password = tilPass.getEditText().getText().toString().trim();

        //sign in user now
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            hideLoadingDialog();  // Hide the current loading dialog

                            // Show success animation
                            showLoadingDialog(R.raw.success);

                            // Delay for 2 seconds before navigating to HomeActivity
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideLoadingDialog(); // Hide the success animation dialog
                                    startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
                                    finish();  // Close the current activity
                                }
                            }, 2000); // 2 seconds delay before navigating
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            isSigningIn = false;
                            hideLoadingDialog();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // go to main menu
            startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
            finish();
        }
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