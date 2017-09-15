package com.example.ethan.ppvii;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import java.util.ArrayList;

/**
 * Show any messages from the puzzle lords in a list format
 * Messages are ordered with the most recent being first
 */
public class Messages extends PPFragment{
    private ArrayList<Note> notes;   //the list of messages to display, this is updated by firebase and the tabbed class


    public static Messages createMessages(Tabbed tabbedIn){
        Messages messages=new Messages();
        messages.tabbed=tabbedIn;
        return messages;
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

        View messageView; //holds either a view showing messages or a TextView saying there are no messages
        if(notes==null || notes.isEmpty()){
            //no notes to display so show a message saying as such
            messageView=displayNullNoteMessage();
        }else{
            //there are some notes so display them
            messageView=displayNotes();
        }

        if(tabbed.isModePuzzleLord()){
            //show the infrastructure to send messages
            LinearLayout lin=displayMessageWritingSystem(inflater, container);
            lin.addView(messageView);
            return lin;
        }

        return messageView; //not puzzle lord mode so just return the body
    }

    /**
     * Show an a field for writing messages and a button for sending them in a linear layout
     * @param inflater used for layout inflation
     * @param container used for layout inflation
     * @return a linear layout with the message writing system
     */
    private LinearLayout displayMessageWritingSystem(LayoutInflater inflater, ViewGroup container){
        View plordView=inflater.inflate(R.layout.plord_messages, container, false);

        final EditText field=(EditText)plordView.findViewById(R.id.pmess_field);

        Button send=(Button)plordView.findViewById(R.id.pmess_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note n=new Note(field.getText().toString(), Date.currDate());
                Firebase mref=new Firebase(tabbed.constants().url()+Tabbed.FB_MESSAGES);
                mref.push().setValue(n);

                //clear the text from the submission field
                field.setText("");

                //show a toast to indicate the message was sent
                Toast.makeText(getActivity().getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show(); //display a message to show that the submission was succesful
            }
        });

        //make a linearlayout to hold both
        LinearLayout lin=new LinearLayout(getActivity());
        lin.setOrientation(LinearLayout.VERTICAL);
        lin.addView(plordView);
        return lin;
    }

    /**
     * Display a list of Notes in a vertical list
     * @return a recyclerview of Note objects in a vertical list showing the message and how long ago it was sent
     */
    private RecyclerView displayNotes(){
        RecyclerView rv=new RecyclerView(getActivity());

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        MessArrayAdapter maa=new MessArrayAdapter(notes);
        rv.setAdapter(maa);
        return rv;
    }

    /**
     * Display a message indicating that there are no notes at this time
     * @return a textview holding the message
     */
    private TextView displayNullNoteMessage(){
        //there are currently no messages so display text saying such
        TextView tv=new TextView(getActivity());
        tv.setText(getString(R.string.messages_notyetloaded));
        return tv;
    }

    /**
     * register that a new note has just been posted by an admin and add it to the list
     * @param n the note that had just been added
     */
    public void addNote(Note n){
        if(notes==null) notes = new ArrayList<>();
        //this is the first note so initialize the list
        notes.add(n);
    }

    /**
     * register that a note has just be removed by an admin and removie it from the list
     * @param n the note to be removed
     */
    public void removeNote(Note n){
        notes.remove(n);
    }
}

class MessArrayAdapter extends RecyclerView.Adapter<MessArrayAdapter.MessViewHolder>{
    ArrayList<Note> values;

    public MessArrayAdapter(ArrayList<Note> listIn){
        values=listIn;
        sort();
    }

    @Override
    public MessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_messages, parent, false);
        return new MessViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessViewHolder holder, int position) {
        Note curr=values.get(position);
        holder.mess.setText(curr.getText());
        holder.date.setText(curr.getDate().formattedTimeSince());
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    /**
     * sort the values arraylist by the following criteria
     * first, pending rise to the top
     * within pending, by date with earliest first
     */
    private void sort(){
        if(values==null)return; //make sure values has been initialized
        boolean done;
        //bubble sort into reverse order (since the arrayadapter puts the 0th element at the top)
        //TODO: v2.0 use a better sort
        //TODO: v1.0 two messages sent within the same minute will have the wrong order
        while(true){
            done=true;
            for(int i=0; i<values.size()-1; i++){
                if(values.get(i).compareTo(values.get(i + 1))==-1){
                    //if the ith comes chronologically before the i+1th swap them
                    Note temp=values.get(i+1);
                    values.set(i + 1, values.get(i));
                    values.set(i, temp);
                    done=false;
                }
            }
            if(done)break;
        }
    }

    class MessViewHolder extends RecyclerView.ViewHolder {
        public TextView mess;
        public TextView date;

        public MessViewHolder(View itemView) {
            super(itemView);
            mess = (TextView)itemView.findViewById(R.id.mess_cv_mess);
            date = (TextView)itemView.findViewById(R.id.mess_cv_date);
        }
    }
}