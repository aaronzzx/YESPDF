package com.aaron.yespdf.common

import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.LayoutRes
import com.aaron.yespdf.R
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.listener.OnItemDragListener

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class BaseAdapter<T, K : BaseViewHolder>(
        @LayoutRes layoutId: Int,
        data: MutableList<T>
) : BaseItemDraggableAdapter<T, K>(layoutId, data) {

    abstract val checkBoxId: Int
    protected abstract val emptyText: CharSequence
    protected abstract val emptyIcon: Int

    protected val checkArray: SparseBooleanArray = SparseBooleanArray()

    var isSelectMode = false
        protected set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): K {
        setEmptyView(R.layout.app_recycler_item_emptyview, parent)
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: K, position: Int) {
        super.onBindViewHolder(holder, position)
        (holder as? EmptyHolder)?.run {
            itvEmpty.text = emptyText
            itvEmpty.setIconTop(emptyIcon)
        }
    }

    override fun onBindViewHolder(holder: K, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            (holder as? BaseHolder)?.run {
                handleCheckBox(this.checkBox, position)
            }
        }
    }

    override fun setOnItemClick(v: View, position: Int) {
        if (getItemViewType(position) == 0) {
            super.setOnItemClick(v, position)
            val cb = v.findViewById<CheckBox>(checkBoxId)
            val isChecked = !cb.isChecked
            cb.isChecked = isChecked
            checkArray.put(position, isChecked)
        }
    }

    override fun setOnItemLongClick(v: View, position: Int): Boolean {
        val consume = super.setOnItemLongClick(v, position)
        if (getItemViewType(position) == 0) {
            isSelectMode = true
            checkArray.put(position, true)
            v.findViewById<CheckBox>(checkBoxId).isChecked = true
            notifyItemRangeChanged(0, itemCount, 0)
            return consume
        }
        return false
    }

    fun selectAll(selectAll: Boolean) {
        if (selectAll) {
            for (index in 0 until itemCount) {
                checkArray.put(index, selectAll)
            }
        } else {
            checkArray.clear()
        }
        notifyItemRangeChanged(0, itemCount, 0)
    }

    fun enterSelectMode(v: View, position: Int) {
        if (getItemViewType(position) == 0) {
            isSelectMode = true
            checkArray.put(position, true)
            v.findViewById<CheckBox>(checkBoxId).isChecked = true
            notifyItemRangeChanged(0, itemCount, 0)
        }
    }

    fun exitSelectMode() {
        isSelectMode = false
        checkArray.clear()
        notifyItemRangeChanged(0, itemCount, 0)
    }

    fun isChecked(position: Int) = checkArray.get(position)

    private fun handleCheckBox(cb: CheckBox, position: Int) {
        cb.visibility = if (isSelectMode) View.VISIBLE else View.GONE
        if (isSelectMode) {
            cb.alpha = 1.0f
            cb.scaleX = 0.8f
            cb.scaleY = 0.8f
            cb.isChecked = checkArray[position]
        }
    }
}