package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.UploadTask;
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

    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();

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
    private TextView noReviewMessage;

    private  RatingBar avgStarsDisp;


    //New Review
    private Button reviewBtn;
    private EditText etReview;
    private RatingBar starsInput;

    private String individualRecipeID;

    private ImageView mainImage;

    private Button followButton;

    private Uri photo1Uri;
    private Uri photo2Uri;
    private Uri photo3Uri;

    private Toolbar myToolbar;

    private int cnt;

    private ProgressDialog progressDialog;

    private void setupBtn(final String profileUID, final String currentUID) {
        // Sets up follow button
        DocumentReference docRef = db.collection("users").document(currentUID);
        final Boolean[] isFollowingBool = {false};
        final OnCompleteListener<DocumentSnapshot> isFollowing = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    if(docData.get("following") == null) {
                        followButton.setText("Follow");
                        setupFollowBtn(profileUID, currentUID);
                    }
                    else {
                        final Map<String, Boolean> followersList = (HashMap<String, Boolean>) docData.get("following");

                        if (followersList.containsKey(profileUID)) {
                            followButton.setText("Unfollow");
                            setupUnfollowBtn(profileUID, currentUID);
                        } else {
                            followButton.setText("Follow");
                            setupFollowBtn(profileUID, currentUID);
                        }
                    }
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        docRef.get().addOnCompleteListener(isFollowing);
    }

    private void setupFollowBtn(final String profileUID, final String currentUID) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followAction(profileUID, currentUID);
                followButton.setText("Unfollow");
                setupUnfollowBtn(profileUID, currentUID);
            }
        });
    }

    private void setupUnfollowBtn(final String profileUID, final String currentUID) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfollowAction(profileUID, currentUID);
                followButton.setText("Follow");
                setupFollowBtn(profileUID, currentUID);
            }
        });
    }

    private void followAction(final String profileUID, final String currentUID) {
        final DocumentReference currUserDoc = db.collection("users").document(currentUID);

        // Add profile's UID into current user's Following table
        final OnCompleteListener<DocumentSnapshot> storeFollowingInCurrentUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followingList = (docData.get("following") != null) ? (HashMap<String, Boolean>) docData.get("following") : new HashMap<String, Boolean>();
                    followingList.put(profileUID, true);
                    currUserDoc.update("following", followingList);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        currUserDoc.get().addOnCompleteListener(storeFollowingInCurrentUser);

        // Add current user into profile user's Followers table
        final DocumentReference profileUserDoc = db.collection("users").document(profileUID);
        final OnCompleteListener<DocumentSnapshot> storeFollowersinProfileUser= new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followersList = (docData.get("followers") != null) ? (HashMap<String, Boolean>) docData.get("followers") : new HashMap<String, Boolean>();
                    followersList.put(currentUID, true);
                    profileUserDoc.update("followers", followersList);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        profileUserDoc.get().addOnCompleteListener(storeFollowersinProfileUser);
    }

    private void unfollowAction(final String profileUID, final String currentUID) {
        // Remove profile's UID from current user's following table
        final DocumentReference currUserDoc = db.collection("users").document(currentUID);
        final OnCompleteListener<DocumentSnapshot> storeUnfollowInCurrentUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followingList = (docData.get("following") != null) ? (HashMap<String, Boolean>) docData.get("following") : new HashMap<String, Boolean>();
                    followingList.remove(profileUID);
                    currUserDoc.update("following", followingList);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        currUserDoc.get().addOnCompleteListener(storeUnfollowInCurrentUser);

        // Remove current user's UID from profile user's followers table
        final DocumentReference profileUserDoc = db.collection("users").document(profileUID);
        final OnCompleteListener<DocumentSnapshot> storeFollowersinProfileUser= new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> followersList = (docData.get("followers") != null) ? (HashMap<String, Boolean>) docData.get("followers") : new HashMap<String, Boolean>();
                    followersList.remove(currentUID);
                    profileUserDoc.update("followers", followersList);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        profileUserDoc.get().addOnCompleteListener(storeFollowersinProfileUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_recipe);


        myToolbar = (Toolbar) findViewById(R.id.indiv_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");

        individualRecipeID = getIntent().getExtras().getString("ID");

        setupViews();

        final ScrollView individual_recipe_layout = (ScrollView) findViewById(R.id.main_scroll);

        followButton = (Button) indiv_rec.findViewById(R.id.follow_button);


        ingredientsTitle.setText("Ingredients");
        methodTitle.setText("Method");
        reviewTitle.setText("Reviews");
        addRevText.setText("Add Review");

        displayNoReviewsMessage();

        final DocumentReference docRef = db.collection("recipes").document(individualRecipeID.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Getting individual recipe");
                    final DocumentSnapshot document = task.getResult();


                    // DISPLAY MAIN INFO
                    if((String) document.get("mainPhotoStoragePath") == null || (String) document.get("mainPhotoStoragePath") == "")
                    {
                        mainImage.setImageResource(R.drawable.eggs);
                    }
                    else
                    {
                        final String path = (String) document.get("mainPhotoStoragePath");
                        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                Glide.with(c)
                                        .load(uri.toString())
                                        .into(mainImage);
                                mainImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        expandPhoto(uri);
                                    }
                                });
                            }
                        });
                    }

                    mainTitle.setText(document.getString("title"));

                    if(document.getString("description") == null || document.getString("description") == "")
                    {
                        mainDescription.setText("No description");
                    }
                    else
                    {
                        mainDescription.setText(document.getString("description"));
                    }

                    numFeeds.setText(document.getString("servings"));
                    cookTime.setText(document.getString("time"));

                    setAverage();

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
                        sb.append("No ingredients");
                    }
                    else if (ingredsList.size() == 1 && ingredsList.get(0).equals("")) {
                        sb.append("No ingredients");
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
                            stepPhoto.setVisibility(View.GONE);
                            stepPhoto.getLayoutParams().width = 0;
                            stepPhoto.getLayoutParams().height = 0;
                        }
                        else {
                            final String path = (String) methodList.get(i).get("storagePath");
                            storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    Glide.with(c)
                                            .load(uri.toString())
                                            .into(stepPhoto);


                                    stepPhoto.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            expandPhoto(uri);
                                        }
                                    });
                                }
                            });

                        }

                        methodLayout.addView(met);
                    }


                    // Store and sort reviews
                    storeAndSortReviews();


                    // GET RECIPE USERNAME AND AVATAR
                    final String recAuthorID = document.getString("userId");

                    if (currentUser.getUid().toString().equals(recAuthorID)) {
                        followButton.setVisibility(View.GONE);
                    }

                    final DocumentReference docRef2 = db.collection("users").document(recAuthorID.toString());
                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "Getting username");
                                DocumentSnapshot document2 = task.getResult();
                                mainName.setText(document2.getString("username"));
                                mainName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Individual_Recipe.this, ProfileActivity.class);
                                        intent.putExtra("ID", recAuthorID);
                                        startActivity(intent);
                                    }
                                });

                                final ImageView mainAvatar = (ImageView) indiv_rec.findViewById(R.id.avatar);
                                setupBtn(recAuthorID, currentUser.getUid());

                                if((String) document2.get("profilePhotoPath") == null || (String) document2.get("profilePhotoPath") == "")
                                {
                                    // default image
                                }
                                else
                                {
                                    final String path = (String) document2.get("profilePhotoPath");
                                    storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(c)
                                                    .load(uri.toString())
                                                    .into(mainAvatar);
                                        }
                                    });
                                }
                            }
                            else {
                                Log.d(TAG, "Getting username fail");
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

    private void displayNoReviewsMessage() {

        final DocumentReference docRef = db.collection("recipes").document(individualRecipeID.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();

                    Map<String, Long> reviewMap = (document.get("reviews") != null) ? (HashMap<String, Long>) document.get("reviews") : new HashMap<String, Long>();
                    if(reviewMap.size() == 0) {
                        noReviewMessage.setVisibility(View.VISIBLE);
                    }
                    else {
                        noReviewMessage.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void setAverage() {
        final DocumentReference docRef = db.collection("recipes").document(individualRecipeID.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    float avgS = 0;
                    if(document.getString("total") != null && document.getString("total") != "") {
                        avgS = Float.parseFloat(document.getString("total")) / Integer.parseInt(document.getString("number"));
                    }
                    avgStarsDisp.setRating(avgS);
                }
            }
        });
    }


    private void storeAndSortReviews() {

        final DocumentReference docRef = db.collection("recipes").document(individualRecipeID.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();

                    final Map<String, Long> reviewMap = (document.get("reviews") != null) ? (HashMap<String, Long>) document.get("reviews") : new HashMap<String, Long>();
                    cnt = reviewMap.size();
                    final List<ReviewCompactObject> reviewCompactObjectList = new ArrayList<>();

                    for (final String key : reviewMap.keySet()) {

                        final DocumentReference docRef3 = db.collection("reviews").document(key);
                        docRef3.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot reviewSnapshot) {
                                if (reviewSnapshot.exists()) {
                                    db.collection("users").document(reviewSnapshot.getString("author")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot reviewUserSnapshot) {
                                            String recipeId = reviewSnapshot.getString("recipeID");
                                            String reviewDate = reviewSnapshot.getString("date");
                                            String photoOne = reviewSnapshot.getString("photo1");
                                            String photoTwo = reviewSnapshot.getString("photo2");
                                            String photoThree = reviewSnapshot.getString("photo3");
                                            float reviewStars = Float.parseFloat(reviewSnapshot.getString("stars"));
                                            String reviewText = reviewSnapshot.getString("text");

                                            String reviewPublisherId = reviewSnapshot.getString("author");
                                            User user = reviewUserSnapshot.toObject(User.class);
                                            String reviewPublisher = user.getUsername();
                                            String reviewPublisherPhotoPath = user.getProfilePhotoPath();

                                            long comparatorValue = reviewMap.get(key);
                                            ReviewCompactObject curr = new ReviewCompactObject(key, recipeId, reviewDate, photoOne, photoTwo, photoThree, reviewStars, reviewText, reviewPublisher, reviewPublisherId, reviewPublisherPhotoPath, comparatorValue);
                                            reviewCompactObjectList.add(curr);

                                            if (--cnt == 0) {
                                                Collections.sort(reviewCompactObjectList, new Comparator<ReviewCompactObject>() {
                                                    @Override
                                                    public int compare(ReviewCompactObject a, ReviewCompactObject b) {
                                                        if (a.comparatorValue > b.comparatorValue)
                                                            return -1;
                                                        else if (a.comparatorValue < b.comparatorValue)
                                                            return 1;
                                                        else return 0;
                                                    }
                                                });

                                                displayReviews(reviewCompactObjectList);
                                            }
                                        }
                                    });
                                }
                            }
                        });

                    }
                }
            }
        });
    }

    private void displayReviews(final List<ReviewCompactObject> reviewList) {
        final LinearLayout reviewLayout = (LinearLayout) findViewById(R.id.reviews);

        for (int i = 0; i < reviewList.size(); i++) {
            final View rev = getLayoutInflater().inflate(R.layout.ir_single_review, null);

            RatingBar revStarsDisp = (RatingBar) rev.findViewById(R.id.rev_stars_disp);

            final ImageView reviewAvatar = (ImageView) rev.findViewById(R.id.review_avatar);
            final TextView reviewName = (TextView) rev.findViewById(R.id.review_name);
            final TextView reviewText = (TextView) rev.findViewById(R.id.review_text);
            final TextView reviewDateText = (TextView) rev.findViewById(R.id.review_date_text);
            final ImageButton imageOne = (ImageButton) rev.findViewById(R.id.image1);
            final ImageButton imageTwo = (ImageButton) rev.findViewById(R.id.image2);
            final ImageButton imageThree = (ImageButton) rev.findViewById(R.id.image3);

            //version 2
            final View rev2 = getLayoutInflater().inflate(R.layout.ir_single_review2, null);

            RatingBar revStarsDisp2 = (RatingBar) rev2.findViewById(R.id.rev_stars_disp2);

            final ImageView reviewAvatar2 = (ImageView) rev2.findViewById(R.id.review_avatar2);
            final TextView reviewName2 = (TextView) rev2.findViewById(R.id.review_name2);
            final TextView reviewText2 = (TextView) rev2.findViewById(R.id.review_text2);
            final TextView reviewDateText2 = (TextView) rev2.findViewById(R.id.review_date_text2);


            if(reviewList.get(i).photoOne == null && reviewList.get(i).photoTwo == null && reviewList.get(i).photoThree == null) {
                if(reviewList.get(i).reviewPublisherPhotoPath == null || reviewList.get(i).reviewPublisherPhotoPath == "")
                {

                }
                else
                {
                    storageReference.child(reviewList.get(i).reviewPublisherPhotoPath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(c)
                                    .load(uri.toString())
                                    .into(reviewAvatar2);
                        }
                    });
                }
                revStarsDisp2.setRating(reviewList.get(i).reviewStars);

                reviewName2.setText(reviewList.get(i).reviewPublisher);

                final String tempID2 = reviewList.get(i).reviewPublisherId;
                reviewName2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Individual_Recipe.this, ProfileActivity.class);
                        intent.putExtra("ID", tempID2);
                        startActivity(intent);
                    }
                });

                if(reviewList.get(i).reviewText.equals("")) {
                    reviewText2.setVisibility(View.GONE);
                }
                else {
                    reviewText2.setText(reviewList.get(i).reviewText);
                    reviewText2.setVisibility(View.VISIBLE);
                }

                reviewDateText2.setText(reviewList.get(i).reviewDate);


                reviewLayout.addView(rev2);

                final ImageButton deleteRevBtn2 = (ImageButton) rev2.findViewById(R.id.delete_rev_btn2);
                // Don't show the delete button if it's not the user's review
                if (reviewList.get(i).reviewPublisherId.equals(currentUser.getUid().toString())) {
                    final int iTemp = i;
                    deleteRevBtn2.setVisibility(View.VISIBLE);
                    deleteRevBtn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(view.getContext())
                                    .setMessage("Are you sure you want to delete this review?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int button) {
                                            deleteReview(reviewList.get(iTemp));
                                        }
                                    }).setNegativeButton(android.R.string.no, null).show();
                        }
                    });
                }
                else {
                    deleteRevBtn2.setVisibility(View.GONE);
                }
            }
            else {
                if(reviewList.get(i).reviewPublisherPhotoPath == null || reviewList.get(i).reviewPublisherPhotoPath == "")
                {
                }
                else
                {
                    storageReference.child(reviewList.get(i).reviewPublisherPhotoPath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(c)
                                    .load(uri.toString())
                                    .into(reviewAvatar);
                        }
                    });
                }

                revStarsDisp.setRating(reviewList.get(i).reviewStars);

                reviewName.setText(reviewList.get(i).reviewPublisher);

                final String tempID = reviewList.get(i).reviewPublisherId;
                reviewName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Individual_Recipe.this, ProfileActivity.class);
                        intent.putExtra("ID", tempID);
                        startActivity(intent);
                    }
                });

                if (reviewList.get(i).reviewText.equals("")){
                    reviewText.setVisibility(View.GONE);
                }
                else {
                    reviewText.setText(reviewList.get(i).reviewText);
                    reviewText.setVisibility(View.VISIBLE);
                }


                reviewDateText.setText(reviewList.get(i).reviewDate);

                displayRevImages(imageOne, imageTwo, imageThree, reviewList.get(i).photoOne, reviewList.get(i).photoTwo, reviewList.get(i).photoThree);
                reviewLayout.addView(rev);

                final ImageButton deleteRevBtn = (ImageButton) rev.findViewById(R.id.delete_rev_btn);
                // Don't show the delete button if it's not the user's review
                if (reviewList.get(i).reviewPublisherId.equals(currentUser.getUid().toString())) {
                    final int iTemp = i;
                    deleteRevBtn.setVisibility(View.VISIBLE);
                    deleteRevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(view.getContext())
                                    .setMessage("Are you sure you want to delete this review?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int button) {
                                            deleteReview(reviewList.get(iTemp));
                                        }
                                    }).setNegativeButton(android.R.string.no, null).show();
                        }
                    });
                }
                else {
                    deleteRevBtn.setVisibility(View.GONE);
                }
            }

        }
    }

    private void deleteReview(ReviewCompactObject review) {
        final String revID = review.reviewId;
        String recID = review.recipeId;
        final float currRevStars = review.reviewStars;
        progressDialog = ProgressDialog.show(Individual_Recipe.this, null, "Deleting review...");

        // Remove review from recipe document and update total and number
        final DocumentReference recDocR = db.collection("recipes").document(recID);

        final OnCompleteListener<DocumentSnapshot> removeReview = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot recipeDocument = task.getResult();

                    final Map<String, Object> recData = recipeDocument.getData();
                    final Map<String, Long> revs = (recData.get("reviews") != null) ? (HashMap<String, Long>) recData.get("reviews") : new HashMap<String, Long>();
                    revs.remove(revID);

                    float tot = 0;
                    if(recipeDocument.getString("total") != null && recipeDocument.getString("total") != "" && !recipeDocument.getString("total").equals("0")) {
                        tot = Float.parseFloat(recipeDocument.getString("total")) - currRevStars;
                    }

                    int num = 0;
                    if(recipeDocument.getString("number") != null && recipeDocument.getString("number") != "" && !recipeDocument.getString("number").equals("0")) {
                        num = Integer.parseInt(recipeDocument.getString("number")) - 1;
                    }
                    recDocR.update("total", Float.toString(tot)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
                    recDocR.update("number", Integer.toString(num)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
                    recDocR.update("reviews", revs).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
                }
            }
        };
        recDocR.get().addOnCompleteListener(removeReview);


        // Delete images from storage
        if (review.photoOne != null) {
            deleteImageFromStorage(review.photoOne);
        }
        if (review.photoTwo != null) {
            deleteImageFromStorage(review.photoTwo);
        }
        if (review.photoThree != null) {
            deleteImageFromStorage(review.photoThree);
        }


        // Remove review document
        db.collection("reviews").document(revID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Successfully deleted review!", Toast.LENGTH_LONG).show();
                setAverage();
                forceReload();
            }
        });

    }

    private void deleteImageFromStorage(String path) {
        StorageReference ref = storageReference.child(path);
        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // do nothing
            }
        });
    }

    private void displayRevImages(final ImageButton revImage1, final ImageButton revImage2, final ImageButton revImage3, final String path1, final String path2, final String path3) {

        // Check that review images exists and set them

        if(path1 != null && path1 != "") {  // set photo1

            storageReference.child(path1).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Glide.with(c)
                            .load(uri.toString())
                            .into(revImage1);


                    revImage1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            expandPhoto(uri);
                        }
                    });
                }
            });

        }
        else {
            revImage1.setVisibility(View.GONE);
            revImage1.getLayoutParams().width = 0;
            revImage1.getLayoutParams().height = 0;
        }

        if(path2 != null && path2 != "") {  // set photo2

            storageReference.child(path2).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Glide.with(c)
                            .load(uri.toString())
                            .into(revImage2);


                    revImage2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            expandPhoto(uri);
                        }
                    });
                }
            });

        }
        else {
            revImage2.setVisibility(View.GONE);
            revImage2.getLayoutParams().width = 0;
            revImage2.getLayoutParams().height = 0;
        }

        if(path3 != null && path3 != "") {  // set photo3

            storageReference.child(path3).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Glide.with(c)
                            .load(uri.toString())
                            .into(revImage3);


                    revImage3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            expandPhoto(uri);
                        }
                    });
                }
            });

        }
        else {
            revImage3.setVisibility(View.GONE);
            revImage3.getLayoutParams().width = 0;
            revImage3.getLayoutParams().height = 0;
        }

    }


    private void setupReviewAddPhotos() {

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

        firstPhotoBtn.setTag(1);
        secondPhotoBtn.setTag(2);
        thirdPhotoBtn.setTag(3);

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
                    setImage(photoButton, photoOptions, photoEdit, photoDelete, pickResult.getUri(), null);
                } else {
                    Toast.makeText(getApplicationContext(), pickResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).show(getSupportFragmentManager());
    }

        private void setImage(final ImageButton photoBtn, final LinearLayout photoOptions, final LinearLayout photoEdit, final ImageButton photoDelete, final Uri uri, String url) {
        if (uri == null && url == null || uri != null && url != null) {
            Log.d(TAG, "Either uri or url should be null.");
            return;
        } else if (uri != null) {
            photoBtn.setImageURI(uri);
            if((int) photoBtn.getTag() == 1) {
                photo1Uri = uri;
            }
            else if ((int) photoBtn.getTag() == 2) {
                photo2Uri = uri;
            }
            else {
                photo3Uri = uri;
            }


        } else { // use url instead

            Glide.with(Individual_Recipe.this)
                    .load(storageReference.child(url))
                    .into(photoBtn);
        }

        photoOptions.setVisibility(View.VISIBLE);

        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandPhoto(uri);
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

    private void expandPhoto(final Uri uri) {
        Intent intent = new Intent(this, IndividualRecipeImageActivity.class);
        intent.putExtra("BitmapUri", uri);
        startActivity(intent);
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
                newReview.put("recipeID", individualRecipeID);
                newReview.put("date", formattedDate);

                if(photo1Uri != null) {
                    final String firebaseStorageFilePath = "images/" + UUID.randomUUID().toString();
                    StorageReference ref = storageReference.child(firebaseStorageFilePath);
                    ref.putFile(photo1Uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
                    newReview.put("photo1", firebaseStorageFilePath);
                }
                else {
                    newReview.put("photo1", null);
                }
                if(photo2Uri != null) {
                    final String firebaseStorageFilePath = "images/" + UUID.randomUUID().toString();
                    StorageReference ref = storageReference.child(firebaseStorageFilePath);
                    ref.putFile(photo2Uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
                    newReview.put("photo2", firebaseStorageFilePath);
                }
                else {
                    newReview.put("photo2", null);
                }
                if(photo3Uri != null) {
                    final String firebaseStorageFilePath = "images/" + UUID.randomUUID().toString();
                    StorageReference ref = storageReference.child(firebaseStorageFilePath);
                    ref.putFile(photo3Uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
                    newReview.put("photo3", firebaseStorageFilePath);
                }
                else {
                    newReview.put("photo3", null);
                }

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
                final DocumentReference recipeDoc = db.collection("recipes").document(individualRecipeID.toString());
                final OnCompleteListener<DocumentSnapshot> storeReviewId = new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            final DocumentSnapshot document = task.getResult();
                            final Map<String, Object> docData = document.getData();
                            final Map<String, Long> revs = (docData.get("reviews") != null) ? (HashMap<String, Long>) docData.get("reviews") : new HashMap<String, Long>();
                            revs.put(reviewId.toString(), new Date().getTime());
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

                Toast.makeText(c,"Review Added!",Toast.LENGTH_LONG).show();

                setAverage();

                forceReload();

            }
        });
    }

    private void forceReload() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
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
        noReviewMessage = (TextView) indiv_rec.findViewById(R.id.no_review_message);

        avgStarsDisp = (RatingBar) indiv_rec.findViewById(R.id.avg_stars_disp);

        // New Review
        reviewBtn = (Button) indiv_rec.findViewById(R.id.review_button);
        etReview = indiv_rec.findViewById(R.id.review_text_input);
        starsInput = (RatingBar) indiv_rec.findViewById(R.id.stars_input);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        final MenuItem bookmarkItem = menu.findItem(R.id.bookmark);

        // Get id of the recipe author
        final DocumentReference docRef = db.collection("recipes").document(individualRecipeID.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Getting id of the recipe author");
                    DocumentSnapshot document = task.getResult();
                    final String recipeAuthorID = document.getString("userId");

                    if(currentUser.getUid().toString().equals(recipeAuthorID)) { // if the user is viewing their own recipe
                        myToolbar.setVisibility(View.GONE);
                    }
                    else {
                        // Check bookmarks
                        final DocumentReference dRef = db.collection("users").document(currentUser.getUid().toString());
                        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Checking bookmarks");
                                    DocumentSnapshot userDocument = task.getResult();
                                    User user = userDocument.toObject(User.class);
                                    Map<String, Long> bookmarks = user.getBookmarkedRecipes();
                                    if (bookmarks.containsKey(individualRecipeID)) {
                                        bookmarkItem.setIcon(ContextCompat.getDrawable(Individual_Recipe.this, R.drawable.ic_bookmark_black_24dp));
                                    } else {
                                        bookmarkItem.setIcon(ContextCompat.getDrawable(Individual_Recipe.this, R.drawable.ic_bookmark_border_black_24dp));
                                    }
                                } else {
                                    Log.d(TAG, "fail");
                                }
                            }
                        });
                    }
                }
                else {
                    Log.d(TAG, "Getting id of the recipe author fail");
                }
            }
        });
        return true;
    }


    //Bookmarking
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final DocumentReference recDocR = db.collection("recipes").document(individualRecipeID.toString());
        final DocumentReference userDocR = db.collection("users").document(currentUser.getUid().toString());

        switch (item.getItemId()) {
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

                                Map<String, Long> bookmarks = user.getBookmarkedRecipes();
                                bookmarks.remove(individualRecipeID);
                                userDocR.update("bookmarkedRecipes", bookmarks).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        recDocR.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                // add to recipe's bookmarkedUsers array.
                                                Map<String, Object> recipeData = documentSnapshot.getData();
                                                Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
                                                bookmarkedUsers.remove(currentUser.getUid().toString());
                                                recDocR.update("bookmarkedUsers", bookmarkedUsers);
                                            }
                                        });
                                    }
                                });
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
                                Map<String, Long> bookmarks = user.getBookmarkedRecipes();
                                bookmarks.put(individualRecipeID, new Date().getTime());
                                userDocR.update("bookmarkedRecipes", bookmarks).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        recDocR.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                // add to recipe's bookmarkedUsers array.
                                                Map<String, Object> recipeData = documentSnapshot.getData();
                                                Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
                                                bookmarkedUsers.put(currentUser.getUid().toString(), true);
                                                recDocR.update("bookmarkedUsers", bookmarkedUsers);
                                            }
                                        });
                                    }
                                });
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
