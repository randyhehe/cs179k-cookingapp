package com.example.randyhe.cookpad;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by Asus on 1/30/2018.
 * https://stackoverflow.com/questions/42781409/restoring-fragment-state-when-changing-fragments-through-bottom-navigation-bar
 * Credit: Code mainly from Harsh Ganatra
 */

public class CustomViewPager extends ViewPager {

    private boolean isPagingEnabled;

    public CustomViewPager(Context context) {
        super(context);
        this.isPagingEnabled = true;
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isPagingEnabled = true;
    }

    public void setPagingEnabled(boolean enabled) {
        this.isPagingEnabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }
}