package com.aaron.yespdf.main;

import android.view.View;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
final class OperationBarHelper {

    static void show(View view) {
        view.setVisibility(View.VISIBLE);
//        view.animate()
//                .setDuration(200)
//                .alpha(1)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                        view.setVisibility(View.VISIBLE);
//                    }
//                })
//                .start();
    }

    static void hide(View view) {
        view.setVisibility(View.GONE);
//        view.animate()
//                .setDuration(200)
//                .alpha(0)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        view.setVisibility(View.GONE);
//                    }
//                })
//                .start();
    }

    private OperationBarHelper() {
    }
}
