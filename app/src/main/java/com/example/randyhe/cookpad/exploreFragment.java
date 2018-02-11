package com.example.randyhe.cookpad;

/**
 * Created by Asus on 2/5/2018.
 */

import android.content.Context;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

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

public class exploreFragment extends Fragment {
    public static feedFragment newInstance() {
        feedFragment fragment = new feedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_explore, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        final Context c = getActivity();

        LinearLayout feed = (LinearLayout) getView().findViewById(R.id.recipesScrollView);

        View a = getLayoutInflater().inflate(R.layout.explore_recipe, null);

        feed.addView(a);

        View b = getLayoutInflater().inflate(R.layout.explore_recipe, null);
        feed.addView(b);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}