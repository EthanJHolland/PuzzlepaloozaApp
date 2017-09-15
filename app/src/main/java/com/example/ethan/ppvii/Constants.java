package com.example.ethan.ppvii;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.firebase.client.Firebase;

import java.util.HashMap;

/**
 * Created by Ethan on 5/5/2016.
 * Holds the fields which describe a specific puzzlehunt
 * This allows different hunts to use the same code with varying parameters
 */
public class Constants {
    private boolean initialized; //whether or not data has been loaded

    //general
    @JsonProperty
    public int NUM_TEAM;      //the number of participating teams

    @JsonProperty
    public int NUM_PROB;      //the number of problems to solve

    //hint costs
    @JsonProperty
    private boolean hintsAvailable;

    @JsonProperty
    public int START_MONEY;

    @JsonProperty
    public int SMALL_COST;  //the cost of a small hint

    @JsonProperty
    public int MED_COST;    //the cost of a medium hint

    @JsonProperty
    public int LARGE_COST;  //the cost of a large hint

    //is this version functioning
    private HashMap<String, Boolean> versions;

    @JsonProperty
    public int[] getPointsArray() {
        return pointsArray;
    }

    @JsonProperty
    private int[] pointsArray;

    //url firebase url
    public String url;

    //root of the url
    public String root;

    //default constructor
    public Constants(){
        initialized=false;
    }

    //constructor
    public Constants(String rootIn){
        this(); //call default constructor
        setRoot(rootIn); //convert from the url text to the corresponding url using the previously made method
    }

    /**
     * Indicate whether the constants data has been loaded
     * @param initialized true iff the constants data has been loaded
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    //getters and setters to indicate to firebase which values to read in/write out
    public int getLARGE_COST() {
        return LARGE_COST;
    }
    public void setLARGE_COST(int LARGE_COST) {
        this.LARGE_COST = LARGE_COST;
    }

    public int getMED_COST() {
        return MED_COST;
    }
    public void setMED_COST(int MED_COST) {
        this.MED_COST = MED_COST;
    }

    public int getSMALL_COST() {
        return SMALL_COST;
    }
    public void setSMALL_COST(int SMALL_COST) {
        this.SMALL_COST = SMALL_COST;
    }

    public int getSTART_MONEY() {
        return START_MONEY;
    }
    public void setSTART_MONEY(int START_MONEY) {
        this.START_MONEY = START_MONEY;
    }

    public int getNUM_PROB() {
        return NUM_PROB;
    }
    public void setNUM_PROB(int NUM_PROB) {
        this.NUM_PROB = NUM_PROB;
    }

    public int getNUM_TEAM() {
        return NUM_TEAM;
    }
    public void setNUM_TEAM(int NUM_TEAM) {
        this.NUM_TEAM = NUM_TEAM;
    }

    public HashMap<String, Boolean> getVersions() {
        return versions;
    }

    public void setVersions(HashMap<String, Boolean> versions) {
        this.versions = versions;
    }

    /**
     * set whether or not the version of this instance is functioning
     * it does not matter whether other versions are functioning so they are not listened for
     * @param b whether or not the current version is functioning
     */
    public void setVersionFunctioning(boolean b){
        versions.remove(Tabbed.version);
        versions.put(Tabbed.version, b);
    }

    public boolean functioning(){
        if(versions ==null)return true; //data has not yet been loaded so assume functioning
        return versions.get(Tabbed.version);
    }

    public boolean getHintsAvailable(){return hintsAvailable;}
    public void setHintsAvailable(boolean b){hintsAvailable=b;}

    public void setRoot(String rootIn){
        root=rootIn;
        url ="https://"+rootIn.trim().toLowerCase()+".firebaseio.com";}
    public String url(){return url;}

    public String getBareRoot(){
        return root;
    }

    public void setUrl(String urlIn){
        url=urlIn;
    }

    public void setToInitialized(){initialized =true;}
    public boolean isInitialized(){return initialized;}

    public void setPointsArray(int[] pointsArray) {
        this.pointsArray = pointsArray;
    }

    /**
     * find the number of digits of the maximum possible score that can be earned on a problem
     * (ex. 2 if max pts is 30, 3 if 100)
     * @return the number of digits in the maximum possible score
     */
    public int maxDigits(){
        return (pointsArray[0]+"").length();
    }

    /**
     * NOTE- THIS WILL OVERWRITE THE DATA AT THE ROOT URL. USE WITH CAUTION
     * initialize a new game at the given url
     * hints- the hint for problem i size "sz" is written as "prob i size sz"
     * leaderboard- teams are written assuming they have solved 0 problems and bought 0 hints and have the name "Team i"
     */
    public void createNewGame(){
        Firebase ref=new Firebase(url);

        //constants
        ref.child(Tabbed.FB_CONSTANTS).setValue(this);

        //errors does not need to be initialized

        //hints
        HashMap<String, HashMap<String, String>> hints=new HashMap<>();
        final String[] size={"small","med","large"};
        for(int i=1; i<=NUM_PROB; i++){
            HashMap<String, String> inner=new HashMap<>();
            for(String s: size)inner.put(s, "prob "+i+" size "+s);
            hints.put(i+"",inner);
        }
        ref.child(Tabbed.FB_HINTS).setValue(hints);

        //leaderboard
        HashMap<String, Team> lead=new HashMap<>();
        for(int i=1; i<=NUM_TEAM; i++){
            lead.put(i+"", new Team(this, i, 300+i, "Team "+i));
        }
        ref.child(Tabbed.FB_LEADERBOARD).setValue(lead);

        //messages does not need to be initialized
        //queue does not need to be initialized
    }
}
