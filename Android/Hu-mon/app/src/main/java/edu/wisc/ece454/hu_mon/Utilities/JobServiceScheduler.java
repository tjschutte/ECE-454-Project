package edu.wisc.ece454.hu_mon.Utilities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerSaveService;

public class JobServiceScheduler {

    public static void scheduleServerSaveJob(Context context, String[] humons, String[] user) {
        ComponentName serviceComponent = new ComponentName(context, ServerSaveService.class);
        JobInfo.Builder builder = new JobInfo.Builder(Integer.parseInt(context.getString(R.string.serverSaveJobId)), serviceComponent);
        builder.setMinimumLatency(10);  //wait 10 ms
        builder.setOverrideDeadline(500); //maximum of 0.5 seconds

        //bundle humons to be saved to pass to service
        PersistableBundle serviceBundle = new PersistableBundle();
        serviceBundle.putStringArray(context.getString(R.string.humonsKey), humons);
        serviceBundle.putStringArray(context.getString(R.string.userKey), user);
        builder.setExtras(serviceBundle);

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule((builder.build()));
    }
}
