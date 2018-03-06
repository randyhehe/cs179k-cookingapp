package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // publish and uploading
    private static final String TAG = "ProfileFragment";
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


    public static profileFragment newInstance() {
        profileFragment fragment = new profileFragment();
        return fragment;
    }

    private void setupViews(View view) {
        tvProfileName = view.findViewById(R.id.profileName);
        tvProfileBio = view.findViewById(R.id.profileBio);
        tvNumRecipes = view.findViewById(R.id.textViewRecipes);
        tvNumFollowers = view.findViewById(R.id.textViewFollowers);
        tvNumFollowing = view.findViewById(R.id.textViewFollowing);
        profileImg = view.findViewById(R.id.profileImg);

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
        getData();
    }

    @Override
    public void onRefresh() {

        getData();
    }

    private void getData() {
        mSwipeRefreshLayout.setRefreshing(true);
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

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getData();
            }
        });
    }

    private void loadRecipeList(final List<String> rList) {
        if (rList.size() < 1) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        mRecyclerView = getView().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext()); // maybe change to view
        mRecyclerView.setLayoutManager(mLayoutManager);
        final List<RecipeCompactObject> recipeCompactObjectList = new ArrayList<>();

        adapterCounter = rList.size();
        for (int i = 0; i < rList.size(); i++) {
            final String recipeId = rList.get(i);
            DocumentReference recipeDocumentReference = db.collection("recipes").document(rList.get(i));
            recipeDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String recipeName = documentSnapshot.getString("title");
                    String recipeTime = documentSnapshot.getString("time");
                    String recipeServings = documentSnapshot.getString("servings");
                    String recipeDescription = documentSnapshot.getString("description");
                    String recipeMainPhotoPath = documentSnapshot.getString("mainPhotoStoragePath");
                    String recipePublisher = username;
                    String recipePublisherPhotoPath = profileImgPath;
                    long comparatorValue = documentSnapshot.getLong("timeCreated");
                    recipeCompactObjectList.add(new RecipeCompactObject(recipeId, recipeName, recipeTime, recipeServings, recipeDescription, recipeMainPhotoPath, recipePublisher, recipePublisherPhotoPath, comparatorValue));

                    if (--adapterCounter == 0) {
                        Collections.sort(recipeCompactObjectList, new Comparator<RecipeCompactObject>() {
                            @Override
                            public int compare(RecipeCompactObject a, RecipeCompactObject b) {
                                if (a.comparatorValue < b.comparatorValue) return -1;
                                else if (a.comparatorValue > b.comparatorValue)  return 1;
                                else return 0;
                            }
                        });
                        mAdapter = new RecipeCompactAdapter(recipeCompactObjectList, getContext(), false);
                        mRecyclerView.setAdapter(mAdapter);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }
}

