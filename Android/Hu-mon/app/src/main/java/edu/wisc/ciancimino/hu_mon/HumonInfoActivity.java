package edu.wisc.ciancimino.hu_mon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HumonInfoActivity extends AppCompatActivity {

    private String HUMON_NAME_KEY;
    private final String ACTIVITY_TITLE = "Humon Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.humon_info_layout);
        setTitle(ACTIVITY_TITLE);

        HUMON_NAME_KEY = getString(R.string.humonNameKey);

    }

    @Override
    protected void onStart() {
        super.onStart();


        String humonName = getIntent().getStringExtra(HUMON_NAME_KEY);
        loadHumonInfo(humonName);

    }

    //TODO: Load humon info using name
    //TODO: Load on background thread
    private void loadHumonInfo(String name) {
        TextView nameView = (TextView) findViewById(R.id.nameTextView);
        nameView.setText(name);
    }
}
