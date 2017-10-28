package edu.wisc.ece454.hu_mon.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.wisc.ece454.hu_mon.R;

public class BattleActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Battle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle_layout);
        setTitle(ACTIVITY_TITLE);
    }
}
