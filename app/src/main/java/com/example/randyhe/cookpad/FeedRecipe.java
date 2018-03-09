package com.example.randyhe.cookpad;

/**
 * Created by Asus on 3/6/2018.
 */

public class FeedRecipe {
    public String recipeId;
    public String userId;
    public boolean isBookmark;
    public String profileUrl;
    public long comparatorValue;

    public FeedRecipe(String recipeID,String userID,boolean isBookmark,String profileUrl, long comparatorValue) {
        this.recipeId = recipeID;
        this.userId = userID;
        this.isBookmark = isBookmark;
        this.profileUrl = profileUrl;
        this.comparatorValue = comparatorValue;
    }
}
