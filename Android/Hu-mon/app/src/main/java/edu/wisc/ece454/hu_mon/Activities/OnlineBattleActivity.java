package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;
import edu.wisc.ece454.hu_mon.Utilities.HumonPartySaver;
import edu.wisc.ece454.hu_mon.Utilities.UserHelper;

public class OnlineBattleActivity extends AppCompatActivity {

    ServerConnection mServerConnection;
    boolean mBound;
    private final String TAG = "BATTLE";
    private String enemyEmail;
    private boolean battleStarting = false;
    private boolean isInitiaor = false;
    private boolean waitingForEnemy = true;

    private final String ACTIVITY_TITLE = "Online Battle";
    private String HUMONS_KEY;
    private final int INSTANCE_TYPE = 0;
    private final int MOVE_TYPE = 1;
    private final int RUN_TYPE = 2;
    private final String COMMAND_TYPE = "commandType";
    private final String WAITING_MESSAGE = "Waiting for enemy...";

    private Humon enemyHumon;
    private Humon playerHumon;
    private int playerHumonIndex;
    private String userEmail;
    private User user;

    private Move playerMove;
    private Move enemyMove;

    private int playerRng;
    private int enemyRng;

    private boolean gameOver;
    private boolean gameSaved;

    private Move.Effect playerStatus;
    private Move.Effect enemyStatus;

    //Queue of messages to be displayed in console (front is 0)
    private ArrayList<String> consoleDisplayQueue;
    private String currentConsoleMessage = "";

    private ProgressBar playerHealthBar;
    private ProgressBar enemyHealthBar;
    private TextView playerStatusTextView;
    private TextView enemyStatusTextView;

    private ArrayList<String> partyHumons;
    private ArrayList<Integer> partyHumonIndices;
    private ArrayList<Move> playerMoveList;
    private ArrayList<Move> enemyMoveList;
    private ArrayAdapter<Move> moveAdapter;

    private GridView playerMovesView;
    private TextView userConsole;

    private int turns;
    private int dmgMultiplier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle_layout);
        setTitle(ACTIVITY_TITLE);
        HUMONS_KEY = getString(R.string.humonsKey);

        Intent parentIntent = getIntent();
        enemyEmail = parentIntent.getStringExtra(getString(R.string.emailKey));
        isInitiaor = parentIntent.getBooleanExtra(getString(R.string.initiatorKey), false);
        Log.i(TAG, "Battle started with: " + enemyEmail);
        battleStarting = true;
        UserHelper.saveToServer(getApplicationContext());

        // Attach to the server communication service
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        partyHumons = new ArrayList<String>();
        partyHumonIndices = new ArrayList<Integer>();
        gameOver = true;
        gameSaved = false;
        turns = 0;
        dmgMultiplier = 1;

        //Setup Grid View and Adapter
        playerMoveList = new ArrayList<Move>();
        enemyMoveList = new ArrayList<Move>();
        playerMovesView = (GridView) findViewById(R.id.moveGridView);
        moveAdapter = new ArrayAdapter<Move>(this,
                R.layout.move_grid_element, playerMoveList);
        playerMovesView.setAdapter(moveAdapter);
        playerMovesView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            choosePlayerMove(playerMoveList.get(position));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        //Setup console
        consoleDisplayQueue = new ArrayList<String>();
        userConsole = (TextView) findViewById(R.id.userConsole);
        userConsole.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayConsoleMessage();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {

        if(!gameOver) {
            if (!mBound) {
                // Attach to the server communication service
                Intent intent = new Intent(this, ServerConnection.class);
                startService(intent);
                bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            }

            Toast toast = Toast.makeText(getApplicationContext(), "Ran away!", Toast.LENGTH_SHORT);
            toast.show();

            //Save the humon's state
            saveHumons();

            //tell enemy you ran away
            mServerConnection.sendMessage(getString(R.string.ServerCommandBattleAction) +
                    ":{\"commandType\":"+ RUN_TYPE + "}");

            mServerConnection.sendMessage(getString(R.string.ServerCommandBattleEnd) +
                    ":{}");

            //return to the menu
            finish();
        }
        // make sure to unbind
        if (mBound) {
            //Intent intent = new Intent(this, ServerConnection.class);
            //stopService(intent);
            unbindService(mServiceConnection);
            mBound = false;
        }
        //retrieve email of the user
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.gameRunningKey), false);
        editor.commit();

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

        if(gameOver) {
            //retrieve email of the user
            SharedPreferences sharedPref = this.getSharedPreferences(
                    getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
            userEmail = sharedPref.getString(getString(R.string.emailKey), "");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.gameRunningKey), true);
            editor.commit();

            userConsole.setVisibility(View.INVISIBLE);
            playerMovesView.setVisibility(View.VISIBLE);
            consoleDisplayQueue.clear();
            gameOver = false;
            gameSaved = false;
            partyHumons.clear();
            partyHumonIndices.clear();
            playerHumonIndex = 0;
            waitingForEnemy = true;

            //status effects of humons
            playerStatus = null;
            enemyStatus = null;

            //load humons into battle
            if(!isInitiaor) {
                loadPartyHumons();
                choosePlayerHumon();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!gameSaved) {
            runAwayDialog();
        }
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
            if(battleStarting) {
                getEnemyParty();
                notifyEnemyStart();
                battleStarting = false;
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

            Log.i(TAG, "In online battle");

            //Receiving iIDs of all enemy Hu-mons, must use hIDs to add to index,
            //iIDs to add to enemy party
            if (command.equals(getString(R.string.ServerCommandGetParty))) {
                Log.i(TAG, "ServerCommandGetPartySuccess");

                //Parse data to get iIDs
                ArrayList<String> enemyiIDs = new ArrayList<String>();
                Log.i(TAG, "Get-Party Payload: " + data);
                try {
                    JSONObject partyJson = new JSONObject(data);
                    String rawParty = partyJson.getString("party");
                    rawParty = rawParty.substring(rawParty.indexOf("[") + 1, rawParty.indexOf("]"));
                    rawParty = rawParty.replaceAll("\\s+","");
                    Log.i(TAG, "Formatted Get-Party Payload: " + rawParty);
                    String [] partyArray = rawParty.split(",");
                    if(partyArray.length == 0) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Opponent has no humons!", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    }
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
                    Log.i(TAG, "Owner of humon: " + humonOwner);

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
            else if(command.equals(getString(R.string.ServerCommandBattleAction))) {
               Log.i(TAG, "Received battle action: " + data);
                try {
                    JSONObject battleJson = new JSONObject(data);

                    if(battleJson.getInt(COMMAND_TYPE) == INSTANCE_TYPE) {
                        String enemyIID = battleJson.getString("iID");
                        loadEnemy(enemyIID);
                        //load humons into battle
                        if(isInitiaor) {
                            loadPartyHumons();
                            choosePlayerHumon();
                        }
                        waitingForEnemy = false;
                        endWaitingMessage();
                    }
                    else if(battleJson.getInt(COMMAND_TYPE) == MOVE_TYPE) {
                        enemyMove = new ObjectMapper().readValue(battleJson.getString("move"), Move.class);
                        enemyRng = battleJson.getInt("rng");
                        waitingForEnemy = false;
                        endWaitingMessage();

                        if(playerMove != null) {
                            startBattleSequence();
                        }
                    }
                    else if(battleJson.getInt(COMMAND_TYPE) == RUN_TYPE) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Opponent ran away!", Toast.LENGTH_SHORT);
                        toast.show();

                        finishBattle();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
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

    private IntentFilter filter = new IntentFilter();

    //Get enemy's humons
    private void getEnemyParty() {
        //Get enemies party
        if(mBound) {
            Log.i(TAG, "Attempting to get party");
            mServerConnection.sendMessage(getString(R.string.ServerCommandGetParty) +
                    ":{\"email\":\"" + enemyEmail + "\"}");
        }
        else {
            Log.i(TAG, "Error: Connection not bound, cannot get party");
        }
    }

    //Notify enemy that battle request was accepted
    private void notifyEnemyStart() {

        //initiator: true for accepted notification

        //Get enemies party
        if(mBound) {
            Log.i(TAG, "Attempting to notify enemy of battle start");
            mServerConnection.sendMessage(getString(R.string.ServerCommandBattleStart) +
                    ":{\"email\":\"" + enemyEmail + "\", \"initiator\":" + isInitiaor + "}");
        }
        else {
            Log.i(TAG, "Error: Connection not bound, cannot get notify enemy");
        }
    }

    /*
     * Loads all humons in party which do not have 0 hp.
     *
     *
     */
    private void loadPartyHumons() {
        Log.i(TAG, "Loading Humons to battle");
        Thread loadThread = new Thread() {
            public void run() {
                String partyFilename = getFilesDir() + "/" + userEmail + getString(R.string.partyFile);

                try {
                    Log.i(TAG, "Attempting to load: " + partyFilename);

                    //load party file
                    String partyString;
                    FileInputStream inputStream = new FileInputStream(partyFilename);
                    int inputBytes = inputStream.available();
                    byte[] buffer = new byte[inputBytes];
                    inputStream.read(buffer);
                    inputStream.close();
                    partyString = new String(buffer, "UTF-8");
                    Log.i(TAG, "User party string: " + partyString);
                    JSONObject fileJson = new JSONObject(partyString);
                    JSONArray humonsArray = fileJson.getJSONArray(HUMONS_KEY);
                    Log.i(TAG, partyFilename + " loaded");

                    //load humon into json object format
                    for(int i = 0; i < humonsArray.length(); i++) {
                        String humonString = humonsArray.getString(i);
                        JSONObject humonJson = new JSONObject(humonString);
                        int hp = humonJson.getInt("hp");
                        if(hp > 0) {
                            String name = humonJson.getString("name");
                            partyHumons.add(name);
                            partyHumonIndices.add(i);
                        }
                    }

                    inputStream.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "No party file for: " + userEmail);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        loadThread.run();
    }

    /*
     * Loads a dialogue with all choosable Humons.
     * Choosing a humon loads it from the file.
     * If no humons available, ends battle.
     *
     */
    private void choosePlayerHumon() {
        Log.i(TAG, "Choosing Humon to battle");
        if(partyHumons.size() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "No available humons!", Toast.LENGTH_SHORT);
            toast.show();

            //return to the menu
            finish();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.party_layout, null);
            builder.setView(dialogView);
            builder.setTitle("Choose Hu-mon");

            //Fill listview with all party humons
            ListView partyListView = (ListView) dialogView.findViewById(R.id.partyListView);
            ArrayAdapter<String> partyAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, partyHumons);
            partyListView.setAdapter(partyAdapter);

            //display the dialog
            final AlertDialog chooseHumonDialog = builder.create();
            chooseHumonDialog.show();

            partyListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            chooseHumonDialog.dismiss();
                            playerHumonIndex = position;
                            loadPlayer();
                            loadPlayerMoves();
                            consoleDisplayQueue.add(WAITING_MESSAGE);
                            displayConsoleMessage();
                        }
                    }
            );

        }
    }

    //Loads first humon in party
    private void loadEnemy(final String enemyIID) {
        Thread loadThread = new Thread() {
            public void run() {
                String partyFilename = getFilesDir() + "/" + getString(R.string.enemyPartyFile);

                try {
                    Log.i(TAG, "Attempting to load: " + partyFilename);

                    //load party file
                    String partyString;
                    FileInputStream inputStream = new FileInputStream(partyFilename);
                    int inputBytes = inputStream.available();
                    byte[] buffer = new byte[inputBytes];
                    inputStream.read(buffer);
                    inputStream.close();
                    partyString = new String(buffer, "UTF-8");
                    JSONObject fileJson = new JSONObject(partyString);
                    JSONArray humonsArray = fileJson.getJSONArray(HUMONS_KEY);
                    Log.i(TAG, partyFilename + " loaded");

                    //locate enemy to load
                    int enemyHumonIndex = -1;
                    for(int i = 0; i < humonsArray.length(); i++) {
                        String humonString = humonsArray.getString(i);
                        JSONObject humonJson = new JSONObject(humonString);
                        if(humonJson.getString("iID").equals(enemyIID)) {
                            enemyHumonIndex = i;
                            break;
                        }
                    }
                    String humonString = humonsArray.getString(enemyHumonIndex);
                    JSONObject humonJson = new JSONObject(humonString);
                    String name = humonJson.getString("name");
                    String description = humonJson.getString("description");
                    String image = null;
                    int level = humonJson.getInt("level");
                    int xp = humonJson.getInt("xp");
                    int hp = humonJson.getInt("hp");
                    int hID = humonJson.getInt("hID");
                    String uID = humonJson.getString("uID");
                    String iID = humonJson.getString("iID");
                    int health = humonJson.getInt("health");
                    int luck = humonJson.getInt("luck");
                    int attack = humonJson.getInt("attack");
                    int speed = humonJson.getInt("speed");
                    int defense = humonJson.getInt("defense");
                    String imagePath = humonJson.getString("imagePath");

                    //load moves
                    ArrayList<Move> moveList = new ArrayList<Move>();
                    JSONArray moveArray = humonJson.getJSONArray("moves");
                    for(int j = 0; j < moveArray.length(); j++) {
                        JSONObject moveJson = moveArray.getJSONObject(j);
                        int moveId = moveJson.getInt("id");
                        String moveName = moveJson.getString("name");
                        boolean moveSelfCast = moveJson.getBoolean("selfCast");
                        int dmg = moveJson.getInt("dmg");
                        Move.Effect moveEffect;
                        String moveEffectString = moveJson.getString("effect");
                        switch(moveEffectString) {
                            case "CONFUSED":
                                moveEffect = Move.Effect.CONFUSED;
                                break;
                            case "PARALYZED":
                                moveEffect = Move.Effect.PARALYZED;
                                break;
                            case "EMBARRASSED":
                                moveEffect = Move.Effect.EMBARRASSED;
                                break;
                            case "POISONED":
                                moveEffect = Move.Effect.POISONED;
                                break;
                            case "SLEPT":
                                moveEffect = Move.Effect.SLEPT;
                                break;
                            default:
                                moveEffect = null;
                        }
                        boolean moveHasEffect = moveJson.getBoolean("hasEffect");
                        String moveDescription = moveJson.getString("description");

                        moveList.add(new Move(moveId, moveName, moveSelfCast, dmg, moveEffect,
                                moveHasEffect, moveDescription));
                    }

                    for(int i = 0; i < moveList.size(); i++) {
                        enemyMoveList.add(moveList.get(i));
                    }

                    enemyHumon = new Humon(name, description, image, level, xp, hID, uID,
                            iID, moveList, health, luck, attack, speed, defense, imagePath, hp);

                    Log.i(TAG, "Enemy is: " + enemyHumon.getName());

                    inputStream.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "No enemy party file exists");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Load data into UI
                TextView nameTextView = (TextView) findViewById(R.id.enemyNameTextView);
                nameTextView.setText(enemyHumon.getName());
                //Log.i(TAG, "Player textview: " + nameTextView.getText().toString());

                TextView levelTextView = (TextView) findViewById(R.id.enemyLevelTextView);
                levelTextView.setText("Lvl " + enemyHumon.getLevel());

                enemyStatusTextView = (TextView) findViewById(R.id.enemyStatusTextView);
                enemyStatusTextView.setText("");

                enemyHealthBar = (ProgressBar) findViewById(R.id.enemyHealthBar);
                enemyHealthBar.setMax(enemyHumon.getHealth());
                enemyHealthBar.setProgress(enemyHumon.getHp());

                //load humon image
                if(enemyHumon.getImagePath() != null) {
                    if(enemyHumon.getImagePath().length() != 0) {
                        Bitmap humonImage = BitmapFactory.decodeFile(enemyHumon.getImagePath());
                        ImageView humonImageView = (ImageView) findViewById(R.id.enemyImageView);
                        humonImageView.setImageBitmap(humonImage);
                    }
                }

            }
        };

        loadThread.run();
        Log.i(TAG, "Finished loading enemy");
    }

    //Loads first humon in party
    private void loadPlayer() {
        Thread loadThread = new Thread() {
            public void run() {
                String partyFilename = getFilesDir() + "/" + userEmail + getString(R.string.partyFile);

                try {
                    Log.i(TAG, "Attempting to load: " + partyFilename);

                    //load party file
                    String partyString;
                    FileInputStream inputStream = new FileInputStream(partyFilename);
                    int inputBytes = inputStream.available();
                    byte[] buffer = new byte[inputBytes];
                    inputStream.read(buffer);
                    inputStream.close();
                    partyString = new String(buffer, "UTF-8");
                    JSONObject fileJson = new JSONObject(partyString);
                    JSONArray humonsArray = fileJson.getJSONArray(HUMONS_KEY);
                    Log.i(TAG, partyFilename + " loaded");

                    //load humon into json object format
                    String humonString = humonsArray.getString(playerHumonIndex);
                    JSONObject humonJson = new JSONObject(humonString);
                    String name = humonJson.getString("name");
                    String description = humonJson.getString("description");
                    String image = null;
                    int level = humonJson.getInt("level");
                    int xp = humonJson.getInt("xp");
                    int hp = humonJson.getInt("hp");
                    int hID = humonJson.getInt("hID");
                    String uID = humonJson.getString("uID");
                    String iID = humonJson.getString("iID");
                    int health = humonJson.getInt("health");
                    int luck = humonJson.getInt("luck");
                    int attack = humonJson.getInt("attack");
                    int speed = humonJson.getInt("speed");
                    int defense = humonJson.getInt("defense");
                    String imagePath = humonJson.getString("imagePath");

                    //load moves
                    ArrayList<Move> moveList = new ArrayList<Move>();
                    JSONArray moveArray = humonJson.getJSONArray("moves");
                    for(int j = 0; j < moveArray.length(); j++) {
                        JSONObject moveJson = moveArray.getJSONObject(j);
                        int moveId = moveJson.getInt("id");
                        String moveName = moveJson.getString("name");
                        boolean moveSelfCast = moveJson.getBoolean("selfCast");
                        int dmg = moveJson.getInt("dmg");
                        Move.Effect moveEffect;
                        String moveEffectString = moveJson.getString("effect");
                        switch(moveEffectString) {
                            case "CONFUSED":
                                moveEffect = Move.Effect.CONFUSED;
                                break;
                            case "PARALYZED":
                                moveEffect = Move.Effect.PARALYZED;
                                break;
                            case "EMBARRASSED":
                                moveEffect = Move.Effect.EMBARRASSED;
                                break;
                            case "POISONED":
                                moveEffect = Move.Effect.POISONED;
                                break;
                            case "SLEPT":
                                moveEffect = Move.Effect.SLEPT;
                                break;
                            default:
                                moveEffect = null;
                        }
                        boolean moveHasEffect = moveJson.getBoolean("hasEffect");
                        String moveDescription = moveJson.getString("description");

                        moveList.add(new Move(moveId, moveName, moveSelfCast, dmg, moveEffect,
                                moveHasEffect, moveDescription));
                    }

                    playerHumon = new Humon(name, description, image, level, xp, hID, uID,
                            iID, moveList, health, luck, attack, speed, defense, imagePath, hp);

                    Log.i(TAG, "Player is: " + playerHumon.getName());

                    //Send server iID of chosen humon
                    mServerConnection.sendMessage(getString(R.string.ServerCommandBattleAction) +
                            ":{\"commandType\":"+ INSTANCE_TYPE + ", \"iID\":\"" + playerHumon.getiID() + "\"}");

                    inputStream.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "No party file for: " + userEmail);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Load data into UI
                TextView nameTextView = (TextView) findViewById(R.id.playerNameTextView);
                nameTextView.setText(playerHumon.getName());
                Log.i(TAG, "Player textview: " + nameTextView.getText().toString());

                TextView levelTextView = (TextView) findViewById(R.id.playerLevelTextView);
                levelTextView.setText("Lvl " + playerHumon.getLevel());

                playerStatusTextView = (TextView) findViewById(R.id.playerStatusTextView);
                playerStatusTextView.setText("");

                playerHealthBar = (ProgressBar) findViewById(R.id.playerHealthBar);
                playerHealthBar.setMax(playerHumon.getHealth());
                playerHealthBar.setProgress(playerHumon.getHp());

                ProgressBar experienceBar = (ProgressBar) findViewById(R.id.playerXpBar);
                experienceBar.setMax(playerHumon.getMaxXp());
                experienceBar.setProgress(playerHumon.getXp());

                //load humon image
                if(playerHumon.getImagePath() != null) {
                    if(playerHumon.getImagePath().length() != 0) {
                        Bitmap humonImage = BitmapFactory.decodeFile(playerHumon.getImagePath());
                        ImageView humonImageView = (ImageView) findViewById(R.id.playerImageView);
                        humonImageView.setImageBitmap(humonImage);
                    }
                }

            }
        };

        loadThread.run();
        Log.i(TAG, "Finished loading player");
    }

    private void loadPlayerMoves() {

        Log.i(TAG, "In Load Player Moves");

        //Clear previous moves
        playerMoveList.clear();

        //load moves into grid
        ArrayList<Move> humonMoves = playerHumon.getMoves();
        for(int i = 0; i < humonMoves.size(); i++) {
            playerMoveList.add(humonMoves.get(i));
        }
        moveAdapter.notifyDataSetChanged();

        for(int i = 0; i < playerMoveList.size(); i++) {
            Log.i(TAG, "Added move: " + playerMoveList.get(i).getName());
        }
    }

    private void choosePlayerMove(Move move) throws JsonProcessingException {
        playerMove = move;
        Random rng = new Random();
        playerRng = Math.max(rng.nextInt(100) - Math.min(playerHumon.getLuck(), 40), 0);

        try {
            mServerConnection.sendMessage(getString(R.string.ServerCommandBattleAction) +
                    ":{\"commandType\":"+ MOVE_TYPE + ", \"move\":" + move.toJson(new ObjectMapper()) +
                    ", \"rng\":" + playerRng +"}");
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if(enemyMove != null) {
            startBattleSequence();
        }
        else {
            waitingForEnemy = true;
            consoleDisplayQueue.add(WAITING_MESSAGE);
            displayConsoleMessage();
        }
    }

    private void startBattleSequence() {

        turns++;
        if(turns % 5 == 0) {
            dmgMultiplier++;
        }

        if(playerFirst()) {
            cureEffect(true);
            useMove(playerMove, true);
            applyPoison(true);

            if(playerHumon.getHp() == 0 || enemyHumon.getHp() == 0) {
                finishBattle();
            } else {

                cureEffect(false);
                useMove(enemyMove, false);
                applyPoison(false);

                if(playerHumon.getHp() == 0 || enemyHumon.getHp() == 0) {
                    finishBattle();
                }
            }
        }
        else {

            cureEffect(false);
            useMove(enemyMove, false);
            applyPoison(false);

            if(playerHumon.getHp() == 0 || enemyHumon.getHp() == 0) {
                finishBattle();
            }
            else {
                cureEffect(true);
                useMove(playerMove, true);
                applyPoison(true);

                if(playerHumon.getHp() == 0 || enemyHumon.getHp() == 0) {
                    finishBattle();
                }
            }
        }
        displayConsoleMessage();
        enemyMove = null;
        playerMove = null;
    }

    /*
     * Attempts to cure the player of any effect they have
     *
     * @param isPlayer  true if the player is being cured
     */
    private void cureEffect(boolean isPlayer) {
        if(isPlayer && playerStatus == null) {
            return;
        }
        if(!isPlayer && enemyStatus == null) {
            return;
        }

        if(isPlayer) {
            switch(playerStatus) {
                case PARALYZED:
                    if(playerRng < Move.PARALYZE_CURE_CHANCE) {
                        consoleDisplayQueue.add(playerHumon.getName() + " is no longer paralyzed.\n");
                        playerStatus = null;
                        playerStatusTextView.setText("");
                    }
                    break;
                case CONFUSED:
                    if(playerRng < Move.CONFUSE_CURE_CHANCE) {
                        consoleDisplayQueue.add(playerHumon.getName() + " is no longer confused.\n");
                        playerStatus = null;
                        playerStatusTextView.setText("");
                    }
                    break;
                case SLEPT:
                    if(playerRng < Move.SLEEP_CURE_CHANCE) {
                        consoleDisplayQueue.add(playerHumon.getName() + " woke up!\n");
                        playerStatus = null;
                        playerStatusTextView.setText("");
                    }
                    break;
                case POISONED:
                    if(playerRng < Move.POISON_CURE_CHANCE) {
                        consoleDisplayQueue.add(playerHumon.getName() + " is no longer poisoned.\n");
                        playerStatus = null;
                        playerStatusTextView.setText("");
                    }
                    break;
                case EMBARRASSED:
                    if(playerRng < Move.EMBARASS_CURE_CHANCE) {
                        consoleDisplayQueue.add(playerHumon.getName() + " is no longer embarrassed.\n");
                        playerStatus = null;
                        playerStatusTextView.setText("");
                    }
                    break;
                default:
                    break;
            }
        }
        if(!isPlayer) {
            switch(enemyStatus) {
                case PARALYZED:
                    if(enemyRng < Move.PARALYZE_CURE_CHANCE) {
                        consoleDisplayQueue.add(enemyHumon.getName() + " is no longer paralyzed.\n");
                        enemyStatus = null;
                        enemyStatusTextView.setText("");
                    }
                    break;
                case CONFUSED:
                    if(enemyRng < Move.CONFUSE_CURE_CHANCE) {
                        consoleDisplayQueue.add(enemyHumon.getName() + " is no longer confused.\n");
                        enemyStatus = null;
                        enemyStatusTextView.setText("");
                    }
                    break;
                case SLEPT:
                    if(enemyRng < Move.SLEEP_CURE_CHANCE) {
                        consoleDisplayQueue.add(enemyHumon.getName() + " woke up!\n");
                        enemyStatus = null;
                        enemyStatusTextView.setText("");
                    }
                    break;
                case POISONED:
                    if(enemyRng < Move.POISON_CURE_CHANCE) {
                        consoleDisplayQueue.add(enemyHumon.getName() + " is no longer poisoned.\n");
                        enemyStatus = null;
                        enemyStatusTextView.setText("");
                    }
                    break;
                case EMBARRASSED:
                    if(enemyRng < Move.EMBARASS_CURE_CHANCE) {
                        consoleDisplayQueue.add(enemyHumon.getName() + " is no longer embarrassed.\n");
                        enemyStatus = null;
                        enemyStatusTextView.setText("");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * Applies poison damage to target
     *
     * @param isPlayer  true if the player is being poisoned
     */
    private void applyPoison(boolean isPlayer) {
        if(isPlayer && playerStatus == Move.Effect.POISONED) {
            int damage = playerHumon.getHealth() * (Move.POISON_DAMAGE/100);
            if(damage < 1) {
                damage = 1;
            }
            consoleDisplayQueue.add(playerHumon.getName() + " took " + damage + " damage from poison.\n");
            playerHumon.setHp(playerHumon.getHp() - damage);
        }
        else if(!isPlayer && enemyStatus == Move.Effect.POISONED) {
            int damage = enemyHumon.getHealth() * (Move.POISON_DAMAGE/100);
            if(damage < 1) {
                damage = 1;
            }
            consoleDisplayQueue.add(enemyHumon.getName() + " took " + damage + " damage from poison.\n");
            enemyHumon.setHp(enemyHumon.getHp() - damage);
        }
    }

    /*
     * Uses the given move and applies damage and effect
     *
     * @param move Move being used
     * @param isPlayer  true if the player is using the move
     */
    private void useMove(Move move, boolean isPlayer) {
        boolean isConfused = false;
        boolean isStunned = false;
        Move confusedMove = copyConfusedMove(move);
        if(isPlayer) {
            int playerMoveDamage = getMoveDamage(move, true);
            Move.Effect playerMoveEffect = getMoveEffect(move, true);
            String displayMessage = playerHumon.getName() + " used " + move.getName() + ".\n";
            if(playerStatus == Move.Effect.SLEPT) {
                displayMessage = playerHumon.getName() + " is sleeping...\n";
                playerMoveDamage = 0;
                playerMoveEffect = null;
                isStunned = true;
                consoleDisplayQueue.add(displayMessage);
            }
            else if(playerStatus == Move.Effect.PARALYZED &&
                    playerRng < Move.PARALYZE_EFFECT_CHANCE) {
                displayMessage = playerHumon.getName() + " is paralyzed...\n";
                playerMoveDamage = 0;
                playerMoveEffect = null;
                isStunned = true;
                consoleDisplayQueue.add(displayMessage);
            }
            else if(playerStatus == Move.Effect.CONFUSED &&
                    playerRng < Move.CONFUSE_EFFECT_CHANCE) {
                displayMessage = playerHumon.getName() + " targeted the wrong Humon in confusion!\n";
                isConfused = true;
                playerMoveDamage = getMoveDamage(confusedMove, true);
            }
            else if(playerStatus == Move.Effect.EMBARRASSED &&
                    playerRng < Move.EMBARASS_EFFECT_CHANCE) {
                displayMessage = playerHumon.getName() + " forgot their move! How embarassing!\n";
                move = playerMoveList.get(playerRng % playerMoveList.size());
                playerMoveDamage = getMoveDamage(move, true);
                playerMoveEffect = getMoveEffect(move, true);
            }

            if(move.isSelfCast() && !isStunned) {
                if(isConfused) {
                    applyMove(move.getName(), playerMoveDamage, playerMoveEffect, true, displayMessage);
                }
                else {
                    applyMove(move.getName(), playerMoveDamage, playerMoveEffect, false, displayMessage);
                }
            }
            else if(!isStunned) {
                if(isConfused) {
                    applyMove(move.getName(), playerMoveDamage, playerMoveEffect, false, displayMessage);
                }
                else {
                    applyMove(move.getName(), playerMoveDamage, playerMoveEffect, true, displayMessage);
                }
            }
        }

        else {
            int enemyMoveDamage = getMoveDamage(move, false);
            Move.Effect enemyMoveEffect = getMoveEffect(move, false);
            String displayMessage = enemyHumon.getName() + " used " + move.getName() + ".\n";
            if(enemyStatus == Move.Effect.SLEPT) {
                displayMessage = "Wild " + enemyHumon.getName() + " is sleeping...\n";
                enemyMoveDamage = 0;
                enemyMoveEffect = null;
                isStunned = true;
                consoleDisplayQueue.add(displayMessage);
            }
            else if(enemyStatus == Move.Effect.PARALYZED &&
                    enemyRng < Move.PARALYZE_EFFECT_CHANCE) {
                displayMessage = "Wild " + enemyHumon.getName() + " is paralyzed...\n";
                enemyMoveDamage = 0;
                enemyMoveEffect = null;
                isStunned = true;
                consoleDisplayQueue.add(displayMessage);
            }
            else if(enemyStatus == Move.Effect.CONFUSED &&
                    enemyRng < Move.CONFUSE_EFFECT_CHANCE) {
                displayMessage = "Wild " + enemyHumon.getName() + " targeted the wrong Humon in confusion!\n";
                isConfused = true;
                enemyMoveDamage = getMoveDamage(confusedMove, true);
            }
            else if(enemyStatus == Move.Effect.EMBARRASSED &&
                    enemyRng < Move.EMBARASS_EFFECT_CHANCE) {
                displayMessage = "Wild " + enemyHumon.getName() + " forgot their move! How embarassing!\n";
                move = enemyMoveList.get(enemyRng % enemyMoveList.size());
                enemyMoveDamage = getMoveDamage(move, true);
                enemyMoveEffect = getMoveEffect(move, true);
            }
            if(move.isSelfCast() && !isStunned) {
                if(isConfused) {
                    applyMove(move.getName(), enemyMoveDamage, enemyMoveEffect, false, displayMessage);
                }
                else {
                    applyMove(move.getName(), enemyMoveDamage, enemyMoveEffect, true, displayMessage);
                }
            }
            else if(!isStunned){
                if(isConfused) {
                    applyMove(move.getName(), enemyMoveDamage, enemyMoveEffect, true, displayMessage);
                }
                else {
                    applyMove(move.getName(), enemyMoveDamage, enemyMoveEffect, false, displayMessage);
                }
            }
        }
    }

    /*
     * Applies the damage and effect of a move to the target
     *
     * @param moveName      name of the move being used
     * @param damage        amount of damage to be dealt
     * @param effect        effect to be applied
     * @param targetEnemy   true to apply to enemy, false to apply to player
     * @param displayMessage    the current message to be displayed to user
     *
     */
    private void applyMove(String moveName, int damage, Move.Effect effect, boolean targetEnemy,
                           String displayMessage) {
        if(targetEnemy) {
            int enemyHp = enemyHumon.getHp() - damage;
            if (enemyHp < 0) {
                enemyHp = 0;
            }
            enemyHumon.setHp(enemyHp);
            enemyHealthBar.setProgress(enemyHp);

            if(damage >= 0) {
                displayMessage += "Applied " + damage + " damage to " + enemyHumon.getName();
            }
            else {
                displayMessage += "Healed " + (damage * -1) + " damage from " + enemyHumon.getName();
            }

            //Apply the status effect
            if(effect != null && enemyStatus == null) {
                enemyStatus = effect;
                enemyStatusTextView.setText(""+ enemyStatus);
                displayMessage += "\n" + enemyHumon.getName() + " is " + enemyStatus + "!";
            }

            Log.i(TAG, displayMessage);
            consoleDisplayQueue.add(displayMessage);
        }
        else {
            int playerHp = playerHumon.getHp() - damage;
            if (playerHp < 0) {
                playerHp = 0;
            }
            playerHumon.setHp(playerHp);
            playerHealthBar.setProgress(playerHp);

            if(damage >= 0) {
                displayMessage += "Applied " + damage + " damage to " + playerHumon.getName();
            }
            else {
                displayMessage += "Healed " + (damage * -1) + " damage from " + playerHumon.getName();
            }

            //Apply the status effect
            if(effect != null && playerStatus == null) {
                playerStatus = effect;
                playerStatusTextView.setText(""+ playerStatus);
                displayMessage += "\n" + playerHumon.getName() + " is " + playerStatus + "!";
            }
            Log.i(TAG, displayMessage);
            consoleDisplayQueue.add(displayMessage);
        }
    }

    //choose which humon will attack first (true if player)
    private boolean playerFirst() {
        if(enemyHumon.getSpeed() == playerHumon.getSpeed()) {
            if(playerRng > enemyRng) {
                return true;
            }
            else if(enemyRng > playerRng) {
                return false;
            }
            return isInitiaor;
        }
        if(enemyHumon.getSpeed() > playerHumon.getSpeed()) {
            return false;
        }
        return true;
    }

    /*
     * Calculates the damage applied by a move
     *
     * @param move Move being used
     * @param isPlayer  true if the player is using the move
     */
    private int getMoveDamage(Move move, boolean isPlayer) {
        double moveBaseDamage = move.getDmg();
        int damage;
        if(isPlayer) {
            if(move.isSelfCast()) {
                damage = (int) ((moveBaseDamage / 100) * playerHumon.getAttack()) - (playerHumon.getDefense() / 2);
            }
            else {
                damage = (int) ((moveBaseDamage / 100) * playerHumon.getAttack()) - (enemyHumon.getDefense() / 2);
            }
            if(move.getDmg() > 0 && damage < 1) {
                damage = 1;
            }
            else if(move.getDmg() < 0 && damage > -1) {
                damage = -1;
            }
            if(playerRng < Move.CRITICAL_CHANCE) {
                damage *= 2;
            }
        }
        else {
            if(move.isSelfCast()) {
                damage = (int) ((moveBaseDamage / 100) * enemyHumon.getAttack()) - (enemyHumon.getDefense() / 2);
            }
            else {
                damage = (int) ((moveBaseDamage / 100) * enemyHumon.getAttack()) - (playerHumon.getDefense() / 2);
            }
            if(move.getDmg() > 0 && damage < 1) {
                damage = 1;
            }
            else if(move.getDmg() < 0 && damage > -1) {
                damage = -1;
            }
            if(enemyRng < Move.CRITICAL_CHANCE) {
                damage *= 2;
            }
        }
        if(damage > 0) {
            damage *= dmgMultiplier;
        }
        return damage;
    }

    /*
     * Calculates the effect applied by a move
     *
     * @param move Move being used
     * @param isPlayer  true if the player is using the move
     */
    private Move.Effect getMoveEffect(Move move, boolean isPlayer) {
        //No effect
        if(!move.isHasEffect()) {
            Log.i(TAG, "Move has no effect");
            return null;
        }

        boolean willEffect = false;
        if(isPlayer) {
            if(move.isSelfCast()) {
                switch(move.getEffect()) {
                    case PARALYZED:
                        if(enemyRng < Move.PARALYZE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case CONFUSED:
                        if(enemyRng < Move.CONFUSE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case SLEPT:
                        if(enemyRng < Move.SLEEP_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case POISONED:
                        if(enemyRng < Move.POISON_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case EMBARRASSED:
                        if(enemyRng < Move.EMBARASS_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    default:
                        break;
                }
            }
            else {
                switch(move.getEffect()) {
                    case PARALYZED:
                        if(playerRng < Move.PARALYZE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && playerRng < Move.PARALYZE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case CONFUSED:
                        if(playerRng < Move.CONFUSE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && playerRng < Move.CONFUSE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case SLEPT:
                        if(playerRng < Move.SLEEP_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && playerRng < Move.SLEEP_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case POISONED:
                        if(playerRng < Move.POISON_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && playerRng < Move.POISON_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case EMBARRASSED:
                        if(playerRng < Move.EMBARASS_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && playerRng < Move.EMBARASS_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        else {
            if(move.isSelfCast()) {
                switch(move.getEffect()) {
                    case PARALYZED:
                        if(playerRng < Move.PARALYZE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case CONFUSED:
                        if(playerRng < Move.CONFUSE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case SLEPT:
                        if(playerRng < Move.SLEEP_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case POISONED:
                        if(playerRng < Move.POISON_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case EMBARRASSED:
                        if(playerRng < Move.EMBARASS_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    default:
                        break;
                }
            }
            else {
                switch(move.getEffect()) {
                    case PARALYZED:
                        if(enemyRng < Move.PARALYZE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && enemyRng < Move.PARALYZE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case CONFUSED:
                        if(enemyRng < Move.CONFUSE_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && enemyRng < Move.PARALYZE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case SLEPT:
                        if(enemyRng < Move.SLEEP_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && enemyRng < Move.PARALYZE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case POISONED:
                        if(enemyRng < Move.POISON_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && enemyRng < Move.PARALYZE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    case EMBARRASSED:
                        if(enemyRng < Move.EMBARASS_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        else if(move.getDmg() < 1 && enemyRng < Move.PARALYZE_APPLY_CHANCE + Move.HEAL_APPLY_CHANCE) {
                            willEffect = true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if(willEffect) {
            return move.getEffect();
        }
        else {
            return null;
        }
    }

    /*
     * Gives the user the option to run away from the battle
     * Called by pressing the back button
     *
     */
    private void runAwayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Run away?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {

                Toast toast = Toast.makeText(getApplicationContext(), "Ran away!", Toast.LENGTH_SHORT);
                toast.show();

                //Save the humon's state
                saveHumons();

                //tell enemy you ran away
                mServerConnection.sendMessage(getString(R.string.ServerCommandBattleAction) +
                        ":{\"commandType\":"+ RUN_TYPE + "}");

                mServerConnection.sendMessage(getString(R.string.ServerCommandBattleEnd) +
                        ":{}");

                //return to the menu
                finish();

            }
        });

        //End the battle
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {

            }
        });

        //display the dialog
        final AlertDialog runAwayDialog = builder.create();
        runAwayDialog.show();
    }

    /*
     * Displays the next message in the console message queue.
     * If queue is empty and game is not over, displays moves grid view
     *
     */
    private void displayConsoleMessage() {
        displayConsole();
        if(consoleDisplayQueue.size() == 0) {
            if(gameOver) {
                    Toast toast = Toast.makeText(this, "Battle Finished", Toast.LENGTH_SHORT);
                    toast.show();

                    //Save the humon's state
                    saveHumons();

                    finish();
            }
            else {
                if(!waitingForEnemy) {
                    displayMoves();
                }
            }
        }
        else {
            userConsole.setText(consoleDisplayQueue.get(0));
            currentConsoleMessage = consoleDisplayQueue.get(0);
            consoleDisplayQueue.remove(0);
        }
    }

    /*
     * Hides the moves grid view and displays the console
     *
     */
    private void displayConsole() {
        playerMovesView.setVisibility(View.INVISIBLE);
        userConsole.setVisibility(View.VISIBLE);
    }

    /*
     * Hides the console and displays the moves grid view
     *
     */
    private void displayMoves() {
        playerMovesView.setVisibility(View.VISIBLE);
        userConsole.setVisibility(View.INVISIBLE);
    }

    /*
     * Called when enemy responds, automatically removes message for waiting
     *
     */
    private void endWaitingMessage() {
        for(int i = 0; i < consoleDisplayQueue.size(); i++) {
            if(consoleDisplayQueue.get(i).equals(WAITING_MESSAGE)) {
                consoleDisplayQueue.remove(i);
            }
        }
        if(consoleDisplayQueue.size() == 0 &&
                currentConsoleMessage.equals(WAITING_MESSAGE)) {
            displayConsoleMessage();
        }
    }

    //Called when a humon is defeated, gives option to capture if player wins and notifies user
    private void finishBattle() {
        String displayText;
        gameOver = true;

        if(playerHumon.getHp() == 0) {
            displayText = playerHumon.getName() + " defeated!";
            Toast toast = Toast.makeText(this, displayText, Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            displayText = enemyHumon.getName() + " defeated, gained "
                    + enemyHumon.getLevel() + " experience!";
            Toast toast = Toast.makeText(this, enemyHumon.getName() + " defeated, gained "
                    + enemyHumon.getLevel() + " experience!", Toast.LENGTH_SHORT);
            toast.show();

            //Give player experience points
            playerHumon.addXp(enemyHumon.getLevel());
            ProgressBar experienceBar = (ProgressBar) findViewById(R.id.playerXpBar);
            experienceBar.setProgress(playerHumon.getXp());

            TextView levelTextView = (TextView) findViewById(R.id.playerLevelTextView);
            levelTextView.setText("Lvl " + playerHumon.getLevel());

        }

        //Update the console
        consoleDisplayQueue.add(displayText);
        displayConsoleMessage();

        mServerConnection.sendMessage(getString(R.string.ServerCommandBattleEnd) +
        ":{}");

    }

    /*
     * Saves the current state of the humons in the battle.
     * Should be called after the battle
     *
     */
    private void saveHumons() {

        AsyncTask<Humon, Integer, Boolean> partySaveTask = new HumonPartySaver(this, false, true);

        //save player's humon data to party
        partySaveTask.execute(playerHumon);
        gameSaved = true;
    }

    /*
     * Creates a copy of the old move in the new move.
     * The target is opposite.
     *
     */
    private Move copyConfusedMove(Move oldMove) {
        Move newMove = new Move(oldMove.getId(), oldMove.getName(), !oldMove.isSelfCast(),
                oldMove.getDmg(), oldMove.getEffect(), oldMove.isHasEffect(), oldMove.getDescription());
        return newMove;
    }

}
