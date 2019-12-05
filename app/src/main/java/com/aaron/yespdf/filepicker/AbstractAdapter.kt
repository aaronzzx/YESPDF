package com.aaron.yespdf.filepicker

import androidx.recyclerview.widget.RecyclerView

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class AbstractAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    abstract fun selectAll(selectAll: Boolean)
    abstract fun reset(): Boolean
}