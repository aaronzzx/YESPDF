package com.aaron.yespdf.common

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.aaron.yespdf.R
import com.aaron.yespdf.common.widgets.BorderImageView

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionHolder(itemView: View) : BaseHolder(itemView) {
    val ivCover1: BorderImageView
    val ivCover2: BorderImageView
    val ivCover3: BorderImageView
    val ivCover4: BorderImageView
    val tvTitle: TextView
    val tvCount: TextView
    override val checkBox: CheckBox

    companion object {
        const val DEFAULT_LAYOUT = R.layout.app_recycler_item_collection
        const val DEFAULT_LAYOUT_HORIZONTAL = R.layout.app_recycler_item_collection_horizontal
    }

    init {
        ivCover1 = itemView.findViewById(R.id.app_iv_1)
        ivCover2 = itemView.findViewById(R.id.app_iv_2)
        ivCover3 = itemView.findViewById(R.id.app_iv_3)
        ivCover4 = itemView.findViewById(R.id.app_iv_4)
        tvTitle = itemView.findViewById(R.id.app_tv_title)
        tvCount = itemView.findViewById(R.id.app_tv_count)
        checkBox = itemView.findViewById(R.id.app_cb)
    }
}