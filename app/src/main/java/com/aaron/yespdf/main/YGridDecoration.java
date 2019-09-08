package com.aaron.yespdf.main;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class YGridDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
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
