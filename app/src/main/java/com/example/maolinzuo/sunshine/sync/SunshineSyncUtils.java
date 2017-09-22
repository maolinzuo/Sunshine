package com.example.maolinzuo.sunshine.sync;

import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.CursorTreeAdapter;

import com.example.maolinzuo.sunshine.data.WeatherContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by maolinzuo on 9/13/17.
 */

public class SunshineSyncUtils {
    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = 3 * 3600;
    private static final int SYNC_FLEXTIME_SECONDS = 3600;

    private static boolean sInitialized;

    private static final String SUNSHINE_SYNC_TAG = "sunshine-sync";

    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context){
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        Job syncSunshineJob = jobDispatcher.newJobBuilder()
                .setService(SunshineFirebaseJobService.class)
                .setTag(SUNSHINE_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS, SYNC_FLEXTIME_SECONDS + SYNC_INTERVAL_SECONDS))
                .setReplaceCurrent(true)
                .build();

        jobDispatcher.schedule(syncSunshineJob);

    }

    synchronized public static void initialize(@NonNull final Context context){
        if(sInitialized ) return;
        sInitialized = true;
        scheduleFirebaseJobDispatcherSync(context);

         /*
         * We need to check to see if our ContentProvider has data to display in our forecast
         * list. However, performing a query on the main thread is a bad idea as this may
         * cause our UI to lag. Therefore, we create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String[] projectionColums = {WeatherContract.WeatherEntry._ID};
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColums,
                        selection,
                        null,
                        null);
                if(cursor == null || cursor.getCount()==0){
                    // if the cursor is null or empty, we need to sync immediately to display data
                    startImmediateSync(context);
                }
                cursor.close();
            }
        });
         /* Finally, once the thread is prepared, fire it off to perform our checks. */
        checkForEmpty.start();
    }

    public static void startImmediateSync(@NonNull Context context){
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

}
