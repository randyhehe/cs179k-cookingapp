package com.example.randyhe.cookpad;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by randyhe on 2/5/18.
 */

public class Recipe {
    @Exclude public Uri mainPhotoUri;
    public String mainPhotoStoragePath;
    public String userId;
    public String title;
    public String desciption;
    public List<String> ingrs;
    public List<Method> methods;
    public List<String> tags;
    public List<String> reviews;

    public Recipe() {} // Needed for Firebase

    public Recipe(Uri mainPhotoUri, String userId, String title, String description, List<String> ingrs, List<Method> methods, List<String> tags) {
        this.mainPhotoUri = mainPhotoUri;
        this.mainPhotoStoragePath = null;
        this.userId = userId;
        this.title = title;
        this.desciption = description;
        this.ingrs = ingrs;
        this.methods = methods;
        this.tags = tags;
        this.reviews = new ArrayList<>();
    }
}
