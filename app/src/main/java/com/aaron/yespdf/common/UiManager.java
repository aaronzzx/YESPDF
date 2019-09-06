package com.aaron.yespdf.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import com.aaron.base.util.StatusBarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class UiManager {

    public static void setStatusBar(Activity activity, Toolbar toolbar) {
        toolbar.setPadding(0, ConvertUtils.dp2px(25), 0, 0);
        int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            StatusBarUtils.setTransparent(activity, true);
//            StatusBarUtils.setColor(activity, Color.WHITE, 0);
//            StatusBarUtils.setStatusBarLight(activity, true);
        } else {
//            StatusBarUtils.setColor(activity, Color.WHITE, 60);
            StatusBarUtils.setTranslucent(activity, 60);
        }
    }

    public static void setTransparentStatusBar(Activity activity, Toolbar toolbar) {
        toolbar.setPadding(0, ConvertUtils.dp2px(25), 0, 0);
        StatusBarUtils.setTransparent(activity);
    }

    public static void setBlackNavigationBar(Activity activity) {
        activity.getWindow().setNavigationBarColor(Color.BLACK);
    }

    public static void showShort(CharSequence text) {
        ToastUtils.showShort(text);
    }

    public static void showShort(@StringRes int res) {
        ToastUtils.showShort(res);
    }

    private UiManager() {}
}
