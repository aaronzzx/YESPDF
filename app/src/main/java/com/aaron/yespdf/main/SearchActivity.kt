package com.aaron.yespdf.main

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.aaron.base.impl.TextWatcherImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.R2
import com.aaron.yespdf.common.CommonActivity
import com.aaron.yespdf.common.DataManager
import com.aaron.yespdf.common.XGridDecoration
import com.aaron.yespdf.common.event.RecentPDFEvent
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.github.anzewei.parallaxbacklayout.ParallaxBack
import kotlinx.android.synthetic.main.app_activity_search.*
import kotlinx.android.synthetic.main.app_include_searchview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ParallaxBack
class SearchActivity : CommonActivity() {

    private var adapter: SearchAdapter? = null

    override fun layoutId(): Int {
        return R.layout.app_activity_search
    }

    override fun createToolbar(): Toolbar? {
        return findViewById(R.id.app_toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        initToolbar()
        initView()
    }

    override fun onStop() {
        super.onStop()
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecentPDFEvent(event: RecentPDFEvent) {
        adapter?.update()
        // 实时更新最新阅读列表
        if (event.isFromPreviewActivity) { // 由 PreviewActivity 发出而接收
            adapter?.filter?.filter(app_et_search.text)
        }
    }

    private fun initView() {
        val pdfList = DataManager.getPdfList()
        app_ibtn_back.setOnClickListener { finish() }
        app_ibtn_inverse.setOnClickListener {
            if (KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.hideSoftInput(this)
            }
            app_ibtn_inverse.isSelected = !app_ibtn_inverse.isSelected
            adapter?.setInverse(app_ibtn_inverse.isSelected)
            adapter?.filter?.filter(app_et_search.text)
        }
        app_ibtn_clear.setOnClickListener {
            app_et_search.setText("")
            if (!KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.showSoftInput(this)
            }
        }
        app_et_search.addTextChangedListener(object : TextWatcherImpl() {
            override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
                app_ibtn_inverse.visibility = if (c.isEmpty()) View.GONE else View.VISIBLE
                app_ibtn_clear.visibility = if (c.isEmpty()) View.GONE else View.VISIBLE
                adapter?.filter?.filter(c)
            }
        })
        app_rv.addItemDecoration(XGridDecoration())
        app_rv.addItemDecoration(YItemDecoration())
        val lm = GridLayoutManager(this, 3)
        lm.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.isEmpty == true || position == 0) {
                    3
                } else 1
            }
        }
        app_rv.layoutManager = lm
        adapter = SearchAdapter(pdfList, false)
        app_rv.adapter = adapter
    }

    private fun initToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private class YItemDecoration : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val pos = parent.getChildAdapterPosition(view)
            if (pos > 3) {
                outRect.top = ConvertUtils.dp2px(24f)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, SearchActivity::class.java)
            context.startActivity(starter)
        }
    }
}