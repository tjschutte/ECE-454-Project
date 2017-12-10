package edu.wisc.ece454.hu_mon.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import edu.wisc.ece454.hu_mon.Activities.WildBattleActivity;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.HumonHealTask;

public class StepService extends Service implements SensorEventListener {

    private static final String TAG = "StepService";

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Intent notificationIntent;

    public StepService() {
    }

    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Step Service Started");

        sensorManager.registerListener(this, stepSensor, 10000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Step Service Killed");
        sensorManager.unregisterListener(this, stepSensor);
    }

    @Override
    //Not supported
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            double steps = event.values[0];
            Log.d(TAG,"Steps: " + steps);
            if(steps % 10 == 0) {

                //heal humons
                AsyncTask<Integer, Integer, Boolean> healHumonTask = new HumonHealTask(getApplicationContext());
                healHumonTask.execute(1);

                double humonFind = Math.random();
                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
                boolean gameRunning = sharedPref.getBoolean(getString(R.string.gameRunningKey), false);
                boolean searchCheat = sharedPref.getBoolean(getString(R.string.searchCheatKey), false);
                Log.d(TAG, "Step Service: game running: " + gameRunning);

                //find humons with no cheats
                if(humonFind < .2 && (sharedPref.getBoolean(getString(R.string.healthyPlaceKey), false) || searchCheat)
                        && !gameRunning) {
                    Log.d(TAG, "Wild hu-mon found!");
                    wildHumonNotification();
                }

                //debug only
                String userEmail = sharedPref.getString(getString(R.string.emailKey), "");
                if((userEmail.equals("test")  || userEmail.equals("dev")) && humonFind < 1 &&
                        !gameRunning) {
                    Log.d(TAG, "Wild hu-mon found!");
                    wildHumonNotification();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Notifies the user of a wild humon
    private void wildHumonNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationIntent = new Intent(this, WildBattleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Battle")
                .setContentText("A Wild Hu-mon appeared")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(69, notification);


    }
}
