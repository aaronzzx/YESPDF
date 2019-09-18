package com.aaron.yespdf.about;

import android.annotation.SuppressLint;
import android.os.Build;

import com.aaron.yespdf.BuildConfig;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.App;
import com.blankj.utilcode.util.ScreenUtils;

import java.util.Locale;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@SuppressLint("ConstantLocale")
interface Info {

    String MY_BLOG     = "https://juejin.im/user/5c3f3b2b5188252580051f8c";
    String MY_EMAIL    = "mailto:aaronzzxup@gmail.com";
    String SOURCE_CODE = "https://github.com/Aaronzzx/YESPDF";
    String MY_GITHUB   = "https://github.com/Aaronzzx";

    String FEEDBACK_SUBJECT = "YES PDF! for Android " + "view" + BuildConfig.VERSION_NAME + "\n"
            + "Feedback(" + Build.BRAND + "-" + Build.MODEL + ")";
    String FEEDBACK_TEXT    = App.getContext().getString(R.string.app_feedback_title) + "\n"
            + "Device: " + Build.BRAND + "-" + Build.MODEL + "\n"
            + "Android Version: " + Build.VERSION.RELEASE + "(SDK=" + Build.VERSION.SDK_INT + ")" + "\n"
            + "Resolution: " + ScreenUtils.getScreenWidth() + "*" + ScreenUtils.getScreenHeight() + "\n"
            + "System Language: " + Locale.getDefault().getLanguage() + "(" + Locale.getDefault().getCountry() + ")" + "\n"
            + "App Version: " + BuildConfig.VERSION_NAME;
}
