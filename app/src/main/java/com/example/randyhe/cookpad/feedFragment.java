package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class feedFragment extends Fragment {

    final private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    final private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private final FirebaseUser currentFirebaseUser = fbAuth.getCurrentUser();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();
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

        //get current users followers:
        String currUs = fbAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(currUs);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    bookmarks = user.getBookmarkedRecipes();
                    Map<String, Object> data = document.getData();

                    HashMap<String, Object> following = (HashMap<String, Object>) data.get("following"); // get who current user is following
                    if (following.size() > 0) { // for every person you are following
                        for (String key : following.keySet()) {

                            followingRecipes(key, feed); //get all the recipes of one person ur following

                        }
                    }
                }
            }
        });

    }

    //get all the recipes of people you're following OR their bookmarked recipes
    private void followingRecipes(String tmpUser, final LinearLayout feed) {
        DocumentReference docRef = db.collection("users").document(tmpUser);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    final Context c = getActivity();
                    boolean isBookmark = false;
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    bookmarks = user.getBookmarkedRecipes();
                    Map<String, Object> data = document.getData();
                    final String us = (String) data.get("username");

                    HashMap<String, Object> recipes = (HashMap<String, Object>) data.get("recipes"); // get recipes of specific following

                    if (bookmarks != null) { //adds bookmarked recipes to the map
                        for (int i = 0; i < bookmarks.size(); ++i) {
                            recipes.put(bookmarks.get(i), null); //get that bookmarked recipe
                        }
                    }


                    if (recipes.size() > 0) { // for every recipe
                        for (String key : recipes.keySet()) {
                            if (recipes.get(key) == null)
                            {
                                isBookmark = true;
                            }
                            else {
                                isBookmark = false;
                            }
                            getRecipe(key, feed, us, isBookmark); //get that recipes' specific stuff
                        }
                    }
                }
            }
        });
    }

    //inflate each individual recipe
    private void getRecipe(final String recipeID, final LinearLayout feed, final String userID, final boolean isBookmark) {
        final Context c = getActivity();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c).build();
        ImageLoader.getInstance().init(config);

        final ArrayList<String> imgUrls = new ArrayList<>();

        DocumentReference dr = db.collection("recipes").document(recipeID);
        dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData(); //inflate recipe

                    View a = getLayoutInflater().inflate(R.layout.feed_recipe, null);
                    TextView recipeName = (TextView) a.findViewById(R.id.recipeTitle);
                    TextView recipeDesc = (TextView) a.findViewById(R.id.recipeDesc);
                    TextView recipePoster = (TextView) a.findViewById(R.id.recipePoster);
                    TextView notificationDesc = (TextView) a.findViewById(R.id.notificationDesc);
                    final ImageView recipePic = (ImageView) a.findViewById(R.id.foodPic);
                    final ImageButton bookmark = (ImageButton) a.findViewById(R.id.bookmarkButton);

                    recipeName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), Individual_Recipe.class);
                            intent.putExtra("ID", recipeID);
                            startActivity(intent);
                        }
                    });

                    if((String) data.get("title") == null || (String) data.get("title") == "")
                    {
                        recipeName.setText("Untitled");
                    }
                    else
                    {
                        recipeName.setText((String) data.get("title"));
                    }
                    if((String) data.get("desciption") == null || (String) data.get("desciption") == "")
                    {
                        recipeDesc.setText("No description available");
                    }
                    else
                    {
                        recipeDesc.setText((String) data.get("desciption"));
                    }
                    if(userID == null || userID == "")
                    {
                        recipePoster.setText("Unknown user");
                    }
                    else
                    {
                        String poster = "by " + userID;
                        recipePoster.setText(poster);
                    }
                    //checks if its recipe or bookmark
                    if (isBookmark)
                    {
                        String ndesc = userID + " bookmarked this recipe";
                        notificationDesc.setText(ndesc);
                    }
                    else {
                        String ndesc = userID + " shared this recipe";
                        notificationDesc.setText(ndesc);
                    }
                    if((String) data.get("mainPhotoStoragePath") == null || (String) data.get("mainPhotoStoragePath") == "")
                    {
                        recipePic.setImageResource(R.drawable.eggs);
                    }
                    else
                    {
                        final String path = (String) data.get("mainPhotoStoragePath");
                        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(c)
                                        .load(uri.toString())
                                        .into(recipePic);
                            }
                        });
                    }
                    if(bookmarks.contains(documentSnapshot.getId()))
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
                            final DocumentReference docRef = db.collection("users").document(fbAuth.getCurrentUser().getUid());
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        if(bookmarks.contains(documentSnapshot.getId()))
                                        {
                                            bookmarks.remove(documentSnapshot.getId());
                                            docRef.update("bookmarkedRecipes",bookmarks);
                                            bookmark.setImageResource(R.drawable.bookmark);
                                            Toast.makeText(c,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            bookmarks.add(documentSnapshot.getId());
                                            docRef.update("bookmarkedRecipes", bookmarks);
                                            bookmark.setImageResource(R.drawable.bookmarked);
                                            Toast.makeText(c, "Recipe has been bookmarked!", Toast.LENGTH_SHORT).show();
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
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}

