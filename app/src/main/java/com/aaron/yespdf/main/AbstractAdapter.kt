package com.aaron.yespdf.main

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R
import com.aaron.yespdf.common.EmptyHolder
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class AbstractAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected lateinit var context: Context
    protected lateinit var inflater: LayoutInflater

    protected val sourceList: List<T>
    protected val selectList: MutableList<T> = ArrayList()
    protected val checkArray: SparseBooleanArray = SparseBooleanArray()

    protected var pickCallback: IPickCallback<T>? = null
    protected var selectMode = false

    private var canSelect = true

    constructor(pickCallback: IPickCallback<T>, sourceList: List<T>) {
        this.pickCallback = pickCallback
        this.sourceList = sourceList
    }

    constructor(sourceList: List<T>, canSelect: Boolean) {
        this.sourceList = sourceList
        this.canSelect = canSelect
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        inflater = LayoutInflater.from(context)
        return if (viewType == TYPE_EMPTY) {
            EmptyHolder(inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false))
        } else {
            createHolder(parent, viewType).apply {
                itemView.setOnClickListener { onTap(this, adapterPosition) }
                if (canSelect) {
                    itemView.setOnLongClickListener {
                        if (this !is EmptyHolder && !selectMode) {
                            pickCallback?.onStartPicking()
                            checkArray.put(adapterPosition, true)
                            checkCurrent(this, adapterPosition)
                            selectList.add(sourceList[adapterPosition])
                            pickCallback?.onSelected(selectList, selectList.size == itemCount)
                            selectMode = true
                            notifyItemRangeChanged(0, itemCount, 0)
                            return@setOnLongClickListener true
                        }
                        false
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        bindHolder(viewHolder, position)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            bindHolder(viewHolder, position)
        } else {
            bindHolder(viewHolder, position, payloads)
        }
    }

    override fun getItemCount(): Int = if (sourceList.isEmpty()) 1 else itemCount()

    override fun getItemViewType(position: Int): Int {
        return if (sourceList.isEmpty()) TYPE_EMPTY else super.getItemViewType(position)
    }

    fun handleCheckBox(cb: CheckBox, position: Int) {
        cb.visibility = if (selectMode) View.VISIBLE else View.GONE
        if (selectMode) {
            cb.alpha = 1.0f
            cb.scaleX = 0.8f
            cb.scaleY = 0.8f
            cb.isChecked = checkArray[position]
        }
    }

    fun selectAll(selectAll: Boolean) {
        for (index in 0 until itemCount) {
            checkArray.put(index, selectAll)
        }
        selectList.clear()
        if (selectAll) {
            for (index in 0 until itemCount) {
                selectList.add(sourceList[index])
            }
        }
        pickCallback?.onSelected(selectList, selectAll)
        notifyItemRangeChanged(0, itemCount, 0)
    }

    fun cancelSelect() {
        selectMode = false
        checkArray.clear()
        selectList.clear()
        notifyItemRangeChanged(0, itemCount, 0)
    }

    abstract fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    abstract fun bindHolder(viewHolder: RecyclerView.ViewHolder, position: Int)
    abstract fun bindHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>)
    abstract fun itemCount(): Int
    abstract fun onTap(viewHolder: RecyclerView.ViewHolder?, position: Int)
    abstract fun checkCurrent(viewHolder: RecyclerView.ViewHolder?, position: Int)

    interface IPickCallback<T> {
        fun onStartPicking()
        fun onSelected(list: List<T>, selectAll: Boolean)
    }

    companion object {
        private const val TYPE_EMPTY = 1
    }
}