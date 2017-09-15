package com.example.ethan.ppvii;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Ethan on 7/26/2016.
 */
public class Events extends PPFragment{
    private ArrayList<String> events;   //the list of events to display, this is updated by firebase and the tabbed class


    public static Events createEvents(Tabbed tabbedIn){
        Events ev=new Events();
        ev.tabbed=tabbedIn;
        return ev;
    }

    /**
     * constructor
     * call super onCreate
     * @param savedInstanceState sent to super constructor
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param inflater the layoutinflator for inflating from xml
     * @param container the ViewGroup used for inflating
     * @param savedInstanceState unused
     * @return the view as described above
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if(!tabbed.constants().functioning())return displayNotFunctioning();

        if(events==null || events.isEmpty()){
            //no events to display so show a message saying as such
            return displayNullNoteMessage();
        }

        //there are some events so display them
        return displayEvents();
    }

    /**
     * Display a list of events in a vertical list
     * @return a recyclerview in a vertical list showing a list of events
     */
    private RecyclerView displayEvents(){
        RecyclerView rv=new RecyclerView(getActivity());

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        EventArrayAdapter maa=new EventArrayAdapter(events);
        rv.setAdapter(maa);
        return rv;
    }

    /**
     * Display a message indicating that there are no new events at this time
     * @return a textview holding the message
     */
    private TextView displayNullNoteMessage(){
        //there are currently no new events so display text saying such
        TextView tv=new TextView(getActivity());
        tv.setText(getString(R.string.events_empty));
        return tv;
    }

    public void addEvent(String s){
        if(events ==null) events = new ArrayList<>();//this is the first event so initialize the list
        events.add(s);
    }
}


class EventArrayAdapter extends RecyclerView.Adapter<EventArrayAdapter.EventViewHolder>{
    private String[] values;

    public EventArrayAdapter(ArrayList<String> listIn){
        values=new String[listIn.size()];
        for(int i=0; i<values.length; i++){
            values[i]=listIn.get(values.length-i-1); //reverse order so newest is first
        }
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_textview, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        holder.text.setText(values[position]);
    }

    @Override
    public int getItemCount() {
        return values.length;
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public EventViewHolder(View itemView) {
            super(itemView);
            text = (TextView)itemView.findViewById(R.id.simple_tv);
        }
    }
}