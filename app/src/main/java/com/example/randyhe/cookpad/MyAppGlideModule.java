package com.example.randyhe.cookpad;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

/**
 * Created by mcast on 2/18/2018.
 */

public class MyAppGlideModule extends AppGlideModule {
//    @Override
//    public void registerComponents(Context context, Glide glide, Registry registry) {
//        // Register FirebaseImageLoader to handle StorageReference
//        registry.append(StorageReference.class, InputStream.class,
//                new FirebaseImageLoader.Factory());
//    }
}
