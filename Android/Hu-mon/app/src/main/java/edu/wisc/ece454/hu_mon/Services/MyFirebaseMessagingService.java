package edu.wisc.ece454.hu_mon.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import edu.wisc.ece454.hu_mon.Activities.MenuActivity;
import edu.wisc.ece454.hu_mon.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received. Notification messages are only received here in onMessageReceived when the app
     * is in the foreground
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data;
            data = remoteMessage.getData();

            if (data.containsKey("FRIEND-REQUEST")) {
                // TODO: Add this to the user object as a pending friend request
                System.out.println(data);

            } else if (data.containsKey("BATTLE-REQUEST")) {
                //TODO: Something with pending intents to accept / decline
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                Intent notificationIntent = new Intent(this, MenuActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Notification notification = new Notification.Builder(this)
                        .setContentTitle("Battle Request")
                        .setContentText("It's t-t-t-t-time to duel!")
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal_background)
                        .addAction(R.drawable.common_google_signin_btn_icon_dark_normal, "I GOT THIS", null) // #0
                        .addAction(R.drawable.common_google_signin_btn_icon_dark_normal, "I DON'T GOT THIS", null)  // #1
                        .build();

                mNotificationManager.notify(69, notification);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        // TODO: Show foreground notification that doesnt have data payload
    }

    /**
     * Create and show a simple notification containing the received FCM message
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        // TODO: Find if / where this is used
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Not sure yet")
                .setContentText(messageBody)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal_background)
                .build();

        mNotificationManager.notify(69, notification);
    }
}