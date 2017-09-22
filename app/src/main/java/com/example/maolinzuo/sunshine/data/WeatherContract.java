package com.example.maolinzuo.sunshine.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.maolinzuo.sunshine.utilities.SunshineDateUtils;

/**
 * Created by maolinzuo on 9/9/17.
 */

public class WeatherContract {
    public static final String CONTENT_AUTHORITY = "com.example.maolinzuo.sunshine";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";

    public static final class WeatherEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WEATHER)
                .build();

        public static final String TABLE_NAME = "weather";


        public static final String COLUMN_DATE = "date";

        /* Weather ID as returned by API, used to identify the icon to be used */
        public static final String COLUMN_WEATHER_ID = "weather_id";

        /* Min and max temperatures in °C for the day (stored as floats in the database) */
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        /* Humidity is stored as a float representing percentage */
        public static final String COLUMN_HUMIDITY = "humidity";

        /* Pressure is stored as a float representing percentage */
        public static final String COLUMN_PRESSURE = "pressure";

        /* Wind speed is stored as a float representing wind speed in mph */
        public static final String COLUMN_WIND_SPEED = "wind";

        // meteorological degrees (e.g, 0 is north, 180 is south). stored as floats
        public static final String COLUMN_DEGREES = "degrees";

        public static Uri buildWeatherUriWithDate(long date){
            return CONTENT_URI.buildUpon().appendPath(Long.toString(date)).build();
        }

        public static String getSqlSelectForTodayOnwards(){
            long normalizedUtcNow = SunshineDateUtils.normalizedDate(System.currentTimeMillis());
            return WeatherEntry.COLUMN_DATE + ">=" + normalizedUtcNow;
        }

    }


}
