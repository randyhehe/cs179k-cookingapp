package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class bookmarkFragment extends Fragment {

    private static final String TAG = "bookmarkFragment";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = auth.getCurrentUser();

    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();

    public static bookmarkFragment newInstance() {
        bookmarkFragment fragment = new bookmarkFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bookmarkfeed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        final LinearLayout feed = (LinearLayout) getView().findViewById(R.id.bookmarkFeed);

        final DocumentReference dRef = db.collection("users").document(currentUser.getUid().toString());
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Checking bookmarks");
                    DocumentSnapshot userDocument = task.getResult();
                    final User user = userDocument.toObject(User.class);
                    final List<String> bookmarks = user.getBookmarkedRecipes();

                    for (int i = 0; i < bookmarks.size(); i++) {
                        final View a = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);

                        final String currRecipeId = bookmarks.get(i).toString();
                        final ImageView recipeImage = (ImageView) a.findViewById(R.id.imageView);
                        final CircleImageView userPic = (CircleImageView) a.findViewById(R.id.userPic);
                        final TextView userName = (TextView) a.findViewById(R.id.username);
                        final TextView recipeName = (TextView) a.findViewById(R.id.recipeName);
                        final TextView recipeTime = (TextView) a.findViewById(R.id.recipeTime);
                        final TextView recipeServings = (TextView) a.findViewById(R.id.recipeServings);
                        final TextView recipeBio = (TextView) a.findViewById(R.id.recipeBio);

                        //GET RECIPE INFO
                        final DocumentReference dRefB = db.collection("recipes").document(currRecipeId);
                        dRefB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "Checking recipe for bookmark fragment");
                                    DocumentSnapshot recipeDocument = task.getResult();

                                    recipeName.setText(recipeDocument.getString("title"));
                                    recipeTime.setText(recipeDocument.getString("time"));
                                    recipeServings.setText(recipeDocument.getString("servings"));
                                    recipeBio.setText(recipeDocument.getString("description"));

                                    storageReference.child(recipeDocument.getString("mainPhotoStoragePath")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(final Uri uri) {
                                            Glide.with(bookmarkFragment.this)
                                                    .load(uri.toString())
                                                    .into(recipeImage);
                                        }
                                    });

                                    // GET RECIPE USER INFO
                                    final DocumentReference dRefU = db.collection("users").document(recipeDocument.getString("userId").toString());
                                    dRefU.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()) {
                                                Log.d(TAG, "Checking recipe user for bookmark fragment");
                                                DocumentSnapshot ruserDocument = task.getResult();

                                                final String path = (String) ruserDocument.get("profilePhotoPath");

                                                if(path != null) {
                                                    storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(final Uri uri) {
                                                            Glide.with(bookmarkFragment.this)
                                                                    .load(uri.toString())
                                                                    .into(userPic);
                                                        }
                                                    });
                                                }
                                                else {
                                                    userPic.setImageResource(R.drawable.kermit_cooking);
                                                }
                                                userName.setText(ruserDocument.getString("username"));
                                            }
                                            else {
                                                Log.d(TAG, "fail");
                                            }
                                        }
                                    });
                                }
                                else {
                                    Log.d(TAG, "fail");
                                }
                            }
                        });
                        a.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), Individual_Recipe.class);
                                intent.putExtra("ID", currRecipeId);
                                startActivity(intent);
                            }
                        });
                        feed.addView(a);
                    }
                }
                else {
                    Log.d(TAG, "fail");
                }
            }
        });

        View b = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);
    }

}