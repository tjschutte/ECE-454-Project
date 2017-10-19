package edu.wisc.ece454.hu_mon.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.R;

public class MoveListActivity extends AppCompatActivity {
    private final String ACTIVITY_TITLE = "Select Move";
    private String MOVE_KEY;
    private String MOVE_POSITION_KEY;
    private int movePosition;

    private ArrayList<String> moveList;
    private ListView moveListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_list_layout);
        setTitle(ACTIVITY_TITLE);

        MOVE_KEY = getString(R.string.moveKey);
        MOVE_POSITION_KEY = getString(R.string.movePositionKey);

        //retrieve position of selected move
        movePosition = getIntent().getIntExtra(MOVE_POSITION_KEY, -1);

        moveList = new ArrayList<String>();


        moveListView = (ListView) findViewById(R.id.moveListView);
        ArrayAdapter<String> moveAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, moveList);
        moveListView.setAdapter(moveAdapter);

        moveListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(MOVE_KEY, moveList.get(position));
                        returnIntent.putExtra(MOVE_POSITION_KEY, movePosition);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }
        );
    }

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

    //TODO: Read in all possible moves and populate list
    //TODO: This needs to be done on a background thread
    private void loadMoves() {

        //TODO: keep humons from last index view and add newly encountered
        moveList.clear();

        moveList.add("Test Move A");
        moveList.add("Test Move B");
        moveList.add("Test Move C");
        moveList.add("Test Move D");
        moveList.add("Test Move E");
        moveList.add("Test Move F");
        moveList.add("Test Move G");
        moveList.add("Test Move H");
        moveList.add("Test Move I");
        moveList.add("Test Move J");
    }

}