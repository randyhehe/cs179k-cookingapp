package com.example.randyhe.cookpad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView tvUID;
    private Button btnSignout;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUID = findViewById(R.id.user_uid);
        btnSignout = findViewById(R.id.sign_out_btn);
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { // not signed in
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            tvUID.setText(currentUser.getUid());
        }
    }

}
