package edu.wisc.ece454.hu_mon.Services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import edu.wisc.ece454.hu_mon.Activities.MenuActivity;
import edu.wisc.ece454.hu_mon.Activities.OnlineBattleActivity;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.ServerBroadcastReceiver;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final int DELAY_MIN = 3;
    private final int SEC_IN_MIN = 60;
    private final int MILISEC_IN_SEC = 1000;

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

            // Friend request
            if (data.containsKey(getString(R.string.ServerCommandFriendRequest))) {
                String res = getString(R.string.ServerCommandFriendRequest) + ": " + data.get(getString(R.string.ServerCommandFriendRequest));

                System.out.println("Faking a server message for a friend request");
                Intent intent = new Intent();
                intent.setAction(getString(R.string.serverBroadCastEvent));
                intent.putExtra(getString(R.string.serverBroadCastResponseKey),res);
                sendBroadcast(intent);

            } else if (data.containsKey(getString(R.string.ServerCommandBattleRequest))) {
                //TODO: Something with pending intents to accept / decline
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                StatusBarNotification[] curNotifications = mNotificationManager.getActiveNotifications();
                int id;
                if (curNotifications != null) {
                    id = curNotifications.length + 1;
                } else {
                    id = 1;
                }

                //Intent notificationIntent = new Intent(this, MenuActivity.class);
                Intent notificationIntent = new Intent(this, OnlineBattleActivity.class);
                String enemyEmail = data.get(getString(R.string.ServerCommandBattleRequest));
                System.out.println("Battle Request sent by: " + enemyEmail);
                notificationIntent.putExtra(getString(R.string.emailKey), enemyEmail);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Intent launchIntent = new Intent(this, OnlineBattleActivity.class);
                PendingIntent acceptIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

                Notification notification = new Notification.Builder(this)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody()) // TODO: Fill these in from the data from server
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal_background)
                        .addAction(R.drawable.common_google_signin_btn_icon_dark_normal, "Accept", acceptIntent)
                        .build();

                // Cancel any other battle requests.
                mNotificationManager.cancel(id);

                mNotificationManager.notify(id, notification);

                // set up alarm
                AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), ServerBroadcastReceiver.class);
                intent.setAction("CANCEL_NOTIFICATION");
                intent.putExtra("notification_id", id);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Notification only lives for 1 minute
                long currentTime = System.currentTimeMillis();
                long alarmDelay = DELAY_MIN * SEC_IN_MIN * MILISEC_IN_SEC;
                long alarmTrigger = currentTime + alarmDelay;

                alarmManager.setExact(AlarmManager.RTC, alarmTrigger, pi);
            } else if (data.containsKey(getString(R.string.ServerCommandBattleAction))) {
                // Player is in a battle, and their opponent just did something.
                // TODO: Decide how to handlet this
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