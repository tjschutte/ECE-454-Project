package edu.wisc.ece454.hu_mon.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.PlaceDetectionService;
import edu.wisc.ece454.hu_mon.Services.StepService;
import edu.wisc.ece454.hu_mon.Utilities.HumonHealTask;
import edu.wisc.ece454.hu_mon.Utilities.UserHelper;

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
    private final String TUT = "Tutorial";
    private final String FIND_HUMONS = "Find Hu-mons anywhere (cheat)";
    private final String HEAL_HUMONS = "Fully Heal Hu-mons (cheat)";

    User user;

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
        Log.i(TAG, "User string (Menu) was: " + userObject);

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
            Log.i(TAG, "Saving user object to shared prefs");
            editor.putString(getString(R.string.userObjectKey), userObject);
            editor.commit();
        }

        menuOption = new String[]{HUMON_INDEX, PARTY, FRIENDS_LIST, MAP, TUT, CREATE_HUMON, HEAL_HUMONS, FIND_HUMONS};

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

        user = UserHelper.loadUser(this);
        if(user.getHcount() > 0) {
            if(stepServiceIntent == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Began searching for hu-mons, will notify when hu-mon found.", Toast.LENGTH_LONG);
                toast.show();
                placeService = new Intent(this, PlaceDetectionService.class);
                stepServiceIntent = new Intent(this, StepService.class);
                startService(stepServiceIntent);
                startService(placeService);
            }
        }

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.gameRunningKey), false);
        editor.commit();
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
        editor.putBoolean(getString(R.string.searchCheatKey), false);
        editor.commit();

        // Save any pertinant data back to the server.
        UserHelper.saveToServer(this);

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
            case TUT:
                intent = new Intent(this, TutorialActivity.class);
                startActivity(intent);
                break;
            case CREATE_HUMON:
                intent = new Intent(this, CreateHumonImageActivity.class);
                startActivity(intent);
                break;
            case HEAL_HUMONS:
                AsyncTask<Integer, Integer, Boolean> healHumonTask = new HumonHealTask(getApplicationContext());
                healHumonTask.execute(200);
                toast.setText("Healed Hu-mons! Cheater!");
                toast.show();
                break;
            case FIND_HUMONS:
                SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.searchCheatKey), true);
                editor.commit();
                toast.setText("Able to find Hu-mons anywhere! Cheater!");
                toast.show();
                break;
            default:
                toast.setText("Error: Bad Menu Item");
                toast.show();
                return;
        }

    }
}
