package com.vas.androidarchitecture;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

/**
 * Android Application class. Used for accessing singletons.
 */
@EApplication
public class ApplicationApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
    }

//    public AppDatabase getDatabase() {
//        return AppDatabase.getInstance(this, mAppExecutors);
//    }

//    public DataRepository getRepository() {
//        return DataRepository.getInstance(getDatabase());
//    }
}
