package com.example.randyhe.cookpad;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;


import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by Monica on 1/28/18.
 */


public class Individual_Recipe extends AppCompatActivity {

    private static final String TAG = "Individual_Recipe";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = auth.getCurrentUser();

    final Context c = this;

    private View indiv_rec;


    private TextView mainTitle;
    private TextView mainDescription;
    private TextView mainName;

    private TextView ingredientsTitle;
    private TextView numFeeds;
    private TextView methodTitle;
    private TextView cookTime;
    private TextView addRevText;
    private TextView reviewTitle;

    private  RatingBar avgStarsDisp;


    //New Review
    private Button reviewBtn;
    private EditText etReview;
    private RatingBar starsInput;
    private String recipeFor;
    private String recipeUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_individual_recipe);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.indiv_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");


        setupViews();

        final ScrollView individual_recipe_layout = (ScrollView) findViewById(R.id.main_scroll);

        Button followButton = (Button) indiv_rec.findViewById(R.id.follow_button);
        followButton.setText("Follow");
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });

        setupSubmitReview();


        ingredientsTitle.setText("Ingredients");
        methodTitle.setText("Method");
        reviewTitle.setText("Reviews");
        addRevText.setText("Add Review");


        individual_recipe_layout.addView(indiv_rec);


        //
        final DocumentReference docRef = db.collection("recipes").document("bce3204e-573a-42a2-b2dc-92941a6dfe75");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()) {
                Log.d(TAG, "Document");
                final DocumentSnapshot document = task.getResult();

                String dbmainImage = document.getString("mainPhotoStoragePath");

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(dbmainImage);

                ImageView mainImage = (ImageView) indiv_rec.findViewById(R.id.main_image);

                Glide.with(c)
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .into(mainImage);


                mainTitle.setText(document.getString("title"));
                mainDescription.setText(document.getString("desciption"));
                recipeFor = document.getId();
                recipeUserID = document.getString("userId");

                numFeeds.setText(document.getString("servings"));
                cookTime.setText(document.getString("time"));


                float avgS = Float.parseFloat(document.getString("total")) / Integer.parseInt(document.getString("number"));
                avgStarsDisp.setRating(avgS);

                //DISPLAY INGREDIENTS LIST
                List<String> ingredsList = (ArrayList<String>)document.get("ingrs");

                View injectorLayout = getLayoutInflater().inflate(R.layout.ir_single_ingredient, null);

                LinearLayout ingredientsLayout = (LinearLayout) findViewById(R.id.ingredients);

                TextView ingredientText = (TextView) injectorLayout.findViewById(R.id.ingredient_text);


                String temp;
                StringBuilder sb = new StringBuilder();

                for(int i = 0; i < ingredsList.size(); i++) {
                    temp = ingredsList.get(i);
                    sb.append(temp);
                    if (i != ingredsList.size() - 1) {
                        sb.append("\n\n");
                    }
                }

                ingredientText.setText(sb.toString());
                ingredientsLayout.addView(injectorLayout);



                //DISPLAY METHOD
                List<Map<String, String>> methodList = (ArrayList<Map<String, String>>)document.get("methods");

                LinearLayout methodsLayout = (LinearLayout) findViewById(R.id.methods);

                for (int i = 0; i < methodList.size(); i++) {
                    View met = getLayoutInflater().inflate(R.layout.ir_single_method, null);

                    TextView stepNum = (TextView) met.findViewById(R.id.step_num);
                    TextView stepText = (TextView) met.findViewById(R.id.step_text);
                    ImageView stepPhoto = (ImageView) met.findViewById(R.id.step_photo);

                    final Map<String, String> methodMap = methodList.get(i);
                    String currT = methodMap.get("instruction");
                    stepNum.setText(Integer.toString(i+1));
                    stepText.setText(currT);

                    if(methodMap.get("storagePath") == null) {
                        stepPhoto.getLayoutParams().width = 0;
                    }

                    methodsLayout.addView(met);
                }






                // DISPLAY REVIEWS
                final Map<String, Boolean> reviewMap = (document.get("reviews") != null) ? (HashMap<String, Boolean>) document.get("reviews") : new HashMap<String, Boolean>();

                final LinearLayout reviewLayout = (LinearLayout) findViewById(R.id.reviews);
                for (final String key : reviewMap.keySet()) {

                    final View rev = getLayoutInflater().inflate(R.layout.ir_single_review, null);


                    DocumentReference docRef3 = db.collection("reviews").document(key);
                    docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, key);

                                DocumentSnapshot document3 = task.getResult();

                                RatingBar revStarsDisp = (RatingBar) rev.findViewById(R.id.rev_stars_disp);

                                final ImageView reviewAvatar = (ImageView) rev.findViewById(R.id.review_avatar);
                                final TextView reviewName = (TextView) rev.findViewById(R.id.review_name);
                                TextView reviewText = (TextView) rev.findViewById(R.id.review_text);
                                TextView reviewDateText = (TextView) rev.findViewById(R.id.review_date_text);


                                reviewAvatar.setImageResource(R.drawable.kermit_cooking);

                                reviewText.setText(document3.getString("text"));
                                reviewDateText.setText(document3.getString("date"));
                                revStarsDisp.setRating(Float.parseFloat(document3.getString("stars")));


                                // GET REVIEWER USERNAME AND AVATAR
                                final DocumentReference docRefUser = db.collection("users").document("cC2zis0WonNTANJjODvSINuSpZr1");
                                docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            Log.d(TAG, "Getting reviewer username");
                                            DocumentSnapshot documentUser = task.getResult();
                                            reviewName.setText(documentUser.getString("username"));
                                        }
                                        else {
                                            Log.d(TAG, "fail");
                                        }
                                    }
                                });
                                reviewLayout.addView(rev);
                            }
                        }
                    });
                }
            }
            else {
                Log.d(TAG, "fail");
            }
            }
        });

        // GET USERNAME AND AVATAR
        final DocumentReference docRef2 = db.collection("users").document("cC2zis0WonNTANJjODvSINuSpZr1");
        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Getting username");
                    DocumentSnapshot document2 = task.getResult();
                    mainName.setText(document2.getString("username"));


                    ImageView mainAvatar = (ImageView) indiv_rec.findViewById(R.id.avatar);
                    mainAvatar.setImageResource(R.drawable.kermit_cooking);
                    /*

                    String dbmainAvatar = document2.getString("mainPhotoStoragePath");

                    StorageReference avatarStorageReference = FirebaseStorage.getInstance().getReference().child(dbmainAvatar);

                    Glide.with(c)
                            .using(new FirebaseImageLoader())
                            .load(avatarStorageReference)
                            .into(mainAvatar);
                    */
                }
                else {
                    Log.d(TAG, "fail");
                }
            }
        });

    }

    private void setupSubmitReview() {
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //Store the date
            Date d = Calendar.getInstance().getTime();

            SimpleDateFormat f = new SimpleDateFormat("MM-dd-yyyy");
            String formattedDate = f.format(d);


            final Float stars = starsInput.getRating();

            final UUID reviewId = UUID.randomUUID();

            //ADD REVIEW
            Map<String, Object> newReview = new HashMap<>();
            newReview.put("author", currentUser.getUid().toString());
            newReview.put("stars", Float.toString(stars));
            newReview.put("text", etReview.getText().toString());
            newReview.put("recipeID", recipeFor);
            newReview.put("date", formattedDate);
            db.collection("reviews").document(reviewId.toString())
                    .set(newReview)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Success in review addition");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Failure in review addition");
                        }
                    });


            //Add review to recipe review set
            final DocumentReference recipeDoc = db.collection("recipes").document("bce3204e-573a-42a2-b2dc-92941a6dfe75");
            final OnCompleteListener<DocumentSnapshot> storeReviewId = new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        final DocumentSnapshot document = task.getResult();
                        final Map<String, Object> docData = document.getData();
                        final Map<String, Boolean> revs = (docData.get("reviews") != null) ? (HashMap<String, Boolean>) docData.get("reviews") : new HashMap<String, Boolean>();
                        revs.put(reviewId.toString(), true);
                        recipeDoc.update("reviews", revs);

                        float tot = Float.parseFloat(document.getString("total")) + stars;
                        int num = Integer.parseInt(document.getString("number"));
                        recipeDoc.update("total", Float.toString(tot));
                        recipeDoc.update("number", Integer.toString(num + 1));
                    }
                }
            };
            recipeDoc.get().addOnCompleteListener(storeReviewId);


            Toast.makeText(c,"Review Added!",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupViews() {
        indiv_rec = getLayoutInflater().inflate(R.layout.individual_recipe_main, null);

        mainTitle = (TextView) indiv_rec.findViewById(R.id.main_title);
        mainDescription = (TextView) indiv_rec.findViewById(R.id.main_description);
        mainName = (TextView) indiv_rec.findViewById(R.id.main_name);

        ingredientsTitle = (TextView) indiv_rec.findViewById(R.id.ingredients_title);
        numFeeds = (TextView) indiv_rec.findViewById(R.id.num_feeds);
        methodTitle = (TextView) indiv_rec.findViewById(R.id.method_title);
        cookTime = (TextView) indiv_rec.findViewById(R.id.cook_time);
        addRevText = (TextView) indiv_rec.findViewById(R.id.add_rev_text);
        reviewTitle = (TextView) indiv_rec.findViewById(R.id.review_title);

        avgStarsDisp = (RatingBar) indiv_rec.findViewById(R.id.avg_stars_disp);

        // New Review
        reviewBtn = (Button) indiv_rec.findViewById(R.id.review_button);
        etReview = indiv_rec.findViewById(R.id.review_text_input);
        starsInput = (RatingBar) indiv_rec.findViewById(R.id.stars_input);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        return true;
    }



    //Bookmarking and liking
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.like:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_favorite_black_24dp).getConstantState())) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_black_24dp));
                    Toast.makeText(c,"Recipe has been unliked!",Toast.LENGTH_SHORT).show();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_black_24dp));
                    Toast.makeText(c,"Recipe has been liked!",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.bookmark:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp).getConstantState())) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_border_black_24dp));
                    Toast.makeText(c,"Recipe has been unbookmarked!",Toast.LENGTH_SHORT).show();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_black_24dp));
                    Toast.makeText(c,"Recipe has been bookmarked!",Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

