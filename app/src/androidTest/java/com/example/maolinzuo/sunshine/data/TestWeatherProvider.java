package com.example.maolinzuo.sunshine.data;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import static com.example.maolinzuo.sunshine.data.TestUtilities.BULK_INSERT_RECORDS_TO_INSERT;
import static com.example.maolinzuo.sunshine.data.TestUtilities.createBulkInsertTestWeatherValues;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by maolinzuo on 9/10/17.
 */

public class TestWeatherProvider {

    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp(){
        deleteAllrecordsFromWeatherTable();
    }

    public void deleteAllrecordsFromWeatherTable(){

        WeatherDbHelper helper = new WeatherDbHelper((InstrumentationRegistry.getTargetContext()));
        SQLiteDatabase database = helper.getWritableDatabase();

        database.delete(WeatherContract.WeatherEntry.TABLE_NAME, null, null);

        database.close();
    }

    @Test
    public void testProviderRegistry(){
        String packageName = mContext.getPackageName();
        String weatherProviderClassName = WeatherProvider.class.getName();

        /*
         *  We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        ComponentName componentName = new ComponentName(packageName, weatherProviderClassName);
        //fail(componentName.toString());
        //com.example.android.sunshine/com.example.android.sunshine.data.WeatherProvider

        try{
            // The packageManager allows us to access information about packages installed on a
            // particular device.
            PackageManager packageManager = mContext.getPackageManager();

            ProviderInfo providerInfo = packageManager.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = WeatherContract.CONTENT_AUTHORITY;

            String incorrectAuthority = "Error: WeatherProvider registered with authority: " + actualAuthority +
                    " instead of expected authority: "+ expectedAuthority;
            assertEquals(incorrectAuthority, actualAuthority, expectedAuthority);

        } catch (PackageManager.NameNotFoundException ex){
             /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            String providerNotRegisteredAtAll = "Error: WeatherProvider not registered at " + mContext.getPackageName();
            fail(providerNotRegisteredAtAll);
        }
    }

    @Test
    public void testBasicWeatherQuery(){
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = TestUtilities.createTestWeatherContentValues();

        long weatherRowId = database.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, contentValues);
        String insertFailed = "Unable to insert into the database";
        assertTrue(insertFailed, weatherRowId != -1);

        database.close();

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        System.out.print(weatherCursor.getPosition());
        weatherCursor.moveToFirst();
        for(int i = 0; i < 9; i++){
            System.out.print("          " +weatherCursor.getColumnName(i) + "\n");
            System.out.print(weatherCursor.getString(i));
        }
        TestUtilities.validateCurrentRecord("testBasicWeatherQuery", weatherCursor, contentValues);
        weatherCursor.close();
    }


    @Test
    public void testBulkInsert(){
        ContentValues[] bulkInsertTestContentValues = createBulkInsertTestWeatherValues();

        //TestContentObserver allows us to test whether or not notifyChange was called appropriately.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.TestContentObserver.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();
        //registerContentObserver(Uri uri, boolean notifyForDescendants, ContentObserver observer)

        contentResolver.registerContentObserver(WeatherContract.WeatherEntry.CONTENT_URI,
                true,
                weatherObserver);

        int insertCount = contentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, bulkInsertTestContentValues);

        // if this fails, its likely that you didnt call notifyChange in ur delete method from the content provider
        weatherObserver.waitForNotificationOrFail();

        contentResolver.unregisterContentObserver(weatherObserver);

        String expectedAndActualInsertedRecordCountDoNotMatch = "Number of expecetd records inserted does not match actual inserted record count";
        assertEquals(expectedAndActualInsertedRecordCountDoNotMatch, insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for(int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()){
            TestUtilities.validateCurrentRecord(
                    "testBulkInsert. Error validating WeatherEntry " + i,
                    cursor,
                    bulkInsertTestContentValues[i]);
        }
        cursor.close();

    }

    @Test
    public void testDeletedAllRecordsFromProvider(){
        testBulkInsert();

        TestUtilities.TestContentObserver weatherObserver = TestUtilities.TestContentObserver.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        contentResolver.registerContentObserver(
                WeatherContract.WeatherEntry.CONTENT_URI,
                true,
                weatherObserver);

        contentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null);

        Cursor shouldBeEmptyCursor = contentResolver.query(
                WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        weatherObserver.waitForNotificationOrFail();

        contentResolver.unregisterContentObserver(weatherObserver);

        String cursorWasNull = "Cursor was null.";
        assertNotNull(cursorWasNull, shouldBeEmptyCursor);

        String allRecordsWereNotDeleted = "Error: All records were not deleted from weather table during delete";
        assertEquals(allRecordsWereNotDeleted, 0, shouldBeEmptyCursor.getCount());

        shouldBeEmptyCursor.close();
    }

}
