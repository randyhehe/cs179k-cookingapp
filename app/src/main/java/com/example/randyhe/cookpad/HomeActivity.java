package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus on 1/28/2018.
 */

public class HomeActivity extends AppCompatActivity
{

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context c = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final ImageButton topOptionsButton = (ImageButton) findViewById(R.id.options);

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
                        if(item.getTitle().toString().equals("Account settings")) {
                            startActivity(new Intent(c, ProfileActivity.class));
                        }
                        return true;
                    }
                });
            }
        });

        final SmartFragmentStatePagerAdapter fragmentPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = findViewById(R.id.frame_layout);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(fragmentPagerAdapter);

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

        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new  Intent(HomeActivity.this, SearchActivity.class);
                intent.putExtra("searchString", s);

                ArrayList<String> queryResults = new ArrayList<>();
                queryResults.add("bce3204e-573a-42a2-b2dc-92941a6dfe75");
                queryResults.add("17436ae2-2374-48a2-9c45-ed6f4b550fe0");
                queryResults.add("665f0975-eace-466e-964e-ce18e5a427be");

                intent.putStringArrayListExtra("ids", queryResults);
                startActivity(intent);
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
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        { // not signed in
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
