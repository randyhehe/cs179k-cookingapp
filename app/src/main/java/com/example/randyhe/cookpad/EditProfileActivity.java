package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mcast on 1/28/2018.
 */

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();

    private EditText etName;
    private EditText etUsername;
    private EditText etBio;
    private Button confirmBtn;
    private User user;
    private TextView tvEditProfilePic;
    private CircleImageView civProfile;
    private ProgressDialog progressDialog;

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
        confirmBtn = findViewById(R.id.confirmChanges);

        civProfile = findViewById(R.id.editProfilePhoto);
        tvEditProfilePic = findViewById(R.id.changeProfilePhoto);
        tvEditProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // edit the image.
                PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult pickResult) {
                        if (pickResult.getError() == null) {
                            civProfile.setImageURI(pickResult.getUri());
                            civProfile.setTag(pickResult.getUri());
                        } else {
                            Toast.makeText(getApplicationContext(), pickResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show(getSupportFragmentManager());
            }
        });
    }

    private void setUpSaveChanges() {
        View.OnClickListener saveChangesListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeChanges(currentFirebaseUser);
            }
        };
        confirmBtn.setOnClickListener(saveChangesListener);
    }

    private void storeChanges(FirebaseUser user) {
        progressDialog = ProgressDialog.show(EditProfileActivity.this, null, "Editing Profile...");


        final DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final User user = documentSnapshot.toObject(User.class);

                // Stores updates
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", etName.getText().toString());
                updates.put("username", etUsername.getText().toString());
                updates.put("bio", etBio.getText().toString());

                final String picturePath  = "images/" + UUID.randomUUID().toString();
                if (civProfile.getTag() != null && civProfile.getTag() instanceof Uri) {
                    updates.put("profilePhotoPath", picturePath);
                }

                docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");

                        if (civProfile.getTag() != null && civProfile.getTag() instanceof Uri) {
                            // also add this into firebase storage
                            StorageReference ref = storageReference.child(picturePath);
                            ref.putFile((Uri) civProfile.getTag()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (user.getProfilePhotoPath() != null && !user.getProfilePhotoPath().equals("") && !user.getProfilePhotoPath().equals(picturePath)) {
                                        StorageReference ref = storageReference.child(user.getProfilePhotoPath());
                                        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Successfully edited profile!", Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Successfully edited profile!", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Successfully edited profile!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

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

                    if (user.getProfilePhotoPath() == null) {
                        // default image
                    } else {
                        Glide.with(EditProfileActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReference.child(user.getProfilePhotoPath()))
                                .into(civProfile);
                        // do i need to set the url tag here?
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}
