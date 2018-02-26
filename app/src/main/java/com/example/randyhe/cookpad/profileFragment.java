package com.example.randyhe.cookpad;

/**
 * Created by Asus on 1/30/2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
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

public class profileFragment extends Fragment {

    // publish and uploading
    private static final String TAG = "ProfileFragment";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();
    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();


    private TextView tvProfileName;
    private TextView tvProfileBio;
    private CircleImageView profileImg;
    private TextView tvNumRecipes;
    private TextView tvNumFollowers;
    private TextView tvNumFollowing;
    private List<String> recipeList;


    public static profileFragment newInstance() {
        profileFragment fragment = new profileFragment();
        return fragment;
    }

    private void setupViews(View view) {
        tvProfileName = (TextView) view.findViewById(R.id.profileName);
        tvProfileBio = (TextView) view.findViewById(R.id.profileBio);
        tvNumRecipes = (TextView) view.findViewById(R.id.textViewRecipes);
        tvNumFollowers = (TextView) view.findViewById(R.id.textViewFollowers);
        tvNumFollowing = (TextView) view.findViewById(R.id.textViewFollowing);
        profileImg = (CircleImageView) view.findViewById(R.id.profileImg);

        // Removes Follow button
        ConstraintLayout profileTop = (ConstraintLayout) view.findViewById(R.id.constraintLayout1);

        Button followBtn = (Button) view.findViewById(R.id.follow);
        followBtn.setVisibility(View.GONE);
        profileTop.removeView(followBtn);
    }

    public void setupEditProfileBtn(View view) {
        //Sets up edit profile button
        //TO-DO: Deflate if not logged in user
        final TextView editProfile = (TextView) view.findViewById(R.id.editProfileButton);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    public void setupTopInfo(View view) {
        tvNumFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                intent.putExtra("ID", currentFirebaseUser.getUid());
                intent.putExtra("Followers", true);
                startActivity(intent);
            }
        });

        tvNumFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                intent.putExtra("ID", currentFirebaseUser.getUid());
                intent.putExtra("Followers", false);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void getData() {
        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    List<String> recipes = user.getRecipes();
                    tvProfileName.setText(user.getUsername());
                    tvNumRecipes.setText(Integer.toString(recipes.size()));
                    tvProfileBio.setText(user.getBio());

                    if (user.getProfilePhotoPath() != null && !user.getProfilePhotoPath().equals("")) {
                        Glide.with(getActivity())
                                .using(new FirebaseImageLoader())
                                .load(storageReference.child(user.getProfilePhotoPath()))
                                .into(profileImg);
                    }

                    tvNumFollowers.setText(Integer.toString(user.getNumFollowers()));
                    tvNumFollowing.setText(Integer.toString(user.getNumFollowing()));
                    loadRecipeList(recipes, user.getUsername());

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_center_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        setupViews(view);
        setupEditProfileBtn(view);
        setupTopInfo(view);
        getData();

    }

    private void loadRecipeList(final List<String> rList, final String user) {
        final LinearLayout feed = (LinearLayout) getView().findViewById(R.id.profileRecipeFeed);

        for(int i = 0; i < rList.size(); i++) {
            final View a = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);

            DocumentReference docRef = db.collection("recipes").document(rList.get(i));
            final String t = rList.get(i);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
//                        Log.d(TAG, t);
//                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                        DocumentSnapshot document = task.getResult();
//                        Recipe r = document.toObject(Recipe.class);
                        CircleImageView userPic = (CircleImageView) a.findViewById(R.id.userPic);
                        TextView username = (TextView) a.findViewById(R.id.username);
                        TextView recipeName = (TextView) a.findViewById(R.id.recipeName);
                        TextView recipeTime = (TextView) a.findViewById(R.id.recipeTime);
                        TextView recipeServings = (TextView) a.findViewById(R.id.recipeServings);
                        TextView recipeBio = (TextView) a.findViewById(R.id.recipeBio);
                        ImageView recipePic = (ImageView) a.findViewById(R.id.imageView);

                        if (document.getString("mainPhotoStoragePath") != null) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(document.getString("mainPhotoStoragePath"));

                            Glide.with(getActivity() /* context */)
                                    .using(new FirebaseImageLoader())
                                    .load(storageReference)
                                    .into(recipePic);
                        }

                        userPic.setImageResource(R.drawable.kermit_cooking);
                        username.setText(user);
                        recipeName.setText(document.getString("title"));
                        recipeBio.setText(document.getString("description"));
                        feed.addView(a);

                        //  TO-DO: Open Edit recipe
                        a.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ManageRecipe.class);
                                intent.putExtra("EDIT", true);
                                intent.putExtra("ID", t);
                                startActivity(intent);
                            }
                        });
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
    }
}