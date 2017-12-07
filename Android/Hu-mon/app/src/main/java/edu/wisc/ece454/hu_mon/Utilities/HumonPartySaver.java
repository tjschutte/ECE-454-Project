package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.R;

/**
 * Created by Michael on 10/30/2017.
 */

//Saves a list of humons to the current users
//Required inputs: List of humons passed in constructor, filename to write to, email of user, context (obtained from activity or service),
//key from getString(R.string.humonsKey)
//Outputs a toast on success or failure
public class HumonPartySaver extends AsyncTask<Humon, Integer, Boolean> {

    private Context context;
    private boolean isEnemy;

    public HumonPartySaver(Context context, boolean isEnemy) {
        this.context = context;
        this.isEnemy = isEnemy;
    }

    @Override
    protected Boolean doInBackground(Humon... humons) {
        boolean goodSave = true;

        String oldParty = "";
        FileInputStream inputStream;
        FileOutputStream outputStream;
        JSONObject partyJSON = new JSONObject();
        JSONArray humonsArray;
        String HUMONS_KEY = context.getString(R.string.humonsKey);

        //Obtain file name
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(context.getString(R.string.emailKey), "");
        String filename = userEmail + context.getString(R.string.partyFile);
        if(isEnemy) {
            filename = context.getString(R.string.enemyPartyFile);
        }
        File partyFile = new File(context.getFilesDir(), filename);

        //read in current party (if it exists)
        try {
            inputStream = new FileInputStream(partyFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            oldParty= new String(buffer, "UTF-8");
            //System.out.println("Current humon index: " + oldIndex);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("No party currently exists for: " + userEmail);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //update and append new humons
        try {

            //append humon on to current object
            if(oldParty.length() == 0) {
                partyJSON = new JSONObject();
                humonsArray = new JSONArray();
            }
            else {
                partyJSON = new JSONObject(oldParty);
                humonsArray = partyJSON.getJSONArray(HUMONS_KEY);
            }

            for(int i = 0; i < humons.length; i++) {

                //check if humon is already in party (and is being updated)
                for(int j = 0; j < humonsArray.length(); j++) {
                    JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                    if(dupCheck.getString("iID").equals(humons[i].getiID())) {
                        //remove humon so it is updated
                        humonsArray.remove(j);
                        break;
                    }
                }

                System.out.println("Saving party humon: " + humons[i].getiID());
                humonsArray.put(humons[i].toJson(new ObjectMapper()));

            }

            partyJSON.put(HUMONS_KEY, humonsArray);

        } catch (Exception e) {
            e.printStackTrace();
            goodSave = false;
        }


        try {
            //write object to file
            //System.out.println("Writing: " + indexJSON.toString());
            System.out.println("Data written to: " + filename);
            outputStream = new FileOutputStream(partyFile);
            outputStream.write(partyJSON.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return goodSave;
    }

    protected void onPostExecute(Boolean result) {
        if(result) {
            Toast toast = Toast.makeText(context, "Party Successfully Updated", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(context, "Party Update Failed", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
