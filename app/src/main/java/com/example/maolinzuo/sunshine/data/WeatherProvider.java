package com.example.maolinzuo.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.maolinzuo.sunshine.utilities.SunshineDateUtils;

/**
 * Created by maolinzuo on 9/9/17.
 */

public class WeatherProvider extends ContentProvider {

    // for UriMatcher
    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;

    private static final UriMatcher sUriMatcher = buildUriMathcer();

    private WeatherDbHelper mOpenHelper;

    public static UriMatcher buildUriMathcer(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = WeatherContract.CONTENT_AUTHORITY;

        /* This URI is content://com.example.android.sunshine/weather/ */
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        /*
         * This URI would look something like content://com.example.android.sunshine/weather/1472214172
         * The "/#" signifies to the UriMatcher that if PATH_WEATHER is followed by ANY number,
         * that it should return the CODE_WEATHER_WITH_DATE code
         */
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    /*In Sunshine, we are only going to be
     * inserting multiple rows of data at a time from a weather forecast. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match){
            case CODE_WEATHER:
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for(ContentValues value: values){
                        long weatherDate = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if(!SunshineDateUtils.isDateNormalized(weatherDate)){
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }
                        //insert(String table, String nullColumnHack, ContentValues values),
                        //the row ID of the newly inserted row, or -1 if an error occurred
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if(_id != -1){
                            rowsInserted ++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if(rowsInserted > 0){
                    /* getApplicationContext() - Returns the context for all activities running in application.
                        getBaseContext() - If you want to access Context from another context within application you can access.
                        getContext() - Returns the context view only current running activity.
                    */
                    //void notifyChange (Uri uri, ContentObserver observer)  observer may be null
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //return super.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        // content://com.example.android.sunshine/weather/1472214172
        switch (match){
            case CODE_WEATHER_WITH_DATE:
                // In the comment above, the last path segment is 1472214172 and represents the number of seconds since the epoch, or UTC time.
                String normalizedUtcDateString = uri.getLastPathSegment();
                // we have to create a string array that only contains one element because this method signature accepts a string array.
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                //SQLiteDatabase.query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_WEATHER:
                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknow uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int rowsDeleted = 0;
        // According to the documentation for SQLiteDatabase, passing "1" for the selection will delete all rows and return the number of rows deleted, which is what the caller of this method expects.
        if(s == null) s = "1";

        int match = sUriMatcher.match(uri);
        switch (match){
            case CODE_WEATHER:
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        s,
                        strings
                );
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: "+ uri);
        }

        if(rowsDeleted!=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;

    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        throw new RuntimeException("We are not implementing insert in Sunshine. Use bulkInsert instead");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new RuntimeException("We are not implementing update in Sunshine");

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in Sunshine.");
    }

    //Android normally handles ContentProvider startup and shutdown automatically. You do not need to start up or shut down a ContentProvider. When you invoke a test method on a ContentProvider, however, a ContentProvider instance is started and keeps running after the test finishes, even if a succeeding test instantiates another ContentProvider. A conflict develops because the two instances are usually running against the same underlying data source (for example, an sqlite database).
    //Implementing shutDown() avoids this conflict by providing a way to terminate the ContentProvider. This method can also prevent memory leaks from multiple instantiations of the ContentProvider, and it can ensure unit test isolation by allowing you to completely clean up the test fixture before moving on to the next test.
    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
