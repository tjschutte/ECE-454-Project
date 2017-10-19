package edu.wisc.ciancimino.hu_mon;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private final String SIGN_IN_FAILURE = "Sign in Failed";
    private final String SIGN_IN_SUCCESS = "Sign in Succeeded";
    private final String REGISTER_FAILURE = "Register Failed";
    private final String REGISTER_SUCCESS = "Register Succeeded";
    private final String PERMISSION_FAILURE = "Must Allow Permissions to Proceed";
    private String EMAIL_KEY;
    private final String ACTIVITY_TITLE = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        setTitle(ACTIVITY_TITLE);

        EMAIL_KEY = getString(R.string.emailKey);
        checkPermissions();
    }

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

        return hasPermissions;
    }

    //Called when hitting the sign in button
    //Retrieves EditText values and attempts to sign in account
    //On success, changes to menu activity
    public void signInAccount(View view) {
        EditText emailText = (EditText) findViewById(R.id.emailEditText);
        EditText passwordText = (EditText) findViewById(R.id.passwordEditText);

        if(emailText.getText().toString().isEmpty() || passwordText.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(this, SIGN_IN_FAILURE, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(!checkPermissions()) {
            Toast toast = Toast.makeText(this, PERMISSION_FAILURE, Toast.LENGTH_SHORT);
            toast.show();
        }
        //TODO: Send email/password to server
        else {
            Toast toast = Toast.makeText(this, SIGN_IN_SUCCESS, Toast.LENGTH_SHORT);
            toast.show();

            //Send email to next activity to retrieve user info
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra(EMAIL_KEY, emailText.getText().toString());
            startActivity(intent);
        }

    }

    //Called when hitting the register button
    //Retrieves EditText values and attempts to register a new account
    //On success, changes to menu activity
    //TODO: This can map to a new activity if we want more info (Screen name, User info)
    public void registerAccount(View view) {
        EditText emailText = (EditText) findViewById(R.id.emailEditText);
        EditText passwordText = (EditText) findViewById(R.id.passwordEditText);

        if(emailText.getText().toString().isEmpty() || passwordText.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), REGISTER_FAILURE, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(!checkPermissions()) {
            Toast toast = Toast.makeText(this, PERMISSION_FAILURE, Toast.LENGTH_SHORT);
            toast.show();
        }
        //TODO: Send email/password to server
        else {
            Toast toast = Toast.makeText(getApplicationContext(), REGISTER_SUCCESS, Toast.LENGTH_SHORT);
            toast.show();

            //Send email to next activity to retrieve user info
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra(EMAIL_KEY, emailText.getText().toString());
            startActivity(intent);
        }

    }
}
