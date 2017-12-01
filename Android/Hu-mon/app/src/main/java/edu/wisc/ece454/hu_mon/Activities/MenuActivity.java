package edu.wisc.ece454.hu_mon.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.PlaceDetectionService;
import edu.wisc.ece454.hu_mon.Services.StepService;
import edu.wisc.ece454.hu_mon.Utilities.JobServiceScheduler;

public class MenuActivity extends SettingsActivity {

    private ListView menuListView;
    private String[] menuOption;
    private String userEmail;
    private String userObject;
    private Intent placeService;
    private Intent stepServiceIntent;

    private String EMAIL_KEY;
    private final String ACTIVITY_TITLE = "Main Menu";
    private static final String TAG = "MENU";

    //Menu values
    private final String HUMON_INDEX = "Hu-mon Index";
    private final String PARTY = "Party";
    private final String FRIENDS_LIST = "Friends List";
    private final String MAP = "Map";
    private final String CREATE_HUMON = "Create Hu-mon";
    private final String HUMON_SEARCH = "Search for Hu-mons (dev)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        setTitle(ACTIVITY_TITLE);

        //save email to shared preferences so all activities have access
        EMAIL_KEY  = getString(R.string.emailKey);
        userEmail = getIntent().getStringExtra(EMAIL_KEY);
        // Get the user object so all activities have access to it.
        userObject = getIntent().getStringExtra(getString(R.string.userObjectKey));
        System.out.println("User string (Menu) was: " + userObject);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Save user Email to the sharedPreferences
        if(userEmail != null) {
            editor.putString(getString(R.string.emailKey), userEmail);
            editor.commit();
        }
        // Save User object to the sharedPreferences
        if (userObject != null) {
            System.out.println("Saving user object to shared prefs");
            editor.putString(getString(R.string.userObjectKey), userObject);
            editor.commit();
        }


        menuOption = new String[]{HUMON_INDEX, PARTY, FRIENDS_LIST, MAP, CREATE_HUMON, HUMON_SEARCH};


        menuListView = (ListView) findViewById(R.id.menuListView);
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, menuOption);
        menuListView.setAdapter(menuAdapter);

        menuListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        changeMenu(menuOption[position]);
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        //Stop the step service
        if(stepServiceIntent != null) {
            stopService(stepServiceIntent);
            stepServiceIntent = null;
        }

        if (placeService != null) {
            Log.d(TAG,"Stopping PlaceDetectionService");
            stopService(placeService);
        }

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.gameRunningKey), false);
        editor.commit();

        // Save any pertinant data back to the server.
        saveToServer();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    //changes activity to the next screen based off menu button hit
    private void changeMenu(String nextMenu) {
        Toast toast = Toast.makeText(getApplicationContext(), nextMenu, Toast.LENGTH_LONG);

        Intent intent;
        switch(nextMenu) {
            case HUMON_INDEX:
                intent = new Intent(this, IndexActivity.class);
                startActivity(intent);
                break;
            case PARTY:
                intent = new Intent(this, PartyActivity.class);
                startActivity(intent);
                break;
            case FRIENDS_LIST:
                intent = new Intent(this, FriendsListActivity.class);
                startActivity(intent);
                break;
            case MAP:
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
            case CREATE_HUMON:
                intent = new Intent(this, CreateHumonImageActivity.class);
                startActivity(intent);
                break;
            case HUMON_SEARCH:
                if(hasHumons()) {
                    if(stepServiceIntent == null) {
                        toast.setText("Began searching for hu-mons, will notify when hu-mon found.");
                        toast.show();
                        placeService = new Intent(this, PlaceDetectionService.class);
                        stepServiceIntent = new Intent(this, StepService.class);
                        startService(placeService);
                        startService(stepServiceIntent);
                    }
                    else {
                        toast.setText("Already searching for hu-mons!");
                        toast.show();
                    }
                }
                else {
                    toast.setText("Cannot search without a humon.");
                    toast.show();
                }
                break;
            default:
                toast.setText("Error: Bad Menu Item");
                toast.show();
                return;
        }

    }

    //Save all humons in party to server (happens on sign out and destruction of app)
    private void saveToServer() {

        //read in current party(if it exists)
        boolean hasPartyFile = true;
        boolean hasUserFile = true;
        FileInputStream inputStream;
        String oldParty = "";
        String oldUser = "";
        File partyFile = new File(getFilesDir(), userEmail + getString(R.string.partyFile));
        File userFile = new File(getFilesDir(), userEmail);
        JSONObject partyJSON;
        JSONObject userJSON;
        JSONArray humonsArray;

        String[] saveHumons = null;
        String[] saveUser = null;

        if (hasHumons()) {
            try {
                inputStream = new FileInputStream(partyFile);
                int inputBytes = inputStream.available();
                byte[] buffer = new byte[inputBytes];
                inputStream.read(buffer);
                inputStream.close();
                oldParty = new String(buffer, "UTF-8");
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("No party currently exists for: " + userEmail);
                hasPartyFile = false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(hasPartyFile) {
                //update HID in party
                try {
                    //append humon on to current object
                    if (oldParty.length() == 0) {
                        System.out.println("Party is empty.");
                        return;
                    } else {
                        partyJSON = new JSONObject(oldParty);
                        humonsArray = partyJSON.getJSONArray(getString(R.string.humonsKey));
                    }

                    saveHumons = new String[humonsArray.length()];

                    //store all humons as JSON strings to pass to service
                    for (int j = 0; j < humonsArray.length(); j++) {
                        JSONObject humonJSON = new JSONObject(humonsArray.getString(j));
                        humonJSON.put("imagePath", "");
                        saveHumons[j] = humonJSON.toString();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Unable to find party file");
            }
        }

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);

        saveUser = new String[] {sharedPref.getString(getString(R.string.userKey), "")};


        // Save user and party.
        JobServiceScheduler.scheduleServerSaveJob(getApplicationContext(), saveHumons, saveUser);

    }

    //check if the user has a humon in their party
    private boolean hasHumons() {
        boolean hasHumons = true;

        //TODO: Instead check user model (return true if hCount == 0)
        File partyFile = new File(this.getFilesDir(), userEmail + getString(R.string.partyFile));
        String partyFileText;

        //read in current index (if it exists)
        try {
            FileInputStream inputStream = new FileInputStream(partyFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            partyFileText = new String(buffer, "UTF-8");

            //check number of humons in file
            JSONObject pObject = new JSONObject(partyFileText);
            JSONArray humonsArray = pObject.getJSONArray(getString(R.string.humonsKey));
            if(humonsArray.length() == 0) {
                hasHumons = false;
            }
        } catch(FileNotFoundException e) {
            System.out.println("No party file for " + userEmail);
            hasHumons = false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            hasHumons = false;
        }

        return hasHumons;
    }


}
