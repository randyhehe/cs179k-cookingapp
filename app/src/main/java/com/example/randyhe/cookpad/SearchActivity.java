package com.example.randyhe.cookpad;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private FirebaseAuth fbAuth;
    private FirebaseFirestore fbFirestore;
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int adapterCounter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTitleBar();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        FirebaseApp.initializeApp(this);
        fbFirestore = FirebaseFirestore.getInstance();
        fbAuth = FirebaseAuth.getInstance();
        fbStorage = FirebaseStorage.getInstance();
        storageReference = fbStorage.getReference();

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

    }

    @Override
    public void onResume() {
        super.onResume();
        populateData();
    }

    @Override
    public void onRefresh() {
        populateData();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id =  item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    private void setupTitleBar() {
        final ActionBar actionBar = getSupportActionBar();
        // titlebar background color
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        // back arrow color
        actionBar.setDisplayHomeAsUpEnabled(true);
        final Drawable arrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        arrow.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(arrow);

        // title text and color
        String searchString = getIntent().getExtras().getString("searchString");
        SpannableString s = new SpannableString("Search: " + searchString);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
    }

    private void populateData() {
        mSwipeRefreshLayout.setRefreshing(true);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this); // maybe change to view
        mRecyclerView.setLayoutManager(mLayoutManager);
        final List<RecipeCompactObject> recipeCompactObjectList = new ArrayList<>();

        final List<String> ids = getIntent().getExtras().getStringArrayList("ids");
        if (ids.size() < 1) {
            // show that none could be found
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            adapterCounter = ids.size();
            for (int i = 0; i < ids.size(); i++) {
                final String recipeId = ids.get(i);
                final DocumentReference doc1 = fbFirestore.collection("recipes").document(recipeId);
                doc1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot recipeSnapshot) {
                        final DocumentReference doc2 = fbFirestore.collection("users").document(recipeSnapshot.getString("userId"));
                        doc2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot recipeUserSnapshot) {
                                float recipeAvgRating = 0;
                                if(recipeSnapshot.getString("total") != null && recipeSnapshot.getString("total") != "") {
                                    recipeAvgRating = Float.parseFloat(recipeSnapshot.getString("total")) / Integer.parseInt(recipeSnapshot.getString("number"));
                                }

                                String recipeName = recipeSnapshot.getString("title");
                                String recipeTime = recipeSnapshot.getString("time");
                                String recipeServings = recipeSnapshot.getString("servings");
                                String recipeDescription = recipeSnapshot.getString("description");
                                String recipeMainPhotoPath = recipeSnapshot.getString("mainPhotoStoragePath");
                                User user = recipeUserSnapshot.toObject(User.class);
                                String recipePublisher = user.getUsername();
                                String recipePublisherPhotoPath = user.getProfilePhotoPath();
                                long comparatorValue = recipeSnapshot.getLong("timeCreated");
                                recipeCompactObjectList.add(new RecipeCompactObject(recipeId, recipeName, recipeTime, recipeServings, recipeDescription, recipeMainPhotoPath, recipePublisher, recipePublisherPhotoPath, recipeAvgRating, comparatorValue));

                                if (--adapterCounter == 0) {
                                    Collections.sort(recipeCompactObjectList, new Comparator<RecipeCompactObject>() {
                                        @Override
                                        public int compare(RecipeCompactObject a, RecipeCompactObject b) {
                                            if (a.comparatorValue > b.comparatorValue) return -1;
                                            else if (a.comparatorValue < b.comparatorValue)  return 1;
                                            else return 0;
                                        }
                                    });
                                    mAdapter = new RecipeCompactAdapter(recipeCompactObjectList, SearchActivity.this, true);
                                    mRecyclerView.setAdapter(mAdapter);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}
