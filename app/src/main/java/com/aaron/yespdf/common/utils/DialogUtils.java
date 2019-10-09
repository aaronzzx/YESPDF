package com.aaron.yespdf.common.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import androidx.annotation.StyleRes;

import com.aaron.yespdf.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DialogUtils {

    public static Dialog createDialog(Context context, int layoutId) {
        Dialog dialog = new Dialog(context);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.AppDialogAnim);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setContentView(layoutId);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createDialog(Context context, View view) {
        Dialog dialog = new Dialog(context);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.AppDialogAnim);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static BottomSheetDialog createBottomSheetDialog(Context context, View view) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);
        View rootView = dialog.getDelegate().findViewById(R.id.design_bottom_sheet);
        if (rootView != null) {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(rootView);
            behavior.setHideable(false);
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.AppBottomDialogAnim);
//            window.findViewById(R.id.design_bottom_sheet)
//                    .setBackgroundResource(android.R.color.transparent);
        }
        return dialog;
    }

    public static BottomSheetDialog createBottomSheetDialog(Context context, View view, @StyleRes int animStyle, boolean transparentBg) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);
        View rootView = dialog.getDelegate().findViewById(R.id.design_bottom_sheet);
        if (rootView != null) {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(rootView);
            behavior.setHideable(false);
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(animStyle);
            if (transparentBg) {
                window.findViewById(R.id.design_bottom_sheet)
                        .setBackgroundResource(android.R.color.transparent);
            }
        }
        return dialog;
    }

    private DialogUtils() {}
}
