package com.example.maolinzuo.sunshine.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.example.maolinzuo.sunshine.data.TestUtilities.getConstantNameByStringValue;
import static com.example.maolinzuo.sunshine.data.TestUtilities.getStaticIntegerField;
import static com.example.maolinzuo.sunshine.data.TestUtilities.getStaticStringField;
import static com.example.maolinzuo.sunshine.data.TestUtilities.studentReadableClassNotFound;
import static com.example.maolinzuo.sunshine.data.TestUtilities.studentReadableNoSuchField;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertTrue;


import com.example.maolinzuo.sunshine.data.TestUtilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by maolinzuo on 9/10/17.
 */

public class TestSunshineDatabase {
    //Context used to perform operations on the database and create WeatherDbHelpers.
    private final Context context = InstrumentationRegistry.getTargetContext();

    //Change Detector Test:
    private static final String packageName = "com.example.maolinzuo.sunshine";
    private static final String dataPackageName = "com.example.maolinzuo.sunshine.data";

    private Class weatherEntryClass;
    private Class weatherDbHelperClass;

    private static final String weatherContractName = ".WeatherContract";
    private static final String weatherEntryName = weatherContractName + "$WeatherEntry";
    private static final String weatherDbHelperName = ".WeatherDbHelper";

    private static final String databaseNameVariableName = "DATA_BASE_NAME";
    private static String REFLECTED_DATABASE_NAME;

    private static final String databaseVersionVariableName = "DATA_BASE_VERSION";
    private static int REFLECTED_DATABASE_VERSION;

    private static final String tableNameVariableName = "TABLE_NAME";
    private static String REFLECTED_TABLE_NAME;

    private static final String columnDateVariableName = "COLUMN_DATE";
    private static String REFLECTED_COLUMN_DATE;

    private static final String columnWeatherIdVariableName = "COLUMN_WEATHER_ID";
    private static String REFLECTED_COLUMN_WEATHER_ID;

    private static final String columnMinVariableName = "COLUMN_MIN_TEMP";
    private static String REFLECTED_COLUMN_MIN;

    private static final String columnMaxVariableName = "COLUMN_MAX_TEMP";
    private static String REFLECTED_COLUMN_MAX;

    private static final String columnHumidityVariableName = "COLUMN_HUMIDITY";
    private static String REFLECTED_COLUMN_HUMIDITY;

    private static final String columnPressureVariableName = "COLUMN_PRESSURE";
    private static String REFLECTED_COLUMN_PRESSURE;

    private static final String columnWindSpeedVariableName = "COLUMN_WIND_SPEED";
    static String REFLECTED_COLUMN_WIND_SPEED;

    private static final String columnWindDirVariableName = "COLUMN_DEGREES";
    static String REFLECTED_COLUMN_WIND_DIR;


    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    @Before
    public void before(){
        try{
            weatherEntryClass = Class.forName(dataPackageName + weatherEntryName);
            if(!BaseColumns.class.isAssignableFrom(weatherEntryClass)){
                fail("WeatherEntry class needs to implement the interface BaseColumns, but does not.");
            }
            REFLECTED_TABLE_NAME = getStaticStringField(weatherEntryClass, tableNameVariableName);
            REFLECTED_COLUMN_DATE = getStaticStringField(weatherEntryClass, columnDateVariableName);
            REFLECTED_COLUMN_WEATHER_ID = getStaticStringField(weatherEntryClass, columnWeatherIdVariableName);
            REFLECTED_COLUMN_MIN = getStaticStringField(weatherEntryClass, columnMinVariableName);
            REFLECTED_COLUMN_MAX = getStaticStringField(weatherEntryClass, columnMaxVariableName);
            REFLECTED_COLUMN_HUMIDITY = getStaticStringField(weatherEntryClass, columnHumidityVariableName);
            REFLECTED_COLUMN_PRESSURE = getStaticStringField(weatherEntryClass, columnPressureVariableName);
            REFLECTED_COLUMN_WIND_SPEED = getStaticStringField(weatherEntryClass, columnWindSpeedVariableName);
            REFLECTED_COLUMN_WIND_DIR = getStaticStringField(weatherEntryClass, columnWindDirVariableName);

            weatherDbHelperClass = Class.forName(dataPackageName + weatherDbHelperName);
            Class weatherDbHelperSuperclass = weatherDbHelperClass.getSuperclass();
            if(weatherDbHelperSuperclass == null || weatherDbHelperSuperclass.equals(Object.class)){
                fail("WeatherDbHelper needs to extend SQLiteOpenHelper");
            } else if(weatherDbHelperSuperclass != null){
                String weatherDbHelperSuperclassName = weatherDbHelperSuperclass.getSimpleName();
                assertTrue("WeatherDbHelper needs to extends SQLiteOpenHelper, not " +weatherDbHelperSuperclassName, SQLiteOpenHelper.class.isAssignableFrom(weatherDbHelperSuperclass));
            }

            REFLECTED_DATABASE_NAME = getStaticStringField(weatherDbHelperClass, databaseNameVariableName);
            REFLECTED_DATABASE_VERSION = getStaticIntegerField(weatherDbHelperClass, databaseVersionVariableName);

            Constructor weatherDbHelperCtor = weatherDbHelperClass.getConstructor(Context.class);
            dbHelper = (SQLiteOpenHelper) weatherDbHelperCtor.newInstance(context);

            context.deleteDatabase(REFLECTED_DATABASE_NAME);

            Method getWritableDatabase = SQLiteOpenHelper.class.getDeclaredMethod("getWritableDatabase");
            database = (SQLiteDatabase) getWritableDatabase.invoke(dbHelper);


        } catch (ClassNotFoundException ex){
            // fail(java.lang.String  message)
            fail(studentReadableClassNotFound(ex));
        } catch (NoSuchFieldException ex){
            fail(studentReadableNoSuchField(ex));
        } catch (IllegalAccessException ex){
            fail(ex.getMessage());
        } catch (NoSuchMethodException ex){
            fail(ex.getMessage());
        } catch (InstantiationException ex){
            fail(ex.getMessage());
        } catch (InvocationTargetException ex){
            fail(ex.getMessage());
        }
    }


    @Test
    public void testDatabaseVersionWasIncremented(){
        int expectedDatabaseVersion = 1;
        String databaseVersionShouldBe1 = "Database version should be " + expectedDatabaseVersion + " not " + REFLECTED_DATABASE_VERSION;

        assertEquals(databaseVersionShouldBe1, expectedDatabaseVersion, REFLECTED_DATABASE_VERSION);
    }


    @Test
    public void testDuplicateDateInsertBehaviorShouldReplace(){
        ContentValues testWeatherValues = TestUtilities.createTestWeatherContentValues();
        long originalWeatherId = testWeatherValues.getAsLong(REFLECTED_COLUMN_WEATHER_ID);
        database.insert(
                WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                testWeatherValues
        );

        long newWeatherId = originalWeatherId + 1;
        testWeatherValues.put(REFLECTED_COLUMN_WEATHER_ID, newWeatherId);
        database.insert(
                WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                testWeatherValues
        );

        Cursor newWeatherIdCursor = database.query(
                REFLECTED_TABLE_NAME,
                new String[]{REFLECTED_COLUMN_DATE},
                null,
                null,
                null,
                null,
                null
        );

        String recordWithNewIdNotFound = "New record did not overwrite previous record for the same date. ";
        assertTrue(recordWithNewIdNotFound, newWeatherIdCursor.getCount() == 1);

        newWeatherIdCursor.close();

    }

    @Test
    public void testNullColumnConstraints(){
        Cursor weatherTableCursor = database.query(
                REFLECTED_TABLE_NAME,
                null, null, null, null, null, null);
        String[] weatherTableColumnNames = weatherTableCursor.getColumnNames();
        weatherTableCursor.close();

        ContentValues testValues = TestUtilities.createTestWeatherContentValues();
        ContentValues testValuesReferenceCopy = new ContentValues(testValues);

        for(String columnName : weatherTableColumnNames){
            if(columnName.equals(WeatherContract.WeatherEntry._ID)) continue;
            testValues.putNull(columnName);
            long shouldFailRowId = database.insert(REFLECTED_TABLE_NAME, null, testValues);

            String variableName = getConstantNameByStringValue(WeatherContract.WeatherEntry.class, columnName);
            String nullRowInsertShouldFail = "Insert should have failed due to a null value for column: " + columnName;
            assertEquals(nullRowInsertShouldFail, -1, shouldFailRowId);

            testValues.put(columnName, testValuesReferenceCopy.getAsDouble(columnName));
        }
        dbHelper.close();

    }

    @Test
    public void testIntegerAutoincrement(){

        
    }

    @Test
    public void testInsertSingleRecordIntoWeatherTable(){
        ContentValues testWeatherValues = TestUtilities.createTestWeatherContentValues();

        long weatherRowId = database.insert(
                REFLECTED_TABLE_NAME,
                null,
                testWeatherValues);

        String insertFailed = "Unable to insert into the database";
        assertNotSame(insertFailed, -1, weatherRowId);

        Cursor weatherCursor = database.query(
                REFLECTED_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        String emptyQueryError = "Error: No records returned from weather query.";
        // Cursor.moveToFirst() will return false it there are no records.
        assertTrue(emptyQueryError, weatherCursor.moveToFirst());

        String expectedWeatherDidntMatchActual = "Expected weather values didnt match actual values.";
        TestUtilities.validateCurrentRecord(expectedWeatherDidntMatchActual, weatherCursor, testWeatherValues);

        // Before every method annotated with @Test, database is deleted. In this method, we have only inserted
        // one single row into the database.
        assertFalse("Error: More than one record returned from weather query.", weatherCursor.moveToNext());
    }

}
