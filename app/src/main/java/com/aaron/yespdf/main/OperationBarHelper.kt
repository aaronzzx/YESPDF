package com.aaron.yespdf.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator

/**
 * @author Aaron aaronzzxup@gmail.com
 */
object OperationBarHelper {
    fun show(view: View) {
        view.animate()
                .scaleX(1.0F)
                .scaleY(1.0F)
                .alpha(1.0F)
                .setDuration(100L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        view.visibility = View.VISIBLE
                    }
                })
                .start()
    }

    fun hide(view: View) {
        view.animate()
                .scaleX(1.1F)
                .scaleY(1.1F)
                .alpha(0F)
                .setDuration(100L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        view.visibility = View.GONE
                    }
                })
                .start()
    }
}