package com.aaron.yespdf.common

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CoverHolder(itemView: View) : BaseHolder(itemView) {
    val ivCover: ImageView
    val tvTitle: TextView
    val tvProgress: TextView
    override val checkBox: CheckBox

    companion object {
        const val DEFAULT_LAYOUT = R.layout.app_recycler_item_cover
    }

    init {
        ivCover = itemView.findViewById(R.id.app_iv_cover)
        tvTitle = itemView.findViewById(R.id.app_tv_title)
        tvProgress = itemView.findViewById(R.id.app_tv_progress)
        checkBox = itemView.findViewById(R.id.app_cb)
    }
}