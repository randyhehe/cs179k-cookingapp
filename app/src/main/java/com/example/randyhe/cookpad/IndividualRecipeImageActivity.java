package com.example.randyhe.cookpad;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class IndividualRecipeImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("BitmapUri");
        if (uri != null) {
            setImageFromBitmap(uri);
        }
    }

    private void setImageFromBitmap(Uri uri) {
        ImageView iv = findViewById(R.id.image);
        Glide.with(IndividualRecipeImageActivity.this)
                .load(uri.toString())
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

