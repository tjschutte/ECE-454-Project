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

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;
import edu.wisc.ece454.hu_mon.Utilities.HumonPartySaver;

public class OnlineBattleActivity extends AppCompatActivity {

    ServerConnection mServerConnection;
    boolean mBound;

    private String enemyEmail;
    private boolean battleStarting = false;
    private boolean isInitiaor = false;
    private boolean waitingForEnemy = true;

    private final String ACTIVITY_TITLE = "Online Battle";
    private String HUMONS_KEY;

    private Humon enemyHumon;
    private Humon playerHumon;
    private int playerHumonIndex;
    private String userEmail;
    private User user;

    private boolean gameOver;
    private boolean gameSaved;

    private Move.Effect playerStatus;
    private Move.Effect enemyStatus;

    //Queue of messages to be displayed in console (front is 0)
    private ArrayList<String> consoleDisplayQueue;

    private ProgressBar playerHealthBar;
    private ProgressBar enemyHealthBar;
    private TextView playerStatusTextView;
    private TextView enemyStatusTextView;

    private ArrayList<String> partyHumons;
    private ArrayList<Integer> partyHumonIndices;
    private ArrayList<Move> playerMoveList;
    private ArrayAdapter<Move> moveAdapter;

    private GridView playerMovesView;
    private TextView userConsole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wild_battle_layout);
        setTitle(ACTIVITY_TITLE);
        HUMONS_KEY = getString(R.string.humonsKey);

        Intent parentIntent = getIntent();
        enemyEmail = parentIntent.getStringExtra(getString(R.string.emailKey));
        isInitiaor = parentIntent.getBooleanExtra(getString(R.string.initiatorKey), false);
        System.out.println("Battle started with: " + enemyEmail);
        battleStarting = true;

        // Attach to the server communication service
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        partyHumons = new ArrayList<String>();
        partyHumonIndices = new ArrayList<Integer>();
        gameOver = true;
        gameSaved = false;

        //Setup Grid View and Adapter
        playerMoveList = new ArrayList<Move>();
        playerMovesView = (GridView) findViewById(R.id.moveGridView);
        moveAdapter = new ArrayAdapter<Move>(this,
                android.R.layout.simple_list_item_1, playerMoveList);
        playerMovesView.setAdapter(moveAdapter);
        playerMovesView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        choosePlayerMove(playerMoveList.get(position));
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
            loadPartyHumons();
            choosePlayerHumon();
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

    private IntentFilter filter = new IntentFilter();

    //Get enemy's humons
    private void getEnemyParty() {
        //Get enemies party
        if(mBound) {
            System.out.println("Attempting to get party");
            mServerConnection.sendMessage(getString(R.string.ServerCommandGetParty) +
                    ":{\"email\":\"" + enemyEmail + "\"}");
        }
        else {
            System.out.println("Error: Connection not bound, cannot get party");
        }
    }

    //Notify enemy that battle request was accepted
    private void notifyEnemyStart() {

        //initiator: true for accepted notification

        //Get enemies party
        if(mBound) {
            System.out.println("Attempting to notify enemy of battle start");
            mServerConnection.sendMessage(getString(R.string.ServerCommandBattleStart) +
                    ":{\"email\":\"" + enemyEmail + "\", \"initiator\":" + isInitiaor + "}");
        }
        else {
            System.out.println("Error: Connection not bound, cannot get notify enemy");
        }
    }

    /*
     * Loads all humons in party which do not have 0 hp.
     *
     *
     */
    private void loadPartyHumons() {
        System.out.println("Loading Humons to battle");
        Thread loadThread = new Thread() {
            public void run() {
                String partyFilename = getFilesDir() + "/" + userEmail + getString(R.string.partyFile);

                try {
                    System.out.println("Attempting to load: " + partyFilename);

                    //load party file
                    String partyString;
                    FileInputStream inputStream = new FileInputStream(partyFilename);
                    int inputBytes = inputStream.available();
                    byte[] buffer = new byte[inputBytes];
                    inputStream.read(buffer);
                    inputStream.close();
                    partyString = new String(buffer, "UTF-8");
                    System.out.println("User party string: " + partyString);
                    JSONObject fileJson = new JSONObject(partyString);
                    JSONArray humonsArray = fileJson.getJSONArray(HUMONS_KEY);
                    System.out.println(partyFilename + " loaded");

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
                    System.out.println("No party file for: " + userEmail);
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
        System.out.println("Choosing Humon to battle");
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
                            consoleDisplayQueue.add("Waiting for enemy...");
                            displayConsoleMessage();
                        }
                    }
            );

        }
    }

    //Loads first humon in party
    private void loadPlayer() {
        Thread loadThread = new Thread() {
            public void run() {
                String partyFilename = getFilesDir() + "/" + userEmail + getString(R.string.partyFile);

                try {
                    System.out.println("Attempting to load: " + partyFilename);

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
                    System.out.println(partyFilename + " loaded");

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

                    System.out.println("Player is: " + playerHumon.getName());

                    //TODO: Send server iID of chosen humon

                    inputStream.close();
                } catch (FileNotFoundException e) {
                    System.out.println("No party file for: " + userEmail);
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
                System.out.println("Player textview: " + nameTextView.getText().toString());

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
        System.out.println("Finished loading player");
    }

    private void loadPlayerMoves() {

        System.out.println("In Load Player Moves");

        //Clear previous moves
        playerMoveList.clear();

        //load moves into grid
        ArrayList<Move> humonMoves = playerHumon.getMoves();
        for(int i = 0; i < humonMoves.size(); i++) {
            playerMoveList.add(humonMoves.get(i));
        }
        moveAdapter.notifyDataSetChanged();

        for(int i = 0; i < playerMoveList.size(); i++) {
            System.out.println("Added move: " + playerMoveList.get(i).getName());
        }
    }

    //TODO: Tell the server your chosen move
    private void choosePlayerMove(Move move) {

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
     * Saves the current state of the humons in the battle.
     * Should be called after the battle
     *
     */
    private void saveHumons() {

        AsyncTask<Humon, Integer, Boolean> partySaveTask = new HumonPartySaver(this, false);

        //save player's humon data to party
        partySaveTask.execute(playerHumon);
        gameSaved = true;

    }

}
