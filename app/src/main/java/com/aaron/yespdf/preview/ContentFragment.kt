package com.aaron.yespdf.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aaron.yespdf.R
import com.aaron.yespdf.common.CommonFragment
import com.aaron.yespdf.preview.ContentHolder.IconTreeItem
import com.shockwave.pdfium.PdfDocument
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import kotlinx.android.synthetic.main.app_fragment_content.*
import kotlinx.android.synthetic.main.app_recycler_item_emptyview.*
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ContentFragment : CommonFragment(), IContentFragInterface {

    private val contentList: MutableList<PdfDocument.Bookmark> = ArrayList()

    override fun update(collection: MutableCollection<PdfDocument.Bookmark>) {
        contentList.clear()
        contentList.addAll(collection)
        initContent()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.app_fragment_content, container, false)
        app_itv_placeholder.setText(R.string.app_have_no_content)
        app_itv_placeholder.setIconTop(R.drawable.app_ic_content_emptyview)
        return layout
    }

    private fun initContent() {
        if (contentList.isNotEmpty()) {
            app_itv_placeholder.visibility = View.GONE
            for (bk in contentList) {
                val icon = if (bk.hasChildren()) R.drawable.app_ic_can_down_grey else R.drawable.app_ic_cannot_down_grey
                val title = bk.title
                val pageId = (bk.pageIdx + 1).toString()
                //创建根节点
                val root = TreeNode.root()
                //创建节点item
                val iconTreeItem = IconTreeItem(icon, title, pageId)
                // 点击监听
                val holder = ContentHolder(activity)
                holder.setOnIconTapListener(object : ContentHolder.OnIconTapListener {
                    override fun onIconTap(node: TreeNode) {
                        holder.treeView.toggleNode(node)
                    }
                })
                holder.setOnNodeTapListener(object : ContentHolder.OnNodeTapListener {
                    override fun onNodeTap(pageId: Int) {
                        (activity as IActivityInterface).onJumpTo(pageId)
                    }
                })
                val cur = TreeNode(iconTreeItem).setViewHolder(holder)
                if (bk.hasChildren()) {
                    addChild(cur, bk.children)
                }
                root.addChild(cur)
                //创建树形视图
                val treeView = AndroidTreeView(activity, root)
                //设置树形视图开启默认动画
                treeView.setDefaultAnimation(false)
                //设置树形视图默认的样式
                treeView.setDefaultContainerStyle(R.style.AppTreeNode)
                //设置树形视图默认的ViewHolder
                treeView.setDefaultViewHolder(ContentHolder::class.java)
                //将树形视图添加到layout中
                app_ll.addView(treeView.view)
            }
        } else {
            app_itv_placeholder.visibility = View.VISIBLE
        }
    }

    private fun addChild(parent: TreeNode, childList: List<PdfDocument.Bookmark>) {
        for (bk in childList) {
            val icon = if (bk.hasChildren()) R.drawable.app_ic_can_down_grey else R.drawable.app_ic_cannot_down_grey
            val title = bk.title
            val pageId = (bk.pageIdx + 1).toString()
            //创建节点item
            val iconTreeItem = IconTreeItem(icon, title, pageId)
            // 点击监听
            val holder = ContentHolder(activity)
            holder.setOnIconTapListener(object : ContentHolder.OnIconTapListener {
                override fun onIconTap(node: TreeNode) {
                    holder.treeView.toggleNode(node)
                }
            })
            holder.setOnNodeTapListener(object : ContentHolder.OnNodeTapListener {
                override fun onNodeTap(pageId: Int) {
                    (activity as IActivityInterface).onJumpTo(pageId)
                }
            })
            val cur = TreeNode(iconTreeItem).setViewHolder(holder)
            if (bk.hasChildren()) {
                addChild(cur, bk.children)
            }
            parent.addChild(cur)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): Fragment {
            return ContentFragment()
        }
    }
}