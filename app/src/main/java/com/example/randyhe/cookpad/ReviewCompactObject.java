package com.example.randyhe.cookpad;

/**
 * Created by Monica on 3/8/18.
 */

public class ReviewCompactObject {
    public String recipeId;
    public String reviewDate;
    public String photoOne;
    public String photoTwo;
    public String photoThree;
    public float reviewStars;
    public String reviewText;
    public String reviewPublisher;
    public String reviewPublisherPhotoPath;
    public long comparatorValue;

    public ReviewCompactObject(String recipeId, String reviewDate, String photoOne, String photoTwo, String photoThree, float reviewStars, String reviewText, String reviewPublisher, String reviewPublisherPhotoPath, long comparatorValue) {
        this.recipeId = recipeId;
        this.reviewDate = reviewDate;
        this.photoOne = photoOne;
        this.photoTwo = photoTwo;
        this.photoThree = photoThree;
        this.reviewStars = reviewStars;
        this.reviewText = reviewText;
        this.reviewPublisher = reviewPublisher;
        this.reviewPublisherPhotoPath = reviewPublisherPhotoPath;
        this.comparatorValue = comparatorValue;
    }
}
