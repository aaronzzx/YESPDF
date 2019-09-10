package com.aaron.yespdf.preview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaron.yespdf.R;
import com.unnamed.b.atv.model.TreeNode;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ContentHolder extends TreeNode.BaseNodeViewHolder<ContentHolder.IconTreeItem> {

    private TreeNode treeNode;
    private ImageView ivIcon;
    private TextView tvTitle;

    private OnIconTapListener mOnIconTapListener;
    private OnNodeTapListener mOnNodeTapListener;

    ContentHolder(Context context) {
        super(context);
    }

    void setOnIconTapListener(OnIconTapListener listener) {
        mOnIconTapListener = listener;
    }

    void setOnNodeTapListener(OnNodeTapListener listener) {
        mOnNodeTapListener = listener;
    }

    @Override
    public View createNodeView(TreeNode node, IconTreeItem value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        treeNode = node;
        View itemView = inflater.inflate(R.layout.app_recycler_item_content, null);
        ivIcon = itemView.findViewById(R.id.app_iv_icon);
        tvTitle = itemView.findViewById(R.id.app_tv_title);
        TextView tvPageId = itemView.findViewById(R.id.app_tv_page);
        ivIcon.setImageResource(value.icon);
        tvTitle.setText(value.title);
        tvPageId.setText(value.pageId);

        ivIcon.setOnClickListener(v -> {
            if (mOnIconTapListener != null) {
                mOnIconTapListener.onIconTap(node);
            }
        });

        itemView.setOnClickListener(v -> {
            if (mOnNodeTapListener != null) {
                mOnNodeTapListener.onNodeTap(Integer.parseInt(value.pageId) - 1);
            }
        });

        return itemView;
    }

    @Override
    public void toggle(boolean active) {
        if (treeNode.isLeaf()) return;
        if (active) {
            int color = context.getResources().getColor(R.color.app_color_accent);
            tvTitle.setTextColor(color);

            ivIcon.setRotation(90);
        } else {
            int color = context.getResources().getColor(R.color.base_black_shallow);
            tvTitle.setTextColor(color);
            ivIcon.setRotation(0);
        }
    }

    static class IconTreeItem {
        int icon;
        String title;
        String pageId;

        public IconTreeItem(int icon, String title, String pageId) {
            this.icon = icon;
            this.title = title;
            this.pageId = pageId;
        }
    }

    interface OnIconTapListener {
        void onIconTap(TreeNode node);
    }

    interface OnNodeTapListener {
        void onNodeTap(int pageId);
    }
}
