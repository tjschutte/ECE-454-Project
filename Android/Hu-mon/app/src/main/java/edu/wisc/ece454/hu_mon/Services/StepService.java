package edu.wisc.ece454.hu_mon.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.RequiresApi;

import edu.wisc.ece454.hu_mon.Activities.WildBattleActivity;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Utilities.StepJobScheduler;

@RequiresApi(api = 23)
public class StepService extends JobService implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private JobParameters jobParameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        StepJobScheduler.scheduleJob(getApplicationContext());
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        System.out.println("Step Job Started");

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
            System.out.println("Steps: " + steps);
            if(steps % 10 == 0) {
                double humonFind = Math.random();
                if(humonFind < .2) {
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
