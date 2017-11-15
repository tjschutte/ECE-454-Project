package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserObjectSaver extends AsyncTask<String, String, String> {

    private String filename = "";
    private Context context;

    public UserObjectSaver(String filename, Context context) {
        this.filename = filename;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... user) {
        System.out.println("Saving user data to file");
        System.out.println(user[0]);
        try {
            FileOutputStream outputStream;
            JSONObject userJSON = new JSONObject(user[0]);
            File userFile = new File(context.getFilesDir(), filename);
            outputStream = new FileOutputStream(userFile);
            outputStream.write(userJSON.toString().getBytes());
            outputStream.close();

        } catch (JSONException e) {
            e.printStackTrace();
            return "Failed to save user";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "Failed to save user";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to save user";
        }
        return "Saved User: " + user[0];
    }

    protected void onPostExecute(String result) {
        System.out.println(result);
    }
}
