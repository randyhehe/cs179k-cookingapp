package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        RelativeLayout recipeItem = view.findViewById(R.id.relLayout1);
        recipeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CreateRecipe.class);
                intent.putExtra("EDIT", true);
                startActivity(intent);
            }
        });
        return view;
    }
}