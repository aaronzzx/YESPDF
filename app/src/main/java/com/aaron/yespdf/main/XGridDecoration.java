package com.aaron.yespdf.main;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class XGridDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        switch (pos % 3) {
            case 0:
            case 1:
            case 2:
                outRect.left = ConvertUtils.dp2px(8);
                outRect.right = ConvertUtils.dp2px(8);
                break;
        }
    }
}
