package com.example.ethan.ppvii;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firebase.client.Firebase;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ethan on 7/12/2016.
 */
public class Queue extends PPFragment {
    private HashMap<String, Submission> submissionMap;  //store all Submissions keyed by their unique id generated from firebase.push

    /**
     * create a new queue object which used the given tabbed instance
     * @param tabbedIn the instance of tabbed the queue should be using
     * @return a new Queue object
     */
    public static Queue createQueue(Tabbed tabbedIn){
        Queue queue=new Queue();
        queue.tabbed=tabbedIn;
        return queue;
    }

    /**
     * Display the submissions made by the teams  and allow the puzzlelord to mark submissions as correct or incorrect
     * submissions are displayed in a sorted list sorted first by status (pending then correct then incorrect)
     *  and then based on date with most recent floating to the top
     * When a solution is marked relevant information is changed in firebase
     * @param inflater the LayoutInflator used to inflate layouts from xml
     * @param container the ViewGroup used for inflating layouts
     * @param savedInstanceState unused
     * @return the view to be displayed
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!tabbed.constants().functioning())return displayNotFunctioning(); //ensure functioning

        View v;
        if(submissionMap==null || submissionMap.isEmpty()){
            //no submissions to display
            v=displayEmptyQueueMessage();
        }else{
            //display submissions
            v= displayQueue(inflater, container);
        }

        return displayButton(v);
    }

    /**
     * Place a button allowing the user to logout below the main body of the page
     * @param v the body of the page which is either a queue or a message indicating there are no submissions at this time
     * @return the view to be displayed to the user
     */
    private View displayButton(View v){
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //add the body view
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); //weight ensures button will be at bottom
        linearLayout.addView(v);

        //create and add the button
        Button b=new Button(getActivity()); //button to allow user to logout
        b.setText(getString(R.string.returntologin_button));
        b.setOnClickListener(new View.OnClickListener() {
            /**
             * go back to the login page
             * @param v unused
             */
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getContext(), Login.class);
                startActivity(loginIntent);
            }
        });
        linearLayout.addView(b);

        return linearLayout;
    }

    /**
     * display a message that there are currently no submissions
     * @return a textview showing the message
     */
    private View displayEmptyQueueMessage(){
        TextView tv=new TextView(getActivity());
        tv.setText(getString(R.string.queue_empty));
        return tv;
    }

    /**
     * Display a liswt of submissions color coded by status
     * Each submission shows the problem number, team name, and submitted answer
     * For pending submissions the user can mark them as incorrect or correct
     * Submissions that were previously marked as incorrect can be marked correct but not the other way around for safety
     * @param inflater used for inflating from xml
     * @param container used for inflating from xml
     * @return a recycler view showing the submissions
     */
    private View displayQueue(LayoutInflater inflater, ViewGroup container){
        View v=inflater.inflate(R.layout.simple_recyclerview, container, false);
        RecyclerView rv=(RecyclerView) v.findViewById(R.id.simple_rv);
        //rv.setHasFixedSize(true); //this specifies that all cards will have the same size for optimization purposes

        //set layout manager so it behaves like a list view
        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        //set adapter
        QueueArrayAdapter qaa=new QueueArrayAdapter(tabbed, submissionMap);
        rv.setAdapter(qaa);

        return v;
    }

    /**
     * Add a new submission to the stored list
     * @param sub submission to be stored
     */
    public void addSubmission(Submission sub){
        if(submissionMap ==null) submissionMap =new HashMap<String, Submission>();
        submissionMap.put(sub.getUid(), sub);
    }

    /**
     * remove a submission that has been removed from firebase
     * @param key the unique id of the submission
     */
    public void removeSubmission(String key){
        submissionMap.remove(key);
    }

    /**
     * get the submission with the given unique id
     * @param key the unique id
     * @return the submission with that uid or null if the submission does not exist
     */
    public Submission getSubmission(String key){
        return submissionMap.get(key);
    }
}

/**
 * adapt a hashmap from unique ids (strings) to submissions so that it can be displayed in a recycler view
 */
class QueueArrayAdapter extends RecyclerView.Adapter<QueueArrayAdapter.SubmissionViewHolder>{
    private ArrayList<Submission> values; //the submissions to be displayed
    private Tabbed tabbed;  //save tabbed

    public QueueArrayAdapter(Tabbed tabbed, HashMap<String, Submission> hm){
        values=new ArrayList<>();
        values.addAll(hm.values()); //no longer need to store keys so convert to arraylist
        this.tabbed=tabbed; //store tabbed
        sort();
    }

    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_queue, parent, false);
        SubmissionViewHolder svh = new SubmissionViewHolder(v);
        return svh;
    }

    @Override
    public void onBindViewHolder(SubmissionViewHolder holder, final int position) {
        final Submission curr=values.get(position);
        String temp; //android does not like concatenation in setText()
        if(tabbed.leaderboardLoaded){
            //leaderboard loaded so get team name
            temp=tabbed.getLeaderboard().team[curr.getTeamNum()-1].getName()+"";
            holder.name.setText(temp);
        }else{
            //just use team number until name is available
            temp="team"+curr.getTeamNum();
            holder.name.setText(temp);
        }
        temp="prob "+curr.getProb();
        holder.prob.setText(temp);
        holder.text.setText(curr.getAns());

        View.OnClickListener posClickList=new View.OnClickListener(){
            /**
             * update the submission to indicate that it is correct
             * update the leaderboard to give the team points for solving the problem
             * @param v unused
             */
            @Override
            public void onClick(View v) {
                //update the leaderboard
                Firebase leadref=new Firebase(tabbed.constants().url()+Tabbed.FB_LEADERBOARD).child(curr.getTeamNum()+"").child("probs").child(curr.getProb()+"").child("points");
                leadref.setValue(pointsForSolving(curr.getProb()));
                System.out.println("bang");

                //mark the submission as correct
                curr.setAnsAsCorrect(); //in case firebase is unreachable make the change locally so it can be indicated to the user
                Firebase qref=new Firebase(tabbed.constants().url()+Tabbed.FB_QUEUE).child(curr.getTeamNum() + "").child(curr.getUid()).child("corr");
                qref.setValue(Submission.CORRECT);
                System.out.println("bing");
                //refresh the view
                refresh();
            }
        };

        View.OnClickListener negClickList=new View.OnClickListener() {
            /**
             *
             * @param v unused
             */
            @Override
            public void onClick(View v) {
                //mark as incorrect
                curr.setAnsAsIncorrect(); //in case firebase is unreachable make the change locally so it can be indicated to the user
                Firebase ref=new Firebase(tabbed.constants().url()+Tabbed.FB_QUEUE);
                ref.child(curr.getTeamNum()+"").child(curr.getUid()).child("corr").setValue(Submission.INCORRECT);

                //refresh the view
                refresh();
            }
        };

        if(curr.ansPending()){
            //pending
            holder.cv.setBackgroundColor(ContextCompat.getColor(tabbed, R.color.color_pending));

            //both the correct and incorrect buttons are enabled
            holder.correct.setEnabled(true);
            holder.incorrect.setEnabled(true);  //this code is necessary because the views are recycled
            holder.correct.setOnClickListener(posClickList);
            holder.incorrect.setOnClickListener(negClickList);
        }else if(curr.ansIncorrect()){
            //incorrect
            holder.cv.setBackgroundColor(ContextCompat.getColor(tabbed, R.color.color_incorrect));

            //only correct button enabled
            holder.correct.setOnClickListener(posClickList);
            holder.correct.setEnabled(true);
            holder.incorrect.setEnabled(false);
        }else{
            //correct
            holder.cv.setBackgroundColor(ContextCompat.getColor(tabbed, R.color.color_correct));

            //no button enabled
            //TODO: v3.0 allow plord to reverse a correct ruling (this is complicated because it messes up the point system so everyones points would
            //have to be adjusted) Even worse if after the first 5 the points are all the same there is no way to know the 5th team which should get raised)
            holder.correct.setEnabled(false);
            holder.incorrect.setEnabled(false);
        }
    }

    /**
     * Calculate the points that would be earned by solving the problem in the current game state
     * @param prob the problem number in question
     * @return the number of points a team would earn if it were to solve the problem in the current game state
     */
    private int pointsForSolving(int prob){
        int[] pointsArray=tabbed.constants().getPointsArray();
        //TODO: v3.0 store an array of problem number and num teams which have solved
        int place=1;
        for(Team t: tabbed.getLeaderboard().team){
            if(t.hasSolved(prob)){
                place++;
                if(place>=pointsArray.length)break;  //make sure it doesn't search longer than necessary
            }
        }
        return pointsArray[Math.min(place,pointsArray.length)];
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    /**
     * sort the values arraylist by the following criteria
     * first, pending rise to the top
     * within pending, by date with earliest first
     */
    private void sort(){
        if(values==null)return; //make sure values has been initialized
        boolean done;
        //bubble sort into reverse order (since the arrayadapter puts the 0th element at the top)
        //TODO: v2.0 use a better sort
        while(true){
            done=true;
            for(int i=0; i<values.size()-1; i++){
                if(values.get(i).compareToQueue(values.get(i + 1))==-1){
                    //if the ith is less than the i+1th swap them
                    Submission temp=values.get(i+1);
                    values.set(i + 1, values.get(i));
                    values.set(i, temp);
                    done=false;
                }
            }
            if(done)break;
        }
    }

    public void refresh(){
        tabbed.refreshTabs();
    }

    class SubmissionViewHolder extends RecyclerView.ViewHolder {
        public CardView cv;
        public TextView prob;
        public TextView name;
        public TextView text;
        public Button correct;
        public Button incorrect;

        public SubmissionViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.prog_cv);
            prob = (TextView)itemView.findViewById(R.id.prog_cv_prob);
            name = (TextView)itemView.findViewById(R.id.prog_cv_name);
            text = (TextView)itemView.findViewById(R.id.prog_cv_text);
            correct = (Button)itemView.findViewById(R.id.prog_cv_yes);
            incorrect = (Button)itemView.findViewById(R.id.prog_cv_no);
        }
    }
}