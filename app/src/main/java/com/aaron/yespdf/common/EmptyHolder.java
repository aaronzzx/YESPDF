package com.aaron.yespdf.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.widgets.ImageTextView;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class EmptyHolder extends RecyclerView.ViewHolder {
    public ImageTextView itvEmpty;

    public EmptyHolder(@NonNull View itemView) {
        super(itemView);
        itvEmpty = itemView.findViewById(R.id.app_itv_placeholder);
    }
}
