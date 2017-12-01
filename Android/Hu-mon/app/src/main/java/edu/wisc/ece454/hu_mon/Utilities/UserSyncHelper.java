package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.os.AsyncTask;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;

public class UserSyncHelper extends AsyncTask<User, Integer, Boolean> {

    User user;
    Context context;
    ServerConnection serverConnection;

    public UserSyncHelper (Context context, User user, ServerConnection serverConnection) {
        this.context = context;
        this.user = user;
        this.serverConnection = serverConnection;
    }

    @Override
    protected Boolean doInBackground(User... users) {
        for (String hID : user.getEncounteredHumons()) {
            serverConnection.sendMessage(context.getString(R.string.ServerCommandGetHumon) + ":{\"hID\":\"" + hID + "\"}");
        }
        for (String iID : user.getParty()) {
            serverConnection.sendMessage(context.getString(R.string.ServerCommandGetHumon) + ":{\"iID\":\"" + iID + "\"}");
        }
        return true;
    }

    protected void onPostExecute(Boolean result) {

    }
}
