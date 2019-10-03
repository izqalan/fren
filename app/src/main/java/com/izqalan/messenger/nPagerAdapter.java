package com.izqalan.messenger;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class nPagerAdapter extends FragmentPagerAdapter {


    public nPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch(i){
            case 0:
                PostingFragment postingFragment = new PostingFragment();
                return postingFragment;
            case 1:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 2:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 3:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;

        }
    }

    // number of tabs
    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    // title of the tabs on the main page
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Collab";
            case 1:
                return "Request";
            case 2:
                return "Chats";
            case 3:
                return "Friends";

                default:
                    return null;
        }

    }
}
