package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        setContentView(R.layout.activity_feed);

        final ImageButton topOptionsButton = (ImageButton) findViewById(R.id.options);

        topOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu optionsMenu = new PopupMenu(HomeActivity.this,topOptionsButton);
                optionsMenu.getMenuInflater().inflate(R.menu.top_navbar_menu,optionsMenu.getMenu());
                optionsMenu.show();

            }
        });

        LinearLayout feed = (LinearLayout) findViewById(R.id.recipesScrollView);

        View a = getLayoutInflater().inflate(R.layout.feed_recipe, null);

        TextView recipeName = (TextView) a.findViewById(R.id.recipeTitle);
        TextView recipeDesc = (TextView) a.findViewById(R.id.recipeDesc);
        TextView recipePoster = (TextView) a.findViewById(R.id.recipePoster);
        TextView notificationDesc = (TextView) a.findViewById(R.id.notificationDesc);
        ImageView recipePic = (ImageView) a.findViewById(R.id.foodPic);
        final ImageButton bookmark = (ImageButton) a.findViewById(R.id.bookmarkButton);

        recipeName.setText("Eggs");
        recipeDesc.setText("This is how to boil an egg.");
        recipePoster.setText("by Melissa");
        notificationDesc.setText("Baldo bookmarked this recipe: ");
        recipePic.setImageResource(R.drawable.eggs);
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bookmark.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.bookmarked).getConstantState()))
                {
                    bookmark.setImageResource(R.drawable.bookmark);
                    Toast.makeText(c,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    bookmark.setImageResource(R.drawable.bookmarked);
                    Toast.makeText(c,"Recipe has been bookmarked!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        View b = getLayoutInflater().inflate(R.layout.feed_recipe, null);

        feed.addView(a);
        feed.addView(b);
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
