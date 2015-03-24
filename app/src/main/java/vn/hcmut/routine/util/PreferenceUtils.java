package vn.hcmut.routine.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {

    private static String REFERENCE_NAME = "SharePreferences";

    public static void saveString(String key, Context myContext, String value) {
        SharedPreferences prefs = myContext.getSharedPreferences(
                REFERENCE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).commit();
    }

    public static String getString(String key, Context myContext,
                                      String value) {
        SharedPreferences prefs = myContext.getSharedPreferences(
                REFERENCE_NAME, Context.MODE_PRIVATE);

        return prefs.getString(key, value);
    }

    public static void saveInt(String key, Context myContext, int value) {
        SharedPreferences prefs = myContext.getSharedPreferences(
                REFERENCE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(key, value).commit();
    }

    public static int getInt(String key, Context myContext, int value) {
        SharedPreferences prefs = myContext.getSharedPreferences(
                REFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key, value);
    }

}
