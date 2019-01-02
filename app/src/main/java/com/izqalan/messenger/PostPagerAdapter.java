package com.izqalan.messenger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PostPagerAdapter extends FragmentPagerAdapter {


    public PostPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch(i){
            case 0:
                return new ChecklistFragment();
            case 1:
                return new MeetupFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Items checklist";
            case 1:
                return "Meet-up";

            default:
                return null;
        }

    }
}
