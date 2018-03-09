package com.example.randyhe.cookpad;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.Intent;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mcast on 1/28/2018.
 */

public class ProfileActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // publish and uploading
    private static final String TAG = "ProfileActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int adapterCounter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


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
    private String user;

    private TextView followButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_center_profile);

        user = getIntent().getExtras().getString("ID");
        setupViews();
        setupBtn(user, currentFirebaseUser.getUid());

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(user);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getData(user);
            }
        });

        setupTopInfo(user);
    }

    @Override
    public void onRefresh() {
        getData(user);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData(user);
    }

    private void setupBtn(final String profileUID, final String currentUID) {
        // Removes edit profile button from layout, leaving just the follow button
        ConstraintLayout profileTop = (ConstraintLayout) findViewById(R.id.constraintLayout1);

        TextView tvEditProfile = findViewById(R.id.editProfileButton);
        tvEditProfile.setVisibility(View.GONE);
        profileTop.removeView(tvEditProfile);

        if(profileUID.equals(currentUID)) {
            followButton.setVisibility(View.GONE);
            return;
        }

        // Sets up follow button
        DocumentReference docRef = db.collection("users").document(currentUID);
        final OnCompleteListener<DocumentSnapshot> isFollowing = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    if(docData.get("following") == null) {
                        followButton.setText("Follow");
                        setupFollowBtn(profileUID, currentUID);
                    }
                    else {
                        final Map<String, Boolean> followersList = (HashMap<String, Boolean>) docData.get("following");

                        if (followersList.containsKey(profileUID)) {
                            followButton.setText("Following");
                            setupUnfollowBtn(profileUID, currentUID);
                        } else {
                            followButton.setText("Follow");
                            setupFollowBtn(profileUID, currentUID);
                        }
                    }
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        docRef.get().addOnCompleteListener(isFollowing);
    }

    private void setupFollowBtn(final String profileUID, final String currentUID) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followAction(profileUID, currentUID);
                followButton.setText("Following");
                setupUnfollowBtn(profileUID, currentUID);
            }
        });
    }

    private void setupUnfollowBtn(final String profileUID, final String currentUID) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfollowAction(profileUID, currentUID);
                followButton.setText("Follow");
                setupFollowBtn(profileUID, currentUID);
            }
        });
    }

    private void setupViews() {
        tvProfileName = (TextView) findViewById(R.id.profileName);
        tvProfileBio = (TextView) findViewById(R.id.profileBio);
        tvNumRecipes = (TextView) findViewById(R.id.textViewRecipes);
        tvNumFollowers = (TextView) findViewById(R.id.textViewFollowers);
        tvNumFollowing = (TextView) findViewById(R.id.textViewFollowing);
        profileImg = (CircleImageView) findViewById(R.id.profileImg);
        followButton = (TextView) findViewById(R.id.follow);
    }

    private void setupTopInfo(final String profileUID) {
        tvNumFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("ID", profileUID);
                intent.putExtra("Followers", true);
                startActivity(intent);
            }
        });

        tvNumFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("ID", profileUID);
                intent.putExtra("Followers", false);
                startActivity(intent);
            }
        });
    }

    private void getData(String uID) {
        mSwipeRefreshLayout.setRefreshing(true);
        DocumentReference docRef = db.collection("users").document(uID);
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
                        Glide.with(ProfileActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReference.child(profileImgPath))
                                .into(profileImg);
                    }

                    tvNumFollowers.setText(Integer.toString(numFollowers));
                    tvNumFollowing.setText(Integer.toString(numFollowing));
                    if(recipeList != null) {
                        loadRecipeList(recipeList);
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void loadRecipeList(final List<String> rList) {
        if (rList.size() < 1) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(ProfileActivity.this); // maybe change to view
        mRecyclerView.setLayoutManager(mLayoutManager);
        final List<RecipeCompactObject> recipeCompactObjectList = new ArrayList<>();

        adapterCounter = rList.size();
        for (int i = 0; i < rList.size(); i++) {
            final String recipeId = rList.get(i);
            DocumentReference recipeDocumentReference = db.collection("recipes").document(rList.get(i));
            recipeDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {
                        float recipeAvgRating = 0;
                        if(documentSnapshot.getString("total") != null && documentSnapshot.getString("total") != "") {
                            recipeAvgRating = Float.parseFloat(documentSnapshot.getString("total")) / Integer.parseInt(documentSnapshot.getString("number"));
                        }

                        String recipeName = documentSnapshot.getString("title");
                        String recipeTime = documentSnapshot.getString("time");
                        String recipeServings = documentSnapshot.getString("servings");
                        String recipeDescription = documentSnapshot.getString("description");
                        String recipeMainPhotoPath = documentSnapshot.getString("mainPhotoStoragePath");
                        String recipePublisher = username;
                        String recipePublisherPhotoPath = profileImgPath;
                        long comparatorValue = documentSnapshot.getLong("timeCreated");
                        recipeCompactObjectList.add(new RecipeCompactObject(recipeId, recipeName, recipeTime, recipeServings, recipeDescription, recipeMainPhotoPath, recipePublisher, recipePublisherPhotoPath, recipeAvgRating, comparatorValue));

                        if (--adapterCounter == 0) {
                            Collections.sort(recipeCompactObjectList, new Comparator<RecipeCompactObject>() {
                                @Override
                                public int compare(RecipeCompactObject a, RecipeCompactObject b) {
                                    if (a.comparatorValue > b.comparatorValue) return -1;
                                    else if (a.comparatorValue < b.comparatorValue) return 1;
                                    else return 0;
                                }
                            });
                            mAdapter = new RecipeCompactAdapter(recipeCompactObjectList, ProfileActivity.this, true);
                            mRecyclerView.setAdapter(mAdapter);
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
            });
        }
    }

    private void followAction(final String profileUID, final String currentUID) {
        final DocumentReference currUserDoc = db.collection("users").document(currentUID);

        // Add profile's UID into current user's Following table
        final OnCompleteListener<DocumentSnapshot> storeFollowingInCurrentUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followingList = (docData.get("following") != null) ? (HashMap<String, Boolean>) docData.get("following") : new HashMap<String, Boolean>();
                    followingList.put(profileUID, true);
                    currUserDoc.update("following", followingList);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        currUserDoc.get().addOnCompleteListener(storeFollowingInCurrentUser);

        // Add current user into profile user's Followers table
        final DocumentReference profileUserDoc = db.collection("users").document(profileUID);
        final OnCompleteListener<DocumentSnapshot> storeFollowersinProfileUser= new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followersList = (docData.get("followers") != null) ? (HashMap<String, Boolean>) docData.get("followers") : new HashMap<String, Boolean>();
                    followersList.put(currentUID, true);
                    profileUserDoc.update("followers", followersList);
                    tvNumFollowers.setText(Integer.toString(followersList.size()));
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        profileUserDoc.get().addOnCompleteListener(storeFollowersinProfileUser);
    }

    private void unfollowAction(final String profileUID, final String currentUID) {
        // Remove profile's UID from current user's following table
        final DocumentReference currUserDoc = db.collection("users").document(currentUID);
        final OnCompleteListener<DocumentSnapshot> storeUnfollowInCurrentUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followingList = (docData.get("following") != null) ? (HashMap<String, Boolean>) docData.get("following") : new HashMap<String, Boolean>();
                    followingList.remove(profileUID);
                    currUserDoc.update("following", followingList);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        currUserDoc.get().addOnCompleteListener(storeUnfollowInCurrentUser);

        // Remove current user's UID from profile user's followers table
        final DocumentReference profileUserDoc = db.collection("users").document(profileUID);
        final OnCompleteListener<DocumentSnapshot> storeFollowersinProfileUser= new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followersList = (docData.get("followers") != null) ? (HashMap<String, Boolean>) docData.get("followers") : new HashMap<String, Boolean>();
                    followersList.remove(currentUID);
                    profileUserDoc.update("followers", followersList);
                    tvNumFollowers.setText(Integer.toString(followersList.size()));
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        profileUserDoc.get().addOnCompleteListener(storeFollowersinProfileUser);
    }

}
