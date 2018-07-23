package com.infobox.hasnat.ume.ume.Utils;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.infobox.hasnat.ume.ume.Fragments.ChatsFragment;
import com.infobox.hasnat.ume.ume.Fragments.FriendsFragment;
import com.infobox.hasnat.ume.ume.Fragments.RequestsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter{


    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3; // 3 is total fragment number (e.x- Friends, Chats, Requests)
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return ""; // ChatsFragment

            case 1:
                return ""; // RequestsFragment

            case 2:
                return ""; // FriendsFragment

            default:
                return null;
        }


        //return super.getPageTitle(position);
    }
}
