package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class IndexActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Hu-mon Index";
    private String HUMON_KEY;

    private ArrayList<Humon> humonList;
    private ArrayAdapter<Humon> indexAdapter;
    private ListView indexListView;

    private String userEmail;
    private String indexFilename;
    private String HUMONS_KEY;

    IntentFilter filter = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_layout);
        setTitle(ACTIVITY_TITLE);

        HUMON_KEY = getString(R.string.humonKey);

        humonList = new ArrayList<Humon>();

        //retrieve email of the user
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        userEmail = sharedPref.getString(getString(R.string.emailKey), "");
        indexFilename = getFilesDir() + "/" + userEmail + getString(R.string.indexFile);
        HUMONS_KEY = getString(R.string.humonsKey);


        indexListView = (ListView) findViewById(R.id.indexListView);
        indexAdapter = new ArrayAdapter<Humon>(this,
                android.R.layout.simple_list_item_1, humonList);
        indexListView.setAdapter(indexAdapter);

        indexListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        viewHumon(humonList.get(position));
                    }
                }
        );
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
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

            System.out.println(command + ": " + data);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        loadHumons();
    }

    @Override
    protected void onPause() {
        super.onPause();try {
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

    protected void onResume() {
        super.onResume();
        filter.addAction(getString(R.string.serverBroadCastEvent));
        registerReceiver(receiver, filter);
    }

    //TODO: Read in moves
    private void loadHumons() {

        humonList.clear();

        Thread loadThread = new Thread() {
            public void run() {
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
                    //add each humon name to list
                    for(int i = 0; i < humonsArray.length(); i++) {
                        //load humon into json object format
                        String humonString = humonsArray.getString(i);
                        JSONObject humonJson = new JSONObject(humonString);
                        String name = humonJson.getString("name");
                        String description = humonJson.getString("description");
                        Bitmap image = null;
                        int level = humonJson.getInt("level");
                        int xp = humonJson.getInt("xp");
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

                        Humon loadedHumon = new Humon(name, description, image, level, xp, hID, uID,
                                iID, moveList, health, luck, attack, speed, defense, imagePath);

                        humonList.add(loadedHumon);

                        System.out.println(loadedHumon.getName() + " added");
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
        indexAdapter.notifyDataSetChanged();
        System.out.println("Finished loading");

    }

    //go to humon info activity to view particular humon
    private void viewHumon(Humon humon) {
        Intent intent = new Intent(this, HumonInfoActivity.class);
        intent.putExtra(HUMON_KEY, (Parcelable) humon);
        startActivity(intent);
    }
}
