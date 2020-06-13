package com.aaron.yespdf.main

import android.widget.ImageView
import com.aaron.base.image.DefaultOption
import com.aaron.base.image.ImageLoader
import com.aaron.yespdf.R
import com.aaron.yespdf.common.App
import com.aaron.yespdf.common.BaseAdapter
import com.aaron.yespdf.common.CoverHolder
import com.aaron.yespdf.common.Settings
import com.aaron.yespdf.common.bean.PDF
import com.blankj.utilcode.util.StringUtils

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionAdapter2(
        data: MutableList<PDF>
) : BaseAdapter<PDF, CoverHolder>(
        if (Settings.isLinearLayout()) CoverHolder.DEFAULT_LAYOUT_HORIZONTAL
        else CoverHolder.DEFAULT_LAYOUT,
        data
) {

    override val checkBoxId: Int = R.id.app_cb
    override val emptyText: CharSequence = App.getContext().getString(R.string.app_have_no_all)
    override val emptyIcon: Int = R.drawable.app_img_all

    override fun convert(helper: CoverHolder, item: PDF) {
        item.run {
            helper.tvTitle.text = name
            helper.tvProgress.text = mContext.getString(R.string.app_already_read, progress)
            if (!StringUtils.isEmpty(cover)) {
                ImageLoader.load(mContext, DefaultOption.Builder(cover).into(helper.ivCover))
            } else {
                helper.ivCover.scaleType = ImageView.ScaleType.FIT_XY
                helper.ivCover.setImageResource(R.drawable.app_img_none_cover)
            }
        }
    }
}