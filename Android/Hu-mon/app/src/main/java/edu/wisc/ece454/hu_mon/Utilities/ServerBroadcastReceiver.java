package edu.wisc.ece454.hu_mon.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Michael on 11/6/2017.
 */

public class ServerBroadcastReceiver extends BroadcastReceiver {
    final String RESPONSE_KEY = "RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String response = intent.getStringExtra(RESPONSE_KEY);
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
}
