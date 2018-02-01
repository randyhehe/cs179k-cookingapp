package com.example.randyhe.cookpad;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

/**
 * Created by Monica on 1/28/18.
 */


public class Individual_Recipe extends AppCompatActivity {
    final Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_individual_recipe);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.indiv_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");

        View indiv_rec = getLayoutInflater().inflate(R.layout.individual_recipe_main, null);

        ScrollView individual_recipe_layout = (ScrollView) findViewById(R.id.main_scroll);

        ImageView mainImage = (ImageView) indiv_rec.findViewById(R.id.main_image);
        TextView mainTitle = (TextView) indiv_rec.findViewById(R.id.main_title);
        TextView mainDescription = (TextView) indiv_rec.findViewById(R.id.main_description);
        ImageView mainAvatar = (ImageView) indiv_rec.findViewById(R.id.avatar);
        TextView mainName = (TextView) indiv_rec.findViewById(R.id.main_name);

        TextView ingredientsTitle = (TextView) indiv_rec.findViewById(R.id.ingredients_title);
        TextView numFeeds = (TextView) indiv_rec.findViewById(R.id.num_feeds);
        TextView methodTitle = (TextView) indiv_rec.findViewById(R.id.method_title);
        TextView cookTime = (TextView) indiv_rec.findViewById(R.id.cook_time);

        mainImage.setImageResource(R.drawable.kermit_cooking);
        mainTitle.setText("Kermit's Yummy Apple Juice ;)");
        mainDescription.setText("This is da bes apple juice in da house");
        mainAvatar.setImageResource(R.drawable.kermit_cooking);
        mainName.setText("Kermit theee Frog");

        Button followButton = (Button) indiv_rec.findViewById(R.id.follow_button);
        followButton.setText("Follow");
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });

        ingredientsTitle.setText("Ingredients");
        numFeeds.setText("20 peeps");
        methodTitle.setText("Method");
        cookTime.setText("30 secs");

        individual_recipe_layout.addView(indiv_rec);

        //String[] ingVals = {"eggs", "milk", "leaves", "apples (most important)"};

        View injectorLayout = getLayoutInflater().inflate(R.layout.single_ingredient, null);

        LinearLayout ingredientsLayout = (LinearLayout) findViewById(R.id.ingredients);

        TextView ingredientText = (TextView) injectorLayout.findViewById(R.id.ingredient_text);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("eggs ");
            sb.append(i);
            if(i != 4) {
                sb.append("\n\n");
            }
        }
        ingredientText.setText(sb.toString());
        ingredientsLayout.addView(injectorLayout);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.like:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_favorite_black_24dp).getConstantState())) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_black_24dp));
                    Toast.makeText(c,"Recipe has been unliked!",Toast.LENGTH_SHORT).show();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_black_24dp));
                    Toast.makeText(c,"Recipe has been liked!",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.bookmark:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp).getConstantState())) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_border_black_24dp));
                    Toast.makeText(c,"Recipe has been unbookmarked!",Toast.LENGTH_SHORT).show();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_black_24dp));
                    Toast.makeText(c,"Recipe has been bookmarked!",Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

