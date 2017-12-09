package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;

public class OnlineBattleActivity extends AppCompatActivity {

    ServerConnection mServerConnection;
    boolean mBound;

    private String enemyEmail;
    private boolean wantEnemyParty = false;

    private final String ACTIVITY_TITLE = "Online Battle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_battle);
        setTitle(ACTIVITY_TITLE);

        Intent parentIntent = getIntent();
        enemyEmail = parentIntent.getStringExtra(getString(R.string.emailKey));
        System.out.println("Battle started with: " + enemyEmail);
        wantEnemyParty = true;

        // Attach to the server communication service
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        // make sure to unbind
        if (mBound) {
            //Intent intent = new Intent(this, ServerConnection.class);
            //stopService(intent);
            unbindService(mServiceConnection);
            mBound = false;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBound) {
            // Attach to the server communication service
            Intent intent = new Intent(this, ServerConnection.class);
            startService(intent);
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }

        filter.addAction(getString(R.string.serverBroadCastEvent));
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get enemies party
//        if(mBound) {
//            System.out.println("Attempting to get party");
//            mServerConnection.sendMessage(getString(R.string.ServerCommandGetParty) +
//                    ":{\"email\":\"" + enemyEmail + "\"}");
//        }
//        else {
//            System.out.println("Error: Connection not bound, cannot get party");
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerConnection.LocalBinder myBinder = (ServerConnection.LocalBinder) service;
            mServerConnection = myBinder.getService();
            mBound = true;
            if(wantEnemyParty) {
                getEnemyParty();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceConnection = null;
            mBound = false;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(getString(R.string.serverBroadCastResponseKey));
            String command;
            String data;
            if (response.indexOf(':') == -1) {
                // Got a bad response from the server. Do nothing.
                Toast toast = Toast.makeText(context, "Error communicating with server. Try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            command = response.substring(0, response.indexOf(':'));
            command = command.toUpperCase();
            data = response.substring(response.indexOf(':') + 1, response.length());

            System.out.println("In online battle");

            //Receiving iIDs of all enemy Hu-mons, must use hIDs to add to index,
            //iIDs to add to enemy party
            if (command.equals(getString(R.string.ServerCommandGetParty))) {
                System.out.println("ServerCommandGetPartySuccess");

                //TODO: Parse data to get iIDs
                ArrayList<String> enemyiIDs = new ArrayList<String>();
                System.out.println("Get-Party Payload: " + data);
                try {
                    JSONObject partyJson = new JSONObject(data);
                    String rawParty = partyJson.getString("party");
                    rawParty = rawParty.substring(rawParty.indexOf("[") + 1, rawParty.indexOf("]"));
                    rawParty = rawParty.replaceAll("\\s+","");
                    System.out.println("Formatted Get-Party Payload: " + rawParty);
                    String [] partyArray = rawParty.split(",");
                    for(int i = 0; i < partyArray.length; i++) {
                        enemyiIDs.add(partyArray[i]);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Save how many humons should be received
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPreferencesFile),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.expectedEnemiesKey), enemyiIDs.size());
                editor.putInt(getString(R.string.expectedHumonsKey), enemyiIDs.size());
                editor.commit();

                //Get instances of all iIDs
                for(int i = 0; i < enemyiIDs.size(); i++) {
                    mServerConnection.sendMessage(context.getString(R.string.ServerCommandGetInstance) +
                            ":{\"iID\":\"" + enemyiIDs.get(i) + "\"}");
                }
            }
            else if(command.equals(getString(R.string.ServerCommandGetInstance))) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    //create Humon object from payload
                    Humon enemyHumon = mapper.readValue(data, Humon.class);

                    //determine ownere of humon
                    String humonOwner = enemyHumon.getiID().substring(0, enemyHumon.getiID().indexOf("-"));
                    System.out.println("Owner of humon: " + humonOwner);

                    //retrieve email of the user
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
                    String userEmail = sharedPref.getString(context.getString(R.string.emailKey), "");

                    //Fetch humon objects of enemy party
                    if(!userEmail.equals(humonOwner)) {
                        mServerConnection.sendMessage(context.getString(R.string.ServerCommandGetHumon) + ":{\"hID\":\"" +
                                enemyHumon.gethID() + "\"}");
                    }

                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private void getEnemyParty() {
        //Get enemies party
        if(mBound) {
            System.out.println("Attempting to get party");
            mServerConnection.sendMessage(getString(R.string.ServerCommandGetParty) +
                    ":{\"email\":\"" + enemyEmail + "\"}");
            wantEnemyParty = false;
        }
        else {
            System.out.println("Error: Connection not bound, cannot get party");
        }
    }

    private IntentFilter filter = new IntentFilter();


}
