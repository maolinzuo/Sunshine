package com.example.maolinzuo.sunshine.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.example.maolinzuo.sunshine.DetailActivity;
import com.example.maolinzuo.sunshine.R;
import com.example.maolinzuo.sunshine.data.SunshinePreferences;
import com.example.maolinzuo.sunshine.data.WeatherContract;

/**
 * Created by maolinzuo on 9/13/17.
 */

public class NotificationUtils {
    /*
     * The columns of data that we are interested in displaying within our notification to let
     * the user know there is new weather data available.
     */
    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 3004 is in no way significant.
     */
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    public static void notifyUserOfNewWeather(Context context){
        Uri todaysWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(SunshineDateUtils.normalizedDate(System.currentTimeMillis()));
        Cursor todaysWeatherCursor = context.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);


        if(todaysWeatherCursor.moveToFirst()){
            int weatherId = todaysWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todaysWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todaysWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();

            int largeArtResouceId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondtion(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(resources, largeArtResouceId);

            String notificationTitle = context.getString(R.string.app_name);
            String notificationText = getNotificationText(context, weatherId, high, low);

            int smallArtResourceId = SunshineWeatherUtils.getSmallArtResourceIdForWeatherConditon(weatherId);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);

            // this intent will be triggered when the user clicks the notificaiton
            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(WEATHER_NOTIFICATION_ID, builder.build());

            SunshinePreferences.saveLastNotificationTime(context, System.currentTimeMillis());

        }
    todaysWeatherCursor.close();
    }

    private static String getNotificationText(Context context, int weatherId, double high, double low){
        String shortDescription = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        String notificationText = String.format(notificationFormat,
                shortDescription,
                SunshineWeatherUtils.formatTemperature(context, high),
                SunshineWeatherUtils.formatTemperature(context, low));
        return notificationText;
    }
}
