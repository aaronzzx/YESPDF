package com.aaron.yespdf.common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class YGridDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (Settings.isHorizontalLayout()) {
            outRect.top = ConvertUtils.dp2px(6f);
            outRect.bottom = ConvertUtils.dp2px(6f);
            return;
        }
        int count = state.getItemCount();
        int pos = parent.getChildAdapterPosition(view);
        if (pos < 3) {
            outRect.top = ConvertUtils.dp2px(8);
        } else if (pos >= count - 3) {
            outRect.top = ConvertUtils.dp2px(24);
            outRect.bottom = ConvertUtils.dp2px(8);
        } else {
            outRect.top = ConvertUtils.dp2px(24);
        }
    }
}
