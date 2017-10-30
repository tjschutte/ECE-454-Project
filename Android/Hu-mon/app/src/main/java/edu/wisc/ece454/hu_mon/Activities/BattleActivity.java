package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import edu.wisc.ece454.hu_mon.R;

public class BattleActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Battle";
    IntentFilter filter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle_layout);
        setTitle(ACTIVITY_TITLE);
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
}
