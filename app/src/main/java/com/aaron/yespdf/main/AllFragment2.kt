package com.aaron.yespdf.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.Cover
import com.aaron.yespdf.common.event.AllEvent
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import kotlinx.android.synthetic.main.app_fragment_all.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllFragment2 : CommonFragment(), IOperation {

    private lateinit var adapter: AllAdapter2
    private val coverList = DataManager.getCoverList()
    private val selectList: MutableList<Cover> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.app_fragment_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).injectOperation(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPdfDeleteEvent(event: PdfDeleteEvent) {
        LogUtils.e(event)
        if (event.isEmpty) {
            DBHelper.deleteCollection(event.dir)
        }
        update()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAllEvent(event: AllEvent) {
        if (event.isEmpty) {
            DBHelper.deleteCollection(event.dir)
        }
        update()
    }

    override fun delete() {
        if (selectList.isNotEmpty()) {
            launch {
                val dirList = withContext(Dispatchers.IO) {
                    val list = DBHelper.deleteCollection(selectList)
                    DataManager.updateAll()
                    list
                }
                UiManager.showShort(R.string.app_delete_completed)
                (activity as MainActivity).finishOperation()
                adapter.notifyDataSetChanged()
                EventBus.getDefault().post(AllDeleteEvent(dirList))
            }
        }
    }

    override fun selectAll(selectAll: Boolean) {
        adapter.selectAll(selectAll)
        selectList.clear()
        if (selectAll) {
            selectList.addAll(coverList)
        }
        val size = selectList.size
        (activity as MainActivity).selectResult(size, size == coverList.size)
    }

    override fun cancelSelect() {
        adapter.exitSelectMode()
        selectList.clear()
    }

    override fun deleteDescription(): String? {
        return getString(R.string.app_whether_delete_collection, selectList.size)
    }

    fun update() {
        DataManager.updateCollection()
        adapter.exitSelectMode()
        selectList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun initView() {
        app_rv_all.addItemDecoration(XGridDecoration())
        app_rv_all.addItemDecoration(YGridDecoration())
        val lm = GridLayoutManager(activity, 3)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (coverList.isEmpty()) {
                    3
                } else 1
            }
        }
        app_rv_all.layoutManager = lm
        adapter = AllAdapter2(coverList)
        adapter.setOnItemClickListener { _, view, position ->
            if (adapter.isSelectMode) {
                val cover = coverList[position]
                val checkBox: CheckBox? = view.findViewById(adapter.checkBoxId)
                if (checkBox?.isChecked == true) {
                    selectList.remove(cover)
                } else {
                    selectList.add(cover)
                }
                val size = selectList.size
                (activity as MainActivity).selectResult(size, size == coverList.size)
            } else {
                val name = coverList[position].name
                val df: DialogFragment = CollectionFragment.newInstance(name)
                df.show(fragmentManager!!, "")
            }
        }
        adapter.setOnItemDragListener(object : OnItemDragListener {
            private lateinit var checkBox: CheckBox
            private lateinit var text: String
            private var fromPos: Int = 0

            override fun onItemDragMoving(
                    viewHolder: RecyclerView.ViewHolder,
                    fromPos: Int,
                    target: RecyclerView.ViewHolder,
                    toPos: Int
            ) {}

            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, position: Int) {
                text = coverList[position].name
                checkBox = viewHolder.itemView.findViewById(R.id.app_cb)
                checkBox.visibility = View.GONE
                fromPos = position
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, position: Int) {
                if (!adapter.isSelectMode) {
                    (activity as MainActivity).startOperation()
                    selectList.add(coverList[position])
                    val size = selectList.size
                    (activity as MainActivity).selectResult(size, size == coverList.size)
                    adapter.enterSelectMode(viewHolder.itemView, position)
                }
                checkBox.visibility = View.VISIBLE

                if (fromPos != position) {
                    DBHelper.updateCollection(text)
                }
            }
        })
        val dragHelper = ItemTouchHelper(ItemDragAndSwipeCallback(adapter))
        dragHelper.attachToRecyclerView(app_rv_all)
        adapter.enableDragItem(dragHelper)
        app_rv_all.adapter = adapter
    }

    companion object {
        fun newInstance(): Fragment {
            return AllFragment2()
        }
    }
}