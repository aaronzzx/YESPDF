package com.aaron.yespdf.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
final class OperationBarHelper {

    static void show(View view) {
        view.animate()
                .setDuration(220)
                .alpha(1)
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    static void hide(View view, float translationY) {
        view.animate()
                .setDuration(220)
                .alpha(0)
                .translationY(translationY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private OperationBarHelper() {
    }
}
