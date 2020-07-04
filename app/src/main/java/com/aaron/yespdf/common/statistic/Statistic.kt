package com.aaron.yespdf.common.statistic

import android.content.Context
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

/**
 * @author aaronzzxup@gmail.com
 * @since 2020/7/4
 */
object Statistic {

    private const val APP_KEY = "5ef6d654167eddf17b000015"
    private const val APP_CHANNEL = "coolapk"

    fun init(context: Context) {
        UMConfigure.init(context, APP_KEY, APP_CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, null)
    }

    fun setLogEnabled(value: Boolean) {
        UMConfigure.setLogEnabled(value)
    }

    fun onResume(context: Context) {
        MobclickAgent.onResume(context)
    }

    fun onPause(context: Context) {
        MobclickAgent.onPause(context)
    }
}