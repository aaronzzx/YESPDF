package com.aaron.yespdf.main

import android.view.View

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal object OperationBarHelper {
    fun show(view: View) {
        view.visibility = View.VISIBLE
    }

    fun hide(view: View) {
        view.visibility = View.GONE
    }
}