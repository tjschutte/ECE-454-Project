package edu.wisc.ece454.hu_mon.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import edu.wisc.ece454.hu_mon.Utilities.HumonPartySaver;

public class WildBattleActivity extends SettingsActivity {

    private final String ACTIVITY_TITLE = "Battle";
    private String HUMONS_KEY;

    private Humon enemyHumon;
    private Humon playerHumon;
    private int playerHumonIndex;
    private String userEmail;
    private User user;

    private boolean gameOver;
    private boolean canCapture;
    private boolean capturedHumon;

    //Queue of messages to be displayed in console (front is 0)
    private ArrayList<String> consoleDisplayQueue;

    private ProgressBar playerHealthBar;
    private ProgressBar enemyHealthBar;

    private ArrayList<String> partyHumons;
    private ArrayList<Integer> partyHumonIndices;
    private ArrayList<Move> playerMoveList;
    private ArrayList<Move> enemyMoveList;
    private ArrayAdapter<Move> moveAdapter;

    private GridView playerMovesView;
    private TextView userConsole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wild_battle_layout);
        setTitle(ACTIVITY_TITLE);

        HUMONS_KEY = getString(R.string.humonsKey);

        partyHumons = new ArrayList<String>();
        partyHumonIndices = new ArrayList<Integer>();

        //Setup Grid View and Adapter
        playerMoveList = new ArrayList<Move>();
        enemyMoveList = new ArrayList<Move>();
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
    protected void onStart() {
        super.onStart();

        //retrieve email of the user
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        userEmail = sharedPref.getString(getString(R.string.emailKey), "");

        userConsole.setVisibility(View.INVISIBLE);
        playerMovesView.setVisibility(View.VISIBLE);
        consoleDisplayQueue.clear();
        gameOver = false;
        canCapture = false;
        capturedHumon = false;
        partyHumons.clear();
        partyHumonIndices.clear();
        playerHumonIndex = 0;

        //load humons into battle
        //loadEnemy();
        //scaleEnemy();
        loadPartyHumons();
        choosePlayerHumon();
        //loadPlayer();
        //loadPlayerMoves();

        //Full heal for development only
        /*if(userEmail.equals("dev") || userEmail.equals("test")) {
            //playerHumon.setHealth(playerHumon.getHealth() * 5);
            playerHumon.setHp(playerHumon.getHealth());
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    /*
     * Loads the enemy and player humons after player humon chosen to battle.
     *
     *
     */
    private void loadBattleHumons() {
        loadPlayer();
        loadPlayerMoves();
        loadEnemy();
        scaleEnemy();

    }

    //randomly selects a humon from index and loads into UI
    private void loadEnemy() {

        Thread loadThread = new Thread() {
            public void run() {
                String indexFilename =  getFilesDir() + "/" + userEmail + getString(R.string.indexFile);;

                try {
                    System.out.println("Attempting to load: " + indexFilename);

                    //load index file
                    String indexString;
                    FileInputStream inputStream = new FileInputStream(indexFilename);
                    int inputBytes = inputStream.available();
                    byte[] buffer = new byte[inputBytes];
                    inputStream.read(buffer);
                    inputStream.close();
                    indexString = new String(buffer, "UTF-8");
                    JSONObject fileJson = new JSONObject(indexString);
                    JSONArray humonsArray = fileJson.getJSONArray(HUMONS_KEY);
                    System.out.println(indexFilename + " loaded");

                    //randomly choose a humon to fight
                    Random rng = new Random();
                    int humonIndex = rng.nextInt(humonsArray.length());

                    //load humon into json object format
                    String humonString = humonsArray.getString(humonIndex);
                    JSONObject humonJson = new JSONObject(humonString);
                    String name = humonJson.getString("name");
                    String description = humonJson.getString("description");
                    Bitmap image = null;
                    int level = 1;
                    int xp = humonJson.getInt("xp");
                    int hp = humonJson.getInt("health");
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
                        //Move.Effect moveEffect = (Move.Effect) moveJson.get("effect");
                        boolean moveHasEffect = moveJson.getBoolean("hasEffect");
                        String moveDescription = moveJson.getString("description");

                        moveList.add(new Move(moveId, moveName, moveSelfCast, dmg, null,
                                moveHasEffect, moveDescription));
                    }

                    enemyHumon = new Humon(name, description, image, level, xp, hID, uID,
                            iID, moveList, health, luck, attack, speed, defense, imagePath, hp);

                    System.out.println("Enemy is: " + enemyHumon.getName());

                    inputStream.close();
                } catch (FileNotFoundException e) {
                    System.out.println("No index file for: " + userEmail);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //load moves
                ArrayList<Move> humonMoves = enemyHumon.getMoves();
                for(int i = 0; i < humonMoves.size(); i++) {
                    enemyMoveList.add(humonMoves.get(i));
                }

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
        System.out.println("Finished loading enemy");
    }

    //Chooses a level for the enemy humon and scales its stats to that level
    private void scaleEnemy() {
        //choose Humon level
        Random rng = new Random();
        int humonLevelRange = 5;
        int humonLevel = rng.nextInt(humonLevelRange * 2) + playerHumon.getLevel() - humonLevelRange;
        if(humonLevel < 1) {
            humonLevel = 1;
        }

        System.out.println("Scaling enemy to level: " + humonLevel);

        //Increment stats on enemy humon by level
        for(int i = 1; i < humonLevel; i++) {
            enemyHumon.levelUp();
        }

        //Load data into UI
        TextView nameTextView = (TextView) findViewById(R.id.enemyNameTextView);
        nameTextView.setText(enemyHumon.getName());
        System.out.println("Enemy textview: " + nameTextView.getText().toString());

        TextView levelTextView = (TextView) findViewById(R.id.enemyLevelTextView);
        levelTextView.setText("Lvl " + enemyHumon.getLevel());

        enemyHealthBar = (ProgressBar) findViewById(R.id.enemyHealthBar);
        enemyHealthBar.setMax(enemyHumon.getHealth());
        enemyHealthBar.setProgress(enemyHumon.getHp());

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
                    JSONObject fileJson = new JSONObject(partyString);
                    JSONArray humonsArray = fileJson.getJSONArray(HUMONS_KEY);
                    System.out.println(partyFilename + " loaded");

                    //load humon into json object format
                    for(int i = 0; i < humonsArray.length(); i++) {
                        String humonString = humonsArray.getString(i);
                        JSONObject humonJson = new JSONObject(humonString);
                        int hp = humonJson.getInt("hp");
                        if(hp > 0 || userEmail.equals("dev")) {
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
                            loadBattleHumons();
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
                    Bitmap image = null;
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
                        //Move.Effect moveEffect = (Move.Effect) moveJson.get("effect");
                        boolean moveHasEffect = moveJson.getBoolean("hasEffect");
                        String moveDescription = moveJson.getString("description");

                        moveList.add(new Move(moveId, moveName, moveSelfCast, dmg, null,
                                moveHasEffect, moveDescription));
                    }

                    if(userEmail.equals("dev")) {
                        health *= 5;
                        hp = health;
                    }

                    playerHumon = new Humon(name, description, image, level, xp, hID, uID,
                            iID, moveList, health, luck, attack, speed, defense, imagePath, hp);

                    System.out.println("Player is: " + playerHumon.getName());

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

    private void choosePlayerMove(Move move) {

        //choose enemy move
        Random rng = new Random();
        int enemyMove = rng.nextInt(enemyMoveList.size());

        String displayMessage;

        if(playerFirst()) {
            int playerMoveDamage = getMoveDamage(move, true);
            int enemyHp = enemyHumon.getHp() - playerMoveDamage;
            if(enemyHp < 0) {
                enemyHp = 0;
            }
            enemyHumon.setHp(enemyHp);
            enemyHealthBar.setProgress(enemyHp);

            displayMessage = playerHumon.getName() + " used " + move.getName() +
                    ".\nApplied " + playerMoveDamage + " damage to wild " + enemyHumon.getName();
            System.out.println(displayMessage);

            consoleDisplayQueue.add(displayMessage);

            if(enemyHp == 0) {
                finishBattle();
            } else {

                int enemyMoveDamage = getMoveDamage(enemyMoveList.get(enemyMove), false);
                int playerHp = playerHumon.getHp() - enemyMoveDamage;
                if (playerHp < 0) {
                    playerHp = 0;
                }
                playerHumon.setHp(playerHp);
                playerHealthBar.setProgress(playerHp);

                displayMessage = "Wild " + enemyHumon.getName() + " used " + enemyMoveList.get(enemyMove).getName() +
                        ".\nApplied " + enemyMoveDamage + " damage to " + playerHumon.getName();
                System.out.println(displayMessage);
                consoleDisplayQueue.add(displayMessage);

                if(playerHp == 0) {
                    finishBattle();
                }
            }
        }
        else {

            int enemyMoveDamage = getMoveDamage(enemyMoveList.get(enemyMove), false);
            int playerHp = playerHumon.getHp() - enemyMoveDamage;
            if(playerHp < 0) {
                playerHp = 0;
            }
            playerHumon.setHp(playerHp);
            playerHealthBar.setProgress(playerHp);

            displayMessage = "Wild " + enemyHumon.getName() + " used " + enemyMoveList.get(enemyMove).getName() +
                    ".\nApplied " + enemyMoveDamage + " damage to " + playerHumon.getName();
            System.out.println(displayMessage);
            consoleDisplayQueue.add(displayMessage);

            if(playerHp == 0) {
                finishBattle();
            }
            else {

                int playerMoveDamage = getMoveDamage(move, true);
                int enemyHp = enemyHumon.getHp() - playerMoveDamage;
                if (enemyHp < 0) {
                    enemyHp = 0;
                }
                enemyHumon.setHp(enemyHp);
                enemyHealthBar.setProgress(enemyHp);

                displayMessage = playerHumon.getName() + " used " + move.getName() +
                        ".\nApplied " + playerMoveDamage + " damage to wild " + enemyHumon.getName();
                System.out.println(displayMessage);
                consoleDisplayQueue.add(displayMessage);

                if(enemyHp == 0) {
                    finishBattle();
                }
            }
        }
        displayConsoleMessage();
    }

    //choose which humon will attack first (true if player)
    private boolean playerFirst() {
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
            damage = (int) ((moveBaseDamage / 100) * playerHumon.getAttack()) - (enemyHumon.getDefense() / 2);
            if(damage < 1) {
                damage = 1;
            }
        }
        else {
            damage = (int) ((moveBaseDamage / 100) * enemyHumon.getAttack()) - (playerHumon.getDefense() / 2);
            if(damage < 1) {
                damage = 1;
            }
        }
        return damage;
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
     * Displays the next message in the console message queue.
     * If queue is empty and game is not over, displays moves grid view
     *
     */
    private void displayConsoleMessage() {
        displayConsole();
        if(consoleDisplayQueue.size() == 0) {
            if(gameOver) {
                if(canCapture) {
                    captureHumonDialog();
                }
                else {
                    Toast toast = Toast.makeText(this, "Battle Finished", Toast.LENGTH_SHORT);
                    toast.show();

                    //Save the humon's state
                    saveHumons();

                    finish();
                }
            }
            else {
                displayMoves();
            }
        }
        else {
            userConsole.setText(consoleDisplayQueue.get(0));
            consoleDisplayQueue.remove(0);
        }
    }

    /*
     * Prompts the user to capture a Humon. If yes a capture sequence starts, else battle ends.
     * Can only be called if the player wins the battle.
     *
     */
    private void captureHumonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //LayoutInflater inflater = this.getLayoutInflater();

        //View dialogView = inflater.inflate(R.layout.capture_humon_dialog, null);
        //builder.setView(dialogView);
        builder.setTitle("Capture wild " + enemyHumon.getName() + "?");

        //Display prompt for Humon to be captured
        //final TextView captureHumonText = (TextView) dialogView.findViewById(R.id.captureHumonTextView);
        //captureHumonText.setText("Capture wild " + enemyHumon.getName() + "?");

        //Attempt to capture the humon
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {

                Toast toast = Toast.makeText(getApplicationContext(), "Humon Captured!", Toast.LENGTH_SHORT);
                toast.show();

                capturedHumon = true;

                nameHumonDialog();
            }
        });

        //End the battle
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {

                Toast toast = Toast.makeText(getApplicationContext(), "Battle Finished!", Toast.LENGTH_SHORT);
                toast.show();

                //Save the humon's state
                saveHumons();

                //return to the menu
                finish();
            }
        });

        //display the dialog
        final AlertDialog captureHumonDialog = builder.create();
        captureHumonDialog.show();
    }

    /*
     * Dialog for user to name newly captured Humon.
     * This ends the battle screen.
     *
     */
    private void nameHumonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.name_humon_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Name Hu-mon");

        //Assign the textboxes so they can be accessed by buttons
        final EditText nameText = (EditText) dialogView.findViewById(R.id.nameEditText);
        nameText.setText(enemyHumon.getName());

        TextView promptTextView = (TextView) dialogView.findViewById(R.id.humonNameTextView);
        promptTextView.setText("Choose a name for newly captured " + enemyHumon.getName() + "!");

        //Add Element to list
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String humonName = nameText.getText().toString();
                if(!humonName.isEmpty()) {
                    enemyHumon.setName(humonName);
                }

                //retrieve user object to get hcount
                loadUser();

                //Set instance id for new humon
                enemyHumon.setIID(userEmail + "-" + user.getHcount());
                user.incrementHCount();

                System.out.println("Saved new humon with IID: " + enemyHumon.getiID());

                //Save both humons
                saveHumons();
                saveUser();

                Toast toast = Toast.makeText(getApplicationContext(), "Battle Finished", Toast.LENGTH_SHORT);
                toast.show();

                //return to the menu
                finish();
            }
        });

        //display the dialog
        final AlertDialog nameHumonDialog = builder.create();
        nameHumonDialog.show();
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

            canCapture = true;
        }

        //Update the console
        consoleDisplayQueue.add(displayText);
        displayConsoleMessage();

    }

    /*
     * Saves the current state of the humons in the battle.
     * Should be called after the battle
     *
     */
    private void saveHumons() {

        AsyncTask<Humon, Integer, Boolean> partySaveTask = new HumonPartySaver(this);
        if(capturedHumon) {
            //save player's humon data and new humon to party
            partySaveTask.execute(new Humon[]{playerHumon, enemyHumon});
        }
        else {
            //save player's humon data to party
            partySaveTask.execute(playerHumon);
        }

    }

    private void loadUser() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        // User object reader.
        try {
            String userString = sharedPref.getString(getString(R.string.userObjectKey), null);
            System.out.println("User String was: " + userString);
            user = new ObjectMapper().readValue(userString, User.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void saveUser() {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        try {
            editor.putString(getString(R.string.userObjectKey), user.toJson(new ObjectMapper()));
            editor.commit();
        } catch (JsonProcessingException e) {
            // idk yet
        }
    }
}
