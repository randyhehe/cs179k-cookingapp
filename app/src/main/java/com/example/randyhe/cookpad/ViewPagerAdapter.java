package com.example.randyhe.cookpad;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

class ViewPagerAdapter extends SmartFragmentStatePagerAdapter {
    private static int NUM_ITEMS = 4;

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return feedFragment.newInstance();
            case 1:
                return profileFragment.newInstance();
            case 2:
                return exploreFragment.newInstance();
            case 3:
                return bookmarkFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
}