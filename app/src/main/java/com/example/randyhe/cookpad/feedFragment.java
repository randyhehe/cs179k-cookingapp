package com.example.randyhe.cookpad;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Asus on 1/30/2018.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class feedFragment extends Fragment {
    public static feedFragment newInstance() {
        feedFragment fragment = new feedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        final Context c = getActivity();


        LinearLayout feed = (LinearLayout) getView().findViewById(R.id.recipesScrollView);

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

        feed.addView(a);

        View b = getLayoutInflater().inflate(R.layout.feed_recipe, null);
        feed.addView(b);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}