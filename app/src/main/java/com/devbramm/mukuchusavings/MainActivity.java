package com.devbramm.mukuchusavings;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
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
import com.google.firebase.auth.UserProfileChangeRequest;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Dialog loadingDialog;
    private MaterialButton signUpBtn;
    private SpannableString spannableString;
    private TextView textView;
    private TextInputLayout tilFullName, tilEmail, tilPass;
    private boolean isSigningUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        signUpBtn = findViewById(R.id.btn_sign_up);
        textView = findViewById(R.id.tv_sign_in);
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilPass = findViewById(R.id.til_pass);

        spannableString = new SpannableString("Already have an account? Sign In");

        setupUI();
    }

    private void setupUI() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check Full Name validation
                if (tilFullName.getEditText().getText().toString().matches("^(\\S+\\s+\\S+.*)$")) {
                    tilFullName.setError(null);
                    tilFullName.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    tilFullName.setEndIconDrawable(R.drawable.round_check_24); // Add a custom check icon
                } else {
                    tilFullName.setEndIconMode(TextInputLayout.END_ICON_NONE);
                }

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
        tilFullName.getEditText().addTextChangedListener(textWatcher);
        tilEmail.getEditText().addTextChangedListener(textWatcher);
        tilPass.getEditText().addTextChangedListener(textWatcher);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = tilFullName.getEditText().getText().toString().trim();
                String email = tilEmail.getEditText().getText().toString().trim();
                String password = tilPass.getEditText().getText().toString().trim();

                boolean isValid = true;

                // Validate full name (at least two names)
                if (!fullName.matches("^(\\S+\\s+\\S+.*)$")) {
                    tilFullName.setError("Kindly provide at least two names");
                    isValid = false;
                }

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
                if (isValid && !isSigningUp) {
                    isSigningUp = true;
                    showLoadingDialog(R.raw.money_loading);
                    signUpUser();
                }
            }
        });

        // Make "Sign In" clickable
        ClickableSpan signInClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navigate to the Sign In page
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // Set clickable text color
                ds.setUnderlineText(false); // Remove underline if desired
            }
        };

        // Apply the ClickableSpan to "Sign In" (position 16 to 23)
        spannableString.setSpan(signInClick, 25, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to the TextView
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance()); // Enable clickable behavior

        textView.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            hideLoadingDialog();
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // go to main menu
            startActivity(new Intent(MainActivity.this, HomePageActivity.class));
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

    private void signUpUser() {
        String fullName = tilFullName.getEditText().getText().toString().trim();
        String email = tilEmail.getEditText().getText().toString().trim();
        String password = tilPass.getEditText().getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            // Update the user profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            assert user != null;
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                hideLoadingDialog();  // Hide the current loading dialog

                                                // Show success animation
                                                showLoadingDialog(R.raw.success);

                                                // Delay for 2 seconds before navigating to HomeActivity
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideLoadingDialog(); // Hide the success animation dialog
                                                        startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                                                        finish();  // Close the current activity
                                                    }
                                                }, 2000); // 2 seconds delay before navigating

                                            } else {
                                                Log.d(TAG, "User profile update failed.");
                                                isSigningUp = false;
                                                hideLoadingDialog();
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed." + task.getException().getMessage().toString(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            isSigningUp = false;
                            hideLoadingDialog();
                        }
                    }
                });
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}