package edu.wisc.ece454.hu_mon.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;

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

        if(data.length() < 100) {
            System.out.println(command + ": " + data);
        }
        else {
            System.out.println(command);
        }

        if(command.equals("CREATE-HUMON")) {
            //retrieve email of the user
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
            String userEmail = sharedPref.getString(context.getString(R.string.emailKey), "");
            String hName = "";
            String hDescription = "";
            String hID = "";

            boolean goodPayload = true;
            try {
                JSONObject serverJSON = new JSONObject(data);
                hName = serverJSON.getString("name");
                hDescription = serverJSON.getString("description");
                hID = serverJSON.getString("hID");
            } catch(Exception e) {
                e.printStackTrace();
                goodPayload = false;
            }

            if(goodPayload) {
                //update HIDS in index and party
                AsyncTask<String, Integer, Boolean> hidUpdateTask = new HumonIDUpdater(context,
                        hName, hDescription);
                hidUpdateTask.execute(hID);
            }
        }

        //  If the email was found by the server, add it to the user object. We do this here and in
        // Friendlist activity for redundancy to make sure it is added.
        else if (command.equals("FRIEND-REQUEST")) {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
            // User object reader.
            try {
                ObjectMapper mapper = new ObjectMapper();
                String userString = sharedPref.getString("userObjectKey", null);
                System.out.println("User String was: " + userString);
                User user = mapper.readValue(userString, User.class);
                User friend = mapper.readValue(data, User.class);
                user.addFriend(friend.getEmail());

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userObjectKey", user.toJson(mapper));
                editor.commit();

            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (command.equals("FRIEND-REQUEST-SUCCESS")) {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
            // User object reader.
            try {
                ObjectMapper mapper = new ObjectMapper();
                String userString = sharedPref.getString("userObjectKey", null);
                System.out.println("User String was: " + userString);
                User user = mapper.readValue(userString, User.class);
                User friend = mapper.readValue(data, User.class);
                user.addFriendRequest(friend.getEmail());

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userObjectKey", user.toJson(mapper));
                editor.commit();

            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
