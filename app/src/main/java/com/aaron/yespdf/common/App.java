package com.aaron.yespdf.common;

import android.app.Application;
import android.content.Context;

import com.github.anzewei.parallaxbacklayout.ParallaxHelper;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class App extends Application {

    private static final String DB_NAME = "yespdf.db";

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this.getApplicationContext();

        DBManager.init(this, DB_NAME);
        registerActivityLifecycleCallbacks(ParallaxHelper.getInstance());
    }

    public static Context getContext() {
        return sContext;
    }
}
