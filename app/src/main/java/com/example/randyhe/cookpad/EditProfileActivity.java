package com.example.randyhe.cookpad;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    private User user;
    private TextView tvEditProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        setupViews();
        setUpSaveChanges();
        loadData();
    }


    private void setupViews() {
        etName = findViewById(R.id.editName);
        etUsername = findViewById(R.id.editUsername);
        etBio = findViewById(R.id.editBio);
        etEmail = findViewById(R.id.editEmail);
        confirmBtn = findViewById(R.id.confirmChanges);

        tvEditProfilePic = findViewById(R.id.changeProfilePhoto);
        tvEditProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditProfileActivity.this, "edit this", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpSaveChanges() {
        View.OnClickListener saveChangesListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeChanges(currentFirebaseUser);
                startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
            }
        };
        confirmBtn.setOnClickListener(saveChangesListener);
    }

    private void storeChanges(FirebaseUser user) {
        DocumentReference docRef = db.collection("users").document(user.getUid());
//        Stores updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString());
        updates.put("username", etUsername.getText().toString());
        updates.put("email", etEmail.getText().toString());
        updates.put("bio", etBio.getText().toString());

        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error updating document", e);
            }
        });
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
