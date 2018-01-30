package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvSignup;
    private TextView tvSignupError;
    private Button btnSignin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
        setupSignupLink();
        setupSigninBtn();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) signin();
    }

    private void setupViews() {
        etEmail = findViewById(R.id.input_email);
        etPassword = findViewById(R.id.input_password);
        tvSignup = findViewById(R.id.link_signup);
        tvSignupError = findViewById(R.id.text_login_error);
        btnSignin = findViewById(R.id.btn_login);
    }

    private void setupSignupLink() {
        View.OnClickListener signupListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        };
        tvSignup.setOnClickListener(signupListener);
    }

    private void setupSigninBtn() {
        final OnCompleteListener<AuthResult> completeListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    signin();
                } else {
                    String errorMessage = reformatSigninErrors(task.getException().getMessage());
                    tvSignupError.setText(errorMessage);
                    tvSignupError.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();
            }
        };

        View.OnClickListener signinListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSignupError.setVisibility(View.GONE);
                if (validSigninPrecheck()) {
                    progressDialog = ProgressDialog.show(LoginActivity.this, null, "Authenticating...");
                    auth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                            .addOnCompleteListener(completeListener);
                }
            }
        };


        btnSignin.setOnClickListener(signinListener);
    }

    private void signin() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
    }

    // Reformat the default sign in errors from Firebase Firestore
    private String reformatSigninErrors(String errorMessage) {
        final String invalidIdentifier = "There is no user record corresponding to this identifier. The user may have been deleted.";
        final String invalidPassword = "The password is invalid or the user does not have a password.";

        if (errorMessage.equals(invalidIdentifier)) {
            return "Account with the email does not exist.";
        } else if (errorMessage.equals(invalidPassword)) {
            return "Password is invalid.";
        } else {
            return errorMessage;
        }
    }

    // Make sure fields are not empty or null to prevent app crash
    private boolean validSigninPrecheck() {
        if (empty(etEmail)) {
            tvSignupError.setText(getResources().getString(R.string.login_empty_email));
            tvSignupError.setVisibility(View.VISIBLE);
            return false;
        } else if (empty(etPassword)) {
            tvSignupError.setText(getResources().getString(R.string.login_empty_password));
            tvSignupError.setVisibility(View.VISIBLE);
            return false;
        } else {
            return true;
        }
    }

    private boolean empty(EditText et) {
        return et.getText().toString().length() == 0;
    }
}
