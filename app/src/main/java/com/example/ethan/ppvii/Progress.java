package com.example.ethan.ppvii;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class Progress extends PPFragment {
    private Team team=null;

    public static Progress createProgress(Tabbed tabbedIn){
        Progress progress=new Progress();
        progress.tabbed=tabbedIn;
        return progress;
    }

    /**
     * Create a view showing the name and relevant information for the team that the user has logged into
     * as well as a list of the problems where each row shows
     *  problem number, whether it has been solved, points earned if solved, and hints bought
     *  if a problem is clicked on, it navigates to the appropriate Submit page
     * @param inflater the LayoutInflator used to inflate layouts from xml
     * @param container the ViewGroup used for inflating layouts
     * @param savedInstanceState unused
     * @return the view to be displayed
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!tabbed.constants().functioning())return displayNotFunctioning();

        View v;
        if(tabbed.isModeAnon()){
            //no progress to show, allow the anon user to go back to the login screen
            TextView tv=new TextView(getActivity());
            tv.setText(getString(R.string.progress_unavailableforanon));
            v=tv;
        }else if(team==null){
            //make sure that the information has loaded to avoid a null pointer exception
            TextView waitingTv=new TextView(getActivity());
            waitingTv.setText(getString(R.string.progess_notyetloaded));
            v=waitingTv;
        }else{
            //show a list of problems
            v=displayQuestionList(inflater, container);
        }

        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        linearLayout.addView(v);
        Button b=new Button(getActivity());
        b.setText(getString(R.string.returntologin_button));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getContext(), Login.class);
                startActivity(loginIntent);
            }
        });
        linearLayout.addView(b);
        return linearLayout; //return the view
    }

    private View displayQuestionList(LayoutInflater inflater, ViewGroup container){
        //information has been loaded so display it
        View v=inflater.inflate(R.layout.fragment_progress, container, false); //inflate the layout

        //set the text for the various labels
        TextView titleTv= (TextView) v.findViewById(R.id.prog_title);
        titleTv.setText(team.getName()); //set the name label
        TextView moneyTv= (TextView) v.findViewById(R.id.prog_money);
        String temp; //android does not like building strings in the .setText(method)
        temp="money: " + team.getMoney() + "";
        moneyTv.setText(temp); //display the money remaining
        TextView rankTv= (TextView) v.findViewById(R.id.prog_rank);
        temp="rank: " + team.rank(tabbed.getLeaderboard().team);
        rankTv.setText(temp); //display the rank
        TextView ptsTv= (TextView) v.findViewById(R.id.prog_pts);
        temp="points: " + team.totalPoints() + "";
        ptsTv.setText(temp); //display the points
        TextView solvedTv= (TextView) v.findViewById(R.id.prog_solved);
        temp="solved: " + team.numSolved() + "/" + tabbed.constants().NUM_PROB;
        solvedTv.setText(temp); //display the number of points earned

        //populate the listview of problems
        ListView probsLv=(ListView) v.findViewById(R.id.prog_lv); //inflate the listview

        //convert the hashmap to an arraylist to be passed to the arrayadapter
        ArrayList<Problem> probList=new ArrayList<>();
        for(int i=1; i<=tabbed.constants().NUM_PROB; i++) probList.add(team.getProb(i)); //put values in arraylist in ascending order

        //initialize custom problem array adapter
        ProblemArrayAdapter problemArrayAdapter=new ProblemArrayAdapter(getContext(), probList);
        probsLv.setAdapter(problemArrayAdapter);
        probsLv.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //set the focusability so that the listview can register clicks

        //set the on item click listener
        probsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * change the view to the submit page for the problem selected
             * @param parent unused
             * @param view unused
             * @param position the position in the arraylist which is 1 less than the problem number
             * @param id unused
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tabbed.getSubmit().initialize(position+1);
                tabbed.prog = false; //used to inform the SectionsPageAdapter that it should show submit not progress
                refresh(); //make sure Tabbed is refreshed to show that the page has been changed
            }
        });
        return v;
    }

    /**
     * update the team object when one of its values is changed on firebase
     * @param teamIn the updated team object
     */
    public void refreshData(Team teamIn){
        team=teamIn;
    }
}

/**
 * A subclass of ArrayAdapter which takes an Arraylist of Problems and displays them in a formatted fashion
 * Each row shows the problem number, whether it is solved, and the hints bought
 */
class ProblemArrayAdapter extends ArrayAdapter<Problem> {
    private final ArrayList<Problem> values; //the arraylist of submissions passed in to be adapted
    private LayoutInflater inflater; //the inflater used to inflate the row layout

    /**
     * Constructor
     * initialize the arrayadapter to hold problems
     * @param valuesIn the arraylist of problems to be adapted
     */
    public ProblemArrayAdapter(Context contextIn, ArrayList<Problem> valuesIn) {
        super(contextIn, R.layout.list_item_progress, valuesIn); //call super constructor
        values = valuesIn;
        inflater = (LayoutInflater) contextIn.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //get the appropriate layout inflater
    }

    /**
     * Create a view which shows the problem number, whether it is solved, and the hints bought
     * @param position the position of the row in the arraylist
     * @param convertView used for inflation
     * @param parent used for inflation
     * @return a view showing the problem number, whether or not it has been solved, and the hints bought
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Problem curr=values.get(position); //get the appropriate Problem object

        // Get view for row item
        View rowView = inflater.inflate(R.layout.list_item_progress, parent, false); //inflate layout

        String temp;
        TextView probnum=(TextView) rowView.findViewById(R.id.prog_probnum);
        probnum.setText(position + 1 + ""); //plus one since Problems start at 1 and arraylist starts at 0

        TextView solvedtv=(TextView) rowView.findViewById(R.id.prog_issolved);
        if(curr.hasSolved()){
            //problem solved
            temp="solved (" + curr.getPoints() + " pts.)";
            solvedtv.setText(temp);
            rowView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_green));
        }else{
            //problem not yet solved
            solvedtv.setText("not yet solved");
            rowView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_red));
        }

        CheckBox cbs= (CheckBox) rowView.findViewById(R.id.prog_s_cb);
        cbs.setChecked(curr.hasBoughtSmall());
        CheckBox cbm= (CheckBox) rowView.findViewById(R.id.prog_m_cb);
        cbm.setChecked(curr.hasBoughtMed());
        CheckBox cbl= (CheckBox) rowView.findViewById(R.id.prog_l_cb);
        cbl.setChecked(curr.hasBoughtLarge());

        /*
        do not delete these lines
        these lines are necessary for the ListView OnItemClickListener to work
        the listener will not work if the rows contain focusable items
         */
        cbs.setFocusable(false);
        cbm.setFocusable(false);
        cbl.setFocusable(false);

        return rowView;
    }

}