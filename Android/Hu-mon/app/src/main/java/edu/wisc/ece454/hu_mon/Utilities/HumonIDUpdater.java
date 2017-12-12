package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;

/**
 * Created by Michael on 10/30/2017.
 */

//Saves a list of humons to the current users
//Required inputs: List of humons passed in constructor, filename to write to, email of user, context (obtained from activity or service),
//key from getString(R.string.humonsKey)
//Outputs a toast on success or failure
public class HumonIDUpdater extends AsyncTask<String, Integer, Boolean> {

    private final String TAG = "HIDUPDATER";
    //file name for humon index
    private String iFilename = "";
    //file name for party
    private String pFilename = "";
    private String email = "";
    private String hName = "";
    private String hDescription = "";
    private Context context;
    private String HUMONS_KEY = "";
    private File oldImageFile;
    private File newImageFile;

    public HumonIDUpdater(Context context, String hName, String hDescription) {
        this.context = context;
        this.hName = hName;
        this.hDescription = hDescription;

        //retrieve email of the user
        SharedPreferences sharedPref = this.context.getSharedPreferences(
                this.context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        this.email = sharedPref.getString(this.context.getString(R.string.emailKey), "");

        //Set filenames for index and party
        this.iFilename = this.email + this.context.getString(R.string.indexFile);
        this.pFilename = this.email + this.context.getString(R.string.partyFile);
        HUMONS_KEY = this.context.getString(R.string.humonsKey);

    }

    @Override
    protected Boolean doInBackground(String... hIDs) {
        boolean goodSave = true;

        String oldIndex = "";
        String oldParty = "";
        FileInputStream inputStream;
        FileOutputStream outputStream;
        JSONObject indexJSON = new JSONObject();
        JSONObject partyJSON = new JSONObject();
        JSONArray humonsArray;
        File indexFile = new File(context.getFilesDir(), iFilename);
        File partyFile = new File(context.getFilesDir(), pFilename);

        //read in current index (if it exists)
        boolean hasIndexFile = true;
        try {
            inputStream = new FileInputStream(indexFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            oldIndex = new String(buffer, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "No index currently exists for: " + email);
            hasIndexFile = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(hasIndexFile) {
            //update HID in index
            boolean foundHumon = false;
            try {
                //append humon on to current object
                if (oldIndex.length() == 0) {
                    Log.d(TAG, iFilename + " is empty.");
                    return false;
                } else {
                    indexJSON = new JSONObject(oldIndex);
                    humonsArray = indexJSON.getJSONArray(HUMONS_KEY);
                }

                //check that parameters match
                for (int j = humonsArray.length() - 1; j >= 0; j--) {
                    JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                    if (dupCheck.getString("name").equals(hName)) {
                        if (dupCheck.getString("uID").equals(email)) {
                            if (dupCheck.getString("description").equals(hDescription)) {
                                Log.d(TAG, "Updating " + dupCheck.getString("name") + " with HID:" + hIDs[0]);
                                dupCheck.put("hID", hIDs[0]);

                                //rename image file to hID
                                oldImageFile = new File(dupCheck.getString("imagePath"));
                                //Log.d(TAG, "Old image path: " + oldImageFile.getPath());
                                newImageFile = new File(context.getFilesDir(), hIDs[0] + ".jpg");
                                oldImageFile.renameTo(newImageFile);
                                //Log.d(TAG, "New image path: " + newImageFile.getPath());
                                dupCheck.put("imagePath", newImageFile.getPath());

                                //humonsArray.remove(j);
                                humonsArray.put(j, dupCheck);
                                foundHumon = true;
                                break;
                            }
                        }
                    }
                }
                indexJSON.put(HUMONS_KEY, humonsArray);
            } catch (Exception e) {
                e.printStackTrace();
                goodSave = false;
            }


            if(foundHumon) {
                try {
                    //write object to file
                    //Log.d(TAG, "Writing: " + indexJSON.toString());
                    Log.d(TAG, "Data written to: " + iFilename);
                    outputStream = new FileOutputStream(indexFile);
                    outputStream.write(indexJSON.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //read in current party(if it exists)
        boolean hasPartyFile = true;
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
            Log.d(TAG, "No party currently exists for: " + email);
            hasPartyFile = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(hasPartyFile) {
            boolean foundHumon = false;
            //update HID in party
            try {
                //append humon on to current object
                if (oldParty.length() == 0) {
                    Log.d(TAG, pFilename + " is empty.");
                    return goodSave;
                } else {
                    partyJSON = new JSONObject(oldParty);
                    humonsArray = partyJSON.getJSONArray(HUMONS_KEY);
                }

                //check that parameters match
                for (int j = humonsArray.length() - 1; j >= 0; j--) {
                    JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                    if (dupCheck.getString("hID").equals("0")) {
                        if (dupCheck.getString("uID").equals(email)) {
                            if (dupCheck.getString("description").equals(hDescription)) {
                                Log.d(TAG, "Updating " + dupCheck.getString("name") + " with HID:" + hIDs[0]);
                                dupCheck.put("hID", hIDs[0]);

                                //rename path to new image file
                                if(newImageFile != null) {
                                    dupCheck.put("imagePath", newImageFile.getPath());
                                }

                                //humonsArray.remove(j);
                                humonsArray.put(j, dupCheck);
                                foundHumon = true;
                                break;
                            }
                        }
                    }
                }

                partyJSON.put(HUMONS_KEY, humonsArray);
            } catch (Exception e) {
                e.printStackTrace();
                goodSave = false;
            }

            if(foundHumon) {
                try {
                    //write object to file
                    //Log.d(TAG, "Writing: " + indexJSON.toString());
                    Log.d(TAG, "Data written to: " + pFilename);
                    outputStream = new FileOutputStream(partyFile);
                    outputStream.write(partyJSON.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String []saveHumons = saveParty(context);
        User user = UserHelper.loadUser(context);
        try {
            String [] saveUser = new String[] { user.toJson(new ObjectMapper()) };
            // Save user and party.
            JobServiceScheduler.scheduleFastServerSaveJob(context, saveHumons, saveUser);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return goodSave;
    }

    protected void onPostExecute(Boolean result) {
        if(result) {
            Toast toast = Toast.makeText(context, "Hu-mon HID Successfully Updated", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(context, "Hu-mon HID Update Failed", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /*
    * Reads all of the party humons and returns them.
    * Updates the user object with iIDs.
    *
    * @param user      user object to be updated
    * @return saveHumons   list of Humons in party saved as JSON strings
    */
    private String[] saveParty(Context context) {

        Log.i(TAG, "In saveParty");
        //read in current party(if it exists)
        boolean hasPartyFile = true;
        FileInputStream inputStream;
        String oldParty = "";
        //retrieve email of the user
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(context.getString(R.string.emailKey), "");
        File partyFile = new File(context.getFilesDir(), userEmail + context.getString(R.string.partyFile));
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
            Log.i(TAG, "No party currently exists for: " + userEmail);
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
}
