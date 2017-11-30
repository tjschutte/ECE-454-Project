package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;
import edu.wisc.ece454.hu_mon.Utilities.UserHelper;

public class FriendsListActivity extends SettingsActivity {

    private final String ACTIVITY_TITLE = "Friends List";

    private ListView friendListView;
    ArrayAdapter<String> friendsListAdapter;
    private ListView friendRequestListView;
    ArrayAdapter<String> friendRequestListAdapter;
    private User user;

    ServerConnection mServerConnection;
    boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list_layout);
        setTitle(ACTIVITY_TITLE);

        user = UserHelper.loadUser(this);

        //setup listview for friends list and requests
        refreshContent();

        // Attach to the server communication service
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
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

            System.out.println("In friendslist");

            // Server data is always assumed correct on login. Save to file and overwrite existing data.
            //  If the email was found by the server, add it to the user object.
            if (command.equals(getString(R.string.ServerCommandFriendRequestSuccess))) {
                System.out.println("ServerCommandFriendRequestSuccess");
                user = UserHelper.loadUser(context);

                try {
                    User friend = new ObjectMapper().readValue(data, User.class);
                    user.addFriend(friend.getEmail());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                UserHelper.saveUser(context, user);
                refreshContent();
            } else if (command.equals(getString(R.string.ServerCommandFriendRequest))) {
                System.out.println("Caught in friendslist");
                user = UserHelper.loadUser(context);
                //User friend = new ObjectMapper().readValue(data, User.class);
                user.addFriendRequest(data.trim());

                UserHelper.saveUser(context, user);
                refreshContent();
            }

        }
    };

    IntentFilter filter = new IntentFilter();

    /**
     * Set listview height based on listview children
     *
     * @param listView
     */
    public static void setDynamicHeight(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        //check adapter if null
        if (adapter == null) {
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        View listItem = null;
        for (int i = 0; i < adapter.getCount(); i++) {
            listItem = adapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        // Add one extra item height for spacing / bottom of screen.
        layoutParams.height = height + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(layoutParams);
        listView.requestLayout();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerConnection.LocalBinder myBinder = (ServerConnection.LocalBinder) service;
            mServerConnection = myBinder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceConnection = null;
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // make sure to unbind
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }

        UserHelper.saveUser(this, user);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
        UserHelper.saveUser(this, user);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBound) {
            // Attach to the server communication service
            Intent intent = new Intent(this, ServerConnection.class);
            startService(intent);
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }

        user = UserHelper.loadUser(this);

        filter.addAction(getString(R.string.serverBroadCastEvent));
        registerReceiver(receiver, filter);

    }

    private void refreshContent() {
        friendListView = (ListView) findViewById(R.id.friendListView);
        friendsListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, user.getFriends());
        friendListView.setAdapter(friendsListAdapter);

        //setup listview for friend requests list
        friendRequestListView = (ListView) findViewById(R.id.friendRequestListView);
        friendRequestListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, user.getfriendRequests());
        friendRequestListView.setAdapter(friendRequestListAdapter);

        if (user.getFriends().size() > 0) {
            friendListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            battleInviteDialog(user.getFriends().get(position));
                        }
                    }
            );
        } else {
            //user.addFriend("No friends yet!");
            //friendsListAdapter.notifyDataSetChanged();
        }

        if (user.getfriendRequests().size() > 0) {
            friendRequestListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            friendRequestChoiceDialog(user.getfriendRequests().get(position));
                        }
                    }
            );
        } else {
            //user.addFriendRequest("No pending friend requests.");
            //friendRequestListAdapter.notifyDataSetChanged();
        }

        setDynamicHeight(friendListView);
        setDynamicHeight(friendRequestListView);

        //setup add friend button
        Button addFriendButton = (Button) findViewById(R.id.addFriendButton);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestDialog();
            }
        });
    }

    //ask user if they would like to battle the given friend, then invite to battle
    private void battleInviteDialog(final String friendName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        builder.setMessage("Send Battle Invite to " + friendName + "?");
        builder.setTitle("Send Battle Request");

        //Add Element to list
        builder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                sendBattleInvite(friendName);
            }
        });

        //Close the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        builder.setNeutralButton("Remove Friend", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                user = UserHelper.loadUser(getApplicationContext());
                user.removeFriend(friendName);
                UserHelper.saveUser(getApplicationContext(), user);
                refreshContent();
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
            public void onClick(DialogInterface dialog, int whichButton) {
                String friendName = friendText.getText().toString();
                if (!friendName.isEmpty()) {
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

    //Create a dialog for user to input new friend name
    private void friendRequestChoiceDialog(final String friendName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //LayoutInflater inflater = this.getLayoutInflater();

        builder.setMessage("Accept friend request from " + friendName + "?");
        builder.setTitle("Pending Friend Request");

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Update the server right away so they can battle.
                requestAccepted(friendName);

                // update the object so UI is accurate
                user = UserHelper.loadUser(getApplicationContext());
                user.addFriend(friendName);
                user.removeFriendRequest(friendName);
                UserHelper.saveUser(getApplicationContext(), user);

                refreshContent();
            }
        });

        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                user = UserHelper.loadUser(getApplicationContext());
                user.removeFriendRequest(friendName);
                UserHelper.saveUser(getApplicationContext(), user);
                refreshContent();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        //display the dialog
        final AlertDialog requestChoiceDialog = builder.create();
        requestChoiceDialog.show();
    }

    private void sendBattleInvite(String friendName) {
        if (mBound) {
            mServerConnection.sendMessage(getString(R.string.ServerCommandBattleRequest) + ":{\"email\":\"" + friendName + "\"}");
            try {
                mServerConnection.sendMessage(getString(R.string.ServerCommandSaveUser) + ":" + user.toJson(new ObjectMapper()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFriendRequest(String friendName) {
        if (mBound) {
            mServerConnection.sendMessage(getString(R.string.ServerCommandFriendRequest) + ":{\"email\":\"" + friendName + "\"}");
        }
    }

    private void requestAccepted(String friendName) {
        if (mBound) {
            mServerConnection.sendMessage(getString(R.string.ServerCommandAcceptRequest) + ":{\"email\":\"" + friendName + "\"}");
        }

    }

}
