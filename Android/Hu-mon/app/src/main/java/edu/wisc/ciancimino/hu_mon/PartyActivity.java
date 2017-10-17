package edu.wisc.ciancimino.hu_mon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class PartyActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Party";
    private String HUMON_NAME_KEY;

    private ArrayList<String> humonList;
    private ListView partyListView;

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

    @Override
    protected void onStart() {
        super.onStart();

        loadHumons();
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
