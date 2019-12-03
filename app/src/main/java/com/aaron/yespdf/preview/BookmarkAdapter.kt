package com.aaron.yespdf.preview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R
import com.aaron.yespdf.common.EmptyHolder
import com.blankj.utilcode.util.TimeUtils
import kotlinx.android.synthetic.main.app_recycler_item_bookmark.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class BookmarkAdapter(private val mBookmarks: List<Bookmark>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mDateFormat: DateFormat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        if (viewType == TYPE_EMPTY) {
            val itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false)
            return EmptyHolder(itemView)
        }
        val itemView = inflater.inflate(R.layout.app_recycler_item_bookmark, parent, false)
        val holder = ViewHolder(itemView)
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            val bk = mBookmarks[pos]
            (context as IActivityInterface).onJumpTo(bk.pageId)
        }
        return holder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (mBookmarks.isNotEmpty() && viewHolder is ViewHolder) {
            val bk = mBookmarks[position]
            viewHolder.itemView.app_tv_title.text = bk.title
            viewHolder.itemView.app_tv_page_id.text = (bk.pageId + 1).toString()
            val time = TimeUtils.millis2String(bk.time, mDateFormat)
            viewHolder.itemView.app_tv_time.text = time
        } else if (viewHolder is EmptyHolder) {
            viewHolder.itvEmpty.visibility = View.VISIBLE
            viewHolder.itvEmpty.setText(R.string.app_have_no_add_bookmark)
            viewHolder.itvEmpty.setIconTop(R.drawable.app_ic_bookmark_emptyview)
        }
    }

    override fun getItemCount(): Int {
        return if (mBookmarks.isEmpty()) {
            1
        } else mBookmarks.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mBookmarks.isEmpty()) {
            TYPE_EMPTY
        } else super.getItemViewType(position)
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val TYPE_EMPTY = 1
    }

    init {
        mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    }
}