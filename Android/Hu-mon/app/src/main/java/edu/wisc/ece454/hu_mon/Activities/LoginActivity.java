package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;

public class LoginActivity extends AppCompatActivity {

    private final String FIELD_MISSING = "Make sure all fields are filled in.";
    private final String PERMISSION_FAILURE = "Must Allow Permissions to Proceed";
    private String EMAIL_KEY;
    private String email;
    private String password;
    private String deviceToken;

    private final String ACTIVITY_TITLE = "Login";
    ServerConnection mServerConnection;
    boolean mServiceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        setTitle(ACTIVITY_TITLE);

        EMAIL_KEY = getString(R.string.emailKey);
        checkPermissions();
        FirebaseApp.initializeApp(this);
        deviceToken = FirebaseInstanceId.getInstance().getToken();
    }



    @Override
    protected void onStart() {
        super.onStart();
        /*
        * Connect to the server
        * */
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Should end up killing the sevice when the app is closed.
        Intent intent = new Intent(this, ServerConnection.class);
        stopService(intent);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        filter.addAction(getString(R.string.serverBroadCastEvent));
        registerReceiver(receiver, filter);
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
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerConnection.LocalBinder myBinder = (ServerConnection.LocalBinder) service;
            mServerConnection = myBinder.getService();
            mServiceBound = true;
        }
    };

    //Checks if user has given the app all necessary permissions to use the app
    //Returns false if any permissions not given, also alerts user to give permission
    private boolean checkPermissions() {
        boolean hasPermissions = true;

        //check for camera permission
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            //activate dialog to ask for permission
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.CAMERA  }, 0);
            hasPermissions = false;
        }

        //check for fine_location permission
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //activate dialog to ask for permission
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0);
            hasPermissions = false;
        }

        return hasPermissions;
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

            // Server data is always assumed correct on login. Save to file and overwrite existing data.
            if (command.equals(getString(R.string.ServerCommandLogin)) || command.equals(getString(R.string.ServerCommandRegister))) {
                //Send email to next activity to retrieve user info
                Intent i = new Intent(context, MenuActivity.class);
                i.putExtra(EMAIL_KEY, email);
                i.putExtra(getString(R.string.userObjectKey), data);
                startActivity(i);
            } else {
                Toast toast = Toast.makeText(context, response, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };

    IntentFilter filter = new IntentFilter();

    //Called when hitting the sign in button
    //Retrieves EditText values and attempts to sign in account
    //On success, changes to menu activity
    public void signInAccount(View view) {
        EditText emailText = (EditText) findViewById(R.id.emailEditText);
        EditText passwordText = (EditText) findViewById(R.id.passwordEditText);

        email = emailText.getText().toString();
        password = passwordText.getText().toString();

        // test account to not need the server
        if (email.equals("test")){
            //Send email to next activity to retrieve user info
            Intent i = new Intent(this, MenuActivity.class);
            i.putExtra(EMAIL_KEY, email);
            startActivity(i);
        }

        if(email.isEmpty() || password.isEmpty()) {
            Toast toast = Toast.makeText(this, FIELD_MISSING, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(!checkPermissions()) {
            Toast toast = Toast.makeText(this, PERMISSION_FAILURE, Toast.LENGTH_SHORT);
            toast.show();
        }

        else {
            if (mServiceBound){
                if (deviceToken != null) {
                    mServerConnection.sendMessage(getString(R.string.ServerCommandLogin) + ":{\"email\":\"" + email + "\",\"password\":\"" + password + "\"," +
                            "\"deviceToken\":\"" + deviceToken + "\"}");
                } else {
                    deviceToken = FirebaseInstanceId.getInstance().getToken();
                    if (deviceToken != null) {
                        mServerConnection.sendMessage(getString(R.string.ServerCommandLogin) + ":{\"email\":\"" + email + "\",\"password\":\"" + password + "\"," +
                                "\"deviceToken\":\"" + deviceToken + "\"}");
                    }
                }
            } else {
                Toast toast = Toast.makeText(this, "Error connecting to server. Try again.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }

    //Called when hitting the register button
    //Retrieves EditText values and attempts to register a new account
    //On success, changes to menu activity
    //TODO: This can map to a new activity if we want more info (Screen name, User info)
    public void registerAccount(View view) {
        EditText emailText = (EditText) findViewById(R.id.emailEditText);
        EditText passwordText = (EditText) findViewById(R.id.passwordEditText);

        email = emailText.getText().toString();
        password = passwordText.getText().toString();


        // test account to not need the server
        if (email.equals("test")){
            //Send email to next activity to retrieve user info
            Intent i = new Intent(this, MenuActivity.class);
            i.putExtra(EMAIL_KEY, email);
            startActivity(i);
        }

        if(emailText.getText().toString().isEmpty() || passwordText.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), FIELD_MISSING, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(!checkPermissions()) {
            Toast toast = Toast.makeText(this, PERMISSION_FAILURE, Toast.LENGTH_SHORT);
            toast.show();
        }

        else {
            if (mServiceBound){
                if (deviceToken != null) {
                    mServerConnection.sendMessage(getString(R.string.ServerCommandRegister) + ":{\"email\":\"" + email + "\",\"password\":\"" + password + "\"," +
                            "\"deviceToken\":\"" + deviceToken + "\"}");
                } else {
                    deviceToken = FirebaseInstanceId.getInstance().getToken();
                    if (deviceToken != null) {
                        mServerConnection.sendMessage(getString(R.string.ServerCommandRegister) + ":{\"email\":\"" + email + "\",\"password\":\"" + password + "\"," +
                                "\"deviceToken\":\"" + deviceToken + "\"}");
                    }
                }
            } else {
                Toast toast = Toast.makeText(this, "Error connecting to server. Try again.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
}
