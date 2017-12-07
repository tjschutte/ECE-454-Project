package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.wisc.ece454.hu_mon.R;

/**
 * Created by Michael on 10/30/2017.
 */

//Heals all humons in party by percentage of max health (Integer should be from 0 to 100)
//Required inputs: Integer value of percent to heal, filename to write to, email of user, context (obtained from activity or service),
//key from getString(R.string.humonsKey)
//Outputs a toast on success or failure
public class HumonHealTask extends AsyncTask<Integer, Integer, Boolean> {

    private Context context;

    public HumonHealTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Integer... healing) {
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

            //Heal humons in party
            for(int j = 0; j < humonsArray.length(); j++) {
                JSONObject humon = new JSONObject(humonsArray.getString(j));
                int maxHealth = humon.getInt("health");
                int hp = humon.getInt("hp");
                int amountHealing = (int) ((healing[0] / 100) * maxHealth);
                if(amountHealing < 1) {
                    amountHealing = 1;
                }
                hp += amountHealing;
                if(hp > maxHealth) {
                    hp = maxHealth;
                }
                humon.put("hp", hp);
                //write updated humon to party
                //humonsArray.remove(j);
                humonsArray.put(j, humon);
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
            System.out.println("Party Successfully Healed");
        }
        else {
            System.out.println("Party Healing Failed");
        }
    }
}
