package com.aaron.yespdf.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import com.aaron.base.util.StatusBarUtils;
import com.aaron.yespdf.R;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class UiManager {

    public static void setNavigationBarColor(Activity activity, @ColorRes int colorRes) {
        int color = activity.getResources().getColor(colorRes);
        activity.getWindow().setNavigationBarColor(color);
    }

    public static void setStatusBar(Activity activity, Toolbar toolbar) {
        toolbar.setPadding(0, ConvertUtils.dp2px(25), 0, 0);
        int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            StatusBarUtils.setTransparent(activity, true);
        } else {
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

    public static void setTransparentNavigationBar(Activity activity) {
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public static void showShort(CharSequence text) {
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
        View toastView = ToastUtils.showCustomShort(R.layout.app_toast);
        TextView tv    = toastView.findViewById(R.id.app_tv);
        tv.setText(text);
    }

    public static void showShort(@StringRes int res) {
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
        View toastView = ToastUtils.showCustomShort(R.layout.app_toast);
        TextView tv = toastView.findViewById(R.id.app_tv);
        tv.setText(res);
    }

    private UiManager() {}
}
