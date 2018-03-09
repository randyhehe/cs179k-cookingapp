package com.example.randyhe.cookpad;

/**
 * Created by randyhe on 3/5/18.
 */

public class RecipeCompactObject {
    public String recipeId;
    public String recipeTitle;
    public String recipeTimeToCook;
    public String recipeServings;
    public String recipeDescription;
    public String recipeMainPhotoPath;
    public String recipePublisher;
    public String recipePublisherPhotoPath;
    public long comparatorValue;
    public float recipeAvgRating;

    public RecipeCompactObject(String recipeId, String recipeTitle, String recipeTimeToCook, String recipeServings, String recipeDescription, String recipeMainPhotoPath, String recipePublisher, String recipePublisherPhotoPath, float recipeAvgRating, long comparatorValue) {
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        this.recipeTimeToCook = recipeTimeToCook;
        this.recipeServings = recipeServings;
        this.recipeDescription = recipeDescription;
        this.recipeMainPhotoPath = recipeMainPhotoPath;
        this.recipePublisher = recipePublisher;
        this.recipePublisherPhotoPath = recipePublisherPhotoPath;
        this.comparatorValue = comparatorValue;
        this.recipeAvgRating = recipeAvgRating;
    }
}
