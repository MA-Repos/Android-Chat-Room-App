package comp2601.com.assign3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import comp2601.com.assign3.communication.Event;
import comp2601.com.assign3.communication.EventHandler;
import comp2601.com.assign3.communication.JSONEvent;
import comp2601.com.assign3.communication.JSONEventSource;
import comp2601.com.assign3.communication.NoEventHandler;
import comp2601.com.assign3.communication.Reactor;
import comp2601.com.assign3.communication.ThreadWithReactor;

/*
 *  Name: Mir Abbas
 *
 *  MainActivity class acts as connection between the server and the game activity.
 *  It executes a reactor pattern to handle events coming from the server.
 *  These are the events the reactor handles:
 *  CONNECTED_RESPONSE
 *  USERS_UPDATED
 *  MOVE_MESSAGE
 *  DISCONNECT_RESPONSE
 *
 *  The reactor pattern code is based on the reactor pattern java code provided
 *  by prof. Tony White:
 *  package edu.carleton.COMP2601.assignment2.communication;
 *
 *
 */


public class MainActivity extends Activity {
    private static String HOST = "10.0.2.2"; //local host when accessing local machine via an emulator
    private static int PORT = 2001; //Port number of server
    static MainActivity instance;  //creating a singleton pattern, so GameActivity can access methods and variables

    private static final String TAG = "MainActivity";
    private String name;  //name of user which is created by random value N + a number from 1 to 100
    private clientThreadWithReactor clientTWR;
    private StartReactor myReactor;
    private Handler handler;  //handler to allow updates to the UI
    private ArrayList<String> users;   //list of users
    private final Context mContext = this;
    private TextView status;  //Status of game. Did not want to play game
    private ProgressDialog myProgressDialog;  //progress spinner

    private String dest;
    private TextView display;
    private Button loginBtn;
    private Button logoutBtn;
    private Spinner mUserSpinner;
    private String mSelectedUser = null;
    private Button clearBtn;
    private Button sendBtn;
    private EditText msgField;
    private ArrayAdapter<String> dataAdapter;


    /* create random name
     * start reactor
     * create array adapter for listview
     * send connect request message to server
     * set up onClick listner for the listview: send a Play Game Request to the person clicked on
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        handler = new Handler();

        display = (TextView) findViewById(R.id.textView_display);
        name = "User " + new Random().nextInt(100);
        loginBtn =(Button) findViewById(R.id.loginBtn);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        mUserSpinner = (Spinner)findViewById(R.id.spinnerUsers);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        clearBtn = (Button) findViewById(R.id.clearBtn);
        msgField = (EditText) findViewById(R.id.editText);

        myReactor = new StartReactor();
        myReactor.execute("");

        mUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedUser = parent.getItemAtPosition(position).toString();
                Log.i("Selected", "Selected User : " + mSelectedUser);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        handler = new Handler();
        users = new ArrayList<String>();
        users.add(getString(R.string.everyone));

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, users);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserSpinner.setAdapter(dataAdapter);


        //Send Connect Request message to server on start up.  Starting the progress spinner dialog.
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put(Common.type, "CONNECT_REQUEST");
                    msg.put(Common.source, name);
                    new Write(msg).execute("");
                    showDialog(0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject msg = new JSONObject();
                //send Game on message to server
                if(!mSelectedUser.equals(getString(R.string.everyone)))
                    display.append(instance.getName()+ ": "+msgField.getText().toString()+"\n");
                try {
                    msg.put("type", "MOVE_MESSAGE");
                    msg.put("source", instance.getName());
                    msg.put("destination", mSelectedUser);
                    msg.put("msg", msgField.getText().toString());
                    instance.new Write(msg).execute("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display.setText("");
            }
        });

    }


    // get an instance of this Activity
    public static MainActivity getInstance(){
        return instance;
    }

    public String getName(){
        return name;
    }

    public String getDestination(){
        return dest;
    }
    //setup for creating a Progress Dialog
    protected Dialog onCreateDialog(int id) {

        // Arg value corresponds to showDialog(0)
        if (id != 0)
            return null;
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage("Loading ...");
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        return myProgressDialog ;
    }

    //on destory sends a disconnect msg to server
    protected void onDestroy(){
        super.onDestroy();
        JSONObject msg = new JSONObject();
        try {
            msg.put("type", "DISCONNECT_REQUEST");
            msg.put("source", name);
            new Write(msg).execute("");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * This class actually creates the server with the reactor and sets up
     * reading etc.
     */
    public class StartReactor extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {
            clientTWR = new clientThreadWithReactor();
            clientTWR.init();
            try {
                clientTWR.run();
            } catch (Exception e) {
                return "Failed to initialize server";
            }

            return "Initialized server";
        }

        protected void onPostExecute(String result) {
            Log.i(TAG,result);
            super.onPostExecute(result);
        }
    }

    /*
     * Require this in order to do network I/O in background
     */
    public class Write extends AsyncTask<String, String, String> {
        JSONObject msg;

        public Write(JSONObject msg) {
            this.msg = msg;
        }

        protected String doInBackground(String... params) {
            try {
                clientTWR.myEventSource.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return msg.toString();
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    /*
     * Implementation of the reactor within an object class.
     */
    private class clientThreadWithReactor {
        public Reactor myReactor;
        public JSONEventSource myEventSource;

        public void init() {
            myReactor = new Reactor();
            myReactor.register("CONNECTED_RESPONSE", new ConnectedResponsedHandler());
            myReactor.register("USERS_UPDATED", new UsersUpdatedHandler());
            myReactor.register("MOVE_MESSAGE", new MoveMessageHandler());
            myReactor.register("DISCONNECT_RESPONSE", new DisconnectResponseHandler());
        }

        void run() throws IOException, ClassNotFoundException, NoEventHandler,
                JSONException {
            myEventSource = new JSONEventSource(HOST, PORT);
            ThreadWithReactor twr = new ThreadWithReactor(myEventSource, myReactor);
            twr.start();
        }

        /*
         * Event handler for the CONNECTED RESPONSE message
         */
        private class ConnectedResponsedHandler implements EventHandler {
            public void handleEvent(Event event) {

                handler.post(new Runnable() {
                    public void run() {
                        myProgressDialog.dismiss();

                        Toast.makeText(getApplicationContext(), getString(R.string.connected), Toast.LENGTH_LONG).show();
                    }
                });

            }
        }

        /*
         * Event handler for the DISCONNECT RESPONSE message
         */
        private class DisconnectResponseHandler implements EventHandler {
            public void handleEvent(Event event) {
                handler.post(new Runnable() {
                    public void run() {
//                        myProgressDialog.dismiss();

                        Toast.makeText(getApplicationContext(), getString(R.string.disconnected), Toast.LENGTH_LONG).show();

                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });

            }
        }

		/*
		 * Event handler for the MOVE MESSAGE
		 */

        private class MoveMessageHandler implements EventHandler {
            public void handleEvent(final Event event) {
                handler.post(new Runnable() {
                    public void run() {
                        final JSONEvent myJSONEvent = (JSONEvent) event;
                        String pressButton = myJSONEvent.get("source") + ": " + myJSONEvent.get("msg");
                        display.append(pressButton + '\n');
                    }
                });
            }
        }


        /*
         * Event handler for the USER_UPDATED message
         */
        private class UsersUpdatedHandler implements EventHandler {
            public void handleEvent(final Event event) {
                handler.post(new Runnable () {
                    public void run() {
                        final JSONEvent myJSONEvent = (JSONEvent) event;
                        users.clear();
                        JSONArray listOfUsersJSON = null;
                        try {
                            listOfUsersJSON = new JSONArray(myJSONEvent.get("users"));
                            users.add(getString(R.string.everyone));
                            for (int i = 0; i < listOfUsersJSON.length(); i++)
                                if (!listOfUsersJSON.getString(i).equalsIgnoreCase(name))
                                    users.add(listOfUsersJSON.getString(i));
                            //update the list view with new user data
                            dataAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}

