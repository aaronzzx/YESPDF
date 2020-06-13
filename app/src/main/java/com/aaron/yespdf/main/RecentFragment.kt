package com.aaron.yespdf.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.event.MaxRecentEvent
import com.aaron.yespdf.common.event.RecentPDFEvent
import com.aaron.yespdf.main.AbstractAdapter.IPickCallback
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import kotlinx.android.synthetic.main.app_fragment_recent.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class RecentFragment : CommonFragment(), IOperation, IPickCallback<PDF> {

    private var adapter: AbstractAdapter<PDF>? = null
    private val recentPDFList: MutableList<PDF> = ArrayList()
    private val selectPDFList: MutableList<PDF> = ArrayList()

    private var isHorizontalLayout = false
    private var xItemDecoration: RecyclerView.ItemDecoration? = null
    private var yItemDecoration: RecyclerView.ItemDecoration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.app_fragment_recent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isHorizontalLayout = Settings.linearLayout
        initData()
        initView()
    }

    override fun onStart() {
        super.onStart()
        if (isHorizontalLayout != Settings.linearLayout) {
            isHorizontalLayout = Settings.linearLayout
            initView()
        }
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
        if (recentPDFList.isNotEmpty()) {
            ThreadUtils.executeByIo<Any>(object : SimpleTask<Any?>() {
                override fun doInBackground(): Any? {
                    val delete: MutableList<PDF> = ArrayList()
                    for (name in event.deleted) {
                        for (pdf in recentPDFList) {
                            if (pdf.name == name) {
                                delete.add(pdf)
                            }
                        }
                    }
                    recentPDFList.removeAll(delete)
                    DBHelper.deleteRecent(delete)
                    return null
                }

                override fun onSuccess(result: Any?) {
                    adapter?.notifyDataSetChanged()
                }
            })
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAllDeleteEvent(event: AllDeleteEvent) {
        if (recentPDFList.isNotEmpty()) {
            ThreadUtils.executeByIo<Any>(object : SimpleTask<Any?>() {
                override fun doInBackground(): Any? {
                    val delete: MutableList<PDF> = ArrayList()
                    for (dir in event.dirList) {
                        for (pdf in recentPDFList) {
                            if (pdf.dir == dir) {
                                delete.add(pdf)
                            }
                        }
                    }
                    recentPDFList.removeAll(delete)
                    DBHelper.deleteRecent(delete)
                    return null
                }

                override fun onSuccess(result: Any?) {
                    adapter?.notifyDataSetChanged()
                }
            })
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecentPDFEvent(event: RecentPDFEvent) {
        recentPDFList.clear()
        recentPDFList.addAll(DBHelper.queryRecentPDF())
        // 实时更新最新阅读列表
        if (event.isFromPreviewActivity) { // 由 PreviewActivity 发出而接收
            adapter?.notifyDataSetChanged()
        } else { // 由于还在 MainActivity 界面，所以不立即更新界面
            app_rv_recent.postDelayed({ adapter?.notifyDataSetChanged() }, 500)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMaxRecentEvent(event: MaxRecentEvent?) {
        adapter?.notifyDataSetChanged()
    }

    override fun onStartPicking() {
        (activity as MainActivity).startOperation()
    }

    override fun onSelected(list: List<PDF>, selectAll: Boolean) {
        LogUtils.e(list)
        selectPDFList.clear()
        selectPDFList.addAll(list)
        (activity as MainActivity).selectResult(list.size, selectAll)
    }

    override fun delete(deleteLocal: Boolean) {
        if (selectPDFList.isNotEmpty()) {
            ThreadUtils.executeByIo<List<String>>(object : SimpleTask<List<String>>() {
                override fun doInBackground(): List<String> {
                    recentPDFList.removeAll(selectPDFList)
                    return DBHelper.deleteRecent(selectPDFList)
                }

                override fun onSuccess(result: List<String>?) {
                    UiManager.showShort(R.string.app_delete_completed)
                    (activity as MainActivity).finishOperation()
                    adapter?.notifyDataSetChanged()
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
        return getString(R.string.app_whether_delete_recent, selectPDFList.size)
    }

    override fun localDeleteVisibility(): Int = View.GONE

    private fun initView() {
        if (xItemDecoration != null) {
            app_rv_recent.removeItemDecoration(xItemDecoration!!)
        }
        if (yItemDecoration != null) {
            app_rv_recent.removeItemDecoration(yItemDecoration!!)
        }
        xItemDecoration = XGridDecoration()
        yItemDecoration = YGridDecoration()
        app_rv_recent.addItemDecoration(xItemDecoration!!)
        app_rv_recent.addItemDecoration(yItemDecoration!!)
        val lm = GridLayoutManager(activity, 3).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (recentPDFList.isEmpty() || isHorizontalLayout) 3 else 1
                }
            }
        }
        app_rv_recent.layoutManager = lm
        adapter = RecentAdapter(this, recentPDFList)
        app_rv_recent.adapter = adapter
    }

    private fun initData() {
        recentPDFList.addAll(DBHelper.queryRecentPDF())
    }

    companion object {
        fun newInstance(): Fragment {
            return RecentFragment()
        }
    }
}