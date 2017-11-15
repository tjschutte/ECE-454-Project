package edu.wisc.ece454.hu_mon.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;

import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.MoveListAdapter;

public class MoveListActivity extends SettingsActivity {
    private final String ACTIVITY_TITLE = "Select Move";
    private String MOVE_KEY;
    private String MOVE_POSITION_KEY;
    private int movePosition;

    private ArrayList<Move> moveList;
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

        moveList = new ArrayList<Move>();


        moveListView = (ListView) findViewById(R.id.moveListView);
        MoveListAdapter moveAdapter = new MoveListAdapter(this, moveList);
        moveListView.setAdapter(moveAdapter);

        moveListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        renameMove(moveList.get(position));
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
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

            Random rng = new Random();
            boolean target = rng.nextBoolean();
            int dmg = rng.nextInt(101);


            Move.Effect effect = null;
            boolean hasEffect = true;
            switch(rng.nextInt(6)) {
                case 5:
                    effect = Move.Effect.PARALYZED;
                    break;
                case 4:
                    effect = Move.Effect.CONFUSED;
                    break;
                case 3:
                    effect = Move.Effect.EMBARRASSED;
                    break;
                case 2:
                    effect = Move.Effect.POISONED;
                    break;
                case 1:
                    effect = Move.Effect.SLEPT;
                    break;
                default:
                    hasEffect = false;
            }

            moveList.add(new Move(i, moveName, target, dmg, effect, hasEffect, "Wattup"));
        }
    }

    private void renameMove(final Move move) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.rename_move_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Rename " + move.getName());

        //Assign the textboxes so they can be accessed by buttons
        final EditText nameText = (EditText) dialogView.findViewById(R.id.renameMoveEditText);
        nameText.setText(move.getName());
        final EditText descriptionText = (EditText) dialogView.findViewById(R.id.descriptionEditText);
        descriptionText.setText(move.getDescription());

        //Send move back to CreateHumon
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String moveName = nameText.getText().toString();
                String moveDesc = descriptionText.getText().toString();

                if(moveName.isEmpty()) {
                    moveName = move.getName();
                }
                if(moveDesc.isEmpty()) {
                    moveDesc = move.getDescription();
                }

                //id, name, selfcast, dmg, effect, hasEffect, description
                Move userMove = new Move(move.getId(), moveName, move.getSelfCast(),
                        move.getDmg(), move.getEffect(), move.isHasEffect(),
                moveDesc);

                Intent returnIntent = new Intent();
                returnIntent.putExtra(MOVE_KEY, (Parcelable) userMove);
                returnIntent.putExtra(MOVE_POSITION_KEY, movePosition);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        //Close the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        //display the dialog
        final AlertDialog RenameMoveDialog = builder.create();
        RenameMoveDialog.show();
    }

}
