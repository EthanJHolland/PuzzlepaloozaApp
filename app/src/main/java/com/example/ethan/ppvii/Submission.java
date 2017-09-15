package com.example.ethan.ppvii;

/**
 * Created by Ethan on 5/18/2016.
 */
public class Submission {
    private int teamNum;
    private String uid; //a unique id generated by firebase that can be used to identify each submission
    private int prob;
    private String ans;
    private int corr;
    private Date date;
    public static final int CORRECT=1;
    public static final int INCORRECT=-1;
    public static final int PENDING=0;

    public Submission(){
        corr=PENDING;
    }

    public Submission(String uid, int teamNum, int prob, String ans) {
        this.teamNum = teamNum;
        this.uid = uid;
        this.prob = prob;
        this.ans = ans;
        corr = PENDING;
    }

    public Submission(String uid, int teamNum, int prob, String ans, Date date) {
        this.teamNum = teamNum;
        this.uid = uid;
        this.prob=prob;
        this.ans=ans;
        this.date=date;
        corr = PENDING;
    }

    public int getCorr() {
        return corr;
    }

    public void setCorr(int corr) {
        this.corr = corr;
    }

    public int getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(int teamNum) {
        this.teamNum = teamNum;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getProb() {
        return prob;
    }

    public void setProb(int prob) {
        this.prob = prob;
    }

    public String getAns() {
        return ans;
    }

    public void setAns(String ans) {
        this.ans = ans;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean ansCorrect(){ return corr==CORRECT;}
    public boolean ansIncorrect(){ return corr==INCORRECT;}
    public boolean ansPending(){ return corr==PENDING;}

    public void setAnsAsCorrect(){corr=CORRECT;}
    public void setAnsAsIncorrect(){corr=INCORRECT;}
    public void setAnsAsPending(){corr=PENDING;}

    @Override
    public String toString() {
        String out;
        out= "\""+ans +"\" submitted on "+ date+ " is ";
        if(ansCorrect()){
            out+="correct!";
        }else if(ansIncorrect()){
            out+="incorrect";
        }else{
            out+="pending";
        }
        return out;
    }

    public int compareToSubmit(Object o) {
        //TODO: v3.0 do this for all comparetos?
        if(Submission.class!=o.getClass())return 0; //if o is not of the correct type, return 0
        Submission s=(Submission)o;

        //in team mode
        //if one is correct and the other isn't, the one which is correct is given priority
        if(ansCorrect() && !s.ansCorrect())return 1;
        if(!ansCorrect() && s.ansCorrect())return -1;
        //otherwise, if one is pending and the other isn't, the one which is pending is given priority
        if(ansPending() && !s.ansPending())return 1;
        if(!ansPending() && s.ansPending())return -1;
        //if they are both the same, compare based on date of submission
        return date.compareTo(s.date);
    }

    public int compareToQueue(Submission s){
        //if one is pending and the other isn't, the one which is pending is given priority
        if(ansPending() && !s.ansPending())return 1;
        if(!ansPending() && s.ansPending())return -1;
        //otherwise, if one is correct and the other isn't, the one which is correct is given priority
        if(ansCorrect() && !s.ansCorrect())return 1;
        if(!ansCorrect() && s.ansCorrect())return -1;
        //if they are both the same, compare based on the reverse of the date of submission for reverse chronological order
        return -1*date.compareTo(s.date);
    }
}

