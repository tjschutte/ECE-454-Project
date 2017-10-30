package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.R;

public class FriendsListActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Friends List";

    private ArrayList<String> friendsList;
    private ListView friendsListView;

    IntentFilter filter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list_layout);
        setTitle(ACTIVITY_TITLE);

        friendsList = new ArrayList<String>();

        //setup listview for friends list
        friendsListView = (ListView) findViewById(R.id.friendsListView);
        ArrayAdapter<String> friendsListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friendsList);
        friendsListView.setAdapter(friendsListAdapter);

        friendsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        battleInviteDialog(friendsList.get(position));
                    }
                }
        );

        //setup add friend button
        Button addFriendButton = (Button) findViewById(R.id.addFriendButton);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestDialog();
            }
        });


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

        loadFriends();
    }


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

    //TODO: Read in all encountered humons and populate list
    //TODO: This needs to be done on a background thread
    private void loadFriends() {

        //TODO: keep humons from last index view and add newly encountered
        friendsList.clear();

        friendsList.add("Test Friend A");
        friendsList.add("Test Friend B");
        friendsList.add("Test Friend C");
        friendsList.add("Test Friend D");
        friendsList.add("Test Friend E");
    }

    //ask user if they would like to battle the given friend, then invite to battle
    private void battleInviteDialog(final String friendName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        builder.setMessage("Send Battle Invite to " + friendName + "?");
        builder.setTitle("Send Battle Request");

        //Add Element to list
        builder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                sendBattleInvite(friendName);
            }
        });

        //Close the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        //display the dialog
        final AlertDialog battleInviteDialog = builder.create();
        battleInviteDialog.show();
    }

    //Create a dialog for user to input new friend name
    private void friendRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.add_friend_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Friend Request");

        //Assign the textboxes so they can be accessed by buttons
        final EditText friendText = (EditText) dialogView.findViewById(R.id.addFriendEditText);

        //Add Element to list
        builder.setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String friendName = friendText.getText().toString();
                if(!friendName.isEmpty()) {
                    sendFriendRequest(friendName);
                }
            }
        });

        //Close the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        //display the dialog
        final AlertDialog friendRequestDialog = builder.create();
        friendRequestDialog.show();
    }

    //TODO: Send a battle invite to friend
    private void sendBattleInvite(String friendName) {
        String displayString = "Invited " + friendName;
        Toast toast = Toast.makeText(this, displayString, Toast.LENGTH_SHORT);
        toast.show();
    }

    //TODO: Send friend request
    private void sendFriendRequest(String friendName) {
        String displayString = "Friend Request Sent to " + friendName;
        Toast toast = Toast.makeText(this, displayString, Toast.LENGTH_SHORT);
        toast.show();
    }

}
