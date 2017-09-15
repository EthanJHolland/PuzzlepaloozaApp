package com.example.ethan.ppvii;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Things to know
 * Initialization
 *  Username and password creation
 *      all team usernames should be teamx@url.com where dx is the team number and "https://root.firebaseio.com" is the location of the database
 *          for example team2@puzzlepalooza.com or team9@ppviii.com
 *      the puzzlelord usernames can be anything containing @ and not containing the word team
 *      passwords must have length of at least 5
 *      for simplicity, all usernames and passwords are stored at https://puzzlepalooza.firebaseio.com regardless of where other data comes from
 *      the hope is that eventually different games will be stored at different urls but all logins will be from the same place
 *  Name changes
 *      the name of a team (as well as the nickname) must be done by hand in the firebase database
 *  Room number
 *      room number is currently not used for anything so leaving the default is fine
 *  Hints
 *      hints must be entered into the firebase database by hand at the beginning
 *      to turn hints off for the first and last days set hintsavailable in constants to false
 *  Nickname
 *      in order to display as much information as possible, each team should have a 3 letter nickname
 *
 * Usage
 *  Message sending
 *      Messages sent within the same minute will be displayed in the wrong order
 *      this is due to the fact that list views start at position 0 and increase as you go down but firebase stores oldest at position 0
 *      this can be fixed by storing the second but this is a lot of extra storage for not a lot of gain
 *
 * Aesthetics
 *  Color scheme
 *      go to res/values/colors.xml
 *      color_correct, color_incorrect, and color_pending are the most important
 *  Logo
 *      to change the app icon and logo change the files in mipmap as well as mbpp.png in drawable
 *      to get the correct image qualities (xhdpi, mdpi, etc) you must look up the different dimensions required
 */
public class Tabbed extends AppCompatActivity {
    private ViewPager mViewPager;
    public SectionsPageAdapter mSPA;

    //save version number so different versions can be functioning
    public static final String version="v01";

    public static final String[] sizes={"small", "med", "large"}; //for consistency

    private Constants constants;

    //flags to ensure correct order of loading
    public boolean leaderboardLoaded=false;

    /*
        not yet logged in=-2 or not_logged_in
        puzzle lord=-1 or puzzle_lord
        guest=0 or guest
        team=team# or team
    */
    public static final int NOT_LOGGED_IN=-2;
    public static final int PUZZLE_LORD=-1;
    public static final int ANON=0;
    public int mode;       //don't initialize here, used to determine what information to show
    private static final int DEFAULT_MODE=-1; //purely for testing purposes, if tabbed is loaded directly what mode should it start in
    private static final String DEFAULT_ROOT="puzzlepaloozavii";  //just in case default url to puzzlepaloozavii
    public boolean prog=true;//for now, true if progress should be displayed and false if submit should be displayed

    //firebase url constants
    public static final String FB_MESSAGES = "/messages";
    public static final String FB_LEADERBOARD = "/leaderboard";
    public static final String FB_ERRORS = "/errors";
    public static final String FB_HINTS = "/hints";
    public static final String FB_QUEUE = "/queue";
    public static final String FB_CONSTANTS = "/constants";

    //classes
    private Submit submit;
    private Progress progress;
    private Messages messages;
    private Queue queue;
    private Events events;
    private ByTeam byteam;
    private Leaderboard leaderboard;
    private ByProb byprob;
    private Labbed labbed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the mode and url from the Intent used to call this class
        mode = getIntent().getIntExtra(Login.TABBED_MODE_CODE, DEFAULT_MODE); //mode is passed in from login, if no mode is found it is assumed to be an anonymous login for safety
        String root = getIntent().getStringExtra(Login.TABBED_ROOT_CODE);
        if (root == null || root.length()<1) root = DEFAULT_ROOT;

        if (constants == null) constants = new Constants(root); //this is the first time being created so initialize
        constants.setRoot(root); //if constants has been created no need to overwrite just set url

        //create class instances
        submit=Submit.createSubmit(this);
        labbed=Labbed.createLabbed(this);
        leaderboard=Leaderboard.createLeaderboard(this);
        byprob=ByProb.createByProb(this);
        byteam=ByTeam.createByTeam(this);
        if(isModePuzzleLord()){
            queue=Queue.createQueue(this);
        }else{
            progress=Progress.createProgress(this);
        }
        events=Events.createEvents(this);
        messages=Messages.createMessages(this);

        setContentView(R.layout.activity_tabbed);

        //inflate the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if(isModePuzzleLord()){
            tabLayout.addTab(tabLayout.newTab().setText("Queue"));
        }else{
            tabLayout.addTab(tabLayout.newTab().setText("Progress"));
        }
        tabLayout.addTab(tabLayout.newTab().setText("Leaders"));
        tabLayout.addTab(tabLayout.newTab().setText("Messages"));
        tabLayout.addTab(tabLayout.newTab().setText("Events"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSPA = new SectionsPageAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());

        mViewPager.setAdapter(mSPA);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //mSPA.getItemPosition(new Submission());
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

        //tell firebase that this is the context it should be using for its calls (this allows its fragments to use firebase as well)
        Firebase.setAndroidContext(this);

        //add listener to load in the  constants data
        //once constants is loaded all other data will be loaded as well
        addConstantsListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    public void refreshTabs() {
        /*
        try catch is needed because the activity is not always active
        if the user has the app running in the background and data is added or changed, refreshTabs() will be called
        however, there are no tabs since the activity is dead
        this will cause the app to throw “Java.lang.IllegalStateException Activity has been destroyed”
        the try catch prevents this
         */
        try{
            mSPA.notifyDataSetChanged();
            labbed.mLPA.notifyDataSetChanged();
            System.out.println("mlpamlpa");
        }catch (IllegalStateException e){
            //app is closed, do nothing
        }
    }

    /**
     * send a notification using tabbed as the context
     * @param title the notification title
     * @param message the notification message
     */
    public void sendNotification(String title, String message) {
        sendNotification(this, title, message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            //return to login screen
            Intent loginIntent = new Intent(this, Login.class);
            startActivity(loginIntent);
            return true;
        }else if( id == R.id.action_refresh){
            //refresh
            //show a toast to indicate that it did actually get refreshed
            Toast.makeText(getApplicationContext(), "Refreshing...", Toast.LENGTH_LONG).show();
            refreshTabs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addHintListener() {
        Firebase hintref = new Firebase(constants.url()+FB_HINTS);
        ChildEventListener listener=new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                HashMap newhints = snapshot.getValue(HashMap.class);
                getSubmit().addHints(Integer.parseInt(snapshot.getKey()), newhints);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //this should never happen
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                //this should never happen
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //this should never happen
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //write an error message to the errors tab on firebase
                Firebase eref = new Firebase(constants.url() + FB_ERRORS);
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/hintref/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that the hints were changed in some way but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        };
        hintref.addChildEventListener(listener);
    }

    private void addEventsListener(){
        final String[] properties={"money","name","number","room"};

        for(int i=1; i<=constants.NUM_TEAM; i++){
            final Team currTeam=leaderboard.team[i-1];

            ChildEventListener chi=new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //do nothing
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.getKey().equalsIgnoreCase("probs"))return; //probs gets updated more efficiently by another listener
                    currTeam.updateProperty(dataSnapshot.getKey(), dataSnapshot.getValue(String.class));
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            };

            new Firebase(constants.url()+FB_LEADERBOARD+"/"+i).addChildEventListener(chi);

            new Firebase(constants.url()+FB_LEADERBOARD+"/"+i+"/probs").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //do nothing
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    int probNum=Integer.parseInt(dataSnapshot.getKey());
                    Problem curr=currTeam.getProb(probNum);
                    Problem in=dataSnapshot.getValue(Problem.class);

                    //report to events
                    Problem.reportDifferences(curr,in, currTeam.getName(), probNum, events);

                    //update local data
                    currTeam.setProb(probNum, in);
                    refreshTabs();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }
    }


    private void addLeaderboardListener() {
        Firebase leaderboardRef = new Firebase(constants.url()+FB_LEADERBOARD);

        // Attach an listener to load the leaderboard data only once
        leaderboardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            /**
             * whenever the leaderboard is changed in firebase, update the local object
             */
            public void onDataChange(DataSnapshot snapshot) {
                Team[] team = new Team[(int) snapshot.getChildrenCount()];  //initialize array of teams
                int index = 0;
                Problem tempProblem; //temporarily stores a problem to be added to the hashmap
                LinkedHashMap lhm;  //store the linked hash map associated with a given problem to extract data
                LinkedHashMap hintsmap; //the map holding the hint data extracted from lhm
                HashMap<String, Boolean> hintsin; //temporarily store the hints for the current problem

                for (DataSnapshot teamSnaphot : snapshot.getChildren()) {
                    HashMap hm = teamSnaphot.getValue(HashMap.class);   //hashmap holding the values for an individual team from firebase
                    Team currTeam = new Team(constants);
                    currTeam.setMoney(Integer.parseInt(hm.get("money").toString()));    //set the money for the team
                    currTeam.setName(hm.get("name").toString());    //set the team name
                    currTeam.setNickname(hm.get("nickname").toString());
                    currTeam.setRoom(Integer.parseInt(hm.get("room").toString()));  //set the team room
                    currTeam.setNumber(Integer.parseInt(hm.get("number").toString()));  //set the team number
                    HashMap<String, Problem> probsHolder = new HashMap<>();  //hashmap to hold the problems for the team
                    List probsListIn = (ArrayList) (hm.get("probs"));   //a list of objects containing the information to reconstruct the problems
                    for (int i = 1; i <= constants.NUM_PROB; i++) {
                        tempProblem = new Problem();
                        lhm = ((LinkedHashMap) probsListIn.get(i));
                        tempProblem.setPoints(Integer.parseInt(lhm.get("points").toString()));          //set points from lhm data

                        hintsmap = ((LinkedHashMap) lhm.get("hints"));      //get hints linked hashmap
                        hintsin = new HashMap<>();  //set up a hasmap to hold the hints
                        for (String s : sizes)
                            hintsin.put(s, hintsmap.get(s).toString().equalsIgnoreCase("true")); //set hints
                        tempProblem.setHints(hintsin);  //set the hints field of the temporary problem to hintsin
                        probsHolder.put(i + "", tempProblem);   //put the problem just created into the hashmap of problems
                    }
                    currTeam.setProbs(probsHolder); //set the probs field of the current team to the problem hashmp just created
                    team[index] = currTeam; //add the team to the array of teams
                    index++;
                }
                getLeaderboard().refreshData(team);
                addEventsListener(); //now that the data has been loaded, listen for any changes in a more precise way that can identify events
                refreshTabs();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Firebase eref = new Firebase(constants.url() + FB_ERRORS);
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/leaderboardref/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that the leaderboard was changed in some way but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        });
    }

    private void addMessageListener() {
        //create a listener to process new messages
        Firebase messageRef = new Firebase(constants.url()+FB_MESSAGES);
        ChildEventListener listener=new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                //TODO: v3.0 input protection
                Note newNote = snapshot.getValue(Note.class);
                messages.addNote(newNote);
                if(newNote.getDate().timeSince()<10){
                    //only send notification for messages sent within the last ten minutes
                    //otherwise everytime the app is loaded there will be a notification for every message ever sent
                    //10 minutes is allowed for communication time
                    sendNotification("The puzzlelords have spoken", newNote.getText());
                }
                refreshTabs();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //this should never happen
                //TODO: v3.0 allow admin to edit messages
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Note delNote = snapshot.getValue(Note.class);
                messages.removeNote(delNote);
                refreshTabs();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //this should never happen
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //write an error message to the errors tab on firebase
                Firebase eref = new Firebase("https://puzzlepalooza.firebaseio.com/errors");
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that the messages were changed in some way but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        };
        messageRef.addChildEventListener(listener);
    }

    private void addQueueListener() {
        final ChildEventListener childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Submission in=dataSnapshot.getValue(Submission.class);
                queue.addSubmission(in);
                refreshTabs();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Submission in=dataSnapshot.getValue(Submission.class);
                queue.getSubmission(in.getUid()).setCorr(in.getCorr()); //set the correctness of the object in the queue to the new correctness
                refreshTabs();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Submission in=dataSnapshot.getValue(Submission.class);
                queue.removeSubmission(in.getUid());
                refreshTabs();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //this should not happen so ignore
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Firebase eref = new Firebase(constants.url() + FB_ERRORS);
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/queueChildEventListener/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that a submission was added but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);            }
        };

        final Firebase qref=new Firebase(constants.url()+FB_QUEUE);
        qref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //add an individual listener for every team
                qref.child(dataSnapshot.getKey() + "").addChildEventListener(childEventListener);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Firebase eref = new Firebase(constants.url() + FB_ERRORS);
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/queue/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that a team made a submission for the first time but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        });
    }

    private void addProgressListener() {
        Firebase progref = new Firebase(constants.url()+FB_QUEUE);
        ChildEventListener listener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                //a submission has been made, add it to the map
                Submission subin = snapshot.getValue(Submission.class);
                getSubmit().addSubmission(subin);
                refreshTabs();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                //submission has been read and evaluated as either right or wrong
                Submission subin = snapshot.getValue(Submission.class);
                //TODO: v3.0 add some sort of input protection to ensure the field being changed is corr
                getSubmit().submissionMarked(subin.getProb(), snapshot.getKey(), subin.getCorr());
                refreshTabs();

                //create notification
                String notifText = "Submission of \"" + subin.getAns() + "\" is ";
                if (subin.ansCorrect()) {
                    notifText += "correct!";
                } else {
                    notifText += "incorrect";
                }
                sendNotification("submission marked", notifText);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //write an error message to the errors tab on firebase
                Firebase eref = new Firebase("https://puzzlepalooza.firebaseio.com/errors");
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/qref/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that the messages were changed in some way but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        };
        progref.child(mode + "").addChildEventListener(listener);
    }

    public void sendNotification(Context context, String title, String message){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.mipmap.mbpp);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.mbpp);
        mBuilder.setLargeIcon(largeIcon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            Intent resultIntent = new Intent(context, Tabbed.class);
            resultIntent.putExtra(Login.TABBED_MODE_CODE, mode); //send the mode in as an extra
            resultIntent.putExtra(Login.TABBED_ROOT_CODE, constants.getBareRoot()); //send the root of the url as an extra

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(Tabbed.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.not
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(7, mBuilder.build());
    }

    private void addConstantsListener() {
        //create a listener for constants
        Firebase constantsRef = new Firebase(constants.url() +FB_CONSTANTS);
        //the first time constants is loaded add all other listeners
        constantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //add other listeners
                addLeaderboardListener();
                addMessageListener();

                if (isModeTeam()) {
                    addProgressListener();
                } else if (isModePuzzleLord()) {
                    addQueueListener();
                }

                if (isModeTeam()) {
                    //only teams are eligible to buy hints so don't get the hints unless a team is logged in
                    addHintListener();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Firebase eref = new Firebase(constants.url() + FB_ERRORS);
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/constantsref/ChildEventListener/onCancelled");
                perr.setAuthmessage("identified that the constants were changed in some way but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        });

        //every time constants is loaded or altered refresh the constants object
        constantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String storeRoot = constants.url();
                constants = snapshot.getValue(Constants.class);
                constants.setUrl(storeRoot);
                constants.setToInitialized();
                refreshTabs();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Firebase eref = new Firebase(constants.url() + FB_ERRORS);
                PPError perr = new PPError();
                perr.setLocation("ppvii/Tabbed/constantsref/ChildEventListener2/onCancelled");
                perr.setAuthmessage("identified that the constants were changed in some way but failed to read data");
                perr.setSysmessage(firebaseError.getMessage());
                eref.push().setValue(perr);
            }
        });
    }

    //allow other classes to access constants
    public Constants constants(){return constants;}

    //allow other classes to get the mode
    public boolean isModePuzzleLord(){return mode==PUZZLE_LORD;}
    public boolean isModeAnon(){return mode==ANON;}
    public boolean isModeTeam(){return mode>0;}

    //getters for different pages
    public Submit getSubmit(){return submit;}
    public Leaderboard getLeaderboard(){return leaderboard;}
    public Progress getProgress(){return progress;}
    public Messages getMessages(){return messages;}
    public Queue getQueue(){return queue;}
    public Events getEvents(){return events;}
    public Labbed getLabbed(){return labbed;}
    public ByProb getByProb(){return byprob;}
    public ByTeam getByTeam(){return byteam;}
}