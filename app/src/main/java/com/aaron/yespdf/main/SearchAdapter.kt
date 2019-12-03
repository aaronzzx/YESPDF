package com.aaron.yespdf.main

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.aaron.base.image.DefaultOption
import com.aaron.base.image.ImageLoader
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.event.RecentPDFEvent
import com.aaron.yespdf.preview.PreviewActivity
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import org.greenrobot.eventbus.EventBus
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class SearchAdapter(
        sourceList: List<PDF>,
        canSelect: Boolean
) : AbstractAdapter<PDF>(sourceList, canSelect), Filterable {

    private var inverse = false
    private val recentPDFEvent: RecentPDFEvent = RecentPDFEvent()
    private val filterList: MutableList<PDF> = ArrayList()

    fun update() {
        DataManager.updatePDFs()
    }

    val isEmpty: Boolean
        get() = filterList.isEmpty()

    fun setInverse(inverse: Boolean) {
        this.inverse = inverse
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val keyword = constraint.toString()
                filterList.clear()
                if (!StringUtils.isEmpty(keyword)) {
                    for (pdf in sourceList) {
                        val contains = pdf.name.contains(keyword) != inverse
                        if (contains) {
                            filterList.add(pdf)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filterList
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filterList.clear()
                filterList.addAll(results.values as MutableList<PDF>)
                notifyDataSetChanged()
            }
        }
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_EMPTY) {
            val itemView = inflater.inflate(EmptyHolder.DEFAULT_LAYOUT, parent, false)
            return EmptyHolder(itemView)
        } else if (viewType == TYPE_HEADER) {
            val itemView = inflater.inflate(HeaderHolder.DEFAULT_LAYOUT, parent, false)
            return HeaderHolder(itemView)
        }
        val itemView = inflater.inflate(CoverHolder.DEFAULT_LAYOUT, parent, false)
        return CoverHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun bindHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is CoverHolder && position < itemCount) {
            val pdf = filterList[position - 1]
            val cover = pdf.cover
            val bookName = pdf.name
            viewHolder.tvTitle.text = bookName
            viewHolder.tvProgress.text = context.getString(R.string.app_already_read) + pdf.progress
            if (!StringUtils.isEmpty(cover)) {
                ImageLoader.load(context, DefaultOption.Builder(cover).into(viewHolder.ivCover))
            } else {
                viewHolder.ivCover.scaleType = ImageView.ScaleType.FIT_XY
                viewHolder.ivCover.setImageResource(R.drawable.app_img_none_cover)
            }
            handleCheckBox(viewHolder.cb, position)
        } else if (viewHolder is HeaderHolder) {
            val count = filterList.size
            viewHolder.tvCount.text = context.getString(R.string.app_total) + count + context.getString(R.string.app_count)
        } else if (viewHolder is EmptyHolder) {
            viewHolder.itvEmpty.visibility = View.VISIBLE
            viewHolder.itvEmpty.setText(R.string.app_have_no_file)
            viewHolder.itvEmpty.setIconTop(R.drawable.app_img_file)
        }
    }

    override fun bindHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        bindHolder(viewHolder, position)
    }

    override fun itemCount(): Int {
        return if (filterList.isEmpty()) {
            1
        } else filterList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (filterList.isEmpty()) {
            return TYPE_EMPTY
        }
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_CONTENT
    }

    override fun onTap(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        if (viewHolder is CoverHolder) {
            val pdf = filterList[position - 1]
            val cur = System.currentTimeMillis()
            @SuppressLint("SimpleDateFormat") val df: DateFormat = SimpleDateFormat("yyyyMMddHHmmss")
            pdf.latestRead = TimeUtils.millis2String(cur, df).toLong()
            DBHelper.updatePDF(pdf)
            DBHelper.insertRecent(pdf)
            DataManager.updateRecentPDFs()
            PreviewActivity.start(context, pdf)
            EventBus.getDefault().post(recentPDFEvent)
        }
    }

    override fun checkCurrent(viewHolder: RecyclerView.ViewHolder?, position: Int) {}

    companion object {
        private const val TYPE_EMPTY = 1
        private const val TYPE_CONTENT = 2
        private const val TYPE_HEADER = 3
    }

}