package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
        missingHumons = findMissingHumons(missingHumons);


        for(int i = 0; i < missingHumons.length; i++) {
            if(missingHumons[i].length() > 0) {
                downloadingHumons = true;
                serverConnection.sendMessage(context.getString(R.string.ServerCommandGetHumon) + ":{\"hID\":\"" + missingHumons[i] + "\"}");
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

    /*
    * Finds Humons missing from device to be downloaded from server
    *
     */
    private String [] findMissingHumons(String [] missingHumons) {

        String oldIndex = "";
        FileInputStream inputStream;
        FileOutputStream outputStream;
        JSONObject indexJSON = new JSONObject();
        JSONArray humonsArray;
        File indexFile = new File(context.getFilesDir(), user.getEmail() + context.getString(R.string.indexFile));

        //read in current index (if it exists)
        try {
            inputStream = new FileInputStream(indexFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            oldIndex = new String(buffer, "UTF-8");
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("No index currently exists for: ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //append new humons
        try {

            //append humon on to current object
            if(oldIndex.length() == 0) {
                return missingHumons;
            }
            else {
                indexJSON = new JSONObject(oldIndex);
                humonsArray = indexJSON.getJSONArray(context.getString(R.string.humonsKey));
            }

            for(int i = 0; i < humonsArray.length(); i++) {
                JSONObject dupCheck = new JSONObject(humonsArray.getString(i));

                //clear Humons already on device
                for(int j = 0; j < missingHumons.length; j++) {
                    if(dupCheck.getString("hID").equals(missingHumons[j])) {
                        missingHumons[j] = "";
                        break;
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return missingHumons;
    }
}
