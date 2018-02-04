package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

public class CreateRecipe extends AppCompatActivity {

    //tags
    EditText textIn;
    Button buttonAdd;
    LinearLayout container;
    int count = 0;

    //methods
    Button addMethod;
    LinearLayout methods;
    int number = 1;

    //ingredients
    Button ingAdd;
    LinearLayout ingCont;

    //publish
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cr);

        // both editing and creating recipe share this code
        setupIngredients();
        setupMethods();
        setupTags();
        setupInitialImages();

        // publishing and editing logic differs here (mostly for data submission)
        boolean isEdit = editOrCreate();
        if (isEdit) {
            setupEdit();
        } else {
            setupPublish();
        }
    }

    // returns true if editing an existing recipe, false if creating a new recipe
    private boolean editOrCreate() {
        Bundle extras = getIntent().getExtras();
        return extras != null && extras.getBoolean("EDIT", false);
    }

    private void setupEdit() {
        // populate existing data into the appropriate fields

        // set submit button to update the current entry
        submitButton = findViewById(R.id.pub);
        submitButton.setText("Edit");
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setupPublish() {
        //stuff for publish
        submitButton = findViewById(R.id.pub);

        submitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                //upload to DB
            }});
    }

    private void setupIngredients() {
        ingAdd = findViewById(R.id.adding);
        ingCont = findViewById(R.id.ing_cont);

        ingAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.activity_cr_row, null);

                Button ingRemove = (Button)addView.findViewById(R.id.remove);
                ingRemove.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }});

                ingCont.addView(addView);
        }});
    }

    private void setupMethods() {
        addMethod = (Button)findViewById(R.id.addmethod);
        methods = (LinearLayout)findViewById(R.id.mthd_cont);

        addMethod.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                final int stepNum = ++number;

                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.activity_cr_method, null);

                TextView tvStepNum = (TextView)addView.findViewById(R.id.num);
                tvStepNum.setText(Integer.toString(stepNum));

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

                Button method_Remove = (Button)addView.findViewById(R.id.remove_mth);
                method_Remove.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        --number;
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }});

                methods.addView(addView);
            }});
    }

    private void setupTags() {
        textIn = findViewById(R.id.textin);
        buttonAdd = findViewById(R.id.add);
        container = findViewById(R.id.container);

        buttonAdd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                ++count;
                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.activity_cr_row, null);
                TextView textOut = (TextView)addView.findViewById(R.id.textout);
                textOut.setText(textIn.getText().toString());
                textIn.setText("");
                container.setVisibility(View.VISIBLE);
                Button buttonRemove = (Button)addView.findViewById(R.id.remove);
                buttonRemove.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        --count;
                        if (count <= 0)
                        {
                            container.setVisibility(View.GONE);
                        }
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }});

                container.addView(addView);
            }});
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
        intent.putExtra("BitmapUri", (Uri) imageButton.getTag());
        startActivity(intent);
    }

    private void chooseImage(final ImageButton imageButton, final LinearLayout photoOptions, final LinearLayout photoEdit, final ImageButton imageDelete) {
        PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult pickResult) {
                if (pickResult.getError() == null) {
                    photoOptions.setVisibility(View.VISIBLE);

                    imageButton.setImageURI(pickResult.getUri());
                    imageButton.setTag(pickResult.getUri());
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
                                    photoOptions.setVisibility(View.GONE);
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
                } else {
                    Toast.makeText(getApplicationContext(), pickResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).show(getSupportFragmentManager());
    }
}
