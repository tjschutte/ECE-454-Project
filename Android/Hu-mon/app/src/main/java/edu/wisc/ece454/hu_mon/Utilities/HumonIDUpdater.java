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

/**
 * Created by Michael on 10/30/2017.
 */

//Saves a list of humons to the current users
//Required inputs: List of humons passed in constructor, filename to write to, email of user, context (obtained from activity or service),
//key from getString(R.string.humonsKey)
//Outputs a toast on success or failure
public class HumonIDUpdater extends AsyncTask<String, Integer, Boolean> {

    //file name for humon index
    private String iFilename = "";
    //file name for party
    private String pFilename = "";
    private String email = "";
    private String hName = "";
    private String hDescription = "";
    private Context context;
    private String HUMONS_KEY = "";

    public HumonIDUpdater(String iFilename, String pFilename, String email, String hName, String hDescription, Context context, String key) {
        this.iFilename = iFilename;
        this.pFilename = pFilename;
        this.email = email;
        this.hName = hName;
        this.hDescription = hDescription;
        this.context = context;
        HUMONS_KEY = key;
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
            System.out.println("No index currently exists for: " + email);
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
                    System.out.println(iFilename + " is empty.");
                    return false;
                } else {
                    indexJSON = new JSONObject(oldIndex);
                    humonsArray = indexJSON.getJSONArray(HUMONS_KEY);
                }

                //check that parameters match
                for (int j = 0; j < humonsArray.length(); j++) {
                    JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                    if (dupCheck.getString("name").equals(hName)) {
                        if (dupCheck.getString("uID").equals(email)) {
                            if (dupCheck.getString("description").equals(hDescription)) {
                                System.out.println("Updating " + dupCheck.getString("name") + " with HID:" + hIDs[0]);
                                dupCheck.put("hID", hIDs[0]);
                                humonsArray.remove(j);
                                humonsArray.put(dupCheck);
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
                    //System.out.println("Writing: " + indexJSON.toString());
                    System.out.println("Data written to: " + iFilename);
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
            System.out.println("No party currently exists for: " + email);
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
                    System.out.println(pFilename + " is empty.");
                    return goodSave;
                } else {
                    partyJSON = new JSONObject(oldParty);
                    humonsArray = partyJSON.getJSONArray(HUMONS_KEY);
                }

                //check that parameters match
                for (int j = 0; j < humonsArray.length(); j++) {
                    JSONObject dupCheck = new JSONObject(humonsArray.getString(j));
                    if (dupCheck.getString("hID").equals("0")) {
                        if (dupCheck.getString("uID").equals(email)) {
                            if (dupCheck.getString("description").equals(hDescription)) {
                                System.out.println("Updating " + dupCheck.getString("name") + " with HID:" + hIDs[0]);
                                dupCheck.put("hID", hIDs[0]);
                                String oldIID = dupCheck.getString("iID");
                                String startIID = oldIID.substring(0, oldIID.indexOf("-") + 1);
                                String endIID = oldIID.substring(oldIID.lastIndexOf("-"));
                                dupCheck.put("iID", startIID + hIDs[0] + endIID);
                                humonsArray.remove(j);
                                humonsArray.put(dupCheck);
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
                    //System.out.println("Writing: " + indexJSON.toString());
                    System.out.println("Data written to: " + pFilename);
                    outputStream = new FileOutputStream(partyFile);
                    outputStream.write(partyJSON.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
}
