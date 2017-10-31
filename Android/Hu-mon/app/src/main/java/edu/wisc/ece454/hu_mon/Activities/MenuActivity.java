package edu.wisc.ece454.hu_mon.Activities;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.StepJobScheduler;

public class MenuActivity extends AppCompatActivity {

    private ListView menuListView;
    private String[] menuOption;
    private String userEmail;

    private String EMAIL_KEY;
    private final String ACTIVITY_TITLE = "Main Menu";

    IntentFilter filter = new IntentFilter();


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
                toast.setText("Began searching for hu-mons, will notify when hu-mon found.");
                toast.show();
                StepJobScheduler.scheduleJob(getApplicationContext());
                break;
            default:
                toast.setText("Error: Bad Menu Item");
                toast.show();
                return;
        }

    }


}
