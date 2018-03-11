package com.example.randyhe.cookpad;

/**
 * Created by Asus on 3/8/2018.
 */

public class recipeVals {
    public boolean isBookmark;
    public long compVal;
    public String user;
    public String photoPath;

    public recipeVals(boolean isBookmark, long compVal,String user,String photoPath)
    {
        this.isBookmark = isBookmark;
        this.compVal = compVal;
        this.user = user;
        this.photoPath = photoPath;
    }
}
