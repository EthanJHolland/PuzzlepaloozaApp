package com.example.ethan.ppvii;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Ethan on 7/28/2016.
 * Display information related to each problem in order of how many people have solved it
 */
public class ByProb extends PPFragment {
    public static ByProb createByProb(Tabbed tabbedIn){
        ByProb bp=new ByProb();
        bp.tabbed=tabbedIn;
        return bp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!tabbed.constants().functioning())return displayNotFunctioning();

        System.out.println("byprob");
        //ensure necessary data has been loaded
        if(!tabbed.constants().isInitialized() || !tabbed.leaderboardLoaded){
            TextView tv = new TextView(getActivity());
            tv.setText(getString(R.string.leaderboard_notyetloaded));
            return tv;
        }

        //otherwise data has been loaded
        //make the views
        RecyclerView rv=new RecyclerView(getActivity());

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayout.VERTICAL);
        rv.setLayoutManager(llm);

        ByProbColArrayAdapter bpcaa=new ByProbColArrayAdapter(tabbed.getLeaderboard().team, tabbed.constants().getPointsArray().length-1, true);
        rv.setAdapter(bpcaa);

        TextView tv=new TextView(getActivity());
        tv.setText("smon");
        return tv;
//        return rv;
    }
}

class ByProbColArrayAdapter extends RecyclerView.Adapter<ByProbColArrayAdapter.ByProbColHolder>{
    private Team[] values;
    private Integer[] probOrder; //the problem numbers sorted into the desired order
    private HashMap<Integer, ArrayList<String>> map; //for each problem hold a list of teams which have solved it
    private final int teamsToShow;

    private Comparator<Integer> mostAtTop=new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return -(map.get(lhs).size()-map.get(rhs).size());
        }
    };

    private Comparator<Integer> leastAtTop=new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return map.get(lhs).size()-map.get(rhs).size();
        }
    };

    public ByProbColArrayAdapter(Team[] arrayIn, int teamsToShowIn, boolean isMost){
        values=arrayIn;
        teamsToShow=teamsToShowIn;
        sort(isMost);
    }

    @Override
    public ByProbColHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_byprob, parent, false);
        return new ByProbColHolder(v);
    }

    @Override
    public void onBindViewHolder(ByProbColHolder holder, int position) {
        String probText="Prob ";
        if(probOrder[position]<10)probText+=" ";
        probText+=probOrder[position];
        holder.num.setText(probText);

        ArrayList<String> solvedList=map.get(probOrder[position]);
        holder.solvedby.setText("solved by: " + solvedList.size());

        String out="";
        for(int i=0; i<Math.min(solvedList.size(), teamsToShow); i++){
            out+=solvedList.get(i)+", ";
        }
        if(solvedList.size()>teamsToShow){
            out+="+ "+(solvedList.size()-teamsToShow)+" others";
        }else if(out.length()>1){
            out=out.substring(0,out.length()-2);
        }
        holder.solvedlist.setText(out);
    }

    /**
     * the number of items to display
     * @return the number of problems
     */
    @Override
    public int getItemCount() {
        return values[0].getProbs().size();
    }

    public void sort(boolean isMost){
        int numProbs=values[0].getProbs().size();

        map=new HashMap<>();
        for(int i=1; i<=numProbs; i++){
            final int currProb=i;
            Team[] temp=values.clone();
            Arrays.sort(temp, new Comparator<Team>() {
                @Override
                public int compare(Team lhs, Team rhs) {
                    return -(lhs.getPointsForProb(currProb)-rhs.getPointsForProb(currProb));
                }
            });

            ArrayList<String> list=new ArrayList<>();
            int count=0;
            while(count<temp.length && temp[count].hasSolved(currProb)){
                list.add(temp[count].getNickname());
                count++;
            }

            map.put(currProb, list); //add to the hashmap
        }

        probOrder=new Integer[numProbs];
        for(int i=1; i<=numProbs; i++)probOrder[i-1]=i;

        if(isMost){
            Arrays.sort(probOrder, mostAtTop);
        }else{
            Arrays.sort(probOrder, leastAtTop);
        }
    }

    class ByProbColHolder extends RecyclerView.ViewHolder {
        public TextView num;
        public TextView solvedby;
        public TextView solvedlist;

        public ByProbColHolder(View itemView) {
            super(itemView);
            num=(TextView)itemView.findViewById(R.id.bp_num);
            solvedby=(TextView)itemView.findViewById(R.id.bp_solvedby);
            solvedlist=(TextView)itemView.findViewById(R.id.bp_solvedlist);
        }
    }
}

