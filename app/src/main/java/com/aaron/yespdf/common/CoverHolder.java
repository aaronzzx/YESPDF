package com.aaron.yespdf.common;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class CoverHolder extends RecyclerView.ViewHolder {

    public static final int DEFAULT_LAYOUT = R.layout.app_recycler_item_cover;

    public ImageView ivCover;
    public TextView tvTitle;
    public TextView tvProgress;
    public CheckBox cb;

    public CoverHolder(@NonNull View itemView) {
        super(itemView);
        ivCover = itemView.findViewById(R.id.app_iv_cover);
        tvTitle = itemView.findViewById(R.id.app_tv_title);
        tvProgress = itemView.findViewById(R.id.app_tv_progress);
        cb = itemView.findViewById(R.id.app_cb);
    }
}
