package edu.wisc.ece454.hu_mon.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;

public class HumonInfoActivity extends SettingsActivity {

    private String HUMON_KEY;
    private final String ACTIVITY_TITLE = "Humon Info";

    private Humon humon;
    private ArrayAdapter<Move> moveAdapter;
    private ArrayList<Move> moveList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.humon_info_layout);
        setTitle(ACTIVITY_TITLE);

        HUMON_KEY = getString(R.string.humonKey);
        moveList = new ArrayList<Move>();
        GridView moveGridView = (GridView) findViewById(R.id.moveGridView);
        moveAdapter = new ArrayAdapter<Move>(this,
                android.R.layout.simple_list_item_1, moveList);
        moveGridView.setAdapter(moveAdapter);

        moveGridView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        viewMove(moveList.get(position));
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();


        humon = (Humon) getIntent().getParcelableExtra(HUMON_KEY);
        loadHumonInfo();

    }

    //Load humon info into UI elements
    private void loadHumonInfo() {


        this.runOnUiThread(new Runnable() {
            public void run() {

                //load all text data
                TextView tempTextView = (TextView) findViewById(R.id.humonNameTextView);
                tempTextView.setText(humon.getName());
                tempTextView = (TextView) findViewById(R.id.humonDescriptionTextView);
                tempTextView.setText(humon.getDescription());
                tempTextView = (TextView) findViewById(R.id.healthValue);
                tempTextView.setText("" + humon.getHealth());
                tempTextView = (TextView) findViewById(R.id.attackValue);
                tempTextView.setText("" + humon.getAttack());
                tempTextView = (TextView) findViewById(R.id.defenseValue);
                tempTextView.setText("" + humon.getDefense());
                tempTextView = (TextView) findViewById(R.id.speedValue);
                tempTextView.setText("" + humon.getSpeed());
                tempTextView = (TextView) findViewById(R.id.luckValue);
                tempTextView.setText("" + humon.getLuck());

                //load moves into grid
                ArrayList<Move> humonMoves = humon.getMoves();
                for(int i = 0; i < humonMoves.size(); i++) {
                    moveList.add(humonMoves.get(i));
                }
                moveAdapter.notifyDataSetChanged();

                //load humon image
                if(humon.getImagePath() != null) {
                    if(humon.getImagePath().length() != 0) {
                        Bitmap humonImage = BitmapFactory.decodeFile(humon.getImagePath());
                        ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);
                        humonImageView.setImageBitmap(humonImage);
                    }
                }

            }
        });

    }

    //View info about move in its own activity
    private void viewMove(Move move) {
        Intent intent = new Intent(this, MoveInfoActivity.class);
        intent.putExtra(getString(R.string.moveKey), move);
        startActivity(intent);
    }
}
