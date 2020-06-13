package com.aaron.yespdf.common.utils

import android.os.Build
import android.view.Window
import android.view.WindowManager
import com.aaron.yespdf.common.LiveDataBus

object NotchUtils {

    const val NOTCH_DISPATCH = "asdiohao_n_d"

    var safeInsetLeft = 0
        private set
    var safeInsetRight = 0
        private set
    var safeInsetTop = 0
        private set
    var safeInsetBottom = 0
        private set

    fun checkNotch(window: Window) {
        if (Build.VERSION.SDK_INT < 28) {
            return
        }
        window.apply {
            attributes = attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            decorView.post {
                decorView.rootWindowInsets?.displayCutout?.apply {
                    NotchUtils.safeInsetLeft = safeInsetLeft
                    NotchUtils.safeInsetRight = safeInsetRight
                    NotchUtils.safeInsetTop = safeInsetTop
                    NotchUtils.safeInsetBottom = safeInsetBottom
                    LiveDataBus.with<SafeInset>(NOTCH_DISPATCH).value = SafeInset(
                            NotchUtils.safeInsetLeft,
                            NotchUtils.safeInsetRight,
                            NotchUtils.safeInsetTop,
                            NotchUtils.safeInsetBottom
                    )
                }
            }
        }
    }

    fun addWindowFlags(window: Window?) {
        if (Build.VERSION.SDK_INT >= 26) {
            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
        }
    }

    fun clearWindowFlags(window: Window?) {
        if (Build.VERSION.SDK_INT >= 26) {
            window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
        }
    }

    data class SafeInset(val left: Int, val right: Int, val top: Int, val bottom: Int)
}