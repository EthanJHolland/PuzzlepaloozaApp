package com.example.ethan.ppvii;

import android.view.View;
import android.widget.TextView;

/**
 * Created by Ethan on 7/17/2016.
 * Abstract class which all Fragments must extend for consistency in method naming and functionality
 */
public abstract class PPFragment extends android.support.v4.app.Fragment {
    protected Tabbed tabbed; //instance of tabbed required for accessing other classes and constants

    /**
     *  something has gone terribly wrong and the app is disabled until it can be fixed
     *  perhaps a security problem or something of the like
     *  therefore, simply show a message so that no information is displayed
     * @return a textview with a message indicating the lack of functioning
     */
    protected View displayNotFunctioning(){
        TextView tv = new TextView(tabbed);
        tv.setText(getString(R.string.not_working));
        return tv;
    }

    /**
     * something has changed so the views should be refreshed
     */
    protected void refresh(){
        tabbed.refreshTabs();
    }
}
