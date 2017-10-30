package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.R;

public class PartyActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Party";
    private String HUMON_NAME_KEY;

    private ArrayList<String> humonList;
    private ListView partyListView;

    IntentFilter filter = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_layout);
        setTitle(ACTIVITY_TITLE);

        HUMON_NAME_KEY = getString(R.string.humonNameKey);

        humonList = new ArrayList<String>();


        partyListView = (ListView) findViewById(R.id.partyListView);
        ArrayAdapter<String> partyAdapter = new ArrayAdapter<String>(this,
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

    //TODO: Read in all encountered humons and populate list
    //TODO: This needs to be done on a background thread
    private void loadHumons() {

        //TODO: keep humons from last index view and add newly encountered
        humonList.clear();

        humonList.add("Test Humon A");
        humonList.add("Test Humon B");
        humonList.add("Test Humon C");
        humonList.add("Test Humon D");
        humonList.add("Test Humon E");
    }

    //go to humon info activity to view particular humon
    private void viewHumon(String humonName) {
        Intent intent = new Intent(this, PartyInfoActivity.class);
        intent.putExtra(HUMON_NAME_KEY, humonName);
        startActivity(intent);
    }
}
