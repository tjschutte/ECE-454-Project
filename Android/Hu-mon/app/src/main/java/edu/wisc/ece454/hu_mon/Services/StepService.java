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

    private int backupSteps;
    private final double ALPHA = 0.8;
    private final double NOISE = .1;
    private final double STEP_THRESHOLD = 1.25;
    private double lastX, lastY, lastZ;
    private boolean firstStep;

    public StepService() {
    }

    @Override
    public void onCreate() {

        //setup data for accelerometer
        backupSteps = 0;
        firstStep = true;

        //setup sensor data
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null) {
            Log.d(TAG, "Using backup step counter");
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Step Service Started");

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            sensorManager.registerListener(this, stepSensor, 10000);
        }


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
            findHumon((int)steps);
        }
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //obtain acceleration values
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            double gravity[] = new double[3];

            //isolate gravity
            gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
            gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
            gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];

            //remove gravity from value
            x -= gravity[0];
            y -= gravity[1];
            z -= gravity[2];

            if(firstStep) {
                lastX = x;
                lastY = y;
                lastZ = z;

                firstStep = false;
            }
            else {

                //obtain the change in acceleration

                double deltaX = Math.abs(lastX - x);
                double deltaY = Math.abs(lastY - y);
                double deltaZ = Math.abs(lastZ - z);
                if (deltaX < NOISE) {
                    deltaX = (float) 0.0;
                }
                //else {
                lastX = x;
                //}

                if (deltaY < NOISE) {
                    deltaY = (float) 0.0;
                }
                //else {
                lastY = y;
                //}

                if (deltaZ < NOISE) {
                    deltaZ = (float) 0.0;
                }
                // else {
                lastZ = z;
                //}

                //check if enough force for step
                if((deltaX + deltaY + deltaZ) > STEP_THRESHOLD) {
                    backupSteps++;
                }
            }
            findHumon(backupSteps);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Checks for a Humon given the step data
    private void findHumon(int steps) {
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
