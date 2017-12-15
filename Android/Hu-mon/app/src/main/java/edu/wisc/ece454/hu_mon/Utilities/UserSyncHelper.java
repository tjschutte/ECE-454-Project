package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;

public class UserSyncHelper extends AsyncTask<User, Integer, Boolean> {

    User user;
    Context context;
    ServerConnection serverConnection;
    private boolean downloadingHumons;

    public UserSyncHelper (Context context, User user, ServerConnection serverConnection) {
        this.context = context;
        this.user = user;
        this.serverConnection = serverConnection;
        downloadingHumons = false;
    }

    @Override
    protected Boolean doInBackground(User... users) {

        //determine Humons to download
        String []missingHumons = user.getEncounteredHumons().toArray(new String[user.getEncounteredHumons().size()]);
        missingHumons = UserHelper.findMissingHumons(missingHumons, context);


        if(missingHumons != null) {
            for (int i = 0; i < missingHumons.length; i++) {
                if(missingHumons[i] != null) {
                    if (missingHumons[i].length() > 0) {
                        downloadingHumons = true;
                        serverConnection.sendMessage(context.getString(R.string.ServerCommandGetHumon) + ":{\"hID\":\"" + missingHumons[i] + "\"}");
                    }
                }
            }
        }

        /*for (String hID : user.getEncounteredHumons()) {
            serverConnection.sendMessage(context.getString(R.string.ServerCommandGetHumon) + ":{\"hID\":\"" + hID + "\"}");
        }*/
        for (String iID : user.getParty()) {
            serverConnection.sendMessage(context.getString(R.string.ServerCommandGetInstance) + ":{\"iID\":\"" + iID + "\"}");
        }
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if(downloadingHumons) {
            Toast toast = Toast.makeText(context, "Downloading Humons to Index! Please Wait!", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(context, "Hu-mon Index Up-to-date!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
