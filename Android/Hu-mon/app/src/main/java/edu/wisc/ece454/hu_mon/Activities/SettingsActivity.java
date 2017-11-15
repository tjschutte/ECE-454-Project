package edu.wisc.ece454.hu_mon.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import edu.wisc.ece454.hu_mon.R;

/**
 * Created by Michael on 11/9/2017.
 */

//Custom Activity to replace AppCompatActivity
//Provides access to a settings menu for all Activities
public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.settings_menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logoutItem:
                System.out.println("Logging out");
                logOutUser();
                break;
            default:
                System.out.println("Error: Bad settings item touched");
        }
        return true;
    }

    //Resets user to initial launch state
    private void logOutUser() {
        //Load intent for login screen
        Intent restartIntent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );

        //Close all previous activities and restart state of login activity
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restartIntent);
    }

}
