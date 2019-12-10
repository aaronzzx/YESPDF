package com.aaron.yespdf.common

import android.view.View
import android.widget.CheckBox
import com.chad.library.adapter.base.BaseViewHolder

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class BaseHolder(itemView: View) : BaseViewHolder(itemView) {

    abstract val checkBox: CheckBox
}