package com.izqalan.messenger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    public ProfilePagerAdapter(FragmentManager fm){ super(fm);}

    public Fragment getItem(int i) {
        switch(i){
            case 0:
                return new FriendsFragment();
            case 1:
//                TODO: Make user post fragment !! chat Fragment is a temp placeholder

                return new PostHistoryFragment();

            default:
                return null;

        }
    }

    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Friends";
            case 1:
                return "Posts history";

            default:
                return null;
        }

    }
}
