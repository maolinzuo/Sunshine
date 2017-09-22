package com.example.maolinzuo.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.example.maolinzuo.sunshine.data.SunshinePreferences;
import com.example.maolinzuo.sunshine.data.WeatherContract;
import com.example.maolinzuo.sunshine.utilities.NetworkUtils;
import com.example.maolinzuo.sunshine.utilities.NotificationUtils;
import com.example.maolinzuo.sunshine.utilities.OpenWeatherJsonUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Created by maolinzuo on 9/13/17.
 */

public class SunshineSyncTask {
    synchronized public static void syncWeather(Context context){
        try{
            URL weatherRequestUrl = NetworkUtils.getUrl(context);
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            ContentValues[] weatherValues = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            if(weatherValues != null && weatherValues.length != 0){
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver contentResolver = context.getContentResolver();
                /* Delete old weather data because we don't need to keep multiple days' data */
                contentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);
                contentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);

                boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);

                long timeSinceLastNotification = SunshinePreferences.getEllapsedTimeSinceLastNotification(context);
                boolean oneDayPassedSinceLastNotification = false;

                if( timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS){
                    oneDayPassedSinceLastNotification = true;
                }
                if(notificationsEnabled && oneDayPassedSinceLastNotification){
                    NotificationUtils.notifyUserOfNewWeather(context);
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
