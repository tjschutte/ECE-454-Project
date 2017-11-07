package edu.wisc.ece454.hu_mon.Activities;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
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
import edu.wisc.ece454.hu_mon.Utilities.StepJobScheduler;

public class MenuActivity extends AppCompatActivity {

    private ListView menuListView;
    private String[] menuOption;
    private String userEmail;

    private String EMAIL_KEY;
    private final String ACTIVITY_TITLE = "Main Menu";

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
        if(userEmail != null) {
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.emailKey), userEmail);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = 23)
    @Override
    protected void onDestroy() {
        //Obtain ID of Step JobService
        int stepJobId = Integer.parseInt(getString(R.string.stepJobId));

        JobScheduler jobScheduler = this.getSystemService(JobScheduler.class);
        jobScheduler.cancel(stepJobId);
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
    @RequiresApi(api = Build.VERSION_CODES.M)
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
                    toast.setText("Began searching for hu-mons, will notify when hu-mon found.");
                    toast.show();
                    StepJobScheduler.scheduleJob(getApplicationContext());
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
