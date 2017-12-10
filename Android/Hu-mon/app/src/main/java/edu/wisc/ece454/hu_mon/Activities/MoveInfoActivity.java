package edu.wisc.ece454.hu_mon.Activities;

import android.os.Bundle;
import android.widget.TextView;

import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;

public class MoveInfoActivity extends SettingsActivity {

    private String MOVE_KEY;
    private final String ACTIVITY_TITLE = "Move Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_info_layout);
        setTitle(ACTIVITY_TITLE);

        MOVE_KEY = getString(R.string.moveKey);

    }

    @Override
    protected void onStart() {
        super.onStart();


        Move move = (Move) getIntent().getParcelableExtra(MOVE_KEY);
        loadMoveInfo(move);

    }

    //Load move info into UI elements
    private void loadMoveInfo(Move move) {

                TextView tempTextView = (TextView) findViewById(R.id.nameTextView);
                tempTextView.setText(move.getName());

                tempTextView = (TextView) findViewById(R.id.damageTextView);
                if(move.getDmg() < 0) {
                    tempTextView.setText("Healing: " + (move.getDmg() * -1));
                }
                else {
                    tempTextView.setText("Damage: " + move.getDmg());
                }

                tempTextView = (TextView) findViewById(R.id.descTextView);
                tempTextView.setText(move.getDescription());

                tempTextView = (TextView) findViewById(R.id.effectTextView);
                if(move.isHasEffect()) {
                    tempTextView.setText("Effect: " + move.getEffect().toString());
                }
                else {
                    tempTextView.setText("Effect: None");
                }

                tempTextView = (TextView) findViewById(R.id.selfCastTextView);
                if(move.isSelfCast()) {
                    tempTextView.setText("Target: Self");
                }
                else {
                    tempTextView.setText("Target: Enemy");
                }
    }
}
