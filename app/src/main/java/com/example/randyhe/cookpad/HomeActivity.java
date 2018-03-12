package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Asus on 1/28/2018.
 */

public class HomeActivity extends AppCompatActivity
{

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final private FirebaseFirestore fbFirestore = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private static final String TAG = "TokenCreationInHome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context c = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final ImageButton topOptionsButton = (ImageButton) findViewById(R.id.options);

        initFCM();

        topOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu optionsMenu = new PopupMenu(HomeActivity.this,topOptionsButton);
                optionsMenu.getMenuInflater().inflate(R.menu.top_navbar_menu,optionsMenu.getMenu());
                optionsMenu.show();

                optionsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        if(item.getTitle().toString().equals("Logout"))
                        {
                            Toast.makeText(c,"You have been logged out.",Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(c, LoginActivity.class));
                        }
                        if(item.getTitle().toString().equals("Create recipe"))
                        {
                            startActivity(new Intent(c, ManageRecipe.class));
                        }
                        return true;
                    }
                });
            }
        });

        final SmartFragmentStatePagerAdapter fragmentPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        final CustomViewPager viewPager = findViewById(R.id.frame_layout);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setPagingEnabled(false);


        BottomNavigationView navBar = findViewById(R.id.navigation);
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.feed:
                        viewPager.setCurrentItem(0, false);
                        return true;
                    case R.id.profile:
                        viewPager.setCurrentItem(1, false);
                        return true;
                    case R.id.explore:
                        viewPager.setCurrentItem(2, false);
                        return true;
                    case R.id.bookmark:
                        viewPager.setCurrentItem(3, false);
                        return true;
                    default:
                        return false;
                }
            }
        });

        final SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String s) {
                searchView.clearFocus();
                fbFirestore.collection("recipes").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        Set<String> queryResults = new HashSet<>();

                        List<DocumentSnapshot> recipes = documentSnapshots.getDocuments();
                        for (DocumentSnapshot document : recipes) {
                            Map<String, Object> data = document.getData();

                            // Matches substring based on ingredients, title, description, tags
                            String title = (String) data.get("title");
                            String description = (String) data.get("description");

                            if ((title != null && title.contains(s)) ||
                                (description != null && description.contains(s))) {
                                queryResults.add(document.getId());
                                continue;
                            }
                            List<String> ingrs = (ArrayList<String>) data.get("ingrs");
                            for (String ingr : ingrs) {
                                if (ingr.contains(s)) {
                                    queryResults.add(document.getId());
                                    continue;
                                }
                            }
                            List<String> tags = (ArrayList<String>) data.get("tags");
                            for (String tag: tags) {
                                if (tag.contains(s)) {
                                    queryResults.add(document.getId());
                                    continue;
                                }
                            }
                        }

                        ArrayList<String> res = new ArrayList<>();
                        res.addAll(queryResults);

                        Intent intent = new  Intent(HomeActivity.this, SearchActivity.class);
                        intent.putExtra("searchString", s);
                        intent.putStringArrayListExtra("ids", res);
                        startActivity(intent);
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                    return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        checkIfLoggedIn();
    }

    public void checkIfLoggedIn() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void sendRegistrationToServer(String token) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = mAuth.getCurrentUser();
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("token", token);
        db.collection("users").document(currentFirebaseUser.getUid()).update(tokens)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }


    private void initFCM(){
        String token = FirebaseInstanceId.getInstance().getToken();
        //Log.d(TAG, "initFCM: token: " + token);
        sendRegistrationToServer(token);

    }


}
