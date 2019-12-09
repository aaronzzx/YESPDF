package com.aaron.yespdf.main

import android.view.View
import android.widget.ImageView
import com.aaron.base.image.DefaultOption
import com.aaron.base.image.ImageLoader
import com.aaron.yespdf.R
import com.aaron.yespdf.common.App
import com.aaron.yespdf.common.BaseAdapter
import com.aaron.yespdf.common.CollectionHolder
import com.aaron.yespdf.common.bean.Cover
import com.blankj.utilcode.util.StringUtils

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllAdapter2(
        data: MutableList<Cover>
) : BaseAdapter<Cover, CollectionHolder>(CollectionHolder.DEFAULT_LAYOUT, data) {

    override val checkBoxId: Int = R.id.app_cb
    override val emptyText: CharSequence = App.getContext().getString(R.string.app_have_no_all)
    override val emptyIcon: Int = R.drawable.app_img_all

    override fun convert(helper: CollectionHolder, item: Cover) {
        item.run {
            helper.tvTitle.text = name
            helper.tvCount.text = mContext.getString(R.string.app_total, count)
            setVisibility(helper, count)
            if (count == 0) return
            setCover(helper.ivCover1, coverList[0])
            if (count == 1) return
            setCover(helper.ivCover2, coverList[1])
            if (count == 2) return
            setCover(helper.ivCover3, coverList[2])
            if (count == 3) return
            setCover(helper.ivCover4, coverList[3])
        }
    }

    private fun setVisibility(holder: CollectionHolder, count: Int) {
        holder.ivCover1.visibility = if (count >= 1) View.VISIBLE else View.INVISIBLE
        holder.ivCover2.visibility = if (count >= 2) View.VISIBLE else View.INVISIBLE
        holder.ivCover3.visibility = if (count >= 3) View.VISIBLE else View.INVISIBLE
        holder.ivCover4.visibility = if (count >= 4) View.VISIBLE else View.INVISIBLE
    }

    private fun setCover(ivCover: ImageView, path: String) {
        if (!StringUtils.isEmpty(path)) {
            ImageLoader.load(mContext, DefaultOption.Builder(path).into(ivCover))
        } else {
            ivCover.scaleType = ImageView.ScaleType.FIT_XY
            ivCover.setImageResource(R.drawable.app_img_none_cover)
        }
    }
}