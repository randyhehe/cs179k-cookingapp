package com.example.randyhe.cookpad;

/**
 * Created by Asus on 3/6/2018.
 */

public class FeedIndividualRecipe
{
    public String recipeID;
    public String userID;
    public boolean isBookmark;
    public String profileUrl;
    public long comparatorVal;

        public FeedIndividualRecipe(String recipeID, String userID, boolean isBookmark, String profileUrl, long comparatorVal)
        {
            this.recipeID = recipeID;
            this.userID = userID;
            this.isBookmark = isBookmark;
            this.profileUrl = profileUrl;
            this.comparatorVal = comparatorVal;
        }
}



/////////////////////////////////////version where most firebase calls in feed fragment

//public class FeedIndividualRecipe
//{
//    public String recipeID;
//    public String profileUrl;
//    public String recipeName;
//    public String recipeDesc;
//    public String recipePoster;
//    public String notificationDesc;
//    public String recipeUrl;
//    public float rating;
//    public boolean beenBookmarked;
//    public long comparatorVal;
//    public boolean isBookmark;
//    public String userID;
//
//    public FeedIndividualRecipe(String recipeID,String profileUrl,String recipeName,String recipeDesc,String recipePoster,String notificationDesc,String recipeUrl,float rating,boolean beenBookmarked, long comparatorVal, boolean isBookmark, String userID)
//    {
//        this.recipeID = recipeID;
//        this.profileUrl = profileUrl;
//        this.recipeName = recipeName;
//        this.recipeDesc = recipeDesc;
//        this.recipePoster = recipePoster;
//        this.notificationDesc = notificationDesc;
//        this.recipeUrl = recipeUrl;
//        this.rating = rating;
//        this.beenBookmarked = beenBookmarked;
//        this.comparatorVal = comparatorVal;
//        this.isBookmark = isBookmark;
//        this.userID = userID;
//    }
//}
