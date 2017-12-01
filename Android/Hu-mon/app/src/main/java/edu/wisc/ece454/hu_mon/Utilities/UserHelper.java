package edu.wisc.ece454.hu_mon.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;

public class UserHelper {

    public static User loadUser(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferencesFile),
                Context.MODE_PRIVATE);
        // User object reader.
        try {
            String userString = sharedPref.getString(context.getString(R.string.userObjectKey), null);
            System.out.println("User String was: " + userString);
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
}
