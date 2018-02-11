package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileFragment extends Fragment {
    public static profileFragment newInstance() {
        profileFragment fragment = new profileFragment();
        return fragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_center_profile, container, false);

        final TextView editProfile = (TextView) view.findViewById(R.id.editProfileButton);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        LinearLayout feed = (LinearLayout) getView().findViewById(R.id.profileRecipeFeed);

        View a = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);

        CircleImageView userPic = (CircleImageView) a.findViewById(R.id.userPic);
        TextView username = (TextView) a.findViewById(R.id.username);
        TextView recipeName = (TextView) a.findViewById(R.id.recipeName);
        TextView recipeTime = (TextView) a.findViewById(R.id.recipeTime);
        TextView recipeServings = (TextView) a.findViewById(R.id.recipeServings);
        TextView recipeBio = (TextView) a.findViewById(R.id.recipeBio);


        userPic.setImageResource(R.drawable.kermit_cooking);
        recipeName.setText("Test Recipe");
        recipeTime.setText("4 hours");
        recipeServings.setText("15 servings");
        recipeBio.setText("This is a test recipe for the profile recipe feed.");

//        TO-DO: Open Edit recipe
        recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateRecipe.class);
                intent.putExtra("EDIT", true);
                startActivity(intent);
            }
        });

        feed.addView(a);

        View b = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);
        feed.addView(b);

        View c = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);
        feed.addView(c);
    }
}