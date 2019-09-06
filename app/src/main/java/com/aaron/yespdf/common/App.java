package com.aaron.yespdf.common;

import android.app.Application;
import android.content.Context;

import com.github.anzewei.parallaxbacklayout.ParallaxHelper;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class App extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this.getApplicationContext();

        DBHelper.init(this, AppConfig.DB_NAME);
        registerActivityLifecycleCallbacks(ParallaxHelper.getInstance());
    }

    public static Context getContext() {
        return sContext;
    }
}
