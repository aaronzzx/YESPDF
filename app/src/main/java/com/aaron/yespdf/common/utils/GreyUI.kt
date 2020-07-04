package com.aaron.yespdf.common.utils

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import android.view.Window

/**
 * @author aaronzzxup@gmail.com
 * @since 2020/7/4
 */
object GreyUI {

    fun grey(window: Window?, value: Boolean) {
        window ?: return

        val paint = makePaint()
        val decorView = window.decorView
        decorView.setLayerType(View.LAYER_TYPE_HARDWARE, if (value) paint else null)
    }

    private fun makePaint(): Paint {
        return Paint().apply {
            val matrix = ColorMatrix().apply {
                setSaturation(0f)
            }
            colorFilter = ColorMatrixColorFilter(matrix)
        }
    }
}