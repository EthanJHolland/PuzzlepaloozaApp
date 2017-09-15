package com.example.ethan.ppvii;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Labbed extends PPFragment {
    private ViewPager mViewPager;
    public LectionsPageAdapter mLPA;

    public static Labbed createLabbed(Tabbed tabbedIn){
        Labbed l=new Labbed();
        l.tabbed=tabbedIn;
        return l;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_labbed, container, false); //inflate the layout

        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.l_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Leaderboard"));
        tabLayout.addTab(tabLayout.newTab().setText("ByProb"));
        tabLayout.addTab(tabLayout.newTab().setText("ByTeam"));
        tabLayout.addTab(tabLayout.newTab().setText("temp"));


        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        mViewPager = (ViewPager) v.findViewById(R.id.l_pager);
        mLPA = new LectionsPageAdapter(tabbed, tabbed.getSupportFragmentManager(), tabLayout.getTabCount());

        mViewPager.setAdapter(mLPA);
        //mViewPager.setOffscreenPageLimit(2); //ensure that all 3 tabs are loaded at all times for speed

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return v;
    }
}