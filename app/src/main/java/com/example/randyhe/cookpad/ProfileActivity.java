package com.example.randyhe.cookpad;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import android.content.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();


    private TextView tvProfileName;
    private TextView tvProfileBio;
    private CircleImageView profileImg;
    private TextView tvNumRecipes;
    private TextView tvNumFollowers;
    private TextView tvNumFollowing;
    private Button followButton;
//    private String profilePhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_center_profile);

        String user = getIntent().getExtras().getString("ID");
//        String user = "eqCQpG2FgNcVire6kuB41YsWjQ42";
        setupViews();
        setupBtn(user, currentFirebaseUser.getUid());
        getData(user);
        setupTopInfo(user);
    }


    private Boolean isFollowing(final String profileUID, final String currentUID) {
        DocumentReference docRef = db.collection("users").document(currentUID);
        final Boolean[] isFollowingBool = {false};
        final OnCompleteListener<DocumentSnapshot> isFollowing = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    if(docData.get("following") == null) {
                        isFollowingBool[0] = false;
                    }
                    else {
                        final Map<String, Boolean> followersList = (HashMap<String, Boolean>) docData.get("following");

                        if (followersList.containsKey(profileUID)) {
                            isFollowingBool[0] = true;
                        } else {
                            isFollowingBool[0] = false;
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
        return isFollowingBool[0];
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
        final Boolean[] isFollowingBool = {false};
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
        followButton = (Button) findViewById(R.id.follow);
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
                    tvNumFollowers.setText(Integer.toString(user.getNumFollowers()));
                    tvNumFollowing.setText(Integer.toString(user.getNumFollowing()));
                    String profilePhotoPath = user.getProfilePhotoPath();
                    if(profilePhotoPath != null) {
                        Glide.with(ProfileActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReference.child(profilePhotoPath))
                                .into(profileImg);
                    }
                    else {
                        profileImg.setImageResource(R.drawable.profile_g);
                    }
                    loadRecipeList(recipes, user.getUsername(), profilePhotoPath);

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void loadRecipeList(final List<String> rList, final String user, final String profilePhotoPath) {
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
                                    .using(new FirebaseImageLoader())
                                    .load(storageReference)
                                    .into(recipePic);
                        }

                        if(profilePhotoPath != null) {
                            Glide.with(ProfileActivity.this)
                                    .using(new FirebaseImageLoader())
                                    .load(storageReference.child(profilePhotoPath))
                                    .into(userPic);
                        }
                        else {
                            userPic.setImageResource(R.drawable.profile_g);
                        }
                        username.setText(user);
                        recipeName.setText(document.getString("title"));
                        recipeBio.setText(document.getString("description"));
                        feed.addView(a);

                        //  TO-DO: Open Edit recipe
                        a.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ProfileActivity.this, Individual_Recipe.class);
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
