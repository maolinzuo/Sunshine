package com.example.maolinzuo.sunshine.data;

import android.content.UriMatcher;
import android.net.Uri;

import static com.example.maolinzuo.sunshine.data.TestUtilities.getStaticIntegerField;
import static com.example.maolinzuo.sunshine.data.TestUtilities.studentReadableNoSuchField;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by maolinzuo on 9/12/17.
 */

public class TestUriMatcher {
    private static final Uri TEST_WEATHER_URI= WeatherContract.WeatherEntry.CONTENT_URI;
    private static final Uri TEST_WEATHER_WITH_DATE_URI = WeatherContract.WeatherEntry.buildWeatherUriWithDate(TestUtilities.DATE_NORMALIZED);

    private static final String weatherCodeVariableName = "CODE_WEATHER";
    private static int REFLECTED_WEATHER_CODE;

    private static final String weatherCodeWithDateVariableName = "CODE_WEAHTER_WITH_DATE";
    private static int REFLECTED_WEATHER_WITH_DATE_CODE;

    private UriMatcher testMatcher;

    @Before
    public void before(){
        try{
            Method buildUriMatcher = WeatherProvider.class.getDeclaredMethod("buildUriMathcer");
            testMatcher = (UriMatcher) buildUriMatcher.invoke(WeatherProvider.class);

            REFLECTED_WEATHER_CODE = getStaticIntegerField(WeatherProvider.class, weatherCodeVariableName);
            REFLECTED_WEATHER_WITH_DATE_CODE = getStaticIntegerField(WeatherProvider.class, weatherCodeWithDateVariableName);


        } catch (NoSuchMethodException ex){
            fail("You havent created a method called buildUriMatcher in the WeatherProvider class");
        } catch (IllegalAccessException ex){
            fail(ex.getMessage());
        } catch (InvocationTargetException ex){
            fail(ex.getMessage());
        } catch (NoSuchFieldException ex){
            fail(studentReadableNoSuchField(ex));
        }
    }

    @Test
    public void testUriMatch(){
        String weatherUriDoesNotMatch = "Error: The CODE_WEATHER URI Was matched incorrectly.";
        int actualWeatherCode = testMatcher.match(TEST_WEATHER_URI);
        int expectedWeatherCode = REFLECTED_WEATHER_CODE;
        assertEquals(weatherUriDoesNotMatch, actualWeatherCode, expectedWeatherCode);

        String weatherWithDateUriDoesNotMatch = "Error: The CODE_WEATHER_WITH_DATE_URI was watched incorrectly.";
        int actualWeatherWithDateCode = testMatcher.match(TEST_WEATHER_WITH_DATE_URI);
        int expectedWeatherWithDateCode = REFLECTED_WEATHER_WITH_DATE_CODE;
        assertEquals(weatherWithDateUriDoesNotMatch,actualWeatherWithDateCode, expectedWeatherWithDateCode);
    }
}
