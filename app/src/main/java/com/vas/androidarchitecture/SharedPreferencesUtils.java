package com.vas.androidarchitecture;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class SharedPreferencesUtils {
    public interface Action1<T> {
        void call(T t);
    }

    private SharedPreferencesUtils() {
    }

    public static SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationApp_.getInstance());
    }

    public static SharedPreferences getSharedPreferences(String preference_file_key) {
        return ApplicationApp_.getInstance().getSharedPreferences(preference_file_key, Context.MODE_PRIVATE);
    }

    public static void writePreferences(Action1<SharedPreferences.Editor> putEditor) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        putEditor.call(editor);
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? Looper.getMainLooper().isCurrentThread()
                : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (isUiThread) {
            // changes immediately the in-memory object writes the updates to disk asynchronously
            editor.apply();
        } else {
            // write the data to disk synchronously,
            // avoid calling it from your main thread because it could pause your UI rendering
            editor.commit();
        }
    }

    public static void writePreferences(final String key, final String value) {
        writePreferences(editor -> editor.putString(key, value));
    }

    public static void writePreferences(final String key, final Boolean value) {
        writePreferences(editor -> editor.putBoolean(key, value));
    }

    public static void writePreferences(final String key, final Float value) {
        writePreferences(editor -> editor.putFloat(key, value));
    }

    public static void writePreferences(final String key, final Integer value) {
        writePreferences(editor -> editor.putInt(key, value));
    }

    public static void writePreferences(final String key, final Long value) {
        writePreferences(editor -> editor.putLong(key, value));
    }

    public static void writePreferences(final String key, final Set<String> value) {
        writePreferences(editor -> editor.putStringSet(key, value));
    }

    public static Map<String, ?> getAllPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getAll();
    }

    public static String getStringPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getString(key, "");
    }

    public static Boolean getBooleanPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getBoolean(key, false);
    }

    public static Float getFloatPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getFloat(key, 0.f);
    }

    public static Integer getIntPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getInt(key, 0);
    }

    public static Long getLongPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getLong(key, 0);
    }

    public static Set<String> getStringSetPreferences(final String key) {
        SharedPreferences sharedPref = getDefaultSharedPreferences();
        return sharedPref.getStringSet(key, new HashSet<>());
    }


}