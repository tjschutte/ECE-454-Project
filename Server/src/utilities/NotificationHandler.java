package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

import main.Global;

public class NotificationHandler {
	
	private static final String API_KEY = "AAAAL2PRmqE:APA91bEQ0DxLElgVjsYzuRqi1gyN1r5ly7Ri8ijoqJVDuyvS67ChgpRqCEYTl4uT0kyGpLDc0_p_4VSXLMKDFxdHtPzABt3NJ6NERkLsmM7J-U4Gfc0M0jVCsKGpvgodVcAAmYmfUsLE ";
	private static final String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

	/**
	 * Sends a push notification to a single device.
	 * @param deviceToken - The firebase device ID
	 * @param notificationTitle - the title of the notification to use
	 * @param notificationBody - the message body
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void sendPushNotification(String deviceToken, String notificationTitle, String notificationBody, JSONObject data) 
			throws IOException, JSONException {
        URL url = new URL(API_URL_FCM);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
 
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "key=" + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
 
        JSONObject json = new JSONObject();
 
        json.put("to", deviceToken.trim());
        JSONObject info = new JSONObject();
        info.put("title", notificationTitle);
        info.put("body", notificationBody);
        json.put("notification", info);
        if (data != null) {
        	json.put("data", data);
        }
        try {
            OutputStreamWriter wr = new OutputStreamWriter(
                    conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
 
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
 
            String output;
            while ((output = br.readLine()) != null) {
                Global.log(-1, "NOTIFICATION DEBUG: " + output);
            }
            
            br.close();
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
 
	}

}
