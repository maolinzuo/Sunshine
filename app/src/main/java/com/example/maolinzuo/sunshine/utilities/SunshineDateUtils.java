package com.example.maolinzuo.sunshine.utilities;

/**
 * Created by maolinzuo on 9/9/17.
 */

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.TimeZone;

import android.content.ContentProvider;
import android.content.Context;
import android.text.format.DateUtils;

import com.example.maolinzuo.sunshine.R;


public class SunshineDateUtils {

    public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    public static long normalizedDate(Long dateInMills){
        long daysSinceEpoch = elapsedDaysSinceEpoch(dateInMills);
        long millisFromEpochToTodayAtMidnightUtc = daysSinceEpoch * DAY_IN_MILLIS;
        return millisFromEpochToTodayAtMidnightUtc;
    }

    private static long elapsedDaysSinceEpoch(long utcDate) {
        return TimeUnit.MILLISECONDS.toDays(utcDate);
    }

    public static boolean isDateNormalized(long date){
        boolean isDateNormalized = false;
        if(date % DAY_IN_MILLIS == 0){
            isDateNormalized = true;
        }
        return isDateNormalized;
    }


    private static long getLocalMidNightFromNormalizedUtcDate(long normalizedUtcDate){
        TimeZone timeZone = TimeZone.getDefault();
        long gmtOffset = timeZone.getOffset(normalizedUtcDate);
        long localMidnightMillis = normalizedUtcDate - gmtOffset;
        return localMidnightMillis;
    }

    public static String getFriendlyDateString(Context context, long normalizedUtcMidnigh, boolean showFullDate){
        long localDate = getLocalMidNightFromNormalizedUtcDate(normalizedUtcMidnigh);
        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(localDate);

        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());
        if(daysFromEpochToProvidedDate == daysFromEpochToToday || showFullDate){
            /*
             * If the date we're building the String for is today's date, the format
             * is "Today, June 24"
             */
            String dayName = getDayName(context, localDate);
            String readableDate = getReadableDateString(context, localDate);

            // today or tomorrow
            if(daysFromEpochToProvidedDate - daysFromEpochToToday < 2){
                String localizedDayName = new SimpleDateFormat("EEEE").format(localDate);
                return readableDate.replace(localizedDayName,dayName);
            } else{
                return readableDate;
            }

        } else if(daysFromEpochToProvidedDate < daysFromEpochToToday + 7){
            return getDayName(context, localDate);
        } else{
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_WEEKDAY;
            return DateUtils.formatDateTime(context, localDate, flags);
        }
    }

    public static String getDayName(Context context, long dateInMillis){
        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(dateInMillis);
        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());

        int daysAfterToday = (int) (daysFromEpochToProvidedDate - daysFromEpochToToday);
        switch (daysAfterToday){
            case 0:
                return context.getString(R.string.today);
            case 1:
                return context.getString(R.string.tomorrow);
            default:
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
        }
    }

    public static String getReadableDateString(Context context, long dateInMillis){
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY;
        return DateUtils.formatDateTime(context, dateInMillis, flags);
    }

    public static long getNormalizedUtcDateForToday(){
        long utcNowMillis = System.currentTimeMillis();
        TimeZone currentTimeZone = TimeZone.getDefault();

        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);
        long normalizedUtcMidnightMillis = TimeUnit.DAYS.toMillis(daysSinceEpochLocal);

        return normalizedUtcMidnightMillis;
    }
}
