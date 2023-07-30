package com.example.firebase_refugees_app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppCache {
    private static final String PREFS_NAME = "MyAppPreferences";

    public static void saveData(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getData(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }
}