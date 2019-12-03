package com.aaron.yespdf.filepicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.aaron.base.impl.OnClickListenerImpl
import com.aaron.base.impl.TextWatcherImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.common.CommonFragment
import com.aaron.yespdf.common.DialogManager
import com.aaron.yespdf.common.DialogManager.ImportDialogCallback
import com.aaron.yespdf.common.GroupingAdapter
import com.aaron.yespdf.common.UiManager
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.app_fragment_view_all.*
import kotlinx.android.synthetic.main.app_include_tv_path.*
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ViewAllFragment : CommonFragment(), IViewAllView, ViewAllAdapter.Callback {

    var adapter: ViewAllAdapter? = null
    var selectList: MutableList<String> = ArrayList()

    private lateinit var presenter: IViewAllPresenter
    private val importDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createImportDialog(activity, object : ImportDialogCallback {
            override fun onInput(et: EditText) {
                et.addTextChangedListener(object : TextWatcherImpl() {
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        newGroupName = charSequence.toString()
                    }
                })
            }

            override fun onLeft(btn: Button) {
                btn.setOnClickListener(object : OnClickListenerImpl() {
                    override fun onViewClick(v: View, interval: Long) {
                        groupingDialog.show()
                    }
                })
            }

            override fun onCenter(btn: Button) {
                btn.setOnClickListener(object : OnClickListenerImpl() {
                    override fun onViewClick(v: View, interval: Long) {
                        setResultBack(SelectActivity.TYPE_BASE_FOLDER, null)
                    }
                })
            }

            override fun onRight(btn: Button) {
                btn.setOnClickListener(object : OnClickListenerImpl() {
                    override fun onViewClick(v: View, interval: Long) {
                        if (StringUtils.isEmpty(newGroupName)) {
                            UiManager.showShort(R.string.app_type_new_group_name)
                        } else {
                            setResultBack(SelectActivity.TYPE_CUSTOM, newGroupName)
                        }
                    }
                })
            }
        })
    }

    private val groupingDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createGroupingDialog(activity, false, object : GroupingAdapter.Callback {
            override fun onAddNewGroup() { // empty
            }

            override fun onAddToGroup(dir: String) {
                setResultBack(SelectActivity.TYPE_TO_EXIST, dir)
            }
        })
    }
    private var newGroupName: String? = null
    private val fileList: MutableList<File> = ArrayList()
    private val onClickListener = View.OnClickListener {
        val path = it.tag as String
        val index = app_ll.indexOfChild(it)
        val count = app_ll.childCount
        app_ll.removeViews(index + 1, count - index - 1)
        presenter.listFile(path)
    }
    private val dataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            selectList.clear()
            (activity as SelectActivity).ibtnSelectAll.isSelected = false
            app_btn_import_count.setText(R.string.app_import_count)
            val enableSelectAll = adapter?.reset() ?: false
            (activity as SelectActivity).ibtnSelectAll.isEnabled = enableSelectAll
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        attachP()
        val view = inflater.inflate(R.layout.app_fragment_view_all, container, false)
        presenter.listStorage()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    fun setFocus() {
        app_horizontal_sv.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        (activity as SelectActivity).ibtnSelectAll.visibility = View.VISIBLE
        (activity as SelectActivity).ibtnSearch.visibility = View.VISIBLE
        (activity as SelectActivity).setRevealParam()
        app_horizontal_sv.isFocusableInTouchMode = true
        app_horizontal_sv.requestFocus()
        app_horizontal_sv.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                if (!presenter.canFinish()) {
                    (activity as SelectActivity).ibtnInverse.isSelected = false
                    (activity as SelectActivity).etSearch.setText("")
                    presenter.goBack()
                    return@OnKeyListener true
                }
            }
            false
        })
    }

    override fun onPause() {
        super.onPause()
        app_horizontal_sv.setOnKeyListener(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter?.unregisterAdapterDataObserver(dataObserver)
        presenter.detach()
    }

    override fun attachP() {
        presenter = ViewAllPresenter(this)
    }

    override fun onShowMessage(stringId: Int) {
        UiManager.showShort(stringId)
    }

    override fun onShowFileList(fileList: List<File>?) {
        this.fileList.clear()
        this.fileList.addAll(fileList!!)
        adapter?.notifyDataSetChanged()
    }

    override fun onShowPath(pathList: List<String>) {
        app_ll.removeViews(1, app_ll.childCount - 1)
        val inflater = LayoutInflater.from(context)
        val parent = StringBuilder(IViewAllPresenter.ROOT_PATH)
        for (dirName in pathList) {
            val tvPath = inflater.inflate(R.layout.app_include_tv_path, null) as TextView
            tvPath.setOnClickListener(onClickListener)
            tvPath.text = dirName
            parent.append("/").append(dirName) // 当前节点生成后即变成下一节点的父节点
            tvPath.tag = parent.toString()
            app_ll.addView(tvPath)
        }
    }

    override fun onDirTap(dirPath: String) {
        presenter.listFile(dirPath)
    }

    @SuppressLint("SetTextI18n")
    override fun onSelectResult(pathList: List<String>, total: Int) {
        LogUtils.e(pathList.size)
        if (total != 0) {
            (activity as SelectActivity).ibtnSelectAll.isSelected = pathList.size == total
        }
        app_btn_import_count.text = getString(R.string.app_import) + "(" + pathList.size + ")"
        selectList.clear()
        selectList.addAll(pathList)
    }

    private fun initView() {
        app_tv_path.setOnClickListener(onClickListener)
        app_tv_path.tag = IViewAllPresenter.ROOT_PATH // 原始路径
        app_btn_import_count.setOnClickListener(object : OnClickListenerImpl() {
            @SuppressLint("SetTextI18n")
            override fun onViewClick(v: View, interval: Long) {
                if (KeyboardUtils.isSoftInputVisible(activity)) {
                    KeyboardUtils.hideSoftInput(activity)
                }
                if (selectList.isEmpty()) {
                    UiManager.showShort(R.string.app_have_not_select)
                } else {
                    importDialog.show()
                }
            }
        })
        (activity as SelectActivity).ibtnSelectAll.setOnClickListener {
            it.isSelected = !it.isSelected
            adapter!!.selectAll(it.isSelected)
        }
        (activity as SelectActivity).ibtnSelectAll.isEnabled = false // XML 设置无效，只能这里初始化
        val lm: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        app_rv_select.layoutManager = lm
        adapter = ViewAllAdapter(fileList, (activity as SelectActivity).importeds, this)
        adapter?.registerAdapterDataObserver(dataObserver)
        app_rv_select.adapter = adapter
    }

    private fun setResultBack(type: Int, groupName: String?) {
        val data = Intent()
        data.putStringArrayListExtra(SelectActivity.EXTRA_SELECTED, selectList as ArrayList<String>)
        data.putExtra(SelectActivity.EXTRA_TYPE, type)
        data.putExtra(SelectActivity.EXTRA_GROUP_NAME, groupName)
        activity.setResult(Activity.RESULT_OK, data)
        activity.finish()
    }

    companion object {
        fun newInstance(): Fragment {
            return ViewAllFragment()
        }
    }
}