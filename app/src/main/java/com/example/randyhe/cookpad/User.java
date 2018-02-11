package com.example.randyhe.cookpad;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Created by mcast on 2/5/2018.
 * Class needed to retrieve information from user collection in firebase
 */

@IgnoreExtraProperties
public class User {
    private String username;
    private String name;
    private String email;
    private String bio;

    public User() {}

//    Set functions
    public void setUsername(String s) { this.username = s; }
    public void setName(String s) { this.name = s; }
    public void setEmail(String s) { this.email = s; }
    public void setBio(String s) { this.bio = s; }

//    Get functions
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
}
