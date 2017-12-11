package edu.wisc.ece454.hu_mon.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.MoveListAdapter;

public class MoveListActivity extends SettingsActivity {
    private final String ACTIVITY_TITLE = "Select Move";
    private final String TAG = "Moves List";
    private String MOVE_KEY;
    private String MOVE_POSITION_KEY;
    private int movePosition;
    private int[] usedMoves;

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

        //retrieve moves already chosen (no duplicates)
        usedMoves = getIntent().getIntArrayExtra(MOVE_KEY);

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
        loadMoves();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    //Load preset moves from assets
    private void loadMoves() {
        Thread loadThread = new Thread() {
            public void run() {
                try {
                    moveList.clear();
                    String filename = "preset_moves.json";
                    Log.i(TAG,"Attempting to load: " + filename);
                    //load moves file
                    String movesString;
                    InputStream inputStream = getAssets().open(filename);
                    int inputBytes = inputStream.available();
                    byte[] buffer = new byte[inputBytes];
                    inputStream.read(buffer);
                    inputStream.close();
                    movesString = new String(buffer, "UTF-8");
                    JSONObject fileJson = new JSONObject(movesString);
                    JSONArray movesArray = fileJson.getJSONArray("moves");

                    Log.i(TAG,filename + " loaded");

                    //add each move to list
                    for(int i = 0; i < movesArray.length(); i++) {
                        JSONObject moveJson = movesArray.getJSONObject(i);
                        int moveId = moveJson.getInt("id");
                        boolean isDuplicate = false;

                        //check if this move was already chosen
                        for(int j = 0; j < usedMoves.length; j++) {
                            if(moveId == usedMoves[j]) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if(isDuplicate) {
                            continue;
                        }
                        String moveName = moveJson.getString("name");
                        boolean moveSelfCast = moveJson.getBoolean("selfCast");
                        int dmg = moveJson.getInt("dmg");

                        Move.Effect moveEffect;
                        String moveEffectString = moveJson.getString("effect");
                        switch(moveEffectString) {
                            case "CONFUSED":
                                moveEffect = Move.Effect.CONFUSED;
                                break;
                            case "PARALYZED":
                                moveEffect = Move.Effect.PARALYZED;
                                break;
                            case "EMBARRASSED":
                                moveEffect = Move.Effect.EMBARRASSED;
                                break;
                            case "POISONED":
                                moveEffect = Move.Effect.POISONED;
                                break;
                            case "SLEPT":
                                moveEffect = Move.Effect.SLEPT;
                                break;
                            default:
                                moveEffect = null;
                        }

                        boolean moveHasEffect = moveJson.getBoolean("hasEffect");
                        String moveDescription = moveJson.getString("description");

                        moveList.add(new Move(moveId, moveName, moveSelfCast, dmg, moveEffect,
                                    moveHasEffect, moveDescription));
                    }
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "No file found for preset_moves.json");
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
        Log.i(TAG, "Finished loading");
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
