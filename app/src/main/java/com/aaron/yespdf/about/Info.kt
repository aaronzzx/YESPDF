package com.aaron.yespdf.about

import android.annotation.SuppressLint
import android.os.Build
import com.aaron.yespdf.BuildConfig
import com.aaron.yespdf.R
import com.aaron.yespdf.common.App
import com.blankj.utilcode.util.ScreenUtils
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@SuppressLint("ConstantLocale")
interface Info {
    companion object {
        const val MY_BLOG = "https://juejin.im/user/5c3f3b2b5188252580051f8c"
        const val MY_EMAIL = "mailto:aaronzzxup@gmail.com"
        const val SOURCE_CODE = "https://github.com/Aaronzzx/YESPDF"
        const val MY_GITHUB = "https://github.com/Aaronzzx"
        val FEEDBACK_SUBJECT = ("YES PDF! for Android " + "view" + BuildConfig.VERSION_NAME + "\n"
                + "Feedback(" + Build.BRAND + "-" + Build.MODEL + ")")
        val FEEDBACK_TEXT = (App.getContext().getString(R.string.app_feedback_title) + "\n"
                + "Device: " + Build.BRAND + "-" + Build.MODEL + "\n"
                + "Android Version: " + Build.VERSION.RELEASE + "(SDK=" + Build.VERSION.SDK_INT + ")" + "\n"
                + "Resolution: " + ScreenUtils.getScreenWidth() + "*" + ScreenUtils.getScreenHeight() + "\n"
                + "System Language: " + Locale.getDefault().language + "(" + Locale.getDefault().country + ")" + "\n"
                + "App Version: " + BuildConfig.VERSION_NAME)
    }
}