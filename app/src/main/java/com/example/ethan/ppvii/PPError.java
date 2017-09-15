package com.example.ethan.ppvii;

/**
 * Created by Ethan on 4/2/2016.
 */

/**
 * holds an error object for ease of writing errors to firebase
 */
public class PPError {
    private String location; //the path to the class where the error occured
    private String authmessage; //the message from the writer of the code
    private String sysmessage; //the error message from the system

    public PPError(){}

    public PPError(String loc, String amess, String smess){
        //TODO: v3.0 add date
        location=loc;
        authmessage=amess;
        sysmessage=smess;
    }

    public void setLocation(String loc){
        location=loc;
    }

    public void setAuthmessage(String amess){
        authmessage=amess;
    }

    public void setSysmessage(String smess){
        sysmessage=smess;
    }

    public String getLocation() {
        return location;
    }

    public String getAuthmessage() {
        return authmessage;
    }

    public String getSysmessage() {
        return sysmessage;
    }
}
