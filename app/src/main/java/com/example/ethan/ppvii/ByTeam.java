package com.example.ethan.ppvii;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ethan on 7/30/2016.
 * Display more information about each team in order of overall rank
 */
public class ByTeam extends PPFragment{
    public static ByTeam createByTeam(Tabbed tabbedIn){
        ByTeam bt=new ByTeam();
        bt.tabbed=tabbedIn;
        return bt;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("byteam "+tabbed.constants().functioning());
        if(!tabbed.constants().functioning())return displayNotFunctioning();

        //ensure necessary data has been loaded
        if(!tabbed.constants().isInitialized() || !tabbed.leaderboardLoaded){
            TextView tv = new TextView(getActivity());
            tv.setText(getString(R.string.leaderboard_notyetloaded));
            return tv;
        }


        //otherwise data has been loaded
        //make the views
        View v=inflater.inflate(R.layout.fragment_byteam, container, false);

        TextView key=(TextView)v.findViewById(R.id.bt_key);
        key.setText("KEY: NS=# of problems solved, MS=money spent, IPR=Iron puzzler ratio (MS/NS), PS=list of problems solved in ascending order");


        RecyclerView rv=(RecyclerView)v.findViewById(R.id.bt_rv);
        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayout.VERTICAL);
        rv.setLayoutManager(llm);


        //find screen dimensions
        Display display = tabbed.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int sw = size.x;

        ByTeamColArrayAdapter btcaa=new ByTeamColArrayAdapter(tabbed.getLeaderboard().sorted, tabbed);
        rv.setAdapter(btcaa);

        return v;
    }
}

class ByTeamColArrayAdapter extends RecyclerView.Adapter<ByTeamColArrayAdapter.ByTeamColHolder>{
    private Team[] values;
    private Tabbed tabbed;
    private int[] widths; //hold the desired textview widths for consistency

    public ByTeamColArrayAdapter(Team[] arrayIn, Tabbed tabbedIn){
        values=arrayIn;
        tabbed=tabbedIn;
        getWidths();
    }

    @Override
    public ByTeamColHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_byteam, parent, false);
        return new ByTeamColHolder(v);
    }

    @Override
    public void onBindViewHolder(ByTeamColHolder holder, int position) {
        Team curr=values[position];
        holder.rank.setText(curr.rank(values));
        holder.name.setText(curr.getNickname());
        holder.numsolved.setText("NS: "+curr.numSolved());
        holder.spent.setText("MS: "+curr.moneySpent());

        if (curr.numSolved() == 0) {
            holder.iron.setText("IPR: N/A");
        }else{
            holder.iron.setText("IPR: "+Math.round(curr.moneySpent()*10.0/curr.numSolved())/10.0+"");
        }
        String listText="";
        for(int i=1; i<=curr.getProbs().size(); i++){
            if(curr.getProb(i).hasSolved())listText+=i+", ";
        }
        if(listText.length()>2)listText=listText.substring(0,listText.length()-2); //remove extra ", "
        if(listText.length()==0){
            holder.listsolved.setText("PS: none");
        }else{
            holder.listsolved.setText("PS: "+listText);
        }
    }

    @Override
    public int getItemCount() {
        return values.length;
    }

    /**
     * determine the desired widths for the holder TextViews for consistency's sake
     * the desired width is based on the maximum amount of text that could be in that TextView
     */
    private void getWidths(){
        widths=new int[5];
        widths[0]=getStringWidth("t-"+(values.length-1));
        widths[1]=getStringWidth("WWW");
        widths[2]=getStringWidth("NS: "+values[0].getProbs().size());
        widths[3]=getStringWidth("MS: "+tabbed.constants().getSTART_MONEY()*10+"");
        widths[4]=Math.max(getStringWidth("IPR: N/A"), getStringWidth("IPR: 999.9"));
    }

    private int getStringWidth(String max){
        TextView tv=new TextView(tabbed);
        Rect bounds = new Rect();
        Paint textPaint = tv.getPaint();
        textPaint.getTextBounds(max,0,max.length(),bounds);
        return bounds.width();
    }

    class ByTeamColHolder extends RecyclerView.ViewHolder {
        public TextView rank;
        public TextView name;
        public TextView numsolved;
        public TextView spent;
        public TextView iron;
        public TextView listsolved;

        public ByTeamColHolder(View itemView) {
            super(itemView);
            rank=(TextView)itemView.findViewById(R.id.bt_rank);
            name=(TextView)itemView.findViewById(R.id.bt_name);
            numsolved=(TextView)itemView.findViewById(R.id.bt_numsolved);
            spent=(TextView)itemView.findViewById(R.id.bt_spent);
            iron=(TextView)itemView.findViewById(R.id.bt_iron);
            listsolved=(TextView)itemView.findViewById(R.id.bt_listsolved);


            rank.setWidth(widths[0]);
            name.setWidth(widths[1]);
            numsolved.setWidth(widths[2]);
            spent.setWidth(widths[3]);
            iron.setWidth(widths[4]);
        }
    }
}


