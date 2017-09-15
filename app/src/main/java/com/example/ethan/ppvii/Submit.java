package com.example.ethan.ppvii;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Submit takes a team and a problem number and has three primary functions
 * First, it displays a list of the previous submissions (if there are any) for that problem and whether they have been marked as correct, incorrect, or are pending
 * The team can also make a new submission which will be sent to the puzzlelords for feedback
 * Lastly, teams can buy hints and view previously bought hints
 */
public class Submit extends PPFragment{
    private Team t;     //the team that is currently logged in
    private HashMap<String, Submission>[] submapArray;   //hold an array of lists of previous submissions organized by problem number
    private int prob=-1; //TODO: v0.1 make it so this isnt always 2

    private EditText ansField; //the field in which teams type potential submissions
    private ListView lv;    //the list view of previous submissions

    private HashMap<Integer, HashMap<String, String>> hintsmap;  //the map of hints loaded from Firebase
    private SubmissionArrayAdapter aa;

    public static Submit createSubmit(Tabbed tabbedIn){
        Submit submit=new Submit();
        submit.tabbed=tabbedIn;
        return submit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!tabbed.constants().functioning())return displayNotFunctioning();

        t=tabbed.getLeaderboard().team[tabbed.mode-1];

        if(t==null){
            TextView tv=new TextView(getActivity());
            tv.setText(getString(R.string.submit_notyetloaded));
            return tv;
        }

        LinearLayout outerlinear=new LinearLayout(getActivity()); //the main layout for the page
        outerlinear.setOrientation(LinearLayout.VERTICAL);

        //create title textview
        TextView titleTv=new TextView(getActivity());
        String temp="Problem " + prob;
        titleTv.setText(temp);
        titleTv.setTextSize(20); //make the title font larger
        LinearLayout.LayoutParams centParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        centParam.gravity = Gravity.CENTER;
        titleTv.setLayoutParams(centParam); //center the title
        outerlinear.addView(titleTv); //add the title

        //create a textview to label the submission text field
        TextView newSubLabelTv=new TextView(getActivity());
        newSubLabelTv.setText("New submission");
        outerlinear.addView(newSubLabelTv); //add the textview

        //create the field where the submission is entered
        ansField =new EditText(getActivity());
        ansField.setHint("submission here"); //set text to explain what the field is for
        outerlinear.addView(ansField); //add submission field

        //add a submit button
        Button submitButton=new Button(getActivity());
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClick();
            }
        });
        submitButton.setText("Submit"); //set the button text
        outerlinear.addView(submitButton); //add the button

        //add a text view labeling the hints bar
        TextView hintLabelTv=new TextView(getActivity());
        hintLabelTv.setText("Hints (Click on a hint to buy or view)"); //set text
        outerlinear.addView(hintLabelTv); //add label

        //create a linear layout that acts as a hints bar
        //it is a horizontal layout showing a clickable label and a text box for each hint
        LinearLayout innerlinear= new LinearLayout(getActivity());
        innerlinear.setOrientation(LinearLayout.HORIZONTAL); //set layout to horizontal
        innerlinear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); //set the width so it takes up the whole screen

        //create listeners
        View.OnClickListener smallListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSmallClick();
            }
        };
        View.OnClickListener medListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMedClick();
            }
        };
        View.OnClickListener largeListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLargeClick();
            }
        };
        View.OnClickListener[] listenerArray={smallListener, medListener, largeListener};

        TextView[] tvsize=new TextView[3]; // an array fo text views in the order small, med, large
        CheckBox[] cbsize=new CheckBox[3]; //arrays are used to avoid hardcode as much as possible
        for(int i=0; i<3; i++){
            tvsize[i]=new TextView(getActivity());  //initialize text views
            tvsize[i].setTextSize(20);  //make font larger for emphasis
            tvsize[i].setOnClickListener(listenerArray[i]); //set onCLickListener
            cbsize[i]=new CheckBox(getActivity());  //intialize check boxes
            cbsize[i].setOnClickListener(listenerArray[i]); //set onCLickListener
            cbsize[i].setEnabled(false);    //set the check boxes so they cannot be unchecked/checked by the user
        }
        //TODO: v3.0 why doesnt this inflate an xml layout? one has already been created

        //make the allignement look nice
        //set the first checkbox and the last textview to each have weight 1, ensuring that the allignment of the center objects is correct
        LinearLayout.LayoutParams leftParam=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cbsize[0].setLayoutParams(leftParam); //give the small checkbox weight 1
        tvsize[2].setLayoutParams(leftParam); //give the large textview weight 1
        tvsize[2].setGravity(Gravity.RIGHT);         //set the gravity of the textview so that the text is on the right side even though the view is large

        //each check boxed is checked iff that hint has been bought
        cbsize[0].setChecked(t.hasBoughtSmall(prob));
        cbsize[1].setChecked(t.hasBoughtMed(prob));
        cbsize[2].setChecked(t.hasBoughtLarge(prob));

        //set text
        tvsize[0].setText("small");
        tvsize[1].setText("med");
        tvsize[2].setText("large");

        //add the textviews and checkboxes to the linearlayout
        for(int i=0; i<3; i++){
            innerlinear.addView(tvsize[i]);
            innerlinear.addView(cbsize[i]);
        }

        outerlinear.addView(innerlinear);         //add the inner layout to the outer layout

        //back in the primary layout
        //add a label for the previous submission list
        TextView prevSubLabelTv=new TextView(getActivity());
        prevSubLabelTv.setText("Previous submissions"); //set label text
        outerlinear.addView(prevSubLabelTv); //add the label

        //create a list view of previous submissions
        lv=new ListView(getActivity()); //initialize
        //aa=new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, slist);
        aa=new SubmissionArrayAdapter(getActivity(), getSortedArrayList()); //initialize a SubmissionArrayAdapter and feed it the sorted list of previous submissions
        lv.setAdapter(aa); //set the array adapter
        lv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        outerlinear.addView(lv);    //add the listview

        Button back=new Button(getActivity());
        back.setText("back");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prob = -1;
                tabbed.prog = true;
                refresh();
            }
        });
        outerlinear.addView(back);

        //set the linear layout params so that the outer layout fills the whole screen
        outerlinear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return outerlinear;
    }

    /**
     * When a new submission is made by this user or another user on the same team, add it to the map of submissions
     * @param subIn a new submission
     */
    public void addSubmission(Submission subIn){
        int index=subIn.getProb()-1; //the index of the problem in the submapArray
        if(submapArray ==null){
            //first submission so initialize
            submapArray =new HashMap[tabbed.constants().NUM_PROB];
        }
        if(submapArray[index]==null){
            //first submission for this problem so initialize
            submapArray[index]=new HashMap<>();
        }
        submapArray[index].put(subIn.getUid(), subIn);
        refresh();
    }

    public void submissionMarked(int prob, String id, int mark){
        if(submapArray[prob]==null || !submapArray[prob].containsKey(id)){
            //input protection
            return;
        }
        submapArray[prob].get(id).setCorr(mark);
        refresh();
    }

    /**
     * Submit an answer along with other identification information to firebase in the form of a submission object
     * @param teamNum the team which is submitting
     * @param probin the problem number
     * @param ans the answer text being submitted
     */
    public void submitAnswer(int teamNum, int probin, String ans) {
        Firebase qref = new Firebase(tabbed.constants().url()+Tabbed.FB_QUEUE); //get the queue reference
        Firebase qpush=qref.child(tabbed.mode+"").push(); //store the push so that the key can be sent
        qpush.setValue(new Submission(qpush.getKey(), teamNum, probin, ans, Date.currDate())); //send the submission to firebase
        //view will be refreshed once Tabbed identifies a submission has been added
    }

    /**
     * convert the hashmap of submissions to a sorted arraylist which can be sent to the SubmissionArrayAdapter
     * @return a sorted arraylist of submissions
     */
    private ArrayList<Submission> getSortedArrayList(){
        int index=prob-1; //shift for array starting at 0 not 1
        if(submapArray==null || submapArray[index]==null){
            //nothing to display so return an empty arraylist
            return new ArrayList<Submission>();
        }
        ArrayList<Submission> sortedList=new ArrayList<Submission>();
        sortedList.addAll(submapArray[index].values());
        boolean done;
        //bubble sort into reverse order (since the arrayadapter puts the 0th element at the top)
        //TODO: v2.0 use a better sort
        while(true){
            done=true;
            for(int i=0; i<sortedList.size()-1; i++){
                if(sortedList.get(i).compareToSubmit(sortedList.get(i + 1))==-1){
                    //if the ith is less than the i+1th swap them
                    Submission temp=sortedList.get(i+1);
                    sortedList.set(i+1, sortedList.get(i));
                    sortedList.set(i, temp);
                    done=false;
                }
            }
            if(done)break;
        }
        return sortedList;
    }

    public void addHints(int pnum, HashMap<String, String> hintsin){
        //TODO: v0.1 make sure it doesnt crash if no connection
        if(hintsmap==null){
            //initialize
            hintsmap=new HashMap<>();
        }
        hintsmap.put(pnum, hintsin);
    }

    private void onLargeClick(){
        if(t.hasBoughtLarge(prob)){
            //hint already bought so display it it
            displaySimpleMessage("Large hint:", hintsmap.get(prob).get("large"));
        }else if(!t.hasBoughtSmall(prob)){
            //user has not bought small or medium hint so the large hint cannot be purchased at this time
            displaySimpleMessage("Hint currently unavailable", "The large hint cannot be purchased until the small and medium hints have been purchased");
        }else if(!t.hasBoughtMed(prob)){
            //user has not bought medium hint so the large hint cannot be purchased at this time
            displaySimpleMessage("Hint currently unavailable", "The large hint cannot be purchased until the medium hint has been purchased");
        }else if(!tabbed.constants().getHintsAvailable()){
            //hints currently unavailable
            displaySimpleMessage("Hint not available", "Hints are not available for purchase at this time");
        }else if(t.getMoney()<tabbed.constants().LARGE_COST){
            //team does not have enough money
            displaySimpleMessage("Not enough money", "You do not have enough money to buy this hint");
        }else {
            //user is attempting to legally purchase the large hint
            displayBuy("large", tabbed.constants().LARGE_COST);
        }
    }

    private void onSmallClick(){
        if(t.hasBoughtSmall(prob)) {
            //hint already bought so display it it
            displaySimpleMessage("Small hint:", hintsmap.get(prob).get("small"));
        }else if(t.getMoney()<tabbed.constants().SMALL_COST){
            //user does not have enough money
            displaySimpleMessage("Not enough money", "You do not have enough money to buy this hint");
        }else{
            //make sure user wants to buy the hint
            displayBuy("small", tabbed.constants().SMALL_COST);
        }
    }

    private void onMedClick(){
        if(t.hasBoughtMed(prob)){
            //hint already bought so display it it
            displaySimpleMessage("Medium hint:", hintsmap.get(prob).get("med"));
        }else if(!t.hasBoughtSmall(prob)){
            //user has not bought small hint so the medium hint cannot be purchased at this time
            displaySimpleMessage("Hint currently unavailable", "The medium hint cannot be purchased until the small hint has been purchased");
        }else if(!tabbed.constants().getHintsAvailable()){
            //hints currently unavailable
            displaySimpleMessage("Hint not available", "Hints are not available for purchase at this time");
        }else if(t.getMoney()<tabbed.constants().MED_COST){
            //team does not have enough money
            displaySimpleMessage("Not enough money", "You do not have enough money to buy this hint");
        }else {
            //user is attempting to legally purchase the medium hint
            displayBuy("med",tabbed.constants().MED_COST);
        }
    }

    private void displayBuy(final String size, int cost){
        // make a dialog box to require confirmation of purchase
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Confirmation of purchase:");
        alert.setMessage("Are you sure you want to purchase the " + size + " hint for " + cost + " pp dollars?\nYou currently have " + t.getMoney() + " dollars remaining");   //display the attempted submission
        alert.setIcon(R.drawable.mbpp);

        // make a confirmation button
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            /**
             * If the yes button is pressed, submit the answer to firebase, close the dialog box,
             *      and create a toast saying that the answer was submitted
             * @param dialog unused
             * @param whichButton unused
             */
            public void onClick(DialogInterface dialog, int whichButton) {
                t.buySizeHint(prob, size);

                displaySimpleMessage(((char) (size.charAt(0) + ('A' - 'a')) + size.substring(1)) + " hint:", hintsmap.get(prob).get(size));

                Firebase teamref=new Firebase(tabbed.constants().url()+Tabbed.FB_LEADERBOARD).child(tabbed.mode+"");
                teamref.child("probs").child(prob+"").child("hints").child(size).setValue(true);
                teamref.child("money").setValue(t.getMoney());
                Toast.makeText(getActivity().getApplicationContext(), "Hint purchased", Toast.LENGTH_LONG).show(); //display a message to show that the submission was succesful
                refresh();
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            /**
             * if the no button is pressed close the dialog box and do nothing
             * @param dialog unused
             * @param whichButton unused
             */
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show(); //display the dialog box


        //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
        //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(Color.GREEN);
        Button neg=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        neg.setTextColor(Color.RED);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GREEN);
    }


    public void setTeam(Team teamIn){
        //TODO: v0.1 call this method when the leaderboard has loaded
        //TODO: v0.1 is this redundant
        t=teamIn;
        refresh();
    }

    /**
     * display a simple popup message with the given title and message
     * the only button is a neutral button which says "okay" and closes the dialog box
     * @param title the title to be displayed in the title slot
     * @param message the body of the message to be displayed in the message slot
     */
    private void displaySimpleMessage(String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(title);
        alert.setMessage(message);
        alert.setIcon(R.drawable.mbpp);

        // Make an "okay" button that simply dismisses the alert
        alert.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
            /**
             * if the okay button is pressed close the dialog box and do nothing
             * @param dialog unused
             * @param whichButton unused
             */
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show(); //display the dialog box

        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.DKGRAY); //set the button text color
    }

    /**
     * when the submit button is pressed
     * display a popup message that requires the user to confirm the submission
     * if the user confirms, the submission is added to the firebase queue, and a toast is shown confirming the answer was submitted
     */
    private void onSubmitClick(){
        // make a dialog box to require confirmation of submission
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Are you sure you want to submit:");
        alert.setMessage("\"" + ansField.getText().toString() + "\"?");   //display the attempted submission
        alert.setIcon(R.drawable.mbpp);

        // make a confirmation button
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            /**
             * If the yes button is pressed, submit the answer to firebase, close the dialog box,
             *      and create a toast saying that the answer was submitted
             * @param dialog
             * @param whichButton
             */
            public void onClick(DialogInterface dialog, int whichButton) {
                submitAnswer(tabbed.mode, prob, ansField.getText().toString());   //add the submission the the firebase queue
                Toast.makeText(getActivity().getApplicationContext(), "Answer submitted!", Toast.LENGTH_LONG).show(); //display a message to show that the submission was succesful
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            /**
             * if the no button is pressed close the dialog box and do nothing
             * @param dialog unused
             * @param whichButton unused
             */
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show(); //display the dialog box

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED); //make the negative button text red (must be done after the AlertDialog is shown)
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GREEN); //make the postiive button text green
    }

    public void initialize(int probIn){
        prob=probIn;
    }
}

/**
 * An array adapter which bridges between an arraylist of Submission and a list view
 * The primary difference is that the SumbissionArrayAdapter shades the rows based on the correctness of the submission
 */
class SubmissionArrayAdapter extends ArrayAdapter<Submission>{
    private final Context context;  //keep track of the context so it can be sent to the super constructor
    private final ArrayList<Submission> values; //the arraylist of submissions passed in to be adapted
    private LayoutInflater mInflater;

    /**
     * Constructor
     * initialize the arrayadapter to hold submissions
     * @param context the android context
     * @param values the arraylist of submissions to be adapted
     */
    public SubmissionArrayAdapter(Context context, ArrayList<Submission> values) {
        super(context, R.layout.list_item_submit, values);
        this.context = context;
        this.values = values;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * call the super method and then color the table rows according to the correctness of the submitted answer
     * red for incorrect, yellow for pending, and green for correct
     * @param position the index of the table row which as well as the index in the arraylist
     * @param convertView passed to the super method
     * @param parent passed to the super method
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //View  view = super.getView(position, convertView, parent);
      //  LinearLayout view = new LinearLayout(getContext());
        Submission curr=values.get(position);
//        view.setOrientation(LinearLayout.VERTICAL);
        /*RelativeLayout view=new RelativeLayout(getContext());

        //create a title label which says the submission
        TextView title=new TextView(getContext());
        title.setText(curr.getAns());

        view.addView(title);

        //create a label specifying the date and time of submission
        TextView when=new TextView(getContext());
        when.setText("submitted "+curr.getDate());
        view.addView(when);

        //create a label showing the correctness/ pendingness of the submission
        //set the color of the row depending on the correctness of the submission
        TextView stat=new TextView(getContext());
        if(curr.ansIncorrect()){
            //answer incorrect
            view.setBackgroundColor(Color.RED);
            stat.setText("incorrect :/");
        }else if(curr.ansCorrect()){
            //answer correct
            view.setBackgroundColor(Color.GREEN);
            stat.setText("correct :)");
        }else{
            //answer pending
            view.setBackgroundColor(Color.YELLOW);
            stat.setText("pending :|");
        }
        stat.setTextSize(title.getTextSize() * 7 / 10);
       // stat.setTextColor(title.getCurrentTextColor()+0.1);
        view.addView(stat);
        return view;*/

        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item_submit, parent, false);

        //create a title label which says the submission
        TextView title= (TextView) rowView.findViewById(R.id.id_title_tv);
        title.setText(curr.getAns());

        //create a label specifying the date and time of submission
        TextView when= (TextView) rowView.findViewById(R.id.id_subtitle_tv);
        when.setText("submitted "+curr.getDate());

        //create a label showing the correctness/ pendingness of the submission
        //set the color of the row depending on the correctness of the submission

        TextView stat= (TextView) rowView.findViewById(R.id.id_correct_tv);
        if(curr.ansIncorrect()){
            //answer incorrect
            rowView.setBackgroundColor(Color.RED);
            stat.setText("incorrect :(");
        }else if(curr.ansCorrect()){
            //answer correct
            rowView.setBackgroundColor(Color.GREEN);
            stat.setText("correct :)");
        }else{
            //answer pending
            rowView.setBackgroundColor(Color.YELLOW);
            stat.setText("pending :|");
        }
        return rowView;

    }
}