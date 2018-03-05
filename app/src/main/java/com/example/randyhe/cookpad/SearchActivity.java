package com.example.randyhe.cookpad;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private FirebaseAuth fbAuth;
    private FirebaseFirestore fbFirestore;
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTitleBar();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        FirebaseApp.initializeApp(this);
        fbFirestore = FirebaseFirestore.getInstance();
        fbAuth = FirebaseAuth.getInstance();
        fbStorage = FirebaseStorage.getInstance();
        storageReference = fbStorage.getReference();

        populateData();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id =  item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    private void setupTitleBar() {
        final ActionBar actionBar = getSupportActionBar();
        // titlebar background color
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        // back arrow color
        actionBar.setDisplayHomeAsUpEnabled(true);
        final Drawable arrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        arrow.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(arrow);

        // title text and color
        String searchString = getIntent().getExtras().getString("searchString");
        SpannableString s = new SpannableString("Search: " + searchString);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
    }

    private void populateData() {
        final List<String> ids = getIntent().getExtras().getStringArrayList("ids");
        if (ids.size() < 1) {
            // show that none could be found
        } else {
            final LinearLayout feed = findViewById(R.id.searchRecipeFeed);
            for (int i = 0; i < ids.size(); i++) {
                final View currView = getLayoutInflater().inflate(R.layout.layout_profile_recipebutton, null);

                final DocumentReference doc1 = fbFirestore.collection("recipes").document(ids.get(i));
                doc1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot recipeDoc) {
                        final DocumentReference doc2 = fbFirestore.collection("users").document(recipeDoc.getString("userId"));
                        doc2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot userDoc) {
                                final CircleImageView userImage = currView.findViewById(R.id.userPic);
                                final TextView username = currView.findViewById(R.id.username);
                                final TextView recipeName = currView.findViewById(R.id.recipeName);
                                final TextView recipeTime = currView.findViewById(R.id.recipeTime);
                                final TextView recipeServings = currView.findViewById(R.id.recipeServings);
                                final TextView recipeBio = currView.findViewById(R.id.recipeBio);
                                final ImageView recipePic  = currView.findViewById(R.id.imageView);


                                StorageReference ref = storageReference.child(recipeDoc.getString("mainPhotoStoragePath"));
                                Glide.with(SearchActivity.this)
                                        .using(new FirebaseImageLoader())
                                        .load(ref)
                                        .into(recipePic);

                                userImage.setImageResource(R.drawable.kermit_cooking);
                                username.setText(userDoc.getString("username"));
                                recipeName.setText(recipeDoc.getString("title"));
                                recipeBio.setText(recipeDoc.getString("description"));
                                recipeTime.setText(recipeDoc.getString("time"));
                                recipeServings.setText(recipeDoc.getString("servings"));
                                feed.addView(currView);

                                currView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(SearchActivity.this, Individual_Recipe.class);
                                        intent.putExtra("ID", recipeDoc.getId());
                                        startActivity(intent);
                                        // view the recipe
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }
}
