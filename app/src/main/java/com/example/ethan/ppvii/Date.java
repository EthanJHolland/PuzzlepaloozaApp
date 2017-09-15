package com.example.ethan.ppvii;

import java.util.Calendar;

/**
 * Created by Ethan on 5/23/2016.
 * Hold the month, day of the month, hour, and minute to determine a Date
 * Used primarily as an object to be written to and read from firebase
 */
public class Date implements Comparable{
    //fields
    private int month;
    private int day;
    private int hour; //0-24
    private int min;

    //default constructor for firebase
    public Date(){}

    /**
     * Create a new Date object with the given month, day, hour, and minute
     * @param month the month to store
     * @param day the day to store
     * @param hour the hour to store
     * @param min the minute to store
     */
    public Date(int month, int day, int hour, int min) {
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.min = min;
    }

    //getters and setters for firebase
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    /**
     * Get the current Date according to the computer's Calendar
     * @return a Date object holding the current Date
     */
    public static Date currDate(){
        Calendar cal = Calendar.getInstance();
        int imonth=cal.get(Calendar.MONTH)+1; //+1 since it starts at 0 instead of jan as 1
        int iday=cal.get(Calendar.DAY_OF_MONTH);
        int ihour=cal.get(Calendar.HOUR);
        if(cal.get(Calendar.AM_PM)==Calendar.PM)ihour+=12;
        int imin=cal.get(Calendar.MINUTE);
        return new Date(imonth, iday, ihour, imin);
    }

    @Override
    public String toString() {
        return month+"/"+day+" at "+hour+":"+min;
    }

    /**
     * Compare two date objects in standard chronological order
     * @param o the Date object to be compared with
     * @return -1 iff the date comes chronologically before the given date, 0 iff the dates are the same, 1 if the date comes chronologically after the given date
     */
    @Override
    public int compareTo(Object o) {
        Date d=(Date)o;
        if(month>d.month)return 1;
        if(month<d.month)return -1;
        if(day>d.day)return 1;
        if(day<d.day)return -1;
        if(hour>d.hour)return 1;
        if(hour<d.hour)return -1;
        if(min>d.min)return 1;
        if(min<d.min)return -1;
        return 0;
    }

    /**
     * determine how long it has been since the given date in minutes
     * @return the number of minutes that have passed since the given date
     */
    public int timeSince(){
        Date curr=currDate();
        int monthsSince=curr.getMonth()-month;
        int daysSince=monthsSince*30+curr.getDay()-day;
        int hoursSince=daysSince*24+curr.getHour()-hour;
        return hoursSince*60+curr.getMin()-min;
    }

    /**
     * Format the elapsed time since the date in the following form (note x varies based on the exact date)
     * if it has been at least a month
     *      month/day
     * if it has been less than a month and more than a day
     *      xd
     * if it has been less than a day and more than an hour
     *      xh
     * if it has been less than an hour
     *      xm
     * @return a string with the formatted date to be displayed
     */
    public String formattedTimeSince(){
        Date curr=currDate();
        //TODO: v1.0 get rid of assumption that all months are 30 days
        //TODO: v1.0 does this actually work? check all possibilities

        if(curr.getMonth()-month>1 || (curr.getMonth()>month && curr.getDay()>=day)){
            //in months
            return month+"/"+day;
        }else if(curr.getMonth()>month || curr.getDay()-day>1 || (curr.getDay()>day && curr.getHour()>=hour)){
            //in days
            return (30+curr.getDay()-day)%30+"d";
        }else if(curr.getDay()>day || curr.getHour()-hour>1 || (curr.getHour()>hour && curr.getMin()>=min)){
            //in hours
            return (24+curr.getHour()-hour)%24+"h";
        }else if(min==curr.getMin()){
            //would say 0m so instead say now
            return "now";
        }else{
            //in min
            return (60+curr.getMin()-min)%60+"m";
        }
    }

}
