package com.example.ethan.ppvii;

/**
 * Created by Ethan on 7/14/2016.
 */

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Leaderboard extends PPFragment {
    //determines the proportion of the screen taken by the row and col
    public static final double COL_WIDTH_FRAC=1/5.0;
    public static final double ROW_HEIGHT_FRAC=1/20.0;

    public int itemw, itemh;     //the width and height of an individual square in the grid representing one team's progress on one problem

    //used for synching scrolling
    public int xPos; //the absolute position of the scrolls
    public int dx=0; //the change in position

    //TODO: v2.0 make these linked lists for most efficient storage
    public ArrayList<RecyclerView> hlist=new ArrayList<>(); //store all recycler views that must be scrolled horizontally so they can be synched

    //store the row and column subviews and the upper left spacerso they can be sized appropriately after the grid elements are measured
    public ArrayList<TextView> rowSubviews=new ArrayList<>();
    public ArrayList<TextView> colSubviews=new ArrayList<>();
    public TextView label;

    //store the teams
    public Team[] team;
    public Team[] sorted; //a sorted copy of the teams array which is updated whenever team is updated

    public RecyclerView.OnScrollListener onHorizScroll=new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dxnew, int dynew) {
            super.onScrolled(recyclerView, dxnew, dynew);
            if(dxnew==dx)return;
            dx=dxnew;
            xPos+=dx;
            updateHorizScrolls(recyclerView);
        }
    };

    public static Leaderboard createLeaderboard(Tabbed tabbedIn){
        Leaderboard lead=new Leaderboard();
        lead.tabbed=tabbedIn;
        return lead;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //TODO: v1.0 app loading screen (see https://developer.xamarin.com/guides/android/user_interface/creating_a_splash_screen/)
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!tabbed.constants().functioning())return displayNotFunctioning();

        System.out.println("leader");

        //ensure necessary data has been loaded
        if(!tabbed.constants().isInitialized() || !tabbed.leaderboardLoaded){
            TextView tv = new TextView(getActivity());
            tv.setText(getString(R.string.leaderboard_notyetloaded));
            return tv;
        }

        //otherwise data has been loaded
        //make the views
        View v=inflater.inflate(R.layout.fragment_leaderboard, container, false);

        //make the body (bottom right)
        makeBody((RecyclerView) v.findViewById(R.id.plead_body));

        //make the scrolling column (bottom left)
        makeCol((RecyclerView) v.findViewById(R.id.plead_col));

        //make the scrolling row (top right)
        makeRow((RecyclerView) v.findViewById(R.id.plead_row));

        //make the upper left label to align everything
        makeLabel((TextView) v.findViewById(R.id.plead_tv));

        return v;
    }

    public void makeLabel(TextView tv){
        tv.setWidth(100);
        tv.setHeight(100);
        label=tv;
    }

    public void makeRow(RecyclerView row){

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutManager(llm);

        String[] probNumArray=new String[tabbed.constants().NUM_PROB];
        for(int i=1; i<=probNumArray.length; i++){
            probNumArray[i-1]=i+"";
        }

        TextArrayAdapter taa=new TextArrayAdapter(probNumArray, rowSubviews); //must set width to 1 not 0 so that the recylerview has a size
        row.setAdapter(taa);

        row.addOnScrollListener(onHorizScroll);
        hlist.add(row);
    }


    public void makeCol(RecyclerView col){
        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutManager(llm);

        String[] teamNameArray=new String[team.length];
        for(int i=0; i<teamNameArray.length; i++)teamNameArray[i]=sorted[i].getNickname();

        TextArrayAdapter taa=new TextArrayAdapter(teamNameArray, colSubviews);
        col.setAdapter(taa);
    }

    public void makeBody(RecyclerView outer){
        InnerArrayAdapter.first=true; //make sure that the first view will be measured

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayout.VERTICAL);
        outer.setLayoutManager(llm);

        OuterArrayAdapter caa=new OuterArrayAdapter(sorted, tabbed);
        outer.setAdapter(caa);
    }

    public void updateHorizScrolls(RecyclerView curr){
        for(RecyclerView rv: hlist){
            ((LinearLayoutManager)rv.getLayoutManager()).scrollToPositionWithOffset(xPos/itemw, -xPos% itemw); //offset moves to the left but we want it to be to the right hence the negative
        }
        dx=0; //this will prevent the onScrollListeners from being triggered for all the other rvs
    }


    /**
     * sort the teams based on the built in team compareTo and store in a seperate sorted array
     * this sorted array is in descending order since recyclerview starts at position 1
     */
    private void sort(){
        if(team==null)return; //make sure values has been initialized
        sorted=new Team[team.length];
        for(int i=0; i<sorted.length; i++)sorted[i]=team[i];
        Arrays.sort(sorted, new Comparator<Team>() {
            @Override
            public int compare(Team lhs, Team rhs) {
                return -lhs.compareTo(rhs); //reverse the normal compare so it is in descending order not ascending
            }
        });
    }

    public void refreshData(Team[] teamIn){
        team=teamIn;
        sort(); //refresh the sorted array as well
        if(tabbed.isModeTeam())tabbed.getProgress().refreshData(teamIn[tabbed.mode - 1]);
        tabbed.leaderboardLoaded=true;   //allow program to continue
    }
}

class OuterArrayAdapter extends RecyclerView.Adapter<OuterArrayAdapter.OuterViewHolder>{
    private Team[] values;
    private final Tabbed tabbed;

    public OuterArrayAdapter(Team[] teamIn, Tabbed tabbedIn){
        values=teamIn;
        tabbed =tabbedIn;
    }

    @Override
    public OuterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_recyclerview, parent, false);
        return new OuterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OuterViewHolder holder, int position) {
        RecyclerView inner=holder.rv;
        final LinearLayoutManager llm=new LinearLayoutManager(tabbed);
        llm.setOrientation(LinearLayout.HORIZONTAL);
        inner.setLayoutManager(llm);

        InnerArrayAdapter raa=new InnerArrayAdapter(values[position], tabbed); //pass in the current team
        inner.setAdapter(raa);

        inner.addOnScrollListener(tabbed.getLeaderboard().onHorizScroll);
        tabbed.getLeaderboard().hlist.add(inner);
    }

    @Override
    public int getItemCount() {
        return values.length;
    }

    class OuterViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView rv;

        public OuterViewHolder(View itemView) {
            super(itemView);
            rv=(RecyclerView)itemView.findViewById(R.id.simple_rv);
        }
    }
}

class InnerArrayAdapter extends RecyclerView.Adapter<InnerArrayAdapter.InnerViewHolder>{
    private Team team;
    private final Tabbed tabbed;
    public static boolean first=true;

    public InnerArrayAdapter(Team teamIn, Tabbed tabbedIn){
        team=teamIn;
        tabbed=tabbedIn;
    }

    @Override
    public InnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.table_item_lb, parent, false);
        if(first) {
            first = false;
            ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        v.getViewTreeObserver().removeOnGlobalLayoutListener(this); //make sure this only happens once by removing the listener

                        Leaderboard lead=tabbed.getLeaderboard();
                        int w=v.getWidth();
                        int h=v.getHeight();
                        lead.itemh =h;
                        lead.itemw =w;

                        //find screen dimensions
                        Display display = tabbed.getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int sw = size.x;
                        int sh = size.y;

                        for(TextView tv: lead.rowSubviews){

                            tv.setWidth(w);
                            tv.setHeight((int) (sh * Leaderboard.ROW_HEIGHT_FRAC));
                            tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                        }
                        for(TextView tv: lead.colSubviews){
                            tv.setHeight(h);
                            tv.setWidth((int) (sw * Leaderboard.COL_WIDTH_FRAC));
                            tv.setGravity(Gravity.CENTER_VERTICAL);
                        }

                        lead.label.setWidth((int)(sw*Leaderboard.COL_WIDTH_FRAC));
                        lead.label.setHeight((int)(sh*Leaderboard.ROW_HEIGHT_FRAC));
                    }
                });
            }
        }

        return new InnerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InnerViewHolder holder, int position) {
        Problem p=team.getProb(position+1); //problem numbers start at 1 but position starts at 0 so add 1
        String ptsText=p.getPoints()+"";
        int maxDigits=tabbed.constants().maxDigits();
        while(ptsText.length()<maxDigits)ptsText="0"+ptsText;

        //set values to holder fields
        holder.pts.setText(ptsText);
        holder.small.setChecked(p.hasBoughtSmall());
        holder.med.setChecked(p.hasBoughtMed());
        holder.large.setChecked(p.hasBoughtLarge());
    }

    @Override
    public int getItemCount() {
        return team.getProbs().size();
    }

    class InnerViewHolder extends RecyclerView.ViewHolder {
        public TextView pts;
        public CheckBox small;
        public CheckBox med;
        public CheckBox large;

        public InnerViewHolder(View itemView) {
            super(itemView);
            pts =(TextView)itemView.findViewById(R.id.table_pts);
            small=(CheckBox)itemView.findViewById(R.id.table_s);
            med=(CheckBox)itemView.findViewById(R.id.table_m);
            large=(CheckBox)itemView.findViewById(R.id.table_l);
        }
    }
}

class TextArrayAdapter extends RecyclerView.Adapter<TextArrayAdapter.TextViewHolder>{
    private String[] values;
    private ArrayList<TextView> list; //the list to add all views to

    public TextArrayAdapter(String[] arrayIn, ArrayList<TextView> listIn){
        values=arrayIn;
        list=listIn;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_textview, parent, false);
        return new TextViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.text.setText(values[position]);
        list.add(holder.text);
    }

    @Override
    public int getItemCount() {
        return values.length;
    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public TextViewHolder(View itemView) {
            super(itemView);
            text = (TextView)itemView.findViewById(R.id.simple_tv);
        }
    }
}