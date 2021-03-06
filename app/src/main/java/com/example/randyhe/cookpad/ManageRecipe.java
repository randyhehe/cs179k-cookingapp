package com.example.randyhe.cookpad;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ManageRecipe extends AppCompatActivity {
    private String TAG = "ManageRecipe.java";
    private View rootView;

    // publish and uploading
    private FirebaseFirestore fbFirestore;
    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private int numMethodImages;

    private int tagNumber = 0;
    private int methodNumber = 1;
    private int ingrNumber = 1;
    private int deleteTempVar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = LayoutInflater.from(this).inflate(R.layout.activity_cr, null);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(rootView);

        // both editing and creating recipe share this code
        setupIngredients();
        setupMethods();
        setupTags();
        setupInitialImages();

        // setup firebase
        FirebaseApp.initializeApp(this);
        fbFirestore = FirebaseFirestore.getInstance();
        fbAuth = FirebaseAuth.getInstance();
        fbStorage = FirebaseStorage.getInstance();
        storageReference = fbStorage.getReference();

        // publishing and editing logic differs here (mostly for data submission)
        boolean isEdit = editOrCreate();
        if (isEdit) {
            setupEdit();
        } else {
            setupPublish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) { // not signed in
            startActivity(new Intent(ManageRecipe.this, LoginActivity.class));
        } else {
            // logged in
        }
    }

    // returns true if editing an existing recipe, false if creating a new recipe
    private boolean editOrCreate() {
        Bundle extras = getIntent().getExtras();
        return extras != null && extras.getBoolean("EDIT", false);
    }

    private void deleteRecipe(final DocumentSnapshot recipeSnapshot) {
        final Map<String, Object> recipeData = recipeSnapshot.getData();

        // delete images from database
        deleteImageFromStorage((String) recipeData.get("mainPhotoStoragePath"));
        List<Map<String, String>> methods = (ArrayList<Map<String, String>>) recipeData.get("methods");
        for (int i = 0; i < methods.size(); i++) {
            String storagePath = methods.get(i).get("storagePath");
            if (storagePath == null) continue;
            else deleteImageFromStorage(storagePath);
        }

        final DocumentReference userDoc = fbFirestore.collection("users").document(fbUser.getUid());
        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Boolean> recipes = (HashMap<String, Boolean>) userData.get("recipes");
                // delete recipe from users collection
                recipes.remove(recipeSnapshot.getId());
                userDoc.update("recipes", recipes).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // delete all entries of this recipe in other's users "bookmarkedRecipes if exists"
                        Map<String, Boolean> bookmarkedUsers = (HashMap<String, Boolean>) recipeData.get("bookmarkedUsers");
                        if (bookmarkedUsers == null || bookmarkedUsers.size() == 0) {
                            fbFirestore.collection("recipes").document(recipeSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Successfully deleted recipe!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        } else {
                            deleteTempVar = bookmarkedUsers.size();
                            for (final String s : bookmarkedUsers.keySet()) {
                                fbFirestore.collection("users").document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        User user = documentSnapshot.toObject(User.class);
                                        Map<String, Long> bookmarkedRecipes = user.getBookmarkedRecipes();
                                        bookmarkedRecipes.remove(recipeSnapshot.getId());
                                        fbFirestore.collection("users").document(s).update("bookmarkedRecipes", bookmarkedRecipes).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // delete recipe from recipes collection
                                                if (--deleteTempVar == 0) {
                                                    fbFirestore.collection("recipes").document(recipeSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(getApplicationContext(), "Successfully deleted recipe!", Toast.LENGTH_LONG).show();
                                                            finish();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                });
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

    private void setupEdit() {
        // populate existing data into the appropriate fields
        String recipeId = getIntent().getExtras().getString("ID");
        DocumentReference dr = fbFirestore.collection("recipes").document(recipeId);
        dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();

                    // set main photo
                    final ImageButton ibMainPhoto = findViewById(R.id.mainphoto_btn);
                    final LinearLayout llMainPhotoOptions = findViewById(R.id.mainphoto_options);
                    final LinearLayout llMainPhotoEdit = findViewById(R.id.mainphoto_edit);
                    final ImageButton ibMainPhotoDelete = findViewById(R.id.mainphoto_delete);
                    setImage(ibMainPhoto, llMainPhotoOptions, llMainPhotoEdit, ibMainPhotoDelete, null, (String) data.get("mainPhotoStoragePath"));

                    // title
                    EditText etTitle = findViewById(R.id.recipe_title);
                    etTitle.setText((String) data.get("title"));

                    // description
                    EditText etDescription = findViewById(R.id.recipe_desc);
                    etDescription.setText((String) data.get("description"));

                    // servings
                    EditText etServings = findViewById(R.id.servings);
                    etServings.setText((String) data.get("servings"));

                    // time
                    EditText etTime = findViewById(R.id.time);
                    etTime.setText((String) data.get("time"));

                    // ingredients
                    List<String> ingrs = (ArrayList<String>) data.get("ingrs");
                    if (ingrs.size() > 0) { // fill in the first ingr
                        EditText etFirstIngr = findViewById(R.id.textout);
                        etFirstIngr.setText(ingrs.get(0));

                        for (int i = 1; i < ingrs.size(); i++) {
                            addIngr(ingrs.get(i));
                        }
                    }

                    // methods
                    List<HashMap<String, Object>> methods = (ArrayList<HashMap<String, Object>>) data.get("methods");
                    if (methods.size() > 0) { // fill in the first method
                        Method firstMethod = new Method((String) methods.get(0).get("instruction"), (String) methods.get(0).get("storagePath"));

                        EditText etFirstMethod = findViewById(R.id.stepout);
                        etFirstMethod.setText(firstMethod.instruction);

                        ImageButton ibFirstImage = findViewById(R.id.addphoto);
                        LinearLayout llFirstImageOptions = findViewById(R.id.photo_options);
                        LinearLayout llFirstImageEdit = findViewById(R.id.photo_edit);
                        ImageButton ibFirstImageDelete = findViewById(R.id.photo_delete);

                        if (firstMethod.storagePath != null) {
                            setImage(ibFirstImage, llFirstImageOptions, llFirstImageEdit, ibFirstImageDelete, null, firstMethod.storagePath);
                        }

                        for (int i = 1; i < methods.size(); i++) {
                            Method currMethod = new Method((String) methods.get(i).get("instruction"), (String) methods.get(i).get("storagePath"));
                            addMethod(currMethod);
                        }
                    }

                    // tags
                    List<String> tags = (ArrayList<String>) data.get("tags");
                    for (int i = 0; i < tags.size(); i++) {
                        addTag(tags.get(i));
                    }

                    Log.d("test", data.toString());
                } else {
                    // no such document
                }
            }
        });

        // set submit button to update the current entry
        Button editButton = findViewById(R.id.pub);
        editButton.setText("Edit");
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editRecipe();
//                if (findViewById(R.id.mainphoto_btn).getTag() != null && findViewById(R.id.mainphoto_btn).getTag() instanceof Uri) {
//                    Log.d(TAG, "set");
//                } else {
//                    Log.d(TAG, "not set");
//                }
            }
        });

        // set delete button to delete the current entry
        Button deleteButton = findViewById(R.id.delete_recipe);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(ManageRecipe.this, null, "Deleting Recipe...");

                final String recipeId = getIntent().getExtras().getString("ID");
                fbFirestore.collection("recipes").document(recipeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        deleteRecipe(documentSnapshot);
                    }
                });
            }
        });
    }

    private void setupPublish() {
        //stuff for publish
        Button submitButton = findViewById(R.id.pub);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Recipe recipe = createRecipe();
                publishRecipe(recipe);
            }
        });
    }

    private void setupIngredients() {
        Button bAddIngr = findViewById(R.id.adding);
        bAddIngr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addIngr("");
            }
        });
    }

    private void addIngr(String ingr) {
        LinearLayout ingCont = findViewById(R.id.ing_cont);
        final int initialIngrNum = ++ingrNumber;

        LayoutInflater layoutInflator = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflator.inflate(R.layout.activity_cr_row, null);
        addView.setTag("ingr" + initialIngrNum);

        final EditText etIngr = addView.findViewById(R.id.textout);
        etIngr.setText(ingr);

        Button ingRemove = addView.findViewById(R.id.remove);
        ingRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) addView.getTag();
                int currIngr = Integer.parseInt(tag.substring(4));
                for (int i = currIngr + 1; i <= ingrNumber; i++) {
                    final View currView = rootView.findViewWithTag("ingr" + i);

                    int newIngrNum = i - 1;
                    currView.setTag("ingr" + newIngrNum);
                }
                ingrNumber--;
                ((LinearLayout) addView.getParent()).removeView(addView);
            }
        });
        ingCont.addView(addView);
    }

    private void addMethod(Method method) {
        LinearLayout methods = findViewById(R.id.mthd_cont);
        final int initialStepNum = ++methodNumber;

        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.activity_cr_method, null);
        addView.setTag("method" + initialStepNum);

        final TextView tvStepNum = (TextView) addView.findViewById(R.id.num);
        tvStepNum.setText(Integer.toString(initialStepNum));

        // image handlers
        final ImageButton ibStepPhoto = addView.findViewById(R.id.addphoto);
        final LinearLayout llPhotoOptions = addView.findViewById(R.id.photo_options);
        final LinearLayout llFirstPhotoEdit = addView.findViewById(R.id.photo_edit);
        final ImageButton ibPhotoDelete = addView.findViewById(R.id.photo_delete);
        ibStepPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(ibStepPhoto, llPhotoOptions, llFirstPhotoEdit, ibPhotoDelete);
            }
        });

        Button method_Remove = addView.findViewById(R.id.remove_mth);
        method_Remove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int currStep = Integer.parseInt(tvStepNum.getText().toString());

                for (int i = currStep + 1; i <= methodNumber; i++) {
                    final View currView = rootView.findViewWithTag("method" + i);
                    final TextView stepNum = currView.findViewById(R.id.num);

                    int newStepNum = i - 1;
                    stepNum.setText(Integer.toString(newStepNum));
                    currView.setTag("method" + newStepNum);
                }
                methodNumber--;
                ((LinearLayout) addView.getParent()).removeView(addView);
            }
        });

        if (method != null) {
            if (method.storagePath != null) {
                setImage(ibStepPhoto, llPhotoOptions, llFirstPhotoEdit, ibPhotoDelete, null, method.storagePath);
            }
            final EditText etMethodInstructions = addView.findViewById(R.id.stepout);
            etMethodInstructions.setText(method.instruction);
        }
        methods.addView(addView);
    }

    private void setupMethods() {
        Button addMethodButton = findViewById(R.id.addmethod);
        addMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addMethod(null);
            }
        });
    }

    private void addTag(String tag) {
        final EditText textIn = findViewById(R.id.textin);
        final LinearLayout container = findViewById(R.id.container);
        ++tagNumber;

        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.activity_cr_row, null);
        addView.setTag("tag" + tagNumber);

        TextView textOut = addView.findViewById(R.id.textout);
        if (tag != null) {
            textOut.setText(tag);
        } else {
            textOut.setText(textIn.getText().toString());
        }
        textIn.setText("");
        container.setVisibility(View.VISIBLE);
        Button buttonRemove = (Button) addView.findViewById(R.id.remove);
        buttonRemove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String tag = (String) addView.getTag();
                int currTag = Integer.parseInt(tag.substring(3));
                for (int i = currTag + 1; i <= tagNumber; i++) {
                    final View currView = rootView.findViewWithTag("tag" + i);
                    int newTagNum = i - 1;
                    currView.setTag("tag" + newTagNum);
                }
                tagNumber--;
                if (tagNumber <= 0) {
                    container.setVisibility(View.GONE);
                }
                ((LinearLayout) addView.getParent()).removeView(addView);
            }
        });

        container.addView(addView);
    }

    private void setupTags() {
        final Button buttonAdd = findViewById(R.id.add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addTag(null);
            }
        });
    }

    private void setupInitialImages() {
        final ImageButton ibMainPhoto = findViewById(R.id.mainphoto_btn);
        final LinearLayout llMainPhotoOptions = findViewById(R.id.mainphoto_options);
        final LinearLayout llMainPhotoEdit = findViewById(R.id.mainphoto_edit);
        final ImageButton ibMainPhotoDelete = findViewById(R.id.mainphoto_delete);
        ibMainPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(ibMainPhoto, llMainPhotoOptions, llMainPhotoEdit, ibMainPhotoDelete);
            }
        });
        final ImageButton ibFirstPhoto = findViewById(R.id.addphoto);
        final LinearLayout llFirstPhotoOptions = findViewById(R.id.photo_options);
        final LinearLayout llFirstPhotoEdit = findViewById(R.id.photo_edit);
        final ImageButton ibFirstPhotoDelete = findViewById(R.id.photo_delete);
        ibFirstPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(ibFirstPhoto, llFirstPhotoOptions, llFirstPhotoEdit, ibFirstPhotoDelete);
            }
        });
    }

    private void expandImage(final ImageButton imageButton) {
        Intent intent = new Intent(this, ImageActivity.class);

        if (imageButton.getTag() instanceof Uri) {
            intent.putExtra("BitmapUri", (Uri) imageButton.getTag());
        } else if (imageButton.getTag() instanceof String) {
            intent.putExtra("FirebaseUrl", (String) imageButton.getTag());
        }
        startActivity(intent);
    }

    // Load image based on uri or url. Either one should be null.
    private void setImage(final ImageButton imageButton, final LinearLayout photoOptions, final LinearLayout photoEdit, final ImageButton imageDelete, Uri uri, String url) {
        if (uri == null && url == null || uri != null && url != null) {
            Log.d(TAG, "Either uri or url should be null.");
            return;
        } else if (uri != null) {
            imageButton.setImageDrawable(null);
            imageButton.setImageURI(uri);
            imageButton.setTag(uri); // only set the uri tag if uploaded/took image from phone
            imageButton.setScaleType(ImageButton.ScaleType.FIT_XY);
        } else { // use url instead
            imageButton.setImageDrawable(null);
            Glide.with(ManageRecipe.this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference.child(url))
                    .into(imageButton);
            imageButton.setTag(url);
            imageButton.setScaleType(ImageButton.ScaleType.FIT_XY);
        }

        photoOptions.setVisibility(View.VISIBLE);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandImage(imageButton);
            }
        });

        photoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(imageButton, photoOptions, photoEdit, imageDelete);
            }
        });

        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage("Are you sure you want to delete this image?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int button) {
                                imageButton.setScaleType(ImageButton.ScaleType.CENTER);
                                photoOptions.setVisibility(View.GONE);
                                imageButton.setTag(null);
                                imageButton.setImageResource(android.R.drawable.ic_menu_camera);
                                imageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        chooseImage(imageButton, photoOptions, photoEdit, imageDelete);
                                    }
                                });
                            }
                        }).setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    private void chooseImage(final ImageButton imageButton, final LinearLayout photoOptions, final LinearLayout photoEdit, final ImageButton imageDelete) {
        PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult pickResult) {
                if (pickResult.getError() == null) {
                    setImage(imageButton, photoOptions, photoEdit, imageDelete, pickResult.getUri(), null);
                } else {
                    Toast.makeText(getApplicationContext(), pickResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).show(getSupportFragmentManager());
    }

    private Recipe createRecipe() {
        String recipeTitle = ((EditText) findViewById(R.id.recipe_title)).getText().toString();
        String recipeDesc = ((EditText) findViewById(R.id.recipe_desc)).getText().toString();
        String recipeServings = ((EditText) findViewById(R.id.servings)).getText().toString();
        String recipeTime = ((EditText) findViewById(R.id.time)).getText().toString();

        final ImageButton mainPhoto = findViewById(R.id.mainphoto_btn);
        final Uri mainPhotoUri = (mainPhoto.getTag() != null && mainPhoto.getTag() instanceof Uri) ? (Uri) mainPhoto.getTag() : null;

        List<Method> methods = new ArrayList<>();
        for (int i = 1; i <= methodNumber; i++) {
            final String tag = "method" + i;
            final View currView = rootView.findViewWithTag(tag);
            final EditText currIns = currView.findViewById(R.id.stepout);

            final ImageButton currImg = currView.findViewById(R.id.addphoto);

            if (currImg.getTag() != null) {
                if (currImg.getTag() instanceof Uri) {
                    final Uri imageUri = (Uri) currImg.getTag();
                    methods.add(new Method(currIns.getText().toString(), imageUri));
                } else {
                    String path = (String) currImg.getTag();
                    methods.add(new Method(currIns.getText().toString(), path));
                }
            } else {
                final Uri imageUri = null;
                methods.add(new Method(currIns.getText().toString(), imageUri));
            }
        }

        List<String> ingrs = new ArrayList<>();
        for (int i = 1; i <= ingrNumber; i++) {
            String tag = "ingr" + i;
            final View currView = rootView.findViewWithTag(tag);
            final EditText currIng = currView.findViewById(R.id.textout);
            ingrs.add(currIng.getText().toString());
        }

        List<String> tags = new ArrayList<>();
        for (int i = 1; i <= tagNumber; i++) {
            String tag = "tag" + i;
            final View currView = rootView.findViewWithTag(tag);
            final EditText currTag = currView.findViewById(R.id.textout);
            tags.add(currTag.getText().toString());
        }

        Recipe recipe = new Recipe(mainPhotoUri, fbUser.getUid(), recipeTitle, recipeDesc, recipeServings, recipeTime, ingrs, methods, tags);

        if (mainPhoto.getTag() != null && mainPhoto.getTag() instanceof String) {
            recipe.mainPhotoStoragePath = (String) mainPhoto.getTag();
        }
        return recipe;
    }

    private void publishRecipe(final Recipe recipe) {
        if (recipe.mainPhotoUri == null) {
            Toast.makeText(ManageRecipe.this, "You need to upload an image for the recipe.", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog = ProgressDialog.show(ManageRecipe.this, null, "Creating Recipe...");

        final UUID recipeId = UUID.randomUUID();
        final DocumentReference userDoc = fbFirestore.collection("users").document(fbUser.getUid());
        final DocumentReference recipesDoc = fbFirestore.collection("recipes").document(recipeId.toString());

        final OnSuccessListener<Void> finishStorage = new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Successfully created recipe!", Toast.LENGTH_LONG).show();
                finish();
            }
        };

        final OnSuccessListener<Void> storeImagesAndRecipeDoc = new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                numMethodImages = 0;
                for (int i = 0; i < recipe.methods.size(); i++) {
                    Method currMethod = recipe.methods.get(i);
                    if (currMethod.photoUri != null) numMethodImages++;
                }

                if (numMethodImages == 0) { // no method images, process main image then store updated recipe doc.
                    StorageReference ref = storageReference.child(recipe.mainPhotoStoragePath);
                    ref.putFile(recipe.mainPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            recipesDoc.set(recipe).addOnSuccessListener(finishStorage);
                        }
                    });
                } else { // contains method images, process all method images, then main image, then store updated recipe doc.
                    for (int i = 0; i < recipe.methods.size(); i++) {
                        Method currMethod = recipe.methods.get(i);
                        if (currMethod.photoUri == null)
                            continue; // skip if no image attached by user

                        String firebaseStorageFilePath = "images/" + UUID.randomUUID().toString();
                        currMethod.storagePath = firebaseStorageFilePath;

                        StorageReference ref = storageReference.child(firebaseStorageFilePath);
                        ref.putFile(currMethod.photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (--numMethodImages == 0) {
                                    StorageReference ref = storageReference.child(recipe.mainPhotoStoragePath);
                                    ref.putFile(recipe.mainPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            recipesDoc.set(recipe).addOnSuccessListener(finishStorage);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        };

        final OnCompleteListener<DocumentSnapshot> storeRecipeIdInUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> docData = document.getData();
                    final Map<String, Boolean> recipes = (docData.get("recipes") != null) ? (HashMap<String, Boolean>) docData.get("recipes") : new HashMap<String, Boolean>();
                    recipes.put(recipeId.toString(), true);
                    userDoc.update("recipes", recipes).addOnSuccessListener(storeImagesAndRecipeDoc);
                } else {
                    /* Else possible errors below
                    // !task.isSucessful(): document failed with exception: task.getException()
                    // task.getResult() == null: document does not exist
                    */
                }
            }
        };
        userDoc.get().addOnCompleteListener(storeRecipeIdInUser);
    }

    private void deleteRemovedImagesFromDB() {
        final String recipeId = getIntent().getExtras().getString("ID");
        final DocumentReference recipeDoc = fbFirestore.collection("recipes").document(recipeId);
        recipeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();
                    final Map<String, Object> oldRecipeData = document.getData();

                    // get recipe images to delete from the database and a list of new uris of images to upload to the database
                    final Set<String> deletedRecipeImages = new HashSet<>();
                    final List<Map<String, String>> list = (ArrayList<Map<String, String>>) oldRecipeData.get("methods");
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).get("storagePath") == null) continue;
                        deletedRecipeImages.add(list.get(i).get("storagePath"));
                    }

                    for (int i = 1; i <= methodNumber; i++) {
                        final String tag = "method" + i;
                        final View currView = rootView.findViewWithTag(tag);
                        final ImageButton currImg = currView.findViewById(R.id.addphoto);

                        if (currImg.getTag() != null && currImg.getTag() instanceof String) {
                            deletedRecipeImages.remove(currImg.getTag());
                        }
                    }

                    for (String s : deletedRecipeImages) {
                        StorageReference ref = storageReference.child(s);
                        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // do nothing
                            }
                        });
                    }

                    final String mainPhotoPath = (String) oldRecipeData.get("mainPhotoStoragePath");

                    final ImageButton mainPhoto = findViewById(R.id.mainphoto_btn);
                    if (mainPhoto.getTag() == null && mainPhoto.getTag() instanceof Uri) {
                        StorageReference ref = storageReference.child(mainPhotoPath);
                        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // do nothing
                            }
                        });
                    }
                }
            }
        });
    }

    private void editRecipeTask(final Recipe recipe) {
        final String recipeId = getIntent().getExtras().getString("ID");

        final DocumentReference recipeDoc = fbFirestore.collection("recipes").document(recipeId);
        // update ingrs
        recipeDoc.update("ingrs", recipe.ingrs).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // update tags
                recipeDoc.update("tags", recipe.tags).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // update methods
                        List<Map<String, Object>> methodsMapFormat = new ArrayList<>();
                        for (int i = 0; i < recipe.methods.size(); i++) {
                            Method currMethod = recipe.methods.get(i);

                            Map<String, Object> methodMapEntry = new HashMap<>();
                            methodMapEntry.put("instruction", currMethod.instruction);
                            methodMapEntry.put("storagePath", currMethod.storagePath);
                            methodsMapFormat.add(methodMapEntry);
                        }

                        recipeDoc.update("methods", methodsMapFormat).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // update title
                                recipeDoc.update("title", recipe.title).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // update description
                                        recipeDoc.update("description", recipe.description).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                recipeDoc.update("servings", recipe.servings).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        recipeDoc.update("time", recipe.time).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // update main storage path
                                                                recipeDoc.update("mainPhotoStoragePath", recipe.mainPhotoStoragePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(getApplicationContext(), "Successfully edited recipe!", Toast.LENGTH_LONG).show();
                                                                        finish();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void editRecipe() {
        final ImageButton mainPhoto = findViewById(R.id.mainphoto_btn);
        if (mainPhoto.getTag() == null) {
            Toast.makeText(ManageRecipe.this, "You need to upload an image for the recipe.", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog = ProgressDialog.show(ManageRecipe.this, null, "Editing Recipe...");

        deleteRemovedImagesFromDB();
        final Recipe recipe = createRecipe();

        final String recipeId = getIntent().getExtras().getString("ID");
        final DocumentReference recipeDoc = fbFirestore.collection("recipes").document(recipeId);

        final OnSuccessListener<UploadTask.TaskSnapshot> editRecipeTask = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                editRecipeTask(recipe);
            }
        };

        OnCompleteListener<DocumentSnapshot> storeImages = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    final DocumentSnapshot document = task.getResult();

                    numMethodImages = 0;
                    for (int i = 0; i < recipe.methods.size(); i++) {
                        Method currMethod = recipe.methods.get(i);
                        if (currMethod.photoUri != null) numMethodImages++;
                    }

                    if (numMethodImages == 0) {
                        if (mainPhoto.getTag() instanceof Uri) {
                            StorageReference ref = storageReference.child(recipe.mainPhotoStoragePath);
                            ref.putFile(recipe.mainPhotoUri).addOnSuccessListener(editRecipeTask);
                        } else {
                            // process the other stuff
                            editRecipeTask(recipe);

                        }

                    } else {
                        for (int i = 0; i < recipe.methods.size(); i++) {
                            Method currMethod = recipe.methods.get(i);
                            if (currMethod.photoUri == null)
                                continue; // skip if no image attached by user

                            String firebaseStorageFilePath = "images/" + UUID.randomUUID().toString();
                            currMethod.storagePath = firebaseStorageFilePath;

                            StorageReference ref = storageReference.child(firebaseStorageFilePath);
                            ref.putFile(currMethod.photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (--numMethodImages == 0) {
                                        if (mainPhoto.getTag() instanceof Uri) {
                                            StorageReference ref = storageReference.child(recipe.mainPhotoStoragePath);
                                            ref.putFile(recipe.mainPhotoUri).addOnSuccessListener(editRecipeTask);
                                        } else {
                                            editRecipeTask(recipe);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        };

        recipeDoc.get().addOnCompleteListener(storeImages);
    }
}