package com.example.randyhe.cookpad;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mcast on 2/18/2018.
 */

public class FollowActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();

    private CircleImageView profilePic;
    private TextView tvUsername;
    private TextView tvRecipeNum;
    private TextView tvFollowersNum;
    private Button followBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_followlist);
        final LinearLayout feed = (LinearLayout) findViewById(R.id.followfeed);

        String user = getIntent().getExtras().getString("ID");
        if(getIntent().getExtras().getBoolean("Followers")) {
            loadFollowersList(user, feed);
        }
        else {
            loadFollowingList(user, feed);
        }
    }


    private void setupViews(View view) {
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        tvRecipeNum = (TextView) view.findViewById(R.id.recipeNum);
        tvFollowersNum = (TextView) view.findViewById(R.id.followersNum);
        profilePic = (CircleImageView) view.findViewById(R.id.profilePic);
        followBtn = (Button) view.findViewById(R.id.followBtn);
    }

    private void loadFollowersList(final String user, final LinearLayout feed) {
        DocumentReference docRef = db.collection("users").document(user);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    List<String> followers = user.getFollowers();
                    if(followers != null ) {
                        for (int i = 0; i < followers.size(); i++) {
                            loadItem(followers.get(i), feed, false);
                        }
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void loadFollowingList(final String user, final LinearLayout feed) {
        DocumentReference docRef = db.collection("users").document(user);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    List<String> following = user.getFollowing();
                    if(following != null) {
                        for (int i = 0; i < following.size(); i++) {
                            loadItem(following.get(i), feed, true);
                        }
                    }
                }
                else {
                    // Not successful
                }

            }
        });
    }

    private void loadItem(final String userId, final LinearLayout feed, final Boolean isFollowing) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);

                    int recipesNum = user.getNumRecipes();
                    int followersNum = user.getNumFollowers();
                    View followItem = getLayoutInflater().inflate(R.layout.snippet_follow_listitem, null);
                    setupViews(followItem);

                    tvUsername.setText(user.getUsername());
                    tvRecipeNum.setText(String.valueOf(recipesNum) + " recipes");
                    tvFollowersNum.setText(String.valueOf(followersNum) + " followers");

                    tvUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(FollowActivity.this, ProfileActivity.class);
                            intent.putExtra("ID", userId);
                            startActivity(intent);
                        }
                    });

                    setupBtn(userId, currentFirebaseUser.getUid());
                    feed.addView(followItem);

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setupBtn(final String profileUID, final String currentUID) {
        // Does not setup a button if it's your own profile
        if(profileUID.equals(currentUID)) {
            followBtn.setVisibility(View.GONE);
            return;
        }

        DocumentReference docRef = db.collection("users").document(currentUID);
        final Boolean[] isFollowingBool = {false};
        final OnCompleteListener<DocumentSnapshot> isFollowing = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    if(docData.get("following") == null) {
                        followBtn.setText("Follow");
                        setupFollowBtn(profileUID, currentUID);
                    }
                    else {
                        final Map<String, Boolean> followersList = (HashMap<String, Boolean>) docData.get("following");

                        if (followersList.containsKey(profileUID)) {
                            followBtn.setText("Following");
                            setupUnfollowBtn(profileUID, currentUID);
                        } else {
                            followBtn.setText("Follow");
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
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followAction(profileUID, currentUID);
                followBtn.setText("Following");
                setupUnfollowBtn(profileUID, currentUID);
            }
        });
    }

    private void setupUnfollowBtn(final String profileUID, final String currentUID) {
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfollowAction(profileUID, currentUID);
                followBtn.setText("Follow");
                setupFollowBtn(profileUID, currentUID);
            }
        });
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
                    tvFollowersNum.setText(Integer.toString(followersList.size()) + " followers");
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
                    followingList.remove(profileUID, true);
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
                    tvFollowersNum.setText(Integer.toString(followersList.size()) + " followers");
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
