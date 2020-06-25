package com.aaron.yespdf.preview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aaron.yespdf.R
import com.aaron.yespdf.preview.ContentHolder.IconTreeItem
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ContentHolder(context: Context?) : BaseNodeViewHolder<IconTreeItem>(context) {

    private lateinit var treeNode: TreeNode
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView

    private var mOnIconTapListener: OnIconTapListener? = null
    private var mOnNodeTapListener: OnNodeTapListener? = null

    fun setOnIconTapListener(listener: OnIconTapListener) {
        mOnIconTapListener = listener
    }

    fun setOnNodeTapListener(listener: OnNodeTapListener) {
        mOnNodeTapListener = listener
    }

    override fun createNodeView(node: TreeNode, value: IconTreeItem): View {
        val inflater = LayoutInflater.from(context)
        treeNode = node
        val itemView = inflater.inflate(R.layout.app_recycler_item_content, null)
        ivIcon = itemView.findViewById(R.id.app_iv_icon)
        tvTitle = itemView.findViewById(R.id.app_tv_title)
        val tvPageId = itemView.findViewById<TextView>(R.id.app_tv_page)
        ivIcon.setImageResource(value.icon)
        tvTitle.text = value.title
        tvPageId.text = value.pageId
        ivIcon.setOnClickListener {
            if (mOnIconTapListener != null) {
                mOnIconTapListener?.onIconTap(node)
            }
        }
        itemView.setOnClickListener {
            if (mOnNodeTapListener != null) {
                mOnNodeTapListener?.onNodeTap(value.pageId.toInt() - 1)
            }
        }
        return itemView
    }

    override fun toggle(active: Boolean) {
        if (treeNode.isLeaf) return
        if (active) {
            val color = context.resources.getColor(R.color.app_content_bookmark_accent)
            tvTitle.setTextColor(color)
            ivIcon.rotation = 90f
        } else {
            val color = context.resources.getColor(R.color.app_content_bookmark_primary)
            tvTitle.setTextColor(color)
            ivIcon.rotation = 0f
        }
    }

    class IconTreeItem(var icon: Int, var title: String, var pageId: String)

    interface OnIconTapListener {
        fun onIconTap(node: TreeNode)
    }

    interface OnNodeTapListener {
        fun onNodeTap(pageId: Int)
    }
}