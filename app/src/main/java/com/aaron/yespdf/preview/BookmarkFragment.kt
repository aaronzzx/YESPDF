package com.aaron.yespdf.preview

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aaron.base.base.BaseFragment
import com.aaron.yespdf.R
import com.aaron.yespdf.common.CommonFragment
import com.blankj.utilcode.util.LogUtils
import kotlinx.android.synthetic.main.app_fragment_bookmark.*
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class BookmarkFragment : CommonFragment(), IBkFragInterface {

    private var mAdapter: RecyclerView.Adapter<*>? = null
    private val mBookmarks: MutableList<Bookmark> = ArrayList()

    override fun update(collection: MutableCollection<Bookmark>) {
        mBookmarks.clear()
        mBookmarks.addAll(collection)
        mBookmarks.sortWith(Comparator { bk1: Bookmark, bk2: Bookmark -> (bk2.time - bk1.time).toInt() })
        mAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.app_fragment_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val lm = LinearLayoutManager(activity)
        app_rv_bookmark.layoutManager = lm
        val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        decoration.setDrawable(ColorDrawable(resources.getColor(R.color.base_black_divider)))
        app_rv_bookmark.addItemDecoration(decoration)
        mAdapter = BookmarkAdapter(mBookmarks)
        app_rv_bookmark.adapter = mAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(): Fragment {
            return BookmarkFragment()
        }
    }
}