package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;

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
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.widget.ImageButton;


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

    private String individualRecipeID;

    private ImageView mainImage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_recipe);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.indiv_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");

        //individualRecipeID = getIntent().getExtras().getString("ID");
        individualRecipeID = "351844ed-af0a-4861-b4e2-aff7fb7d3b93";

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




        ingredientsTitle.setText("Ingredients");
        methodTitle.setText("Method");
        reviewTitle.setText("Reviews");
        addRevText.setText("Add Review");





        //
        final DocumentReference docRef = db.collection("recipes").document(individualRecipeID.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Document");
                    final DocumentSnapshot document = task.getResult();


                    // DISPLAY MAIN INFO

                    if(document.getString("imagePath") == null || document.getString("imagePath") == "")
                    {
                        mainImage.setImageResource(R.drawable.kermit_cooking);
                    }
                    else
                    {
                        Glide.with(c)
                                .load(document.getString("imagePath"))
                                .into(mainImage);
                    }



                    mainTitle.setText(document.getString("title"));

                    if(document.getString("desciption") == null || document.getString("desciption") == "")
                    {
                        mainDescription.setText("No desc");
                    }
                    else
                    {
                        mainDescription.setText(document.getString("desciption"));
                    }

                    recipeFor = document.getId();
                    recipeUserID = document.getString("userId");

                    numFeeds.setText(document.getString("servings"));
                    cookTime.setText(document.getString("time"));


                    float avgS = 0;
                    if(document.getString("total") != null && document.getString("total") != "") {
                        avgS = Float.parseFloat(document.getString("total")) / Integer.parseInt(document.getString("number"));
                    }
                    avgStarsDisp.setRating(avgS);

                    setupReviewAddPhotos();
                    setupSubmitReview();

                    //DISPLAY INGREDIENTS LIST
                    List<String> ingredsList = (ArrayList<String>)document.get("ingrs");

                    View injectorLayout = getLayoutInflater().inflate(R.layout.ir_single_ingredient, null);

                    LinearLayout ingredientsLayout = (LinearLayout) findViewById(R.id.ingredients);

                    TextView ingredientText = (TextView) injectorLayout.findViewById(R.id.ingredient_text);


                    String temp;
                    StringBuilder sb = new StringBuilder();

                    if(ingredsList.size() == 0) {
                        sb.append("No ingredients\n");
                    }
                    else if (ingredsList.size() == 1 && ingredsList.get(0).equals("")) {
                        sb.append("No ingredients\n");
                    }
                    else {
                        for (int i = 0; i < ingredsList.size(); i++) {
                            temp = ingredsList.get(i);
                            sb.append(temp);
                            if (i != ingredsList.size() - 1) {
                                sb.append("\n\n");
                            }
                        }
                    }

                    ingredientText.setText(sb.toString());
                    ingredientsLayout.addView(injectorLayout);



                    //DISPLAY METHOD
                    List<HashMap<String, Object>> methodList = (ArrayList<HashMap<String, Object>>) document.get("methods");

                    LinearLayout methodLayout = (LinearLayout) findViewById(R.id.methods);
                    for(int i = 0; i < methodList.size(); i++){
                        LayoutInflater li = LayoutInflater.from(Individual_Recipe.this);
                        View met = li.inflate(R.layout.ir_single_method, null);

                        final LinearLayout methodsLayout = (LinearLayout) findViewById(R.id.methods);

                        final TextView stepNum = (TextView) met.findViewById(R.id.step_num);
                        final TextView stepText = (TextView) met.findViewById(R.id.step_text);
                        final ImageButton stepPhoto = (ImageButton) met.findViewById(R.id.step_photo);

                        stepNum.setText(Integer.toString(i + 1));
                        stepText.setText((String) methodList.get(i).get("instruction"));

                        if(methodList.get(i).get("storagePath") == null || methodList.get(i).get("storagePath") == "") {
                            stepPhoto.setVisibility(View.INVISIBLE);
                            stepPhoto.getLayoutParams().width = 0;
                            stepPhoto.getLayoutParams().height = 0;
                        }
                        else {
                            Glide.with(c)
                                    .load(methodList.get(i).get("storagePath"))
                                    .into(stepPhoto);

                            stepPhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //expandPhoto(stepPhoto);
                                }
                            });
                        }

                        methodLayout.addView(met);
                    }



                    // DISPLAY REVIEWS
                    final Map<String, Boolean> reviewMap = (document.get("reviews") != null) ? (HashMap<String, Boolean>) document.get("reviews") : new HashMap<String, Boolean>();

                    final LinearLayout reviewLayout = (LinearLayout) findViewById(R.id.reviews);
                    for (final String key : reviewMap.keySet()) {



                        final DocumentReference docRef3 = db.collection("reviews").document(key);
                        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, key);
                                    final View rev = getLayoutInflater().inflate(R.layout.ir_single_review, null);

                                    DocumentSnapshot document3 = task.getResult();

                                    RatingBar revStarsDisp = (RatingBar) rev.findViewById(R.id.rev_stars_disp);

                                    final ImageView reviewAvatar = (ImageView) rev.findViewById(R.id.review_avatar);
                                    final TextView reviewName = (TextView) rev.findViewById(R.id.review_name);
                                    //final RelativeLayout reviewDesc = (RelativeLayout) rev.findViewById(R.id.review_desc);
                                    final TextView reviewText = (TextView) rev.findViewById(R.id.review_text);
                                    final TextView reviewDateText = (TextView) rev.findViewById(R.id.review_date_text);
                                    final LinearLayout reviewImages = (LinearLayout) rev.findViewById(R.id.review_images);
                                    final ImageButton imageOne = (ImageButton) rev.findViewById(R.id.image1);
                                    final ImageButton imageTwo = (ImageButton) rev.findViewById(R.id.image2);
                                    final ImageButton imageThree = (ImageButton) rev.findViewById(R.id.image3);

                                    //version 2
                                    final View rev2 = getLayoutInflater().inflate(R.layout.ir_single_review2, null);

                                    RatingBar revStarsDisp2 = (RatingBar) rev2.findViewById(R.id.rev_stars_disp2);

                                    final ImageView reviewAvatar2 = (ImageView) rev2.findViewById(R.id.review_avatar2);
                                    final TextView reviewName2 = (TextView) rev2.findViewById(R.id.review_name2);
                                    //final RelativeLayout reviewDesc2 = (RelativeLayout) rev2.findViewById(R.id.review_desc2);
                                    final TextView reviewText2 = (TextView) rev2.findViewById(R.id.review_text2);
                                    final TextView reviewDateText2 = (TextView) rev2.findViewById(R.id.review_date_text2);




                                    // TODO Display review images


                                    if(document3.getString("photo1") == null && document3.getString("photo2") == null && document3.getString("photo3") == null) {
                                        reviewAvatar2.setImageResource(R.drawable.kermit_cooking);

                                        reviewText2.setText(document3.getString("text"));
                                        reviewDateText2.setText(document3.getString("date"));
                                        revStarsDisp2.setRating(Float.parseFloat(document3.getString("stars")));

                                        // GET REVIEWER USERNAME AND AVATAR
                                        String revAuthorID = document3.getString("author");
                                        final DocumentReference docRefUser = db.collection("users").document(revAuthorID.toString());
                                        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    Log.d(TAG, "Getting reviewer username");
                                                    DocumentSnapshot documentUser = task.getResult();
                                                    if(documentUser.exists()) {
                                                        reviewName2.setText(documentUser.getString("username"));

                                                        if(documentUser.getString("profilePhotoPath") == null || documentUser.getString("profilePhotoPath") == "")
                                                        {
                                                            reviewAvatar2.setImageResource(R.drawable.kermit_cooking);
                                                        }
                                                        else
                                                        {
                                                            Glide.with(c)
                                                                    .load(documentUser.getString("profilePhotoPath"))
                                                                    .into(reviewAvatar2);
                                                        }

                                                    }
                                                }
                                                else {
                                                    Log.d(TAG, "fail");
                                                }
                                            }
                                        });
                                        reviewLayout.addView(rev2);
                                    }
                                    else {
                                        reviewAvatar.setImageResource(R.drawable.kermit_cooking);

                                        reviewText.setText(document3.getString("text"));
                                        reviewDateText.setText(document3.getString("date"));
                                        revStarsDisp.setRating(Float.parseFloat(document3.getString("stars")));

                                        // GET REVIEWER USERNAME AND AVATAR
                                        String revAuthorID = document3.getString("author");
                                        final DocumentReference docRefUser = db.collection("users").document(revAuthorID.toString());
                                        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    Log.d(TAG, "Getting reviewer username");
                                                    DocumentSnapshot documentUser = task.getResult();
                                                    if(documentUser.exists()) {
                                                        reviewName.setText(documentUser.getString("username"));

                                                        if(documentUser.getString("profilePhotoPath") == null || documentUser.getString("profilePhotoPath") == "")
                                                        {
                                                            reviewAvatar.setImageResource(R.drawable.kermit_cooking);
                                                        }
                                                        else
                                                        {
                                                            Glide.with(c)
                                                                    .load(documentUser.getString("profilePhotoPath"))
                                                                    .into(reviewAvatar);
                                                        }
                                                    }
                                                }
                                                else {
                                                    Log.d(TAG, "fail");
                                                }
                                            }
                                        });
                                        displayRevImages(imageOne, imageTwo, imageThree, docRef3);
                                        reviewLayout.addView(rev);
                                    }


                                }
                            }
                        });
                    }


                    // GET RECIPE USERNAME AND AVATAR
                    final String recAuthorID = document.getString("userId");
                    final DocumentReference docRef2 = db.collection("users").document(recAuthorID.toString());
                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "Getting username");
                                DocumentSnapshot document2 = task.getResult();
                                mainName.setText(document2.getString("username"));


                                ImageView mainAvatar = (ImageView) indiv_rec.findViewById(R.id.avatar);
                                mainAvatar.setImageResource(R.drawable.kermit_cooking);

                                // TODO DISPLAY REVIEWER AVATAR PHOTO
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


        individual_recipe_layout.addView(indiv_rec);
    }

    private void displayRevImages(final ImageButton revImage1, final ImageButton revImage2, final ImageButton revImage3, final DocumentReference documentReference) {

        // Check that review images exists and set them
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Checking review images");
                    DocumentSnapshot userDocument = task.getResult();
                    if(userDocument.getString("photo1") != null && userDocument.getString("photo1") != "") {  // set photo1
                        Glide.with(c)
                                .load("http://via.placeholder.com/300.png")
                                .into(revImage1);
                    }
                    else {
                        revImage1.setVisibility(View.INVISIBLE);
                        revImage1.getLayoutParams().width = 0;
                        revImage1.getLayoutParams().height = 0;
                    }

                    if(userDocument.getString("photo2") != null && userDocument.getString("photo2") != "") {  // set photo2
                        Glide.with(c)
                                .load("http://via.placeholder.com/300.png")
                                .into(revImage2);
                    }
                    else {
                        revImage2.setVisibility(View.INVISIBLE);
                        revImage2.getLayoutParams().width = 0;
                        revImage2.getLayoutParams().height = 0;
                    }

                    if(userDocument.getString("photo3") != null && userDocument.getString("photo3") != "") {  // set photo3
                        Glide.with(c)
                                .load("http://via.placeholder.com/300.png")
                                .into(revImage3);
                    }
                    else {
                        revImage3.setVisibility(View.INVISIBLE);
                        revImage3.getLayoutParams().width = 0;
                        revImage3.getLayoutParams().height = 0;
                    }
                }
                else {
                    Log.d(TAG, "fail");
                }
            }
        });


        revImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandPhoto(revImage1);
            }
        });

        revImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandPhoto(revImage2);
            }
        });

        revImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandPhoto(revImage3);
            }
        });
    }


    private void setupReviewAddPhotos() {

        // ADD PHOTOS STUFF
        //Review Image Upload
        final ImageButton firstPhotoBtn;
        final LinearLayout firstPhotoOptions;
        final LinearLayout firstPhotoEdit;
        final ImageButton firstPhotoDelete;

        final ImageButton secondPhotoBtn;
        final LinearLayout secondPhotoOptions;
        final LinearLayout secondPhotoEdit;
        final ImageButton secondPhotoDelete;

        final ImageButton thirdPhotoBtn;
        final LinearLayout thirdPhotoOptions;
        final LinearLayout thirdPhotoEdit;
        final ImageButton thirdPhotoDelete;


        firstPhotoBtn = indiv_rec.findViewById(R.id.first_photo_btn);
        firstPhotoOptions = indiv_rec.findViewById(R.id.first_photo_options);
        firstPhotoEdit = indiv_rec.findViewById(R.id.first_photo_edit);
        firstPhotoDelete = indiv_rec.findViewById(R.id.first_photo_delete);

        secondPhotoBtn = indiv_rec.findViewById(R.id.second_photo_btn);
        secondPhotoOptions = indiv_rec.findViewById(R.id.second_photo_options);
        secondPhotoEdit = indiv_rec.findViewById(R.id.second_photo_edit);
        secondPhotoDelete = indiv_rec.findViewById(R.id.second_photo_delete);

        thirdPhotoBtn = indiv_rec.findViewById(R.id.third_photo_btn);
        thirdPhotoOptions = indiv_rec.findViewById(R.id.third_photo_options);
        thirdPhotoEdit = indiv_rec.findViewById(R.id.third_photo_edit);
        thirdPhotoDelete = indiv_rec.findViewById(R.id.third_photo_delete);

        firstPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto(firstPhotoBtn, firstPhotoOptions, firstPhotoEdit, firstPhotoDelete);
            }
        });

        secondPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto(secondPhotoBtn, secondPhotoOptions, secondPhotoEdit, secondPhotoDelete);
            }
        });

        thirdPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto(thirdPhotoBtn, thirdPhotoOptions, thirdPhotoEdit, thirdPhotoDelete);
            }
        });
    }

    private void choosePhoto(final ImageButton photoButton, final LinearLayout photoOptions, final LinearLayout photoEdit, final ImageButton photoDelete) {

        PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult pickResult) {
                if (pickResult.getError() == null) {
                    //
                    setImage(photoButton, photoOptions, photoEdit, photoDelete, pickResult.getUri(), null);
                } else {
                    Toast.makeText(getApplicationContext(), pickResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).show(getSupportFragmentManager());

    }


    private void setImage(final ImageButton photoBtn, final LinearLayout photoOptions, final LinearLayout photoEdit, final ImageButton photoDelete, Uri uri, String url) {
        if (uri == null && url == null || uri != null && url != null) {
            Log.d(TAG, "Either uri or url should be null.");
            return;
        } else if (uri != null) {
            photoBtn.setImageURI(uri);
            photoBtn.setTag(uri); // only set the uri tag if uploaded/took image from phone
            Toast.makeText(getApplicationContext(), uri.toString(), Toast.LENGTH_SHORT).show();

        } else { // use url instead

            StorageReference storageReferenceSI = FirebaseStorage.getInstance().getReference();
            Glide.with(Individual_Recipe.this)
                    .load(storageReferenceSI.child(url))
                    .into(photoBtn);
            Toast.makeText(getApplicationContext(), url.toString(), Toast.LENGTH_SHORT).show();

        }

        photoOptions.setVisibility(View.VISIBLE);
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandPhoto(photoBtn);
            }
        });

        photoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto(photoBtn, photoOptions, photoEdit, photoDelete);
                Toast.makeText(c,"Clicked edit photo!",Toast.LENGTH_SHORT).show();
            }
        });

        photoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage("Are you sure you want to delete this image?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int button) {
                                photoOptions.setVisibility(View.GONE);
                                photoBtn.setTag(null);
                                photoBtn.setImageResource(android.R.drawable.ic_menu_camera);
                                photoBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        choosePhoto(photoBtn, photoOptions, photoEdit, photoDelete);
                                    }
                                });
                            }
                        }).setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    private void displayRevPhoto(final ImageButton photoBtn, Uri uri, String url) {
        if (uri == null && url == null || uri != null && url != null) {
            Log.d(TAG, "Either uri or url should be null.");
            return;
        } else if (uri != null) {
            photoBtn.setImageURI(uri);
            photoBtn.setTag(uri); // only set the uri tag if uploaded/took image from phone
        } else { // use url instead

            StorageReference storageReferenceSI = FirebaseStorage.getInstance().getReference();
            Glide.with(Individual_Recipe.this)
                    .load(storageReferenceSI.child(url))
                    .into(photoBtn);
        }
    }

    private void displayMethodPhoto(final ImageButton photoBtn, Uri uri, String url) {
        if (uri == null && url == null || uri != null && url != null) {
            Log.d(TAG, "Either uri or url should be null.");
            return;
        } else if (uri != null) {
            photoBtn.setImageURI(uri);
            photoBtn.setTag(uri); // only set the uri tag if uploaded/took image from phone
        } else { // use url instead

            StorageReference storageReferenceSI = FirebaseStorage.getInstance().getReference();
            Glide.with(Individual_Recipe.this)
                    .load(storageReferenceSI.child(url))
                    .into(photoBtn);
        }
    }

    private void expandPhoto(final ImageButton photoButton) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("BitmapUri", (Uri) photoButton.getTag());
        startActivity(intent);
    }

    private void setupSubmitReview() {
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final DocumentReference userDocRef = db.collection("users").document(currentUser.getUid().toString());

                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "Checking likes and bookmarks");
                            DocumentSnapshot userDocumentMain = task.getResult();
                            final Map<String, Object> userDataMain = userDocumentMain.getData();
                            final Map<String, Boolean> reviewed = (userDataMain.get("reviewed") != null) ? (HashMap<String, Boolean>) userDataMain.get("reviewed") : new HashMap<String, Boolean>();

                            if(reviewed.containsKey(individualRecipeID.toString())) {
                                Toast.makeText(c,"You can't review the same recipe more than once!",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else {
                                //Store the date
                                Date d = Calendar.getInstance().getTime();

                                SimpleDateFormat f = new SimpleDateFormat("MM-dd-yyyy");
                                String formattedDate = f.format(d);


                                final Float stars = starsInput.getRating();

                                final UUID reviewId = UUID.randomUUID();


                                // ADD IMAGES


                                //ADD REVIEW
                                Map<String, Object> newReview = new HashMap<>();
                                newReview.put("author", currentUser.getUid().toString());
                                newReview.put("stars", Float.toString(stars));
                                newReview.put("text", etReview.getText().toString());
                                newReview.put("recipeID", recipeFor);
                                newReview.put("date", formattedDate);
                                //newReview.put("picy", p1.toString());
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

                                // Add review to user
                                final OnCompleteListener<DocumentSnapshot> storeReviewIdtoUser = new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            final DocumentSnapshot userDocument = task.getResult();
                                            final Map<String, Object> userData = userDocument.getData();
                                            final Map<String, Boolean> userRevs = (userData.get("reviewed") != null) ? (HashMap<String, Boolean>) userData.get("reviewed") : new HashMap<String, Boolean>();
                                            userRevs.put(individualRecipeID.toString(), true);
                                            userDocRef.update("reviewed", userRevs);
                                        }
                                    }
                                };
                                userDocRef.get().addOnCompleteListener(storeReviewIdtoUser);


                                //Add review to recipe review set
                                final DocumentReference recipeDoc = db.collection("recipes").document(individualRecipeID.toString());
                                final OnCompleteListener<DocumentSnapshot> storeReviewId = new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            final DocumentSnapshot document = task.getResult();
                                            final Map<String, Object> docData = document.getData();
                                            final Map<String, Boolean> revs = (docData.get("reviews") != null) ? (HashMap<String, Boolean>) docData.get("reviews") : new HashMap<String, Boolean>();
                                            revs.put(reviewId.toString(), true);
                                            recipeDoc.update("reviews", revs);

                                            float tot = 0;
                                            if(document.getString("total") == null || document.getString("total") == "") {
                                                tot = 0 + stars;
                                            }
                                            else {
                                                tot = Float.parseFloat(document.getString("total")) + stars;
                                            }
                                            int num = 0;
                                            if(document.getString("number") == null || document.getString("number") == "") {
                                                num = 0;
                                            }
                                            else {
                                                num = Integer.parseInt(document.getString("number"));
                                            }
                                            recipeDoc.update("total", Float.toString(tot));
                                            recipeDoc.update("number", Integer.toString(num + 1));
                                        }
                                    }
                                };
                                recipeDoc.get().addOnCompleteListener(storeReviewId);

                                etReview.setText("");
                                starsInput.setRating((float) 0.0);
                                Toast.makeText(c,"Review Added!",Toast.LENGTH_SHORT).show();
                            }


                        }
                        else {
                            Log.d(TAG, "fail");
                        }
                    }
                });

            }
        });
    }


    private void setupViews() {
        indiv_rec = getLayoutInflater().inflate(R.layout.individual_recipe_main, null);
        mainImage = (ImageView) indiv_rec.findViewById(R.id.main_image);
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

        final MenuItem likeItem = menu.findItem(R.id.like);
        final MenuItem bookmarkItem = menu.findItem(R.id.bookmark);

        // Check likes and bookmarks
        final DocumentReference dRef = db.collection("users").document(currentUser.getUid().toString());
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Checking likes and bookmarks");
                    DocumentSnapshot userDocument = task.getResult();
                    final Map<String, Object> userData = userDocument.getData();
                    final Map<String, Boolean> likes = (userData.get("likes") != null) ? (HashMap<String, Boolean>) userData.get("likes") : new HashMap<String, Boolean>();
                    if(likes.containsKey(individualRecipeID.toString())) {  // if the recipe has been liked before
                        likeItem.setIcon(ContextCompat.getDrawable(Individual_Recipe.this, R.drawable.ic_favorite_black_24dp));
                    }
                    else {
                        likeItem.setIcon(ContextCompat.getDrawable(Individual_Recipe.this, R.drawable.ic_favorite_border_black_24dp));
                    }
                    User user = userDocument.toObject(User.class);
                    List<String> bookmarks = user.getBookmarkedRecipes();
                    if(bookmarks.contains(individualRecipeID.toString()))
                    {
                        bookmarkItem.setIcon(ContextCompat.getDrawable(Individual_Recipe.this, R.drawable.ic_bookmark_black_24dp));
                    }
                    else
                    {
                        bookmarkItem.setIcon(ContextCompat.getDrawable(Individual_Recipe.this, R.drawable.ic_bookmark_border_black_24dp));
                    }
                }
                else {
                    Log.d(TAG, "fail");
                }
            }
        });
        return true;
    }


    //Bookmarking and liking
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final DocumentReference recDocR = db.collection("recipes").document(individualRecipeID.toString());
        final DocumentReference userDocR = db.collection("users").document(currentUser.getUid().toString());

        switch (item.getItemId()) {
            case R.id.like:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_favorite_black_24dp).getConstantState())) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_black_24dp));

                    //Remove like from recipe
                    final OnCompleteListener<DocumentSnapshot> removeLikeId = new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                final DocumentSnapshot recDocument2 = task.getResult();

                                int likes = Integer.parseInt(recDocument2.getString("numLikes"));
                                recDocR.update("numLikes", Integer.toString(likes - 1));
                            }
                        }
                    };
                    recDocR.get().addOnCompleteListener(removeLikeId);

                    // Remove like from user
                    final OnCompleteListener<DocumentSnapshot> removeReviewId = new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                final DocumentSnapshot userDocument2 = task.getResult();
                                final Map<String, Object> userData2 = userDocument2.getData();
                                final Map<String, Boolean> likes2 = (userData2.get("likes") != null) ? (HashMap<String, Boolean>) userData2.get("likes") : new HashMap<String, Boolean>();
                                likes2.remove(individualRecipeID.toString());
                                userDocR.update("likes", likes2);
                            }
                        }
                    };
                    userDocR.get().addOnCompleteListener(removeReviewId);

                    Toast.makeText(c,"Recipe has been unliked!",Toast.LENGTH_SHORT).show();
                }
                else {

                    //Add like to recipe
                    final OnCompleteListener<DocumentSnapshot> storeLikeId = new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                final DocumentSnapshot recDocument = task.getResult();

                                int likes = 0;
                                if(recDocument.getString("numLikes") != null && recDocument.getString("numLikes") != "") {
                                    likes = Integer.parseInt(recDocument.getString("numLikes"));
                                }
                                recDocR.update("numLikes", Integer.toString(likes + 1));
                            }
                        }
                    };
                    recDocR.get().addOnCompleteListener(storeLikeId);

                    // Add like to user
                    final OnCompleteListener<DocumentSnapshot> storeReviewId = new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                final DocumentSnapshot userDocument = task.getResult();
                                final Map<String, Object> userData = userDocument.getData();
                                final Map<String, Boolean> likes = (userData.get("likes") != null) ? (HashMap<String, Boolean>) userData.get("likes") : new HashMap<String, Boolean>();
                                likes.put(individualRecipeID.toString(), true);
                                userDocR.update("likes", likes);
                            }
                        }
                    };
                    userDocR.get().addOnCompleteListener(storeReviewId);

                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_black_24dp));
                    Toast.makeText(c,"Recipe has been liked!",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.bookmark:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp).getConstantState())) {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_border_black_24dp));

                    // Remove bookmark from user
                    final OnCompleteListener<DocumentSnapshot> removeBookmarkId = new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                final DocumentSnapshot userDocument3 = task.getResult();

                                User user = userDocument3.toObject(User.class);
                                List<String> bookmarks = user.getBookmarkedRecipes();
                                bookmarks.remove(individualRecipeID.toString());
                                userDocR.update("bookmarkedRecipes", bookmarks);
                            }
                        }
                    };
                    userDocR.get().addOnCompleteListener(removeBookmarkId);

                    Toast.makeText(c,"Recipe has been unbookmarked!",Toast.LENGTH_SHORT).show();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_black_24dp));

                    // Add bookmark to user
                    final OnCompleteListener<DocumentSnapshot> addBookmarkId = new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                final DocumentSnapshot userDocument4 = task.getResult();

                                User user = userDocument4.toObject(User.class);
                                List<String> bookmarks = user.getBookmarkedRecipes();
                                bookmarks.add(individualRecipeID.toString());
                                userDocR.update("bookmarkedRecipes", bookmarks);
                            }
                        }
                    };
                    userDocR.get().addOnCompleteListener(addBookmarkId);


                    Toast.makeText(c,"Recipe has been bookmarked!",Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}


