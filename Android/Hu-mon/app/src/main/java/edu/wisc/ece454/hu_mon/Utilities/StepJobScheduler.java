package edu.wisc.ece454.hu_mon.Utilities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.RequiresApi;

import edu.wisc.ece454.hu_mon.Services.StepService;

/**
 * Created by Michael on 10/25/2017.
 */

public class StepJobScheduler {
    @RequiresApi(api = 23)
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, StepService.class);
        JobInfo.Builder builder = new JobInfo.Builder(1, serviceComponent);
        builder.setMinimumLatency(10000);   //wait 10 seconds
        builder.setOverrideDeadline(30000); //maximum of 30 seconds
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule((builder.build()));
    }
}
