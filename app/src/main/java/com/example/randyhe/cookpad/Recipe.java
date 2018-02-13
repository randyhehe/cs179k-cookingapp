package com.example.randyhe.cookpad;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by randyhe on 2/5/18.
 */

public class Recipe {
    @Exclude public Uri mainPhotoUri;
    public String userId;
    public String title;
    public String description;
    public String servings;
    public String time;
    public List<String> ingrs;
    public List<Method> methods;
    public List<String> tags;

    public List<String> reviews;
    public Long timeCreated;
    public String mainPhotoStoragePath;


    public Recipe() {} // Needed for Firebase

    public Recipe(Uri mainPhotoUri, String userId, String title, String description, String servings, String time,  List<String> ingrs, List<Method> methods, List<String> tags) {
        this.mainPhotoUri = mainPhotoUri;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.servings = servings;
        this.time = time;

        this.ingrs = ingrs;
        this.methods = methods;
        this.tags = tags;

        this.reviews = new ArrayList<>();
        this.mainPhotoStoragePath = "images/" + UUID.randomUUID().toString();
        this.timeCreated = new Date().getTime();
    }
}
