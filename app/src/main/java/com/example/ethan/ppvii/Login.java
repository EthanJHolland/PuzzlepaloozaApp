package com.example.ethan.ppvii;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.util.ArrayList;

/**
 * The login screen
 * Has username and password fields with input protection
 * Password is not visible while being typed
 * Also has a button to login as guest which means you can only view the leaderboard
 */
public class Login extends AppCompatActivity{

    //reference to url of firebase
    private static Firebase loginRef; //all logins will be done from here

    //Keep track of the login task to ensure we can cancel it if requested
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText userField;
    private EditText passField;
    private View formView;

    //fields for sending intent to Tabbed
    private int tabbedMode=Tabbed.NOT_LOGGED_IN;
    public static final String TABBED_MODE_CODE="TABBED_MODE";
    public static final String TABBED_ROOT_CODE="TABBED_ROOT";

    //store roots for anon login
    private ArrayList<String> roots;

    @Override
    /**
     * intialize the application
     * initialize firebase
     * set view to login screen
     * pair fields with corresponding xml objects
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        loginRef =new Firebase("https://puzzlepalooza.firebaseio.com");  //must be kept after setting android context for firebase

        //read in the possible roots for anon login
        addRootRef();

        setContentView(R.layout.activity_login);

        // match fields with xml objects
        userField = (EditText) findViewById(R.id.user_id);
        passField = (EditText) findViewById(R.id.pass_id);

        passField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.user_id || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button SignInButton = (Button) findViewById(R.id.sign_in_button_id);
        if (SignInButton != null) {
            SignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }

        Button AnonButton= (Button) findViewById(R.id.anon_button_id);
        if(AnonButton!=null){
            AnonButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayRootOptions();
                }
            });
        }

        formView = findViewById(R.id.login_form_id);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        userField.setError(null);
        passField.setError(null);

        // Store values at the time of the login attempt.
        String username = userField.getText().toString();
        String password = passField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passField.setError(getString(R.string.error_field_required));
            focusView = passField;
            cancel = true;
        }else if (!isPasswordValid(password)) {
            passField.setError(getString(R.string.error_invalid_password));
            focusView = passField;
            cancel = true;
        }

        // Check for a valid username address, if the user entered one
        if (TextUtils.isEmpty(username)) {
            userField.setError(getString(R.string.error_field_required));
            focusView = userField;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            userField.setError(getString(R.string.error_invalid_user));
            focusView = userField;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            //kick off a background task to perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, null);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Attempts to sign in as an anonymous user
     */
    private void attemptAnonLogin(String root) {
        if (mAuthTask != null) {
            //some task is already running so this must have been a mistake
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        mAuthTask = new UserLoginTask(null, null, root);
        mAuthTask.execute((Void) null);
    }


    /**
     * check to see if a username is valid before actually checking if it matches a stored value
     * @param user the username to check
     * @return whether or not the username is valid
     */
    private boolean isUsernameValid(String user) {
        return user.contains("@");
    }

    /**
     * check to see if a username meets some requirement before actually checking if it matches a stored value
     * @param pass the password to check
     * @return whether or not the password is valid
     */
    private boolean isPasswordValid(String pass) {
        return pass.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean showp) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formView.animate().setDuration(shortAnimTime).alpha(
                    showp ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (showp) {
                        formView.setVisibility(View.GONE);
                    } else {
                        formView.setVisibility(View.VISIBLE);
                    }
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            //TODO: make this match above if else (if it does not already)
            if(showp){
                formView.setVisibility(View.GONE);
            }else{
                formView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addRootRef(){
        if(roots==null)roots=new ArrayList<>();
        loginRef.child("roots").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                roots.add((String)dataSnapshot.getValue());
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

            }
        });
    }

    /**
     * Let the user chose which of a list of games they would like to view
     */
    private void displayRootOptions(){
        if(roots==null || roots.isEmpty()){
            displaySimpleMessage("Anonymous login unavailable at this time", "If this problem persists please check your internet connection");
            return;
        }

        // make a dialog box to allow user to identify which hunt they would like to observe
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Please chose tournament you would like to view");

        alert.setIcon(R.drawable.mbpp);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(roots);

        alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptAnonLogin(arrayAdapter.getItem(which));
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            /**
             * if the no button is pressed close the dialog box
             * @param dialog unused
             * @param whichButton unused
             */
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show(); //display the dialog box

        Button neg=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        neg.setTextColor(Color.RED);
    }


    /**
     * display a simple popup message with the given title and message
     * the only button is a neutral button which says "okay" and closes the dialog box
     * @param title the title to be displayed in the title slot
     * @param message the body of the message to be displayed in the message slot
     */
    private void displaySimpleMessage(String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(title);
        alert.setMessage(message);
        alert.setIcon(R.drawable.mbpp);

        // Make an "okay" button that simply dismisses the alert
        alert.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
            /**
             * if the okay button is pressed close the dialog box and do nothing
             * @param dialog unused
             * @param whichButton unused
             */
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show(); //display the dialog box

        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.DKGRAY); //set the button text color
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private String user;    //the username (null if anon)
        private String pass;    //the attempted password to be checked (null if anon)
        private String root;    //the root url to access (only nonnull for anon)
        private boolean anon;   //true iff this is an anonymous user

        private boolean flag=false;

        UserLoginTask(String uin, String pin, String rootin) {
            if(uin==null){
                //anonymous login
                anon=true;
                user=null;
                pass=null;
                root=rootin;
            }else{
                //actual attempted login
                anon=false;
                user = uin;
                pass = pin;
                root=null;
            }
        }

        @Override
        /**
         * attempt either an anonymous or user login based on the class fields
         * return true iff the login was succesful
         */
        protected Boolean doInBackground(Void... params) {
            // attempt authentication with firebase
            if(anon){
                //attempt anon login
                loginRef.authAnonymously(new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        // succesful
                        tabbedMode = Tabbed.ANON;
                        flag = true;
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        // there was an error
                        flag = true;
                    }
                });
            }else{
                //attempt team or admin login
                //TODO: v3.0 check if it is the username or password which is wrong
                loginRef.authWithPassword(user.trim(), pass, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        String email = (String) authData.getProviderData().get("email");
                        if (email.contains("team")) {
                            //team mode
                            //get the portion from the end of team to just before the @
                            email = email.substring(email.indexOf('m') + 1, email.indexOf('@'));
                            tabbedMode = Integer.parseInt(email);
                        } else {
                            //puzzle lord mode
                            tabbedMode = Tabbed.PUZZLE_LORD;
                        }
                        flag = true;
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        // login failed
                        flag = true;
                    }
                });
            }

            //wait for a response from firebase
            //TODO: v1.0 make this legit if possible
            while(true){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(flag)break;
            }

            return tabbedMode!=Tabbed.NOT_LOGGED_IN;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
                //login succesful so go to Tabbed page
                Intent tabbedIntent = new Intent(getBaseContext(), Tabbed.class);
                tabbedIntent.putExtra(TABBED_MODE_CODE, tabbedMode); //send the mode in as an extra
                if(user!=null){
                    //not anon (root is already set for anon logins)
                    root=user.trim().substring(user.indexOf('@') + 1, user.lastIndexOf('.'));
                }
                tabbedIntent.putExtra(TABBED_ROOT_CODE, root); //send the url of the url as an extra
                startActivity(tabbedIntent);
            } else {
                //show an error saying the password is incorrect to the user
                passField.setError(getString(R.string.error_incorrect_password));
                passField.setText("");
                passField.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

