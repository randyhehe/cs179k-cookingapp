package com.example.randyhe.cookpad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Asus on 3/6/2018.
 */

public class feedRecipeAdapter extends RecyclerView.Adapter<feedRecipeAdapter.ViewHolder>
{
    private List<FeedIndividualRecipe> mDataset;
    private Context context;
    private boolean viewOrManage;
    final private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    final private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();
    Map<String, Long> origbookmarks;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public feedRecipeAdapter(List<FeedIndividualRecipe> myDataset, Context c, boolean viewOrManage) {
        mDataset = myDataset;
        context = c;

        //fbStorage = FirebaseStorage.getInstance();
        //storageReference = fbStorage.getReference();
        this.viewOrManage = viewOrManage;
    }

    @Override
    public feedRecipeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recipe, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(feedRecipeAdapter.ViewHolder holder, int position)
    {
        final View a = holder.view;
        final ImageView profilePic = (ImageView) a.findViewById(R.id.profile);
        final TextView recipeName = (TextView) a.findViewById(R.id.recipeTitle);
        final TextView recipeDesc = (TextView) a.findViewById(R.id.recipeDesc);
        final TextView recipePoster = (TextView) a.findViewById(R.id.recipePoster);
        final TextView notificationDesc = (TextView) a.findViewById(R.id.notificationDesc);
        final ImageView recipePic = (ImageView) a.findViewById(R.id.foodPic);
        final ImageButton bookmark = (ImageButton) a.findViewById(R.id.bookmarkButton);
        final RatingBar avgStarsDisp = a.findViewById(R.id.avg_stars_disp);
        final FeedIndividualRecipe ir = mDataset.get(position);


        DocumentReference dr = db.collection("recipes").document(ir.recipeID);
        dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData(); //inflate recipe

                    String profileP = ir.profileUrl;
                    if(profileP != null)
                    {
                        storageReference.child(profileP).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context)
                                        .load(uri.toString())
                                        .into(profilePic);
                            }
                        });
                    }

                    if((String) data.get("title") == null || (String) data.get("title") == "")
                    {
                        recipeName.setText("No title");
                    }
                    else
                    {
                        recipeName.setText((String)data.get("title"));
                    }
                    recipeName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, Individual_Recipe.class);
                            intent.putExtra("ID", ir.recipeID);
                            context.startActivity(intent);
                        }
                    });

                    //
                    // recipeDesc.setText(Long.toString(ir.comparatorVal));
                    if((String) data.get("description") == null || (String) data.get("description") == "")
                    {
                        recipeDesc.setText("No description available.");
                    }
                    else
                    {
                        recipeDesc.setText((String) data.get("description"));
                    }


                    if (ir.isBookmark)
                    {
                        if(ir.userID == null || ir.userID == "")
                        {
                            recipePoster.setText("Unknown user");
                        }
                        else
                        {
                            DocumentReference docRef = db.collection("users").document((String)data.get("userId"));
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if(document.exists())
                                        {
                                            User user = document.toObject(User.class);
                                            String poster = user.getUsername();
                                            recipePoster.setText("by " + poster);
                                        }
                                    }
                                }
                            });
                            //recipePoster.setText("by Saddam");
                        }
                        notificationDesc.setText(ir.userID + " bookmarked this recipe");
                    }
                    else
                    {
                        if (ir.userID == null || ir.userID == "")
                        {
                            recipePoster.setText("Unknown user");
                        } else
                        {
                            recipePoster.setText("by " + ir.userID);
                        }
                        notificationDesc.setText(ir.userID + " shared this recipe");
                    }

                    if((String) documentSnapshot.getString("mainPhotoStoragePath") == null || (String) documentSnapshot.getString("mainPhotoStoragePath") == "")
                    {
                    }
                    else
                    {
                        storageReference.child((String) documentSnapshot.getString("mainPhotoStoragePath")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context)
                                        .load(uri.toString())
                                        .into(recipePic);
                            }
                        });
                    }
                    recipePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, Individual_Recipe.class);
                            intent.putExtra("ID", ir.recipeID);
                            context.startActivity(intent);
                        }
                    });

                    if(documentSnapshot.getString("total") != null && documentSnapshot.getString("total") != "") {
                        avgStarsDisp.setRating(Float.parseFloat(documentSnapshot.getString("total")) / Integer.parseInt(documentSnapshot.getString("number")));
                    }

                    final DocumentReference docRef = db.collection("users").document(fbAuth.getCurrentUser().getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            User user = document.toObject(User.class);
                            origbookmarks = user.getBookmarkedRecipes();
                            if(origbookmarks.containsKey(documentSnapshot.getId()))
                            {
                                bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                            }
                            else
                            {
                                bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                            }
                        }
                    });

                    bookmark.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            final DocumentReference docRef = db.collection("users").document(fbAuth.getCurrentUser().getUid());
                            final DocumentReference recipeDocRef = db.collection("recipes").document(ir.recipeID);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        User user = document.toObject(User.class);
                                        origbookmarks = user.getBookmarkedRecipes();
                                        if(origbookmarks.containsKey(ir.recipeID))
                                        {
                                            origbookmarks.remove(ir.recipeID);
                                            docRef.update("bookmarkedRecipes",origbookmarks);
                                            bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                                            Toast.makeText(context,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();

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
                                        else {
                                            origbookmarks.put(ir.recipeID, new Date().getTime());
                                            docRef.update("bookmarkedRecipes", origbookmarks);
                                            bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                                            Toast.makeText(context, "Recipe has been bookmarked!", Toast.LENGTH_SHORT).show();

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
                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        /////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////// version where most work is done in feed fragment

//        String profileP = ir.profileUrl;
//        if(profileP != null)
//        {
//            storageReference.child(profileP).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    Glide.with(context)
//                            .load(uri.toString())
//                            .into(profilePic);
//                }
//            });
//        }
//
//        recipeName.setText(ir.recipeName);
//        recipeName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, Individual_Recipe.class);
//                intent.putExtra("ID", ir.recipeID);
//                context.startActivity(intent);
//            }
//        });
//
//        recipeDesc.setText(Long.toString(ir.comparatorVal));
//
//        if (ir.isBookmark)
//        {
//            if(ir.recipePoster == null || ir.recipePoster == "")
//            {
//                recipePoster.setText("Unknown user");
//            }
//            else
//            {
//                recipePoster.setText(ir.recipePoster);
//            }
//            notificationDesc.setText(ir.userID + " bookmarked this recipe");
//        }
//        else
//        {
//            if (ir.userID == null || ir.userID == "")
//            {
//                recipePoster.setText("Unknown user");
//            } else
//            {
//                recipePoster.setText("by " + ir.userID);
//            }
//            notificationDesc.setText(ir.userID + " shared this recipe");
//        }
//
//
////        if(ir.recipeUrl == null || ir.recipeUrl == "")
////        {
////        }
////        else
////        {
////            storageReference.child(ir.recipeUrl).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                @Override
////                public void onSuccess(Uri uri) {
////                    Glide.with(context)
////                            .load(uri.toString())
////                            .into(recipePic);
////                }
////            });
////        }
//
//        if(ir.beenBookmarked)
//        {
//            bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
//        }
//        else
//        {
//            bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
//        }
//
//        bookmark.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                final DocumentReference docRef = db.collection("users").document(fbAuth.getCurrentUser().getUid());
//                final DocumentReference recipeDocRef = db.collection("recipes").document(ir.recipeID);
//                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if(task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            User user = document.toObject(User.class);
//                            origbookmarks = user.getBookmarkedRecipes();
//                            if(origbookmarks.containsKey(ir.recipeID))
//                            {
//                                origbookmarks.remove(ir.recipeID);
//                                docRef.update("bookmarkedRecipes",origbookmarks);
//                                bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
//                                Toast.makeText(context,"Recipe has been unbookmarked.",Toast.LENGTH_SHORT).show();
//
//                                recipeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                        Map<String, Object> recipeData = documentSnapshot.getData();
//                                        Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
//                                        bookmarkedUsers.remove(fbAuth.getCurrentUser().getUid());
//                                        recipeDocRef.update("bookmarkedUsers", bookmarkedUsers);
//                                    }
//                                });
//                            }
//                            else {
//                                origbookmarks.put(ir.recipeID, new Date().getTime());
//                                docRef.update("bookmarkedRecipes", origbookmarks);
//                                bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
//                                Toast.makeText(context, "Recipe has been bookmarked!", Toast.LENGTH_SHORT).show();
//
//                                recipeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                        Map<String, Object> recipeData = documentSnapshot.getData();
//                                        Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
//                                        bookmarkedUsers.put(fbAuth.getCurrentUser().getUid(), true);
//                                        recipeDocRef.update("bookmarkedUsers", bookmarkedUsers);
//                                    }
//                                });
//                            }
//                        } else {
//                            Log.d(TAG, "get failed with ", task.getException());
//                        }
//                    }
//                });
//            }
//        });
//
//        avgStarsDisp.setRating(ir.rating);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
