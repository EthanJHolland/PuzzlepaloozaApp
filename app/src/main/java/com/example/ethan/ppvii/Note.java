package com.example.ethan.ppvii;

/**
 * Created by Ethan on 3/31/2016.
 * Holds a message from the puzzlelords to be displayed in the messgae tab
 * The message is associated with the date (and time) of it being posted
 */
public class Note implements Comparable {
    private Date date;      //the date the note was sent
    private String text;    //the message itself

    //default constructor for firebase
    public Note(){}

    /**
     * constructor- initialize a new note
     * @param textIn the message
     * @param dateIn the date the note was sent
     */
    public Note(String textIn, Date dateIn){
        text=textIn;
        date=dateIn;
    }

    /**
     * get the message text
     * @return the message
     */
    public String getText(){
        return text;
    }

    /**
     * get the date
     * @return the day the note was posted
     */
    public Date getDate(){
        return date;
    }

    /**
     * @param another the note to be compared with
     * @return -1 iff the note was sent before another, 0 iff they were sent at the same time on the same date, and 1 if the note was sent after another
     */
    @Override
    public int compareTo(Object another) {
        Note n=(Note) another;
        return date.compareTo(n.date);
    }

    @Override
    public boolean equals(Object another){
        Note n=(Note) another;
        return text.equals(n.text);
    }
}
