package com.example.maolinzuo.sunshine.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.renderscript.Double2;

import com.example.maolinzuo.sunshine.R;

/**
 * Created by maolinzuo on 9/13/17.
 */

public final class SunshinePreferences {

    /*
     * In order to uniquely pinpoint the location on the map when we launch the map intent, we
     * store the latitude and longitude. We will also use the latitude and longitude to create
     * queries for the weather.
     */
    public static final String PREF_COORD_LAT = "coord_lat";
    public static final String PREF_COORD_LONG = "coord_long";

    public static void setLocationDetails(Context context, double lat, double lon){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(PREF_COORD_LAT, Double.doubleToLongBits(lat));
        editor.putLong(PREF_COORD_LONG, Double.doubleToLongBits(lon));
        editor.apply();
    }

    public static boolean isMetric(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String keyForUnits = context.getString(R.string.pref_units_key);
        String defaultUnits = context.getString(R.string.pref_units_metric);
        String preferenceUnits = sharedPreferences.getString(keyForUnits, defaultUnits);
        String metric = context.getString(R.string.pref_units_metric);

        boolean userPrefersMetric = false;
        if(metric.equals(preferenceUnits)){
            userPrefersMetric = true;
        }
        return userPrefersMetric;
    }

    public static double[] getLocationCoordinates(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        double[] preferenceCoordinates = new double[2];
        preferenceCoordinates[0] = Double.longBitsToDouble(sharedPreferences.getLong(PREF_COORD_LAT, Double.doubleToRawLongBits(0.0)));
        preferenceCoordinates[1] = Double.longBitsToDouble(sharedPreferences.getLong(PREF_COORD_LONG, Double.doubleToRawLongBits(0.0)));
        return preferenceCoordinates;
    }

    public static boolean isLocationLatLonAvailable(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean spContainLat = sharedPreferences.contains(PREF_COORD_LAT);
        boolean spContainLon = sharedPreferences.contains(PREF_COORD_LONG);

        return spContainLat && spContainLon;
    }

    public static String getPreferredWeatherLocation(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String keyForLocation = context.getString(R.string.pref_location_key);
        String defaultLocation = context.getString(R.string.pref_location_default);

        return sharedPreferences.getString(keyForLocation, defaultLocation);
    }

    public static boolean areNotificationsEnabled(Context context){
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);

        boolean shouldDisplayNotificationsByDefault = context.getResources().getBoolean(R.bool.show_notifications_by_default);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean shouldDisplayNotifications = sharedPreferences.getBoolean(displayNotificationsKey, shouldDisplayNotificationsByDefault);
        return shouldDisplayNotifications;
    }

    public static long getEllapsedTimeSinceLastNotification(Context context){
        long lastNotificationTimeMillis = SunshinePreferences.getLastNotificationTimeInMillis(context);
        long timeSinceLastNotification = System.currentTimeMillis() - lastNotificationTimeMillis;
        return timeSinceLastNotification;
    }

    public static long getLastNotificationTimeInMillis(Context context){
        String lastNotificationKey = context.getString(R.string.pref_last_notification);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        long lastNotificationTime = sharedPreferences.getLong(lastNotificationKey, 0);
        return lastNotificationTime;
    }

    public static void saveLastNotificationTime(Context context, long time){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        editor.putLong(lastNotificationKey, time);
        editor.apply();
    }

    public static void resetLocationCoordinates(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(PREF_COORD_LAT);
        editor.remove(PREF_COORD_LONG);
        editor.apply();
    }
}
