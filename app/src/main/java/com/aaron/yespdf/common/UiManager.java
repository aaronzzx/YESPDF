package com.aaron.yespdf.common;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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

    public static void setNavigationBarColor(Activity activity, int color) {
        activity.getWindow().setNavigationBarColor(color);
    }

    public static void setStatusBarBlack(Activity activity, Toolbar toolbar) {
        toolbar.setPadding(0, 0, 0, 0);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().setStatusBarColor(Color.BLACK);
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
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        View toastView = ToastUtils.showCustomShort(R.layout.app_toast);
        TextView tv    = toastView.findViewById(R.id.app_tv);
        tv.setText(text);
    }

    public static void showShort(@StringRes int res) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        View toastView = ToastUtils.showCustomShort(R.layout.app_toast);
        TextView tv = toastView.findViewById(R.id.app_tv);
        tv.setText(res);
    }

    private UiManager() {}
}
