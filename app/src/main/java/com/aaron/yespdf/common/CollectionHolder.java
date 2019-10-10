package com.aaron.yespdf.common;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.widgets.BorderImageView;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class CollectionHolder extends RecyclerView.ViewHolder {

    public static final int DEFAULT_LAYOUT = R.layout.app_recycler_item_collection;

    public BorderImageView ivCover1;
    public BorderImageView ivCover2;
    public BorderImageView ivCover3;
    public BorderImageView ivCover4;
    public TextView tvTitle;
    public TextView tvCount;
    public CheckBox cb;

    public CollectionHolder(@NonNull View itemView) {
        super(itemView);
        ivCover1 = itemView.findViewById(R.id.app_iv_1);
        ivCover2 = itemView.findViewById(R.id.app_iv_2);
        ivCover3 = itemView.findViewById(R.id.app_iv_3);
        ivCover4 = itemView.findViewById(R.id.app_iv_4);
        tvTitle = itemView.findViewById(R.id.app_tv_title);
        tvCount = itemView.findViewById(R.id.app_tv_count);
        cb = itemView.findViewById(R.id.app_cb);
    }
}
