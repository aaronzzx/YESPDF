package com.aaron.yespdf;

import android.app.Application;
import android.content.Context;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class App extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this.getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
