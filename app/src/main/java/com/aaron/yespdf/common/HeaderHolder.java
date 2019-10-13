package com.aaron.yespdf.common;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class HeaderHolder extends RecyclerView.ViewHolder {

    public static final int DEFAULT_LAYOUT = R.layout.app_recycler_item_search_header;

    public TextView tvCount;

    public HeaderHolder(@NonNull View itemView) {
        super(itemView);
        tvCount = itemView.findViewById(R.id.app_tv_count);
    }
}
