package com.example.randyhe.cookpad;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mcast on 1/28/2018.
 */

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();

    private EditText etName;
    private EditText etUsername;
    private EditText etBio;
    private EditText etEmail;
    private TextView confirmBtn;

    private String name;
    private String username;
    private String bio;
    private String email;
    private Map<String, Object> data;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        setupViews();
//        Toast.makeText(this, "" + currentFirebaseUser.getUid(), Toast.LENGTH_LONG).show();
        loadData();
    }


    private void setupViews() {
        etName = findViewById(R.id.editName);
        etUsername = findViewById(R.id.editUsername);
        etBio = findViewById(R.id.editBio);
        etEmail = findViewById(R.id.editEmail);
        confirmBtn = findViewById(R.id.confirmChanges);
    }

    private void loadData() {
        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    user = document.toObject(User.class);
                    etName.setText(user.getName());
                    etUsername.setText(user.getUsername());
                    etBio.setText(user.getBio());
                    etEmail.setText(user.getEmail());

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

}
