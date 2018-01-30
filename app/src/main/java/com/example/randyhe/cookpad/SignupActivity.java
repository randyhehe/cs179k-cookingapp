package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvError;
    private Button btnSignup;
    private TextView tvSignin;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        FirebaseApp.initializeApp(this);

        setupViews();
        setupSigninLink();
        setupSignupBtn();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) signin();
    }

    private void signin() {
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
    }

    private void setupViews() {
        etUsername = findViewById(R.id.input_username);
        etEmail = findViewById(R.id.input_email);
        etPassword = findViewById(R.id.input_password);
        tvError = findViewById(R.id.signup_error);
        btnSignup = findViewById(R.id.btn_signup);
        tvSignin = findViewById(R.id.link_signin);
        etConfirmPassword = findViewById(R.id.input_confirmpassword);
    }

    private void setupSigninLink() {
        View.OnClickListener  signinListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        };
        tvSignin.setOnClickListener(signinListener);
    }

    private void setupSignupBtn() {
        final OnCompleteListener<AuthResult> signupComplete = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    storeUser(user); // add entry with additional information into database
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                } else {
                    tvError.setText(task.getException().getMessage());
                    tvError.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();
            }
        };

        View.OnClickListener signupListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvError.setVisibility(View.GONE);
                if (validSignupPrecheck()) {
                    progressDialog = ProgressDialog.show(SignupActivity.this, null, "Creating Account...");
                    auth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                            .addOnCompleteListener(SignupActivity.this, signupComplete);
                }
            }
        };
        btnSignup.setOnClickListener(signupListener);
    }

    /*
     * Checks validity of username field and makes sure both password fields match.
     * Ensures email and password is not empty before being further verified by Firebase servers (or crash)
     */
    private boolean validSignupPrecheck() {
        if (!validUsername(etUsername)) {
            tvError.setText(getResources().getString(R.string.signup_invalid_username));
            tvError.setVisibility(View.VISIBLE);
            return false;
        } else if (empty(etEmail)) {
            tvError.setText(getResources().getString(R.string.signup_empty_email));
            tvError.setVisibility(View.VISIBLE);
            return false;
        } else if (empty(etPassword)) {
            tvError.setText(getResources().getString(R.string.signup_empty_password));
            tvError.setVisibility(View.VISIBLE);
            return false;
        } else if (empty(etConfirmPassword)
                || !etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            tvError.setText(getResources().getString(R.string.signup_invalid_confirmpassword));
            tvError.setVisibility(View.VISIBLE);
            return false;
        } else {
            return true;
        }
    }

    private boolean empty(EditText et) {
        return et.getText().toString().length() == 0;
    }

    private boolean validUsername(EditText et) {
        String username = et.getText().toString();
        if (username.length() < 6) return false;

        final String usernameRegex = "^[a-zA-Z0-9]*$";
        Pattern p = Pattern.compile(usernameRegex);
        Matcher m = p.matcher(username);
        return m.find();
    }

    /* Use UID as the unique identifier for the database and store username.
     *  Don't need to store default email and password because these fields are managed by Firebase Auth.
     */
    private void storeUser(FirebaseUser user) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", etUsername.getText().toString());

        db.collection("users").document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}
