package com.example.randyhe.cookpad;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.Intent;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mcast on 1/28/2018.
 */

public class ProfileActivity extends AppCompatActivity {

    // publish and uploading
    private static final String TAG = "ProfileActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();

    private TextView tvProfileName;
    private TextView tvProfileBio;
    private CircleImageView profileImg;
    private TextView tvNumRecipes;
    private TextView tvNumFollowers;
    private TextView tvNumFollowing;
    private List<String> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_center_profile);

        ConstraintLayout profileTop = (ConstraintLayout) findViewById(R.id.constraintLayout1);


        TextView tvEditProfile = findViewById(R.id.editProfileButton);
        tvEditProfile.setVisibility(View.GONE);
        profileTop.removeView(tvEditProfile);

//        String user = getIntent().getExtras().getString("ID");
        String user = "cC2zis0WonNTANJjODvSINuSpZr1";
        setupViews();
        getData(user);
        setupTopInfo();

    }

    private void setupViews() {
        tvProfileName = (TextView) findViewById(R.id.profileName);
        tvProfileBio = (TextView) findViewById(R.id.profileBio);
        tvNumRecipes = (TextView) findViewById(R.id.textViewRecipes);
        tvNumFollowers = (TextView) findViewById(R.id.textViewFollowers);
        tvNumFollowing = (TextView) findViewById(R.id.textViewFollowing);
        profileImg = (CircleImageView) findViewById(R.id.profileImg);
    }

    private void setupTopInfo() {
        tvNumFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("ID", currentFirebaseUser.getUid());
                startActivity(intent);
            }
        });

        tvNumFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("ID", currentFirebaseUser.getUid());
                startActivity(intent);
            }
        });
    }

    private void getData(String uID) {
        DocumentReference docRef = db.collection("users").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    List<String> recipes = user.getRecipes();
                    tvProfileName.setText(user.getUsername());
                    tvNumRecipes.setText(Integer.toString(recipes.size()));
                    tvProfileBio.setText(user.getBio());
                    loadRecipeList(recipes, user.getUsername());

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void loadRecipeList(final List<String> rList, final String user) {
        final LinearLayout feed = (LinearLayout) findViewById(R.id.profileRecipeFeed);

        for(int i = 0; i < rList.size(); i++) {
            final View a = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);

            DocumentReference docRef = db.collection("recipes").document(rList.get(i));
            final String t = rList.get(i);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        CircleImageView userPic = (CircleImageView) a.findViewById(R.id.userPic);
                        TextView username = (TextView) a.findViewById(R.id.username);
                        TextView recipeName = (TextView) a.findViewById(R.id.recipeName);
                        TextView recipeTime = (TextView) a.findViewById(R.id.recipeTime);
                        TextView recipeServings = (TextView) a.findViewById(R.id.recipeServings);
                        TextView recipeBio = (TextView) a.findViewById(R.id.recipeBio);
                        ImageView recipePic = (ImageView) a.findViewById(R.id.imageView);

                        if (document.getString("mainPhotoStoragePath") != null) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(document.getString("mainPhotoStoragePath"));

                            Glide.with(ProfileActivity.this /* context */)
                                    .load(storageReference)
                                    .into(recipePic);
                        }

                        userPic.setImageResource(R.drawable.kermit_cooking);
                        username.setText(user);
                        recipeName.setText(document.getString("title"));
                        recipeBio.setText(document.getString("description"));
                        feed.addView(a);

                        //  TO-DO: Open Edit recipe
                        recipeName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ProfileActivity.this, ManageRecipe.class);
                                intent.putExtra("EDIT", true);
                                intent.putExtra("ID", t);
                                startActivity(intent);
                            }
                        });
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
    }

}
