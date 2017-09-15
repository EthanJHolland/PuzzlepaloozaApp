package com.example.ethan.ppvii;

import java.util.HashMap;
import java.util.Map;

/**
 * Hold the values of a single teams progress on a problem
 * this class should only be used in conjuction with a team
 * the problem number is not stored here, it must be found through the team object
 * @author Ethan Holland
 * Created by Ethan on 4/8/2016.
 */
public class Problem {
    //hint size constants
    private static final String SMALL="small";
    private static final String MED="med";
    private static final String LARGE="large";

    private Map<String, Boolean> hints;
    private int points;

    /**
     * default constructor
     * assume problem is unsolved and no hints have been bought
     * instatiate the hint hashmap
     */
    public Problem(){
        points=0;
        hints=new HashMap<String, Boolean>();
        hints.put(SMALL, false);
        hints.put(MED, false);
        hints.put(LARGE, false);
    }

    /**
     * hints getter
     * @return a hasmap containing three String keys (Problem.SMALL, Problem.MED, and Problem.LARGE each paired with a boolean indicating if that hint has been bought
     */
    public Map<String, Boolean> getHints() {
        return hints;
    }

    /**
     * determine if the problem has been solved
     * @return return true iff the team has solved this problem
     */
    public boolean hasSolved() {
        return points!=0;
    }

    public void setPoints(int pointsIn){points=pointsIn;}

    public void setHints(Map<String, Boolean> hints) {
        this.hints = hints;
    }

    /**
     * points getter
     * @return the number of points the team earned for this problem or 0 if unsolved
     */
    public int getPoints() {
        return points;
    }

    /**
     * @return true iff the team has bought the small hint
     */
    public boolean hasBoughtSmall(){
        return hasBoughtHint(SMALL);
    }

    /**
     * @return true iff the team has bought the med hint
     */
    public boolean hasBoughtMed(){
        return hasBoughtHint(MED);
    }

    /**
     * @return true iff the team has bought the large hint
     */
    public boolean hasBoughtLarge(){
        return hasBoughtHint(LARGE);
    }

    /**
     * @param size the size of the hint to buy, either Problem.SMALL, Problem.MED, or Problem.LARGE
     * @return true iff the team has bought the hint of size String size
     */
    private boolean hasBoughtHint(String size){
        return hints.get(size);
    }

    /**
     * change the hasmap to indicate the team has bought the small hint
     */
    public void buySmallHint(){
        buyHint(SMALL);
    }

    /**
     * change the hasmap to indicate the team has bought the medium hint
     */
    public void buyMedHint(){
        buyHint(MED);
    }

    /**
     * change the hasmap to indicate the team has bought the large hint
     */
    public void buyLargeHint(){
        buyHint(LARGE);
    }

    /**
     * change the hasmap to indicate the team has bought a hint of size String size
     * @param size the size of the hint to buy, either Problem.SMALL, Problem.MED, or Problem.LARGE
     */
    public void buyHint(String size){
        hints.remove(size);
        hints.put(size, true);
    }

    /**
     * indicate that the team ahs solved this problem
     * @param pts the number of points earned on the problem
     */
    public void solve(int pts){
        points=pts;
    }

    /**
     * Find what, if anything has been changed from an old to a new Problem object and write the changes to Events to be displayed to the user
     * @param curr the original problem
     * @param p2 the new version of the problem object which may or may not have modifications
     * @param events the events object to write changes to
     */
    public static void reportDifferences(Problem curr, Problem p2, String teamName, int probNum, Events events){
        if(curr.getPoints()!=p2.getPoints())events.addEvent(teamName+" has solved problem "+probNum+" for "+p2.getPoints()+" points");
        if(p2.hasBoughtSmall() && !curr.hasBoughtSmall())events.addEvent(teamName+" has bought the small hint for problem "+probNum);
        if(p2.hasBoughtMed() && !curr.hasBoughtMed())events.addEvent(teamName+" has bought the medium hint for problem "+probNum);
        if(p2.hasBoughtLarge() && !curr.hasBoughtLarge())events.addEvent(teamName+" has bought the large hint for problem "+probNum);
    }
}
