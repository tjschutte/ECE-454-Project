package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;

public class UserHelper {

    private static final String TAG = "USERHELPER";

    public static User loadUser(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        // User object reader.
        try {
            String userString = sharedPref.getString(context.getString(R.string.userObjectKey), null);
            Log.d(TAG, "User String was: " + userString);
            User user = new ObjectMapper().readValue(userString, User.class);
            return user;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveUser(Context context, User user) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        user.dataCleaner();

        try {
            editor.putString(context.getString(R.string.userObjectKey), user.toJson(new ObjectMapper()));
            editor.commit();
        } catch (JsonProcessingException e) {
            // idk yet
        }
    }

    public static User objFromString(String user) {
        try {
            return new ObjectMapper().readValue(user, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Save all humons in party to server (happens on sign out and destruction of app)
    public static void saveToServer(Context context) {

        User user = loadUser(context);
        String[] saveHumons = null;

        if (user.getHcount() > 0) {
            saveEncounteredHumons(user, context);
            saveHumons = saveParty(user, context);
        }
        Log.i(TAG, "Encountered humons saved: " + user.getEncounteredHumons().size()
                + " Party humons saved: " + user.getParty().size());

        String[] saveUser;
        try {
            saveUser = new String[] { user.toJson(new ObjectMapper()) };
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            saveUser = new String[] {""};
        }

        // Save user and party.
        JobServiceScheduler.scheduleServerSaveJob(context, saveHumons, saveUser);

    }

    /*
     * Reads all of the party humons and returns them.
     * Updates the user object with iIDs.
     *
     * @param user      user object to be updated
     * @return saveHumons   list of Humons in party saved as JSON strings
     */
    private static String[] saveParty(User user, Context context) {

        Log.i(TAG, "In saveParty");
        //read in current party(if it exists)
        boolean hasPartyFile = true;
        FileInputStream inputStream;
        String oldParty = "";
        File partyFile = new File(context.getFilesDir(), user.getEmail() + context.getString(R.string.partyFile));
        JSONObject partyJSON;
        JSONArray humonsArray;
        String[] saveHumons = null;

        try {
            inputStream = new FileInputStream(partyFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            oldParty = new String(buffer, "UTF-8");
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "No party currently exists for: " + user.getEmail());
            hasPartyFile = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(hasPartyFile) {
            //update HID in party
            try {
                //append humon on to current object
                if (oldParty.length() == 0) {
                    Log.i(TAG, "Party is empty.");
                    return saveHumons;
                } else {
                    partyJSON = new JSONObject(oldParty);
                    humonsArray = partyJSON.getJSONArray(context.getString(R.string.humonsKey));
                }

                saveHumons = new String[humonsArray.length()];
                Log.i(TAG, "Party humons in file: " + humonsArray.length());

                //store all humons as JSON strings to pass to service
                for (int j = 0; j < humonsArray.length(); j++) {
                    JSONObject humonJSON = new JSONObject(humonsArray.getString(j));
                    humonJSON.put("imagePath", "");
                    saveHumons[j] = humonJSON.toString();
                    user.addPartyMember(humonJSON.getString("iID"));
                    Log.i(TAG, "Updated user");
                    Log.i(TAG, humonJSON.getString("iID"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Log.i(TAG, "Unable to find party file");
        }
        return saveHumons;
    }

    /*
     * Reads all of the encountered humons and adds the hIDs to user.
     *
     * @param user      user object to be updated
     */
    private static void saveEncounteredHumons(User user, Context context) {

        Log.i(TAG, "In saveEncounteredHumons");
        //read in encountered humons
        boolean hasIndexFile = true;
        FileInputStream inputStream;
        String oldIndex = "";
        File indexFile = new File(context.getFilesDir(), user.getEmail() + context.getString(R.string.indexFile));
        JSONObject indexJSON;
        JSONArray humonsArray;

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
            Log.i(TAG, "No index currently exists for: " + user.getEmail());
            hasIndexFile = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(hasIndexFile) {
            //update HID in party
            try {
                //append humon on to current object
                if (oldIndex.length() == 0) {
                    Log.i(TAG, "Index is empty.");
                    return;
                } else {
                    indexJSON = new JSONObject(oldIndex);
                    humonsArray = indexJSON.getJSONArray(context.getString(R.string.humonsKey));
                }

                Log.i(TAG, "Index humons in file: " + humonsArray.length());

                //update user object with hIDs
                for (int j = 0; j < humonsArray.length(); j++) {
                    JSONObject humonJSON = new JSONObject(humonsArray.getString(j));
                    user.addEncounteredHumon(humonJSON.getString("hID"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Log.i(TAG, "Unable to find index file");
        }
    }
}
