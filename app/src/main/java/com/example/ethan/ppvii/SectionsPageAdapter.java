package com.example.ethan.ppvii;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ethan on 3/31/2016.
 */
public class SectionsPageAdapter extends FragmentStatePagerAdapter {
    private int numTab;
    private Tabbed tabbed;

    public SectionsPageAdapter(Tabbed tabbed, FragmentManager fm, int numTab) {
        super(fm);
        this.numTab=numTab;
        this.tabbed=tabbed;
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
                if(tabbed.isModePuzzleLord()){
                    return tabbed.getQueue();
                }else if(tabbed.prog){
                    return tabbed.getProgress();
                }else{
                    return tabbed.getSubmit();
                }
            case 1:
                return tabbed.getLabbed();
            case 2:
                return tabbed.getMessages();
            case 3:
                return tabbed.getEvents();
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
            case 3:
                return "SECTION 4";
        }
        return null;
    }
}

