package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Asus on 1/30/2018.
 */

import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.ContentValues.TAG;


public class feedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    final private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    final private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    //private final StorageReference storageReference = fbStorage.getReference();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int followingCounter;
    private Map<String,recipeVals> recipeIds = new HashMap<>(0);
    private int recipesCounter;

    @Override
    public void onRefresh() {
        populateFeed();
    }

    public static feedFragment newInstance() {
        feedFragment fragment = new feedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testfeedfrag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                populateFeed();
            }
        });
    }

    private void populateFeed()
    {
        getNumRecipes();
    }

    private void getNumRecipes()
    {
        final TextView tmp = getView().findViewById(R.id.activityFeedDesc);
        recipeIds = new HashMap<>();
        final List<FeedIndividualRecipe> list = new ArrayList<>();
        mSwipeRefreshLayout.setRefreshing(true);
        mRecyclerView = getView().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext()); // maybe change to view
        mRecyclerView.setLayoutManager(mLayoutManager);
        final String currUser = fbAuth.getCurrentUser().getUid();
        db.collection("users").document(currUser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot user1Snapshot) {
                final User user1 = user1Snapshot.toObject(User.class);

                List<String> following = user1.getFollowing();
                if(following.size()==0)
                {
                    tmp.setText("Follow someone to see what they're up to!");
                    RecyclerView.Adapter mAdapter = new feedRecipeAdapter(list, getContext(), true);
                    mRecyclerView.setAdapter(mAdapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                else
                {
                    tmp.setText("See what your friends have been up to!");
                    followingCounter = following.size();
                    for (int i = 0; i < following.size(); i++) {
                        db.collection("users").document(following.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot user2Snapshot) {
                                final User user2 = user2Snapshot.toObject(User.class);
                                List<String> recipes = new ArrayList<>();
                                Map<String,recipeVals> recipesMap = new HashMap<String, recipeVals>() {};
                                if(user1.getRecipes() != null)
                                {
                                    recipes = user2.getRecipes();
                                }
                                if(recipes != null)
                                {
                                    for (int i = 0; i < recipes.size() ; i++)
                                    {
                                        recipeVals tmp = new recipeVals(false,0,user2.getUsername(),user2.getProfilePhotoPath());
                                        recipesMap.put(recipes.get(i),tmp);
                                    }
                                }
                                Map<String, Long> bookmarks = new HashMap<>();
                                if(user2.getBookmarkedRecipes() != null)
                                {
                                    bookmarks = user2.getBookmarkedRecipes();
                                }
                                if(bookmarks != null)
                                {
                                    for (Map.Entry<String,Long> entry: bookmarks.entrySet())
                                    {
                                        recipeVals tmp = new recipeVals(true,entry.getValue(),user2.getUsername(),user2.getProfilePhotoPath());
                                        recipesMap.put(entry.getKey(),tmp);
                                    }
                                }

                                recipeIds.putAll(recipesMap);
                                if (--followingCounter == 0) { // all the recipe ids are in the list
                                    recipesCounter = recipeIds.size();
                                    for (final Map.Entry<String,recipeVals> entry: recipeIds.entrySet()) {
                                        db.collection("recipes").document(entry.getKey()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot recipeSnapshot) {
                                                // create compactObject with properties from recipeSnapshot
//                                            if(currUser == )
                                                if(!entry.getValue().isBookmark)
                                                {
                                                    list.add(new FeedIndividualRecipe(entry.getKey(),
                                                            entry.getValue().user,entry.getValue().isBookmark,entry.getValue().photoPath,(long)recipeSnapshot.get("timeCreated")));
                                                }
                                                else
                                                {
                                                    list.add(new FeedIndividualRecipe(entry.getKey(),
                                                            entry.getValue().user,entry.getValue().isBookmark,entry.getValue().photoPath,entry.getValue().compVal));
                                                }

                                                if (--recipesCounter == 0) {
                                                    Collections.sort(list,new Comparator<FeedIndividualRecipe>()
                                                    {
                                                        @Override
                                                        public int compare(FeedIndividualRecipe a, FeedIndividualRecipe b) {
                                                            if (a.comparatorVal < b.comparatorVal) return 1;
                                                            else if (a.comparatorVal > b.comparatorVal) return -1;
                                                            else return 0;
                                                        }
                                                    });
                                                    RecyclerView.Adapter mAdapter = new feedRecipeAdapter(list, getContext(), true);
                                                    mRecyclerView.setAdapter(mAdapter);
                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                }
                                            }
                                        });
                                    }

                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}