package com.aaron.yespdf.common.utils

import android.graphics.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
object ImageUtils {

    fun handleImageEffect(bm: Bitmap, saturation: Float): Bitmap {
        val bmp = Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint()
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)
        paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
        canvas.drawBitmap(bm, 0f, 0f, paint)
        return bmp
    }
}