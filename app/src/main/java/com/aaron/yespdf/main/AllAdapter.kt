package com.aaron.yespdf.main

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.aaron.base.image.DefaultOption
import com.aaron.base.image.ImageLoader
import com.aaron.yespdf.R
import com.aaron.yespdf.common.CollectionHolder
import com.aaron.yespdf.common.EmptyHolder
import com.aaron.yespdf.common.bean.Cover
import com.blankj.utilcode.util.StringUtils

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class AllAdapter(
        commInterface: ICommInterface<Cover>,
        private val fm: FragmentManager,
        sourceList: List<Cover>
) : AbstractAdapter<Cover>(commInterface, sourceList) {

    override fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = inflater.inflate(CollectionHolder.DEFAULT_LAYOUT, parent, false)
        return CollectionHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun bindHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is CollectionHolder) {
            if (sourceList.isNotEmpty()) {
                val c = sourceList[position]
                val coverList = c.coverList
                val count = c.count
                viewHolder.tvTitle.text = c.name
                viewHolder.tvCount.text = context.getString(R.string.app_total) + count + context.getString(R.string.app_count)
                setVisibility(viewHolder, count)
                if (count == 0) return
                setCover(viewHolder.ivCover1, coverList[0])
                if (count == 1) return
                setCover(viewHolder.ivCover2, coverList[1])
                if (count == 2) return
                setCover(viewHolder.ivCover3, coverList[2])
                if (count == 3) return
                setCover(viewHolder.ivCover4, coverList[3])
            }
            handleCheckBox(viewHolder.cb, position)
        } else if (viewHolder is EmptyHolder) {
            viewHolder.itvEmpty.visibility = View.VISIBLE
            viewHolder.itvEmpty.setText(R.string.app_have_no_all)
            viewHolder.itvEmpty.setIconTop(R.drawable.app_img_all)
        }
    }

    override fun bindHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            bindHolder(viewHolder, position)
        } else {
            if (viewHolder is CollectionHolder && position < itemCount) {
                handleCheckBox(viewHolder.cb, position)
            }
        }
    }

    override fun itemCount(): Int {
        return sourceList.size
    }

    override fun onTap(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        if (viewHolder is CollectionHolder) {
            if (viewHolder.cb.visibility == View.VISIBLE) {
                val cover = sourceList[position]
                val isChecked = !viewHolder.cb.isChecked
                viewHolder.cb.isChecked = isChecked
                if (viewHolder.cb.isChecked) {
                    selectList.add(cover)
                } else {
                    selectList.remove(cover)
                }
                checkArray.put(position, isChecked)
                commInterface?.onSelect(selectList, selectList.size == itemCount)
            } else {
                val name = sourceList[position].name
                val df: DialogFragment = CollectionFragment.newInstance(name)
                df.show(fm, "")
            }
        }
    }

    override fun checkCurrent(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        if (viewHolder is CollectionHolder) {
            viewHolder.cb.isChecked = true
        }
    }

    fun reset() {
        selectMode = false
        checkArray.clear()
        selectList.clear()
    }

    private fun setVisibility(holder: CollectionHolder, count: Int) {
        holder.ivCover1.visibility = if (count >= 1) View.VISIBLE else View.INVISIBLE
        holder.ivCover2.visibility = if (count >= 2) View.VISIBLE else View.INVISIBLE
        holder.ivCover3.visibility = if (count >= 3) View.VISIBLE else View.INVISIBLE
        holder.ivCover4.visibility = if (count >= 4) View.VISIBLE else View.INVISIBLE
    }

    private fun setCover(ivCover: ImageView, path: String) {
        if (!StringUtils.isEmpty(path)) {
            ImageLoader.load(context, DefaultOption.Builder(path).into(ivCover))
        } else {
            ivCover.scaleType = ImageView.ScaleType.FIT_XY
            ivCover.setImageResource(R.drawable.app_img_none_cover)
        }
    }

}