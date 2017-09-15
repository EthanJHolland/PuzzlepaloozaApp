package com.example.ethan.ppvii;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.TextView;

/**
 * Created by Ethan on 3/31/2016.
 */
public class LectionsPageAdapter extends FragmentStatePagerAdapter {
    private int numTab;
    private Tabbed tabbed;

    public LectionsPageAdapter(Tabbed tabbed, FragmentManager fm, int numTab) {
        super(fm);
        this.numTab=numTab;
        this.tabbed =tabbed;
    }

    @Override
    public int getItemPosition(Object o){
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        switch(position){
            case 0:
                return tabbed.getLeaderboard();
            case 1:
                return tabbed.getByProb();
            case 2:
                return tabbed.getByTeam();
            case 3:
                return Messages.createMessages(tabbed);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTab;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "SECTION 1";
            case 1:
                return "SECTION 2";
            case 2:
                return "SECTION 3";
        }
        return null;
    }
}

