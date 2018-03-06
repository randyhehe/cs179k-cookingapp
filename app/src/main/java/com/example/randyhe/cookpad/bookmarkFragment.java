package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class bookmarkFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "bookmarkFragment";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = auth.getCurrentUser();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int adapterCounter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public static bookmarkFragment newInstance() {
        bookmarkFragment fragment = new bookmarkFragment();
        return fragment;
    }

    @Override
    public void onRefresh() {
        getData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bookmarkfeed, container, false);
    }

    public void getData() {
        mSwipeRefreshLayout.setRefreshing(true);
        mRecyclerView = getView().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext()); // maybe change to view
        mRecyclerView.setLayoutManager(mLayoutManager);
        final List<RecipeCompactObject> recipeCompactObjectList = new ArrayList<>();

        final DocumentReference userDocumentReference = db.collection("users").document(currentUser.getUid());
        userDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot userSnapshot) {
                User user = userSnapshot.toObject(User.class);
                final Map<String, Long> bookmarks = user.getBookmarkedRecipes();
                adapterCounter = bookmarks.size();
                for (final String recipeId : bookmarks.keySet()) {
                    final DocumentReference recipeDocumentReference = db.collection("recipes").document(recipeId);
                    recipeDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot recipeSnapshot) {
                            db.collection("users").document(recipeSnapshot.getString("userId")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot recipeUserSnapshot) {
                                    String recipeName = recipeSnapshot.getString("title");
                                    String recipeTime = recipeSnapshot.getString("time");
                                    String recipeServings = recipeSnapshot.getString("servings");
                                    String recipeDescription = recipeSnapshot.getString("description");
                                    String recipeMainPhotoPath = recipeSnapshot.getString("mainPhotoStoragePath");
                                    User user = recipeUserSnapshot.toObject(User.class);
                                    String recipePublisher = user.getUsername();
                                    String recipePublisherPhotoPath = user.getProfilePhotoPath();
                                    long comparatorValue = bookmarks.get(recipeId);
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
                                        mAdapter = new RecipeCompactAdapter(recipeCompactObjectList, getContext(), true);
                                        mRecyclerView.setAdapter(mAdapter);
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
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
                mSwipeRefreshLayout.setRefreshing(true);
                getData();
            }
        });
    }
}