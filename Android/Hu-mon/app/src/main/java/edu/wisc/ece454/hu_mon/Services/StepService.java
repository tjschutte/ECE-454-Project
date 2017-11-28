package edu.wisc.ece454.hu_mon.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import edu.wisc.ece454.hu_mon.Activities.WildBattleActivity;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.JobServiceScheduler;

@RequiresApi(api = 23)
public class StepService extends JobService implements SensorEventListener {

    private static final String TAG = "StepService";

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private JobParameters jobParameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        JobServiceScheduler.scheduleStepJob(getApplicationContext());
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        Log.i(TAG,"Step Job Started");

        sensorManager.registerListener(this, stepSensor, 10000);
        jobParameters = params;

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        jobFinished(jobParameters, false);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            double steps = event.values[0];
            Log.d(TAG,"Steps: " + steps);
            if(steps % 10 == 0) {
                double humonFind = Math.random();
                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
                if(humonFind < .2 && sharedPref.getBoolean("inHealthyPlace", false)) {
                    System.out.println("Wild hu-mon found!");
                    wildHumonNotification();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }

    //Notifies the user of a wild humon
    private void wildHumonNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, WildBattleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Battle")
                .setContentText("A Wild Hu-mon appeared")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal_background)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(69, notification);


    }
}
