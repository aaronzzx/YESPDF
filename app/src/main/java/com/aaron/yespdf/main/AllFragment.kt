package com.aaron.yespdf.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.Cover
import com.aaron.yespdf.common.event.AllEvent
import com.aaron.yespdf.main.AbstractAdapter.ICommInterface
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import kotlinx.android.synthetic.main.app_fragment_all.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllFragment : CommonFragment(), IOperation, ICommInterface<Cover> {

    private var adapter: AllAdapter? = null
    private val coverList = DataManager.getCoverList()
    private val selectCollections: MutableList<Cover> = ArrayList()

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

    override fun onStartOperation() {
        (activity as MainActivity).startOperation()
    }

    override fun onSelect(list: List<Cover>, selectAll: Boolean) {
        selectCollections.clear()
        selectCollections.addAll(list)
        (activity as MainActivity).selectResult(list.size, selectAll)
    }

    override fun delete() {
        if (selectCollections.isNotEmpty()) {
            ThreadUtils.executeByIo<List<String>>(object : SimpleTask<List<String>>() {
                override fun doInBackground(): List<String>? {
                    val list = DBHelper.deleteCollection(selectCollections)
                    DataManager.updateAll()
                    return list
                }

                override fun onSuccess(dirList: List<String>?) {
                    UiManager.showShort(R.string.app_delete_completed)
                    (activity as MainActivity).finishOperation()
                    adapter?.notifyDataSetChanged()
                    dirList?.run { EventBus.getDefault().post(AllDeleteEvent(this)) }
                }
            })
        }
    }

    override fun selectAll(selectAll: Boolean) {
        adapter?.selectAll(selectAll)
    }

    override fun cancelSelect() {
        adapter?.cancelSelect()
    }

    override fun deleteDescription(): String? {
        return getString(R.string.app_will_delete) + " " + selectCollections.size + " " + getString(R.string.app_delete_for_all)
    }

    fun update() {
        DataManager.updateCollection()
        adapter?.reset()
        adapter?.notifyDataSetChanged()
    }

    private fun initView() {
        app_rv_all.addItemDecoration(XGridDecoration())
        app_rv_all.addItemDecoration(YGridDecoration())
        val lm = GridLayoutManager(activity, 3)
        lm.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (coverList.isEmpty()) {
                    3
                } else 1
            }
        }
        app_rv_all.layoutManager = lm
        adapter = fragmentManager?.let { AllAdapter(this, it, coverList) }
        app_rv_all.adapter = adapter
    }

    companion object {
        fun newInstance(): Fragment {
            return AllFragment()
        }
    }
}