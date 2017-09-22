package com.example.maolinzuo.sunshine.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.example.maolinzuo.sunshine.utilities.SunshineDateUtils;
import com.example.maolinzuo.sunshine.utils.PollingCheck;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.StandardSocketOptions;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_DATE;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_DEGREES;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_HUMIDITY;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_MAX_TEMP;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_MIN_TEMP;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_PRESSURE;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_WEATHER_ID;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.COLUMN_WIND_SPEED;
import static com.example.maolinzuo.sunshine.data.WeatherContract.WeatherEntry.CONTENT_URI;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Created by maolinzuo on 9/10/17.
 */

public class TestUtilities {

    static final long DATE_NORMALIZED = 1475280000000L;

    static final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static String getStaticStringField(Class clasz, String variableName)
    throws NoSuchFieldException, IllegalAccessException{
        Field stringField = clasz.getDeclaredField(variableName);
        stringField.setAccessible(true);
        String value = (String) stringField.get(null);
        return value;
    }

    static Integer getStaticIntegerField(Class clasz, String variableName)
            throws NoSuchFieldException, IllegalAccessException{
        Field stringField = clasz.getDeclaredField(variableName);
        stringField.setAccessible(true);
        Integer value = (Integer) stringField.get(null);
        return value;
    }

    static String studentReadableNoSuchField(NoSuchFieldException e){
        String message = e.getMessage();
        Pattern p = Pattern.compile("No field (\\w*) in class L.*/(\\w*\\$?\\w*);");
        Matcher m = p.matcher(message);

        if(m.find()){
            String missingFieldName = m.group(1);
            String classForField = m.group(2).replace("\\$", ".");
            String fieldNotFoundReadableMessage = "Cloudn't find " + missingFieldName + " in class " + classForField;
            return fieldNotFoundReadableMessage;
        }else{
            return message;
        }
    }

    static String studentReadableClassNotFound(ClassNotFoundException e){
        String message = e.getMessage();
        int indexBeforeSimpleClassName = message.lastIndexOf('.');
        String simpleClassNameThatIsMissing = message.substring(indexBeforeSimpleClassName + 1);
        simpleClassNameThatIsMissing = simpleClassNameThatIsMissing.replaceAll("\\$", ".");
        String fullClassNotFoundReadableMessage = "Couldn't find the class "
                + simpleClassNameThatIsMissing;
        return fullClassNotFoundReadableMessage;
    }

    static ContentValues createTestWeatherContentValues() {

        ContentValues testWeatherValues = new ContentValues();

        testWeatherValues.put(COLUMN_DATE, DATE_NORMALIZED);
        testWeatherValues.put(COLUMN_DEGREES, 1.1);
        testWeatherValues.put(COLUMN_HUMIDITY, 1.2);
        testWeatherValues.put(COLUMN_PRESSURE, 1.3);
        testWeatherValues.put(COLUMN_MAX_TEMP, 75);
        testWeatherValues.put(COLUMN_MIN_TEMP, 65);
        testWeatherValues.put(COLUMN_WIND_SPEED, 5.5);
        testWeatherValues.put(COLUMN_WEATHER_ID, 321);

        return testWeatherValues;
    }

    static String getConstantNameByStringValue(Class clazz, String value){
        for(Field f: clazz.getDeclaredFields()){
            int modifiers = f.getModifiers();
            Class<?> type = f.getType();
            boolean isPublicStaticFinalString = Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic((modifiers)) && type.isAssignableFrom(String.class);
            if(isPublicStaticFinalString){
                String fieldName = f.getName();
                try{
                    String fieldValue = (String) clazz.getDeclaredField(fieldName).get(null);
                    if(fieldValue.equals(value)) return fieldName;
                } catch (NoSuchFieldException ex){
                    return null;
                } catch (IllegalAccessException ex){
                    return null;
                }
            }

        }
        return null;
    }

    static void validateCurrentRecord(String message, Cursor cursor, ContentValues value){
        Set<Map.Entry<String, Object>> valueSet = value.valueSet();

        for(Map.Entry<String, Object> entry: valueSet){
            String columnName = entry.getKey();
            int index = cursor.getColumnIndex(columnName);
            // Test if the column is contained within the cursor
            String columnNotFoundError = "Column " + columnName + " not found. " + message;
            assertTrue(columnNotFoundError, index != -1);

            String expectedValue = entry.getValue().toString();
            String actualValue = cursor.getString(cursor.getColumnIndex(columnName));

            String valuesDontMatchError = "Actual value " + actualValue + " didnt match the expected value " +
                    expectedValue + ". " + message;
            assertEquals(valuesDontMatchError, expectedValue, actualValue);

        }

    }

    static ContentValues[] createBulkInsertTestWeatherValues(){
        ContentValues[] bulkTestWeatherValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        long testDate = DATE_NORMALIZED;
        long normalizedTestDate = SunshineDateUtils.normalizedDate(testDate);

        for(int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++){
            normalizedTestDate += SunshineDateUtils.DAY_IN_MILLIS;

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(COLUMN_DATE, normalizedTestDate);
            weatherValues.put(COLUMN_DEGREES, 1.1);
            weatherValues.put(COLUMN_HUMIDITY, 1.2 + 0.01* (float) i);
            weatherValues.put(COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
            weatherValues.put(COLUMN_MAX_TEMP, 75 + i);
            weatherValues.put(COLUMN_MIN_TEMP, 65 - i);
            weatherValues.put(COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
            weatherValues.put(COLUMN_WEATHER_ID, 321);

            bulkTestWeatherValues[i] = weatherValues;
        }
        return bulkTestWeatherValues;
    }


    //NOTE: This only tests that the onChange function is called; it DOES NOT test that the correct Uri is returned.
    static class TestContentObserver extends ContentObserver{
        final HandlerThread mHT;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht){
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        static TestContentObserver getTestContentObserver(){
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        void waitForNotificationOrFail(){

            new PollingCheck(5000){
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }
}
