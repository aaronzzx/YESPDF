package com.aaron.yespdf.filepicker

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R
import com.aaron.yespdf.common.EmptyHolder
import com.aaron.yespdf.common.HeaderHolder
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import kotlinx.android.synthetic.main.app_recycler_item_filepicker.view.*
import java.io.File
import java.text.SimpleDateFormat

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ViewAllAdapter(
        private val fileList: MutableList<File>,
        private val importedList: List<String>?,
        private val callback: Callback
) : AbstractAdapter(), Filterable {

    private var context: Context? = null
    private var inverse = false
    private var filterList: MutableList<File>
    private val selectList: MutableList<String> = ArrayList()
    private val checkArray = SparseBooleanArray()
    fun setInverse(inverse: Boolean) {
        this.inverse = inverse
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val keyword = constraint.toString()
                if (StringUtils.isEmpty(keyword)) {
                    filterList = fileList
                } else {
                    filterList = ArrayList()
                    for (file in fileList) {
                            val contains = file.name.contains(keyword) != inverse
                            if (contains) {
                                filterList.add(file)
                            }
                        }
                }
                val results = FilterResults()
                results.values = filterList
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filterList = results.values as MutableList<File>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (context == null) context = parent.context
        val inflater = LayoutInflater.from(context)
        if (viewType == TYPE_EMPTY) {
            val itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false)
            return EmptyHolder(itemView)
        } else if (viewType == TYPE_HEADER) {
            val itemView = inflater.inflate(R.layout.app_recycler_item_search_header, parent, false)
            val holder = HeaderHolder(itemView)
            val horizontal = ConvertUtils.dp2px(16f)
            val top = ConvertUtils.dp2px(8f)
            holder.itemView.setPadding(horizontal, top, horizontal, 0)
            holder.itemView.visibility = View.GONE
            return holder
        }
        val itemView = inflater.inflate(R.layout.app_recycler_item_filepicker, parent, false)
        val holder = ViewHolder(itemView)
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            val file = filterList[pos]
            if (file.isDirectory) {
                callback.onDirTap(file.absolutePath)
            } else {
                if (holder.itemView.app_cb.isEnabled && !importedList!!.contains(file.absolutePath)) {
                    holder.itemView.app_cb.isChecked = !holder.itemView.app_cb.isChecked
                    if (holder.itemView.app_cb.isChecked && !selectList.contains(file.absolutePath)) {
                        selectList.add(file.absolutePath)
                    } else {
                        selectList.remove(file.absolutePath)
                    }
                }
                checkArray.put(pos, holder.itemView.app_cb.isChecked)
                callback.onSelectResult(selectList, fileCount())
            }
        }
        return holder
    }

    @SuppressLint("SetTextI18n,SimpleDateFormat")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is EmptyHolder) {
            viewHolder.itvEmpty.visibility = View.VISIBLE
            viewHolder.itvEmpty.setText(R.string.app_have_no_file)
            viewHolder.itvEmpty.setIconTop(R.drawable.app_img_file)
        } else if (viewHolder is HeaderHolder) {
            viewHolder.itemView.visibility = View.VISIBLE
            val count = filterList.size
            viewHolder.tvCount.text = context?.getString(R.string.app_total, count)
        } else {
            val holder = viewHolder as ViewHolder
            val temp = if (filterList.size == itemCount) position else position - 1
            val file = filterList[temp]
            val name = file.name
            var desc = context?.getString(R.string.app_item, 0)
            val lastModified = TimeUtils.millis2String(file.lastModified(), SimpleDateFormat("yyyy/MM/dd HH:mm"))
            if (file.isDirectory) {
                holder.itemView.app_iv_icon.setImageResource(R.drawable.app_ic_folder_yellow_24dp)
                holder.itemView.app_iv_next.visibility = View.VISIBLE
                holder.itemView.app_cb.visibility = View.GONE
                val files = file.listFiles(FileFilterImpl())
                if (files != null) desc = context?.getString(R.string.app_item, files.size)
            } else { // 大小 MB 留小数点后一位
                var size = (file.length().toDouble() / 1024 / 1024).toString()
                size = size.substring(0, size.indexOf(".") + 2)
                desc = "$size MB  -  "
                holder.itemView.app_iv_next.visibility = View.GONE
                holder.itemView.app_cb.visibility = View.VISIBLE
                holder.itemView.app_cb.setPadding(0, 0, 0, 0)
                if (file.name.endsWith(".pdf")) {
                    holder.itemView.app_iv_icon.setImageResource(R.drawable.app_ic_pdf)
                }
                //判断是否已导入
                if (importedList?.isNotEmpty() == true && importedList.contains(file.absolutePath)) {
                    holder.itemView.app_cb.isEnabled = false
                    holder.itemView.app_cb.setPadding(0, 0, ConvertUtils.dp2px(2f), 0)
                } else {
                    holder.itemView.app_cb.isEnabled = true
                }
                holder.itemView.app_cb.isChecked = checkArray[position]
                holder.itemView.app_tv_title.text = name
                holder.itemView.app_tv_description.text = desc + lastModified
            }
            holder.itemView.app_tv_title.text = name
            holder.itemView.app_tv_description.text = desc + lastModified
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, position, payloads)
        } else {
            if (viewHolder is ViewHolder) {
                if (viewHolder.itemView.app_cb.visibility == View.VISIBLE) {
                    viewHolder.itemView.app_cb.isChecked = checkArray[position]
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (filterList.isEmpty()) {
            return TYPE_EMPTY
        }
        return if (position == 0 && filterList.size != fileList.size) {
            TYPE_HEADER
        } else TYPE_CONTENT
    }

    override fun getItemCount(): Int {
        if (filterList.isEmpty()) {
            return 1
        } else if (filterList.size != fileList.size) {
            return filterList.size + 1
        }
        return filterList.size
    }

    override fun selectAll(selectAll: Boolean) {
        checkArray.clear()
        selectList.clear()
        for (file in filterList) {
            if (file.isFile && importedList?.contains(file.absolutePath) == false) {
                checkArray.put(filterList.indexOf(file), selectAll)
            }
            if (selectAll && file.isFile && importedList?.contains(file.absolutePath) == false) {
                selectList.add(file.absolutePath)
            }
        }
        callback.onSelectResult(selectList, fileCount())
        notifyItemRangeChanged(0, itemCount, 0)
    }

    override fun reset(): Boolean {
        checkArray.clear()
        selectList.clear()
        for (file in filterList) {
            if (file.isFile && importedList?.contains(file.absolutePath) == false) {
                return true
            }
        }
        return false
    }

    private fun fileCount(): Int {
        var count = 0
        for (file in filterList) {
            if (file.isFile && importedList?.contains(file.absolutePath) == false) {
                count++
            }
        }
        return count
    }

    private class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Callback {
        fun onDirTap(dirPath: String)
        fun onSelectResult(pathList: List<String>, total: Int)
    }

    companion object {
        private const val TYPE_EMPTY = 1
        private const val TYPE_CONTENT = 2
        private const val TYPE_HEADER = 3
    }

    init {
        filterList = fileList
    }
}