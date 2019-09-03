package com.aaron.yespdf;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.appcompat.widget.Toolbar;

import com.aaron.base.util.StatusBarUtils;
import com.blankj.utilcode.util.ConvertUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class UiManager {

    public static void setStatusBar(Activity activity, Toolbar toolbar) {
        toolbar.setPadding(0, ConvertUtils.dp2px(25), 0, 0);
        int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            StatusBarUtils.setTransparent(activity, true);
        } else {
            StatusBarUtils.setTranslucent(activity, 60);
        }
    }

    private UiManager() {}
}
