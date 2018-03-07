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
import android.widget.RatingBar;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class feedFragment extends Fragment {

    final private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    final private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();
    Map<String, Long> origbookmarks;
    Map<String, Long> bookmarks;
    List<String> listRecipes = new ArrayList<String>();
    private RatingBar avgStarsDisp;

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
                    origbookmarks = user.getBookmarkedRecipes();
                    Map<String, Object> data = document.getData();

                    List<String> following = user.getFollowing(); // get who current user is following
                    if (following.size() > 0) { // for every person you are following
                        for (String key : following) {
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
                    //Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    if(document.exists())
                    {
                        User user = document.toObject(User.class);
                        Map<String, Object> data = document.getData();
                        final String us = (String) data.get("username");

                        List<String> recipes = user.getRecipes(); // get recipes of specific


                        if (bookmarks != null)
                        { //adds bookmarked recipes to the map
                            recipes.addAll(bookmarks.keySet());
                        }

                        String profileUrl = user.getProfilePhotoPath();
                        int recipeNum = user.getNumRecipes();
                        int totalNum = 0;
                        if (recipes.size() > 0) { // for every recipe
                            for (String key : recipes)
                            {
                                if (totalNum >= recipeNum)
                                {
                                    isBookmark = true;
                                }
                                else {
                                    isBookmark = false;
                                }
                                getRecipe(key, feed, us, isBookmark, profileUrl); //get that recipes' specific stuff
                                totalNum++;
                            }
                        }
                    }
                }
            }
        });
    }

    //inflate each individual recipe
    private void getRecipe(final String recipeID, final LinearLayout feed, final String userID, final boolean isBookmark, final String profileUrl) {
        if(listRecipes == null)
        {
            listRecipes.add(recipeID);
            inflateRecipe(recipeID,feed,userID,isBookmark,profileUrl);
        }
        else
        {
            if(!listRecipes.contains(recipeID))
            {
                listRecipes.add(recipeID);
                inflateRecipe(recipeID,feed,userID,isBookmark,profileUrl);
            }
        }

    }

    private void inflateRecipe (final String recipeID, final LinearLayout feed, final String userID, final boolean isBookmark, final String profileUrl)
    {
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
                        final ImageView profilePic = (ImageView) a.findViewById(R.id.profile);
                        TextView recipeName = (TextView) a.findViewById(R.id.recipeTitle);
                        TextView recipeDesc = (TextView) a.findViewById(R.id.recipeDesc);
                        final TextView recipePoster = (TextView) a.findViewById(R.id.recipePoster);
                        TextView notificationDesc = (TextView) a.findViewById(R.id.notificationDesc);
                        final ImageView recipePic = (ImageView) a.findViewById(R.id.foodPic);
                        final ImageButton bookmark = (ImageButton) a.findViewById(R.id.bookmarkButton);
                        avgStarsDisp = a.findViewById(R.id.avg_stars_disp);

                        float avgS = 0;
                        if(documentSnapshot.getString("total") != null && documentSnapshot.getString("total") != "") {
                            avgS = Float.parseFloat(documentSnapshot.getString("total")) / Integer.parseInt(documentSnapshot.getString("number"));
                        }
                        avgStarsDisp.setRating(avgS);

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
                        if((String) data.get("description") == null || (String) data.get("description") == "")
                        {
                            recipeDesc.setText("No description available");
                        }
                        else
                        {
                            recipeDesc.setText((String) data.get("description"));
                        }

                        String profileP = profileUrl;
                        if(profileP != null)
                        {
                            storageReference.child(profileP).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(c)
                                            .load(uri.toString())
                                            .into(profilePic);
                                }
                            });
                        }

                        //checks if its recipe or bookmark
                        if (isBookmark)
                        {
                            if(userID == null || userID == "")
                            {
                                recipePoster.setText("Unknown user");
                            }
                            else
                            {
                                DocumentReference docRef = db.collection("users").document((String) data.get("userId"));
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists())
                                            {
                                                User user = document.toObject(User.class);
                                                String poster = user.getUsername();
                                                String posterString = "by " + poster;
                                                recipePoster.setText(posterString);
                                            }
                                        }
                                    }
                                });
                            }
                            String ndesc = userID + " bookmarked this recipe";
                            notificationDesc.setText(ndesc);
                        }
                        else {
                            if(userID == null || userID == "")
                            {
                                recipePoster.setText("Unknown user");
                            }
                            else
                            {
                                String posterString = "by " + userID;
                                recipePoster.setText(posterString);
                            }
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
                        if(origbookmarks.containsKey(documentSnapshot.getId()))
                        {
                            bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                        }
                        else
                        {
                            bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                        }
                        bookmark.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                final DocumentReference docRef = db.collection("users").document(fbAuth.getCurrentUser().getUid());
                                final DocumentReference recipeDocRef = db.collection("recipes").document(recipeID);

                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            if(origbookmarks.containsKey(documentSnapshot.getId()))
                                            {
                                                origbookmarks.remove(documentSnapshot.getId());
                                                docRef.update("bookmarkedRecipes",origbookmarks);
                                                bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                                                Toast.makeText(c,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();

                                                recipeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        Map<String, Object> recipeData = documentSnapshot.getData();
                                                        Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
                                                        bookmarkedUsers.put(fbAuth.getCurrentUser().getUid(), true);
                                                        recipeDocRef.update("bookmarkedUsers", bookmarkedUsers);
                                                    }
                                                });
                                            }
                                            else {
                                                origbookmarks.put(documentSnapshot.getId(), new Date().getTime());
                                                docRef.update("bookmarkedRecipes", origbookmarks);
                                                bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                                                Toast.makeText(c, "Recipe has been bookmarked!", Toast.LENGTH_SHORT).show();

                                                recipeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        Map<String, Object> recipeData = documentSnapshot.getData();
                                                        Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
                                                        bookmarkedUsers.remove(fbAuth.getCurrentUser().getUid());
                                                        recipeDocRef.update("bookmarkedUsers", bookmarkedUsers);
                                                    }
                                                });
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
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