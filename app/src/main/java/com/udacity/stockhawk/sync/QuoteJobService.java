package com.udacity.stockhawk.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class QuoteJobService extends JobService {

    public static final String ACTION_JOB_STARTED = "com.udacity.stockhawk.sync.JOB_STARTED";

    @Override
    public boolean onStartJob(JobParameters jobParameters)
    {
        if(jobParameters.getJobId() == QuoteSyncJob.ONE_OFF_ID)
        {
            Intent intent = new Intent();
            intent.setAction(ACTION_JOB_STARTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        Intent nowIntent = new Intent(getApplicationContext(), QuoteIntentService.class);
        getApplicationContext().startService(nowIntent);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }


}
