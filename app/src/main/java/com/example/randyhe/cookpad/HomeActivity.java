package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static org.xmlpull.v1.XmlPullParser.TYPES;

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
                            startActivity(new Intent(c, CreateRecipe.class));
                        }
                        if(item.getTitle().toString().equals("Account settings")) {
                            startActivity(new Intent(c, ProfileActivity.class));
                        }
                        return true;
                    }
                });
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        final CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.frame_layout);
        final ViewPagerAdapter adapter = new ViewPagerAdapter (getSupportFragmentManager());
        adapter.addFragment(new feedFragment(), "feed");
        //adapter.addFragment(new profileFragment(), "profile");
        viewPager.setAdapter(adapter);

        //final FragmentManager fm = getSupportFragmentManager();

        final Vector<String> fragments = new Vector<String>();
        fragments.add("feed");

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        //Toast.makeText(c,Integer.toString(fragments.size()),Toast.LENGTH_SHORT).show();
                        switch (item.getItemId()) {
                            case R.id.feed:
                                //selectedFragment = feedFragment.newInstance();
                                viewPager.setCurrentItem(fragments.indexOf("feed"),false);
                                break;
                            case R.id.profile:
                                //selectedFragment = profileFragment.newInstance();
                                if(!adapter.containsFragment("profile"))
                                {
                                    fragments.add("profile");
                                    adapter.addFragment(new profileFragment(), "profile");
                                    adapter.notifyDataSetChanged();
//                                    viewPager.setAdapter(adapter);
                                }
                                viewPager.setCurrentItem(fragments.indexOf("profile"),false);
                                break;
                            case R.id.explore:
                                //selectedFragment = profileFragment.newInstance();
                                if(!adapter.containsFragment("explore"))
                                {
                                    fragments.add("explore");
                                    adapter.addFragment(new exploreFragment(), "explore");
                                    adapter.notifyDataSetChanged();
//                                    viewPager.setAdapter(adapter);
                                }
                                viewPager.setCurrentItem(fragments.indexOf("explore"),false);
                                break;
//                            case R.id.explore:
//                                selectedFragment = ItemThreeFragment.newInstance();
//                                break;
                            case R.id.bookmark:
                                if(!adapter.containsFragment("bookmark")) {
                                    fragments.add("bookmark");
                                    adapter.addFragment(new bookmarkFragment(), "bookmark");
                                    adapter.notifyDataSetChanged();
//                                    viewPager.setAdapter(adapter);
                                }
                                viewPager.setCurrentItem(fragments.indexOf("bookmark"), false);
                                break;
                        }

//                        Toast.makeText(c,Integer.toString(fm.getBackStackEntryCount()),Toast.LENGTH_SHORT).show();
//                        FragmentTransaction transaction = fm.beginTransaction();
//                        transaction.replace(R.id.frame_layout, selectedFragment);
//                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.frame_layout, feedFragment.newInstance());
//        transaction.commit();
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
