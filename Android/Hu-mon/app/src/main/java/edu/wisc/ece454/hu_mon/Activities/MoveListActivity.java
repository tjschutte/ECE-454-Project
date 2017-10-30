package edu.wisc.ece454.hu_mon.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;

public class MoveListActivity extends AppCompatActivity {
    private final String ACTIVITY_TITLE = "Select Move";
    private String MOVE_KEY;
    private String MOVE_POSITION_KEY;
    private int movePosition;

    private ArrayList<Move> moveList;
    private ListView moveListView;

    IntentFilter filter = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_list_layout);
        setTitle(ACTIVITY_TITLE);

        MOVE_KEY = getString(R.string.moveKey);
        MOVE_POSITION_KEY = getString(R.string.movePositionKey);

        //retrieve position of selected move
        movePosition = getIntent().getIntExtra(MOVE_POSITION_KEY, -1);

        moveList = new ArrayList<Move>();


        moveListView = (ListView) findViewById(R.id.moveListView);
        ArrayAdapter<Move> moveAdapter = new ArrayAdapter<Move>(this,
                android.R.layout.simple_list_item_1, moveList);
        moveListView.setAdapter(moveAdapter);

        moveListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(MOVE_KEY, (Parcelable) moveList.get(position));
                        returnIntent.putExtra(MOVE_POSITION_KEY, movePosition);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
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

        loadMoves();
    }

    @Override
    protected void onStop() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();

        super.onStop();
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

    //TODO: Read in all possible moves and populate list
    //TODO: This needs to be done on a background thread
    private void loadMoves() {

        moveList.clear();
        for(int i = 0; i < 10; i++) {
            String moveName = "Test Move ";
            char uniqueLetter = 'A';
            uniqueLetter += i;
            moveName += uniqueLetter;
            moveList.add(new Move(i, moveName, false, 10, null, false, "Wattup"));
        }
    }

}
