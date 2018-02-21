package com.example.randyhe.cookpad;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("BitmapUri");
        String url = intent.getStringExtra("FirebaseUrl");
        if (uri != null) {
            setImageFromBitmap(uri);
        } else if (url != null) {
            Log.d("test", url);
            setImageFromFirebaseUrl(url);
        }
    }

    private void setImageFromBitmap(Uri uri) {
        ImageView iv = findViewById(R.id.image);
        iv.setImageURI(uri);
    }

    private void setImageFromFirebaseUrl(String url) {
        ImageView iv = findViewById(R.id.image);

        FirebaseApp.initializeApp(this);
        FirebaseStorage fbStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = fbStorage.getReference();
        Glide.with(ImageActivity.this)
                .using(new FirebaseImageLoader())
                .load(storageReference.child(url))
                .into(iv);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id =  item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
