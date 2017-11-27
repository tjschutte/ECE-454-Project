package edu.wisc.ece454.hu_mon.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;

public class FriendsListActivity extends SettingsActivity {

    private final String ACTIVITY_TITLE = "Friends List";

    private ArrayList<String> friendList;
    private ArrayList<String> friendRequestList;
    private ListView friendListView;
    private ListView friendRequestListView;
    private User user;

    ServerConnection mServerConnection;
    boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list_layout);
        setTitle(ACTIVITY_TITLE);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);

        // User object reader.
        try {
            String userString = sharedPref.getString(getString(R.string.userObjectKey), null);
            System.out.println("User String was: " + userString);
            user = new ObjectMapper().readValue(userString, User.class);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        friendList = user.getFriends();
        friendRequestList = user.getfriendRequests();

        //setup listview for friends list
        friendListView = (ListView) findViewById(R.id.friendListView);
        ArrayAdapter<String> friendsListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friendList);
        friendListView.setAdapter(friendsListAdapter);

        //setup listview for friend requests list
        friendRequestListView = (ListView) findViewById(R.id.friendRequestListView);
        ArrayAdapter<String> friendRequestListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friendRequestList);
        friendRequestListView.setAdapter(friendRequestListAdapter);

        if (friendList.size() > 0) {
            friendListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            battleInviteDialog(friendList.get(position));
                        }
                    }
            );
        } else {
            friendList.add("No friends yet!");
            friendsListAdapter.notifyDataSetChanged();
        }

        if (friendRequestList.size() > 0) {
            friendRequestListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // Add a accept / decline dialog.
                            // For decline, just remove from request list.  Other user can always delete
                            // and request again. (Or we could tell server / other device to delete)
                            // For accept need to tell server so it can issue a notification / update DB
                            friendRequestChoiceDialog(friendRequestList.get(position));
                        }
                    }
            );
        } else {
            friendRequestList.add("No pending friend requests.");
            friendRequestListAdapter.notifyDataSetChanged();
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

        // Attach to the server communication service
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

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
        // Check if we have accepted any friend requests.  If we have, get the user from shared pref
        // again (incase it was updated by us adding a friend or getting a friend request)
        // update it.  And write it back out to shared pref
        // TODO: That.
    }

    @Override
    protected void onPause() {
        super.onPause();
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

        // On Resume called, need to make sure to pull user object out again in case it has updated
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        try {
            String userString = sharedPref.getString(getString(R.string.userObjectKey), null);
            System.out.println("User String was: " + userString);
            user = new ObjectMapper().readValue(userString, User.class);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


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

    //Create a dialog for user to input new friend name
    private void friendRequestChoiceDialog(String friendName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //LayoutInflater inflater = this.getLayoutInflater();

        builder.setMessage("Accept friend request from " + friendName + "?");
        builder.setTitle("Pending Friend Request");

        builder.setPositiveButton("Accpet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Accpet the friend request
            }
        });

        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Decline the request
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
