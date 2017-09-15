package com.example.ethan.ppvii;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ethan on 4/8/2016.
 * Store all information relevant to a team including performance on the problems
 */
public class Team implements Comparable{
    private int room;
    private int number;
    private String name;
    private String nickname;
    private Map<String, Problem> probs;
    private int money;

    private final Constants constants; //store the constants object so fields can be accesed

    public Team(Constants constantsIn){
        probs=new HashMap<>();
        for(int i=1; i<=constantsIn.NUM_PROB; i++) {
            probs.put(i+"", new Problem());
        }
        money=constantsIn.START_MONEY;
        constants=constantsIn;
    }

    public Team(Constants constantsIn, int numberIn, int roomIn, String nameIn){
        this(constantsIn);
        room=roomIn;
        number=numberIn;
        name=nameIn;
    }

    public void setProbs(Map<String, Problem> probs) {
        this.probs = probs;
    }

    /**
     * replace the problem for a given problem number with a new problem object
     * @param probNum the problem object to store
     * @param in the problem number to store the object in
     */
    public void setProb(int probNum, Problem in){
        probs.remove(probNum+"");
        probs.put(probNum+"", in);
    }

    public Problem getProb(int num){
        return probs.get(num+"");
    }

    public Map<String, Problem> getProbs() {
        return probs;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int numberIn){
        number=numberIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn){
        name=nameIn;
    }

    public String getNickname(){return nickname;}

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getRoom(){
        return room;
    }

    public void setRoom(int roomIn){
        room=roomIn;
    }

    public int getMoney() {
        return money;
    }

    public int moneySpent(){
        int out=0;
        for(Problem p: probs.values()){
            if(p.hasBoughtSmall())out+=constants.getSMALL_COST();
            if(p.hasBoughtMed())out+=constants.getMED_COST();
            if(p.hasBoughtLarge())out+=constants.getLARGE_COST();
        }
        return out;
    }

    public void setMoney(int moneyIn){
        money=moneyIn;
    }

    public boolean hasBoughtSmall(int prob){return probs.get(prob+"").hasBoughtSmall();}
    public boolean hasBoughtMed(int prob){return probs.get(prob+"").hasBoughtMed();}
    public boolean hasBoughtLarge(int prob){return probs.get(prob+"").hasBoughtLarge();}
    public boolean hasBoughtSize(String size, int prob) {
        if (size.equals("small")) return hasBoughtSmall(prob);
        if (size.equals("med")) return hasBoughtMed(prob);
        return size.equals("large") && hasBoughtLarge(prob); //return false if it satisfies none
    }

    /**
     *
     * @param teams an array of teams which is assumed to include the team being examined
     * @return the rank of the team with a "t-" before it if they are tied for that rank with other teams (ex. 9 or t-5)
     */
    public String rank(Team[] teams){
        boolean first=false;
        boolean second=false; //two teams with the same team must have the same tot points as the given team since teams includes the given team
        int out=1;
        int curr=totalPoints();
        for(Team t: teams){
            int tot=t.totalPoints();
            if(tot==curr){
                second=first;
                first=true;
            }else if(tot>curr){
                out++;
            }
        }
        if(second)return "t-"+out;
        return out+"";
    }

    public int totalPoints(){
        int sum=0;
        for(int i=1; i<=constants.NUM_PROB; i++){
            sum+=getPointsForProb(i);
        }
        return sum;
    }

    public int numSolved(){
        int sum=0;
        for(int i=1; i<=constants.NUM_PROB; i++){
            if(hasSolved(i))sum++;
        }
        return sum;
    }

    /**
     * Get the number of points earned on a given problem
     * @param i the problem number to be checked
     * @return the number of points earned on that problem
     */
    public int getPointsForProb(int i){
        return getProb(i).getPoints();
    }

    public void buySizeHint(int prob, String size){
        if(size.equals("small")){
            buySmallHint(prob);
        }else if(size.equals("med")){
            buyMedHint(prob);
        }else{
            buyLargeHint(prob);
        }
    }

    public void buySmallHint(int prob){
        if(getProb(prob).hasBoughtSmall())return;
        money-=constants.SMALL_COST;
        getProb(prob).buySmallHint();
    }

    public void buyMedHint(int prob){
        if(getProb(prob).hasBoughtMed())return;
        money-=constants.MED_COST;
        getProb(prob).buyMedHint();
    }

    public void buyLargeHint(int prob){
        if(getProb(prob).hasBoughtLarge())return;
        money-=constants.LARGE_COST;
        getProb(prob).buyLargeHint();
    }

    public boolean hasSolved(int prob){
        return getProb(prob).hasSolved();
    }

    /**
     * Update a property
     * the only properties which cannot be updated in this way are
     * probs- because it is inneficient, probs should be updated problem by problem
     * number- this is a safety measure, number should never be changed after intialization
     * @param propName the name of the field (and consequently the key in the firebase json tree) related to the given property
     * @param newValue the new value to store in the given property's location
     */
    public void updateProperty(String propName, String newValue){
        if(propName.equals("money")){
            money=Integer.parseInt(newValue);
        }else if(propName.equals("name")){
            name=newValue;
        }else if(propName.equals("nickname")){
            nickname=newValue;
        }else if(propName.equals("room")){
            room=Integer.parseInt(newValue);
        }
    }

    @Override
    public String toString() {
        return "Team{" +
                "money=" + money +
                ", name='" + name + '\'' +
                ", number=" + number +
                ", room=" + room +
                '}';
    }

    /**
     * compare two teams by total points then problems solved then money remaining
     * @param o a team to be compared with
     * @return 1 iff the given team is greater than o, 0 iff they are equal, and -1 iff o is greater than the given team
     */
    @Override
    public int compareTo(Object o) {
        Team t=(Team)o; //cast to team
        //first compare by total points
        if(totalPoints()>t.totalPoints())return 1;
        if(totalPoints()<t.totalPoints())return -1;
        //if equal compare by problems solved
        if(numSolved()>t.numSolved())return 1;
        if(numSolved()<t.numSolved())return -1;
        //lastly by money remaining
        if(getMoney()>t.getMoney())return 1;
        if(getMoney()<t.getMoney())return -1;
        //consider them to be equivalent in ranking
        return 0;
    }
}
