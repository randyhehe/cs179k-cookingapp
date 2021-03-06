package com.example.randyhe.cookpad;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mcast on 2/5/2018.
 * Class needed to retrieve information from user collection in firebase
 */

@IgnoreExtraProperties
public class User {
    private String profilePhotoPath;
    private String username;
    private String name;
    private String email;
    private String bio;
    private String test;
    private Map<String, Boolean> recipes;
    private Map<String, Boolean> followers;
    private Map<String, Boolean> following;
    private Map<String, Long> bookmarkedRecipes;

    public User() {}

//    Set functions
    public void setUsername(String s) { this.username = s; }
    public void setName(String s) { this.name = s; }
    public void setEmail(String s) { this.email = s; }
    public void setBio(String s) { this.bio = s; }
    public void setProfilePhotoPath(String s) { this.profilePhotoPath = s; }

//    Get functions
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }

    public Map<String, Long> getBookmarkedRecipes() {
        if (bookmarkedRecipes == null) {
            bookmarkedRecipes = new HashMap<String, Long>();
        }
        return bookmarkedRecipes;
    }

    public String getProfilePhotoPath() { return profilePhotoPath; }
    public List<String> getRecipes() {
        if(recipes != null) {
            return new ArrayList<String>(this.recipes.keySet());
        }
        return new ArrayList<String>();
    }
    public List<String> getFollowers() {
        if(followers != null ) {
            return new ArrayList<String>(this.followers.keySet());
        }
        return new ArrayList<String>();
    }
    public List<String> getFollowing() {
        if(following != null) {
            return new ArrayList<String>(this.following.keySet());
        }
        return new ArrayList<String>();
    }
    public int getNumFollowers() {
        if(this.followers == null) { return 0; }
        return followers.size();
    }
    public int getNumFollowing() {
        if(this.following == null) { return 0; }
        return following.size();
    }
    public int getNumRecipes() {
        if(this.recipes == null) { return 0; }
        return recipes.size();
    }


}
