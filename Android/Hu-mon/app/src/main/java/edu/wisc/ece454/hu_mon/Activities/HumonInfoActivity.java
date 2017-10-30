package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import edu.wisc.ece454.hu_mon.R;

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
