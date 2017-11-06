package edu.wisc.ece454.hu_mon.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import edu.wisc.ece454.hu_mon.R;

public class PartyInfoActivity extends AppCompatActivity {

    private String HUMON_NAME_KEY;
    private final String ACTIVITY_TITLE = "Party Hu-mon Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_info_layout);
        setTitle(ACTIVITY_TITLE);

        HUMON_NAME_KEY = getString(R.string.humonNameKey);

    }

    @Override
    protected void onStart() {
        super.onStart();


        String humonName = getIntent().getStringExtra(HUMON_NAME_KEY);
        loadHumonInfo(humonName);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    //TODO: Load humon info using name
    //TODO: Load on background thread
    private void loadHumonInfo(String name) {
        TextView nameView = (TextView) findViewById(R.id.nameTextView);
        nameView.setText(name);
    }
}
