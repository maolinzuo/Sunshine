package com.example.maolinzuo.sunshine.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by maolinzuo on 9/13/17.
 */

public class SunshineSyncIntentService extends IntentService {
    public SunshineSyncIntentService(){
        super("SunshineSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
