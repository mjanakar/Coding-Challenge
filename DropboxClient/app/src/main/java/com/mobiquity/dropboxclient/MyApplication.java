package com.mobiquity.dropboxclient;

import android.app.Application;
import android.content.Context;

/**
 * Created by shanki on 4/17/15.
 */
public class MyApplication extends Application {
    private static MyApplication myApplication;

    public static Context getAppContext() {
        return myApplication.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }
}
