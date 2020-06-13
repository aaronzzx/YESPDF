package com.aaron.yespdf.common.bean

import android.app.Activity
import androidx.annotation.DrawableRes

/**
 * @author aaronzzxup@gmail.com
 * @since 2020/6/14
 */
data class Shortcut(
        val target: Class<out Activity>,
        val id: String,
        val shortLabel: String,
        val longLabel: String,
        @DrawableRes val icon: Int,
        val extraKey: String? = null,
        val extraValue: String? = null
)