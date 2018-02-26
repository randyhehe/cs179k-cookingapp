package com.example.randyhe.cookpad;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class feedFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();
    List<String> bookmarks;

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

        final LinearLayout feed = (LinearLayout) getView().findViewById(R.id.recipesScrollView);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c).build();
        ImageLoader.getInstance().init(config);

        final ArrayList<String> imgUrls = new ArrayList<>();

        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    bookmarks = user.getBookmarkedRecipes();

                    db.collection("recipes").get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            final DocumentSnapshot d = document;
                                            View a = getLayoutInflater().inflate(R.layout.feed_recipe, null);
                                            TextView recipeName = (TextView) a.findViewById(R.id.recipeTitle);
                                            TextView recipeDesc = (TextView) a.findViewById(R.id.recipeDesc);
                                            TextView recipePoster = (TextView) a.findViewById(R.id.recipePoster);
                                            TextView notificationDesc = (TextView) a.findViewById(R.id.notificationDesc);
                                            ImageView recipePic = (ImageView) a.findViewById(R.id.foodPic);
                                            final ImageButton bookmark = (ImageButton) a.findViewById(R.id.bookmarkButton);

                                            if(document.getString("title") == null || document.getString("title") == "")
                                            {
                                                recipeName.setText("No title");
                                            }
                                            else
                                            {
                                                recipeName.setText(document.getString("title"));
                                            }
                                            if(document.getString("desciption") == null || document.getString("desciption") == "")
                                            {
                                                recipeDesc.setText("No desc");
                                            }
                                            else
                                            {
                                                recipeDesc.setText(document.getString("desciption"));
                                            }
                                            if(document.getString("userId") == null || document.getString("userId") == "")
                                            {
                                                recipePoster.setText("No poster");
                                            }
                                            else
                                            {
                                                recipePoster.setText(document.getString("userId"));
                                            }
                                            notificationDesc.setText("Baldo bookmarked this recipe: ");
                                            if(document.getString("imagePath") == null || document.getString("imagePath") == "")
                                            {
                                                recipePic.setImageResource(R.drawable.eggs);
                                            }
                                            else
                                            {

                                                Glide.with(c)
                                                        .load(document.getString("imagePath"))
                                                        .into(recipePic);
                                            }
                                            if(bookmarks.contains(d.getId()))
                                            {
                                                bookmark.setImageResource(R.drawable.bookmarked);
                                            }
                                            else
                                            {
                                                bookmark.setImageResource(R.drawable.bookmark);
                                            }
                                            bookmark.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {
                                                    final DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
                                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if(task.isSuccessful()) {
                                                                if(bookmarks.contains(d.getId()))
                                                                {
                                                                    bookmarks.remove(d.getId());
                                                                    docRef.update("bookmarkedRecipes",bookmarks);
                                                                    bookmark.setImageResource(R.drawable.bookmark);
                                                                    Toast.makeText(c,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();
                                                                }
                                                                else
                                                                {
                                                                    bookmarks.add(d.getId());
                                                                    docRef.update("bookmarkedRecipes",bookmarks);
                                                                    bookmark.setImageResource(R.drawable.bookmarked);
                                                                    Toast.makeText(c,"Recipe has been bookmarked!",Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                Log.d(TAG, "get failed with ", task.getException());
                                                            }
                                                        }
                                                    });
//                                        if(bookmark.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.bookmarked).getConstantState()))
//                                        {
//                                            bookmark.setImageResource(R.drawable.bookmark);
//                                            Toast.makeText(c,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();
//                                        }
//                                        else
//                                        {
//                                            bookmark.setImageResource(R.drawable.bookmarked);
//                                            Toast.makeText(c,"Recipe has been bookmarked!",Toast.LENGTH_SHORT).show();
//                                        }
                                                }
                                            });

                                            feed.addView(a);
                                        }
                                    }
                                }
                            });
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}