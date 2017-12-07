package edu.wisc.ece454.hu_mon.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import edu.wisc.ece454.hu_mon.R;

public class PartyActivity extends SettingsActivity {

    private final String ACTIVITY_TITLE = "Party";
    private String HUMON_KEY;
    private String HUMONS_KEY;

    private String userEmail;
    private String partyFilename;

    private ArrayList<Humon> humonList;
    private ArrayAdapter<Humon> partyAdapter;
    private ListView partyListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_layout);
        setTitle(ACTIVITY_TITLE);

        HUMON_KEY = getString(R.string.humonKey);

        humonList = new ArrayList<Humon>();

        //retrieve email of the user
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        userEmail = sharedPref.getString(getString(R.string.emailKey), "");
        partyFilename = getFilesDir() + "/" + userEmail + getString(R.string.partyFile);
        HUMONS_KEY = getString(R.string.humonsKey);

        partyListView = (ListView) findViewById(R.id.partyListView);
        partyAdapter = new ArrayAdapter<Humon>(this,
                android.R.layout.simple_list_item_1, humonList);
        partyListView.setAdapter(partyAdapter);

        partyListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        viewHumon(humonList.get(position));
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadHumons();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    private void loadHumons() {

        humonList.clear();

        Thread loadThread = new Thread() {
            public void run() {
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

                    System.out.println(partyString + " loaded");
                    //add each humon name to list
                    for(int i = 0; i < humonsArray.length(); i++) {
                        //load humon into json object format
                        String humonString = humonsArray.getString(i);
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

                        Humon loadedHumon = new Humon(name, description, image, level, xp, hID, uID,
                                iID, moveList, health, luck, attack, speed, defense, imagePath, hp);

                        humonList.add(loadedHumon);

                        System.out.println(loadedHumon.getName() + " with HID: " + loadedHumon.gethID() + " added");
                    }
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
            }
        };

        loadThread.run();
        partyAdapter.notifyDataSetChanged();
        System.out.println("Finished loading");

    }

    //go to humon info activity to view particular humon
    private void viewHumon(Humon humon) {
        Intent intent = new Intent(this, PartyInfoActivity.class);
        intent.putExtra(HUMON_KEY, humon);
        startActivity(intent);
    }
}
