package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileFragment extends Fragment {

    // publish and uploading
    private static final String TAG = "ProfileFragment";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();


    private TextView tvProfileName;
    private TextView tvProfileBio;
    private CircleImageView profileImg;
    private TextView tvNumRecipes;
    private TextView tvNumFollowers;
    private TextView tvNumFollowing;
    private List<String> recipeList;
    private String profileImgPath;
    private String username;
    private String bio;
    private int numFollowers;
    private int numFollowing;


    public static profileFragment newInstance() {
        profileFragment fragment = new profileFragment();
        return fragment;
    }

    private void setupViews(View view) {
        tvProfileName = (TextView) view.findViewById(R.id.profileName);
        tvProfileBio = (TextView) view.findViewById(R.id.profileBio);
        tvNumRecipes = (TextView) view.findViewById(R.id.textViewRecipes);
        tvNumFollowers = (TextView) view.findViewById(R.id.textViewFollowers);
        tvNumFollowing = (TextView) view.findViewById(R.id.textViewFollowing);
        profileImg = (CircleImageView) view.findViewById(R.id.profileImg);

        // Removes Follow button
        ConstraintLayout profileTop = (ConstraintLayout) view.findViewById(R.id.constraintLayout1);

        Button followBtn = (Button) view.findViewById(R.id.follow);
        followBtn.setVisibility(View.GONE);
        profileTop.removeView(followBtn);
    }

    public void setupEditProfileBtn(View view) {
        //Sets up edit profile button
        //TO-DO: Deflate if not logged in user
        final TextView editProfile = (TextView) view.findViewById(R.id.editProfileButton);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    public void setupTopInfo(View view) {
        tvNumFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                intent.putExtra("ID", currentFirebaseUser.getUid());
                intent.putExtra("Followers", true);
                startActivity(intent);
            }
        });

        tvNumFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                intent.putExtra("ID", currentFirebaseUser.getUid());
                intent.putExtra("Followers", false);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIfUpdated();
    }

    private void checkIfUpdated() {
        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    if(!username.equals(user.getUsername())) {
                        username = user.getUsername();
                        tvProfileName.setText(username);
                    }
                    if(user.getNumRecipes() != recipeList.size()) {
                        Log.d(TAG, "Recipe size: " + Integer.toString(recipeList.size()));
                        Set<String> oldRecipes = new LinkedHashSet<String>(recipeList);
                        Set<String> newRecipes = new LinkedHashSet<String>(user.getRecipes());
                        newRecipes.removeAll(oldRecipes);
                        List<String> recipesToAdd = new ArrayList<String>(newRecipes);
                        loadRecipeList(recipesToAdd);
                    }
                    if(bio != null && !bio.equals(user.getBio())) {
                        bio = user.getBio();
                        tvProfileBio.setText(bio);
                    }
                    if(profileImgPath != null && !profileImgPath.equals(user.getProfilePhotoPath())) {
                        profileImgPath = user.getProfilePhotoPath();
                        if (profileImgPath != null && !profileImgPath.equals("")) {
                            Glide.with(getActivity())
                                    .using(new FirebaseImageLoader())
                                    .load(storageReference.child(profileImgPath))
                                    .into(profileImg);
                        }
                    }
                    if(user.getNumFollowers() != numFollowers) {
                        numFollowers = user.getNumFollowers();
                        tvNumFollowers.setText(Integer.toString(numFollowers));
                    }
                    if(user.getNumFollowing() != numFollowing) {
                        numFollowing = user.getNumFollowing();
                        tvNumFollowing.setText(Integer.toString(numFollowing));
                    }
                }
            }
        });
    }

    private void getData() {
        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    recipeList = user.getRecipes();
                    username = user.getUsername();
                    bio = user.getBio();
                    numFollowers = user.getNumFollowers();
                    numFollowing = user.getNumFollowing();
                    tvProfileName.setText(username);
                    tvNumRecipes.setText(Integer.toString(recipeList.size()));
                    tvProfileBio.setText(bio);

                    profileImgPath = user.getProfilePhotoPath();
                    if (profileImgPath != null && !profileImgPath.equals("")) {
                        Glide.with(getActivity())
                                .using(new FirebaseImageLoader())
                                .load(storageReference.child(profileImgPath))
                                .into(profileImg);
                    }

                    tvNumFollowers.setText(Integer.toString(numFollowers));
                    tvNumFollowing.setText(Integer.toString(numFollowing));
                    loadRecipeList(recipeList);

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_center_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        setupViews(view);
        setupEditProfileBtn(view);
        setupTopInfo(view);
        getData();
    }

    private void loadRecipeList(final List<String> rList) {
        final LinearLayout feed = (LinearLayout) getView().findViewById(R.id.profileRecipeFeed);

        for(int i = 0; i < rList.size(); i++) {
            final View a = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);

            DocumentReference docRef = db.collection("recipes").document(rList.get(i));
            final String t = rList.get(i);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
//                        Log.d(TAG, t);
//                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                        DocumentSnapshot document = task.getResult();
//                        Recipe r = document.toObject(Recipe.class);
                        CircleImageView userPic = (CircleImageView) a.findViewById(R.id.userPic);
                        TextView userName = (TextView) a.findViewById(R.id.username);
                        TextView recipeName = (TextView) a.findViewById(R.id.recipeName);
                        TextView recipeTime = (TextView) a.findViewById(R.id.recipeTime);
                        TextView recipeServings = (TextView) a.findViewById(R.id.recipeServings);
                        TextView recipeBio = (TextView) a.findViewById(R.id.recipeBio);
                        ImageView recipePic = (ImageView) a.findViewById(R.id.imageView);

                        userName.setText(username);
                        recipeName.setText(document.getString("title"));
                        recipeBio.setText(document.getString("description"));
                        recipeTime.setText(document.getString("time"));
                        recipeServings.setText(document.getString("servings"));
                        feed.addView(a);

                        //  TO-DO: Open Edit recipe
                        a.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ManageRecipe.class);
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

