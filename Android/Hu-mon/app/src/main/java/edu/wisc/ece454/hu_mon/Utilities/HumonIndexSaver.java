package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
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

/**
 * Created by Michael on 10/30/2017.
 */

//Saves a list of humons to the current users
//Required inputs: List of humons passed in constructor, filename to write to, email of user, context (obtained from activity or service),
    //key from getString(R.string.humonsKey)
//Outputs a toast on success or failure
public class HumonIndexSaver extends AsyncTask<Humon, Integer, Boolean> {

    private String filename = "";
    private String email = "";
    private Context context;
    private String HUMONS_KEY = "";
    private boolean isSync;

    public HumonIndexSaver(String filename, String email, Context context, String key, boolean isSync) {
        this.filename = filename;
        this.email = email;
        this.context = context;
        HUMONS_KEY = key;
        this.isSync = isSync;
    }

    @Override
    protected Boolean doInBackground(Humon... humons) {
        boolean goodSave = true;

        String oldIndex = "";
        FileInputStream inputStream;
        FileOutputStream outputStream;
        JSONObject indexJSON = new JSONObject();
        JSONArray humonsArray;
        File indexFile = new File(context.getFilesDir(), filename);

        //read in current index (if it exists)
        try {
            inputStream = new FileInputStream(indexFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            oldIndex = new String(buffer, "UTF-8");
            //System.out.println("Current humon index: " + oldIndex);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("No index currently exists for: " + email);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //append new humons
        try {

            //append humon on to current object
            if(oldIndex.length() == 0) {
                indexJSON = new JSONObject();
                humonsArray = new JSONArray();
            }
            else {
                indexJSON = new JSONObject(oldIndex);
                humonsArray = indexJSON.getJSONArray(HUMONS_KEY);
            }

            if(isSync) {
                for(int i = 0; i < humons.length; i++) {
                    //check if humon should be updated
                    for(int j = 0; j < humonsArray.length(); j++) {
                        JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                        if(dupCheck.getInt("hID") == humons[i].gethID()) {
                            //update the old humon
                            humonsArray.remove(j);
                            break;
                        }
                    }

                    humonsArray.put(humons[i].toJson(new ObjectMapper()));
                }
            }
            else {
                for(int i = 0; i < humons.length; i++) {
                    //check that name is not duplicated (for user's email only)
                    for(int j = 0; j < humonsArray.length(); j++) {
                        JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                        if(dupCheck.getString("name").equals(humons[i].getName())) {
                            if(dupCheck.getString("uID").equals(email)) {
                                if(dupCheck.getString("description").equals(humons[i].getDescription())) {
                                    return false;
                                }
                            }
                        }
                    }
                    humonsArray.put(humons[i].toJson(new ObjectMapper()));
                }
            }
            indexJSON.put(HUMONS_KEY, humonsArray);

        } catch (Exception e) {
            e.printStackTrace();
            goodSave = false;
        }


        try {
            //write object to file
            //System.out.println("Writing: " + indexJSON.toString());
            System.out.println("Data written to: " + filename);
            outputStream = new FileOutputStream(indexFile);
            outputStream.write(indexJSON.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return goodSave;
    }

    protected void onPostExecute(Boolean result) {
        if(result) {
            Toast toast = Toast.makeText(context, "Hu-mon Successfully Created", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(context, "Hu-mon Creation Failed", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
